package com.aura.lamplight.puppeteer.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public record HeldItemGroupCondition(Group group, InteractionHand hand) implements TriggerCondition {

    private static final Codec<InteractionHand> HAND_CODEC = Codec.STRING.comapFlatMap(
        name -> switch (name) {
            case "main_hand" -> DataResult.success(InteractionHand.MAIN_HAND);
            case "off_hand" -> DataResult.success(InteractionHand.OFF_HAND);
            default -> DataResult.error(() -> "Unknown hand: " + name + " (expected main_hand or off_hand)");
        },
        hand -> hand == InteractionHand.MAIN_HAND ? "main_hand" : "off_hand"
    );

    public static final MapCodec<HeldItemGroupCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Group.CODEC.fieldOf("group").forGetter(HeldItemGroupCondition::group),
            HAND_CODEC.optionalFieldOf("hand", InteractionHand.MAIN_HAND).forGetter(HeldItemGroupCondition::hand)
        ).apply(instance, HeldItemGroupCondition::new)
    );

    @Override
    public boolean test(final ServerPlayer player) {
        ItemStack stack = player.getItemInHand(hand);
        if (group == Group.EMPTY_HAND) {
            return stack.isEmpty();
        }
        if (stack.isEmpty()) {
            return false;
        }
        return group.test(stack);
    }

    @Override
    public MapCodec<? extends TriggerCondition> codec() {
        return MAP_CODEC;
    }

    public enum Group {
        EMPTY_HAND("empty", stack -> false),
        SWORD("sword", stack -> heldItemPath(stack).endsWith("sword")),
        PICKAXE("pickaxe", stack -> heldItemPath(stack).endsWith("pickaxe")),
        AXE("axe", stack -> heldItemPath(stack).endsWith("axe") && !heldItemPath(stack).endsWith("pickaxe")),
        SHOVEL("shovel", stack -> heldItemPath(stack).endsWith("shovel")),
        HOE("hoe", stack -> heldItemPath(stack).endsWith("hoe")),
        BOW("bow", stack -> stack.getItem() instanceof net.minecraft.world.item.BowItem),
        CROSSBOW("crossbow", stack -> stack.getItem() instanceof net.minecraft.world.item.CrossbowItem),
        TRIDENT("trident", stack -> stack.getItem() instanceof net.minecraft.world.item.TridentItem),
        MACE("mace", stack -> stack.getItem() instanceof net.minecraft.world.item.MaceItem),
        SHIELD("shield", stack -> stack.getItem() instanceof net.minecraft.world.item.ShieldItem),
        FISHING_ROD("fishing_rod", stack -> stack.getItem() instanceof net.minecraft.world.item.FishingRodItem),
        SPYGLASS("spyglass", stack -> stack.getItem() instanceof net.minecraft.world.item.SpyglassItem),
        SHEARS("shears", stack -> stack.getItem() instanceof net.minecraft.world.item.ShearsItem),
        FLINT("flint", stack -> stack.getItem() instanceof net.minecraft.world.item.FlintAndSteelItem),
        BUCKET("bucket", stack -> stack.getItem() instanceof net.minecraft.world.item.BucketItem),
        BUCKET_FILLED("bucket_filled", stack -> stack.getItem() instanceof net.minecraft.world.item.BucketItem
            && !heldItemPath(stack).equals("bucket")),
        LANTERN("lantern", stack -> heldBlockId(stack, "lantern")),
        TORCH("torch", stack -> heldBlockId(stack, "torch")),
        MAP("map", stack -> stack.getItem() instanceof net.minecraft.world.item.MapItem),
        THROWABLE("throwable", stack -> heldItemPath(stack).endsWith("snowball")
            || heldItemPath(stack).endsWith("ender_pearl")
            || heldItemPath(stack).endsWith("egg")),
        FOOD("food", stack -> stack.has(DataComponents.FOOD)),
        DRINK("drink", stack -> stack.getItem() instanceof net.minecraft.world.item.PotionItem
            || heldItemPath(stack).contains("milk_bucket")
            || heldItemPath(stack).contains("honey_bottle"));

        public static final Codec<Group> CODEC = Codec.STRING.comapFlatMap(
            name -> {
                for (Group group : values()) {
                    if (group.id.equals(name)) {
                        return DataResult.success(group);
                    }
                }
                return DataResult.error(() -> "Unknown item group: " + name);
            },
            group -> group.id
        );

        private final String id;
        private final Predicate<ItemStack> test;

        Group(final String id, final Predicate<ItemStack> test) {
            this.id = id;
            this.test = test;
        }

        boolean test(final ItemStack stack) {
            return test.test(stack);
        }

        private static boolean heldBlockId(final ItemStack stack, final String substring) {
            return stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem
                && BuiltInRegistries.BLOCK.getKey(blockItem.getBlock()).getPath().contains(substring);
        }

        private static String heldItemPath(final ItemStack stack) {
            return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        }
    }
}
