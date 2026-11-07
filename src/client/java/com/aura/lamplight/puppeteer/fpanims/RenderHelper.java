package com.aura.lamplight.puppeteer.fpanims;

import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;

public class RenderHelper {
    private static final Set<String> DENIED_NAMESPACES = Set.of();
    private static final Set<String> USE_ITEM_RENDERER_NAMESPACES = Set.of();
    private static final Set<String> DENIED_ITEMS = Set.of();

    public static boolean shouldRenderCustom(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof BlockItem blockItem) {
            Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
            if (itemId == null) {
                return false;
            } else if (!(item instanceof BucketItem) && !stack.is(Items.MILK_BUCKET) && !stack.is(Items.POWDER_SNOW_BUCKET) && stack.getUseAnimation() != ItemUseAnimation.EAT && !stack.is(ItemTags.BANNERS) && !stack.is(Items.STRING) && !stack.is(Items.REDSTONE) && !stack.is(Items.LEVER) && !stack.is(Items.TRIPWIRE_HOOK)) {
                String namespace = itemId.getNamespace();
                String fullItemId = itemId.toString();
                if (!USE_ITEM_RENDERER_NAMESPACES.contains(namespace) && !stack.is(Items.CONDUIT) && !(item instanceof SignItem) && !(item instanceof HangingSignItem) && !namespace.equals("sophisticatedstorage") && !FpAnimsConfig.isInRenderBlockAsItem(itemId.toString())) {
                    Block block = blockItem.getBlock();
                    BlockState blockState = block.defaultBlockState();
                    if (!blockState.is(FpAnimsTags.GLASS_PANES) && !blockState.is(BlockTags.RAILS) && !blockState.is(BlockTags.CLIMBABLE) && !blockState.is(BlockTags.COMBINATION_STEP_SOUND_BLOCKS)) {
                        if (!(block instanceof EntityBlock)) {
                            return true;
                        } else {
                            return !DENIED_NAMESPACES.contains(namespace) && !DENIED_ITEMS.contains(fullItemId);
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
