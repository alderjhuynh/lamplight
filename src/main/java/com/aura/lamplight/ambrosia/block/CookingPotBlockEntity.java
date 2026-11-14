package com.aura.lamplight.ambrosia.block;

import com.aura.lamplight.ambrosia.AmbrosiaSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class CookingPotBlockEntity extends BlockEntity {
    public static final int MAX_INGREDIENTS = 4;

    private final List<ItemStack> ingredients = new ArrayList<>();
    private int cookProgress;

    public CookingPotBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COOKING_POT, pos, state);
    }

    public List<ItemStack> getIngredients() {
        return this.ingredients;
    }

    public int getCookProgress() {
        return this.cookProgress;
    }

    public boolean isEmpty() {
        return this.ingredients.isEmpty();
    }

    public boolean canAdd(ItemStack stack) {
        if (stack.isEmpty() || this.ingredients.size() >= MAX_INGREDIENTS) {
            return false;
        }
        for (ItemStack ingredient : this.ingredients) {
            if (ItemStack.isSameItem(ingredient, stack)) {
                return false;
            }
        }
        return true;
    }

    public void addIngredient(ItemStack stack) {
        this.ingredients.add(stack.copyWithCount(1));
        this.cookProgress = 0;
        this.sync();
    }

    public ItemStack popIngredient() {
        if (this.ingredients.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack popped = this.ingredients.removeLast();
        this.cookProgress = 0;
        this.sync();
        return popped;
    }

    public void clearIngredients() {
        this.ingredients.clear();
        this.cookProgress = 0;
        this.sync();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null) {
            for (ItemStack ingredient : this.ingredients) {
                Containers.dropItemStack(this.level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, ingredient);
            }
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CookingPotBlockEntity pot) {
        if (level.isClientSide()) {
            return;
        }
        if (pot.ingredients.isEmpty()) {
            return;
        }
        if (!state.getValue(CampfireCookingPotBlock.LIT)) {
            pot.cookProgress = 0;
            return;
        }
        CookingPotRecipe recipe = CookingPotRecipes.find(state.getValue(CampfireCookingPotBlock.FLUID), pot.ingredients);
        if (recipe == null) {
            pot.cookProgress = 0;
            return;
        }
        if (++pot.cookProgress >= recipe.cookTime()) {
            pot.clearIngredients();
            level.setBlock(pos, state.setValue(CampfireCookingPotBlock.FLUID, recipe.result()), Block.UPDATE_ALL);
            level.playSound(null, pos, AmbrosiaSounds.COOKING_POT_INSERT, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, net.minecraft.world.level.gameevent.GameEvent.FLUID_PLACE, pos);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.ingredients.isEmpty()) {
            output.store("Ingredients", ItemStack.CODEC.listOf(), this.ingredients);
        }
        output.putInt("CookProgress", this.cookProgress);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.ingredients.clear();
        this.ingredients.addAll(input.read("Ingredients", ItemStack.CODEC.listOf()).orElse(List.of()));
        this.cookProgress = input.getIntOr("CookProgress", 0);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}