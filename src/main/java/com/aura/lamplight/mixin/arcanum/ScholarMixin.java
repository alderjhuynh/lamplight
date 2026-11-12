package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments;
import com.aura.lamplight.arcanum.server.RebuildHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class ScholarMixin {

    @Inject(method = "popExperience", at = @At("HEAD"), cancellable = true)
    private void lamplight$scholarBlockXp(ServerLevel level, BlockPos pos, int amount, CallbackInfo ci) {
        Player player = level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 8.0D,
                entity -> entity instanceof Player && !((Player) entity).isSpectator());
        if (player == null) return;
        int scholarLvl = RebuildHandler.armorLvl(player, ArcanumEnchantments.SCHOLAR);
        if (scholarLvl <= 0) return;
        ci.cancel();
        if ((Boolean) level.getGameRules().get(GameRules.BLOCK_DROPS)) {
            ExperienceOrb.award(level, Vec3.atCenterOf(pos), Math.round(amount * (1.0F + 0.2F * scholarLvl)));
        }
    }
}
