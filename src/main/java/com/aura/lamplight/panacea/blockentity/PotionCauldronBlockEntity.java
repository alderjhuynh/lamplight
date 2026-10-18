package com.aura.lamplight.panacea.blockentity;

import com.aura.lamplight.panacea.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PotionCauldronBlockEntity extends BlockEntity {
    private List<PotionContents> layers = new ArrayList<>();

    public PotionCauldronBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.POTION_CAULDRON, pos, state);
    }

    public PotionCauldronBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public List<PotionContents> getLayers() {
        return layers;
    }

    public boolean isEmpty() {
        return layers.isEmpty();
    }

    public void addLayer(PotionContents contents) {
        if (layers.size() < 3) {
            layers.add(contents);
            setChanged();
        }
    }

    public Optional<PotionContents> removeTopLayer() {
        if (layers.isEmpty()) {
            return Optional.empty();
        }
        Optional<PotionContents> top = Optional.of(layers.getLast());
        layers.removeLast();
        setChanged();
        return top;
    }

    public void clearLayers() {
        layers.clear();
        setChanged();
    }

    public int getBlendedColor() {
        if (layers.isEmpty()) {
            return PotionContents.BASE_POTION_COLOR;
        }
        int red = 0;
        int green = 0;
        int blue = 0;
        for (PotionContents layer : layers) {
            int color = layer.getColor();
            red += (color >> 16) & 0xFF;
            green += (color >> 8) & 0xFF;
            blue += color & 0xFF;
        }
        int count = layers.size();
        return (red / count) << 16 | (green / count) << 8 | blue / count;
    }

    public static PotionContents merge(List<PotionContents> contentsList) {
        int count = Math.max(1, contentsList.size());
        Map<Holder<MobEffect>, MobEffectInstance> merged = new LinkedHashMap<>();
        for (PotionContents contents : contentsList) {
            for (MobEffectInstance effect : contents.getAllEffects()) {
                long scaledDuration = Math.round(effect.getDuration() / (double) count);
                MobEffectInstance existing = merged.get(effect.getEffect());
                if (existing == null) {
                    merged.put(
                            effect.getEffect(),
                            new MobEffectInstance(
                                    effect.getEffect(),
                                    (int) Math.max(1, scaledDuration),
                                    effect.getAmplifier(),
                                    effect.isAmbient(),
                                    effect.isVisible()
                            )
                    );
                } else {
                    int duration = Math.min(72000, existing.getDuration() + (int) scaledDuration);
                    merged.put(
                            effect.getEffect(),
                            new MobEffectInstance(
                                    effect.getEffect(),
                                    duration,
                                    Math.max(existing.getAmplifier(), effect.getAmplifier()),
                                    existing.isAmbient() || effect.isAmbient(),
                                    existing.isVisible() || effect.isVisible()
                            )
                    );
                }
            }
        }
        return new PotionContents(Optional.empty(), Optional.empty(), List.copyOf(merged.values()), Optional.empty());
    }

    public static ItemStack mixedPotionStack(PotionContents contents) {
        ItemStack stack = new ItemStack(Items.POTION);
        stack.set(DataComponents.POTION_CONTENTS, contents);
        stack.set(DataComponents.CUSTOM_NAME, mixedName("Mixed Potion of ", contents));
        return stack;
    }

    public static ItemStack mixedArrowStack(PotionContents contents) {
        ItemStack stack = new ItemStack(Items.TIPPED_ARROW);
        stack.set(DataComponents.POTION_CONTENTS, contents);
        stack.set(DataComponents.CUSTOM_NAME, mixedName("Mixed Arrow of ", contents));
        return stack;
    }

    private static Component mixedName(String prefix, PotionContents contents) {
        List<MutableComponent> parts = new ArrayList<>();
        for (MobEffectInstance effect : contents.getAllEffects()) {
            parts.add(PotionContents.getPotionDescription(effect.getEffect(), effect.getAmplifier()));
        }
        MutableComponent joined = Component.empty();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                String separator;
                if (i == parts.size() - 1) {
                    separator = parts.size() == 2 ? " and " : ", and ";
                } else {
                    separator = ", ";
                }
                joined.append(separator);
            }
            joined.append(parts.get(i));
        }
        return Component.literal(prefix).append(joined).withStyle(style -> style.withItalic(false));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!layers.isEmpty()) {
            output.store("Layers", PotionContents.CODEC.listOf(), layers);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        layers = new ArrayList<>(input.read("Layers", PotionContents.CODEC.listOf()).orElse(List.of()));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
