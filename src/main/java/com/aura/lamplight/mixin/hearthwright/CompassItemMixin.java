package com.aura.lamplight.mixin.hearthwright;

import com.aura.lamplight.hearthwright.block.LodestoneBlocks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CompassItem.class)
public class CompassItemMixin {

    @WrapOperation(
            method = "useOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"
            )
    )
    private boolean hearthwright$acceptSandstoneLodestones(BlockState state, Object rawType, Operation<Boolean> original) {
        return original.call(state, rawType) || LodestoneBlocks.isLodestone(state);
    }
}
