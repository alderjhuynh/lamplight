package com.aura.lamplight.puppeteer.fpanims;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

public final class TorchDetect {
    private static final Map<Item, Boolean> CACHE = new WeakHashMap<>();

    public static boolean isTorchLike(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        } else {
            Item item = stack.getItem();
            Boolean cached = CACHE.get(item);
            if (cached != null) {
                return cached;
            } else {
                boolean result = detectTorchLike(item);
                CACHE.put(item, result);
                return result;
            }
        }
    }

    private static boolean detectTorchLike(Item item) {
        if (item instanceof StandingAndWallBlockItem sw) {
            Block floor = sw.getBlock();
            Block wall = tryGetWallBlockReflective(sw);
            if (isTorchBlock(floor) || isTorchBlock(wall)) {
                return true;
            }

            if (isLanternOrCandle(floor) || isLanternOrCandle(wall)) {
                return false;
            }
        }

        if (item instanceof BlockItem bi) {
            Block b = bi.getBlock();
            BlockState s = b.defaultBlockState();
            if (isLanternOrCandle(b) || b instanceof RedstoneLampBlock) {
                return false;
            }

            if (isTorchBlock(b)) {
                return true;
            }

            try {
                int light = s.getLightEmission();
                double size = s.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty()).bounds().getSize();
                if (light >= 10 && size < (double) 0.5F) {
                    return true;
                }
            } catch (Throwable var7) {
            }
        }

        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        if (id != null) {
            String path = id.getPath().toLowerCase();
            if (path.contains("lantern") || path.contains("candle") || path.contains("lamp")) {
                return false;
            }

            if (path.contains("torch") || path.contains("sconce")) {
                return true;
            }
        }

        return false;
    }

    private static Block tryGetWallBlockReflective(StandingAndWallBlockItem sw) {
        try {
            var f = StandingAndWallBlockItem.class.getDeclaredField("wallBlock");
            f.setAccessible(true);
            Object val = f.get(sw);
            return val instanceof Block ? (Block) val : null;
        } catch (Throwable var3) {
            return null;
        }
    }

    private static boolean isTorchBlock(Block b) {
        return b instanceof TorchBlock || b instanceof WallTorchBlock || b instanceof RedstoneTorchBlock || b instanceof RedstoneWallTorchBlock;
    }

    private static boolean isLanternOrCandle(Block b) {
        return b instanceof LanternBlock || b instanceof CandleBlock || b instanceof CandleCakeBlock;
    }
}
