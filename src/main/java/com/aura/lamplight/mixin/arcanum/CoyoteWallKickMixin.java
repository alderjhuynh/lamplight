package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments;
import com.aura.lamplight.arcanum.server.RebuildHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(LivingEntity.class)
public class CoyoteWallKickMixin {

    @Shadow
    protected boolean jumping;

    @Unique
    private int lamplight$lastGroundTick = -999;

    @Unique
    private boolean lamplight$coyoteUsed;

    @Unique
    private int lamplight$wallKickCooldown;

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void lamplight$coyoteAndWallKick(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) return;
        if (lamplight$wallKickCooldown > 0) lamplight$wallKickCooldown--;
        if (player.onGround()) {
            lamplight$lastGroundTick = player.tickCount;
            lamplight$coyoteUsed = false;
            return;
        }
        if (!this.jumping) return;
        if (RebuildHandler.armorLvl(player, ArcanumEnchantments.COYOTE) > 0
                && !lamplight$coyoteUsed
                && player.tickCount - lamplight$lastGroundTick <= 20) {
            lamplight$coyoteUsed = true;
            player.jumpFromGround();
            return;
        }
        if (RebuildHandler.armorLvl(player, ArcanumEnchantments.WALL_KICK) > 0
                && player.horizontalCollision && lamplight$wallKickCooldown <= 0) {
            lamplight$wallKickCooldown = 20;
            player.jumpFromGround();
            double lookX = player.getLookAngle().x;
            double lookZ = player.getLookAngle().z;
            player.push(-lookX * 0.2D, 0.0D, -lookZ * 0.2D);
            player.hurtMarked = true;
            player.fallDistance = 0.0F;
            level.sendParticles(ParticleTypes.POOF, player.getX(), player.getY(0.5D), player.getZ(),
                    6, 0.2D, 0.2D, 0.2D, 0.01D);
        }
    }
}
