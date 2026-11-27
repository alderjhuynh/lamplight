package com.aura.lamplight.client.mixin.folio;

import com.aura.lamplight.folio.client.DragonbladeVignette;
import com.aura.lamplight.folio.DragonbladeTeleportRequestPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
@SuppressWarnings ("all")
public abstract class DragonbladeAttackMixin {
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void lamplight$dragonbladeTeleport(CallbackInfoReturnable<Boolean> cir) {
        if (!DragonbladeVignette.isActive()) return;
        Minecraft mc = (Minecraft) (Object) this;
        if (mc.player == null || mc.level == null) return;
        if (com.aura.lamplight.folio.effect.Frozen.is(mc.player)) return;

        Vec3 start = mc.player.getEyePosition();
        Vec3 dir = mc.player.getLookAngle();
        double range = 30.0D;
        Vec3 end = start.add(dir.scale(range));
        AABB box = new AABB(start, end).inflate(1.0D);

        Entity closest = null;
        double closestDist = range;
        for (Entity e : mc.level.getEntities(mc.player, box)) {
            if (e == mc.player) continue;
            if (!lamplight$isDragonbladeTarget(e)) continue;
            AABB bb = e.getBoundingBox();
            java.util.Optional<Vec3> hit = bb.clip(start, end);
            if (hit.isPresent()) {
                double d = start.distanceTo(hit.get());
                if (d < closestDist) {
                    closest = e;
                    closestDist = d;
                }
            }
        }

        if (closest != null) {
            ClientPlayNetworking.send(new DragonbladeTeleportRequestPayload(closest.getId()));
            com.aura.lamplight.folio.client.DragonbladeVignette.addTeleported(closest.getId());
            if (mc.player != null) {
                mc.player.swing(InteractionHand.MAIN_HAND);
                mc.player.crit(closest);
            }
            cir.setReturnValue(true);
        }
    }

    @Unique
    private static boolean lamplight$isDragonbladeTarget(Entity entity) {
        if (!entity.isAlive()) return false;
        if (entity instanceof AbstractWindCharge) return true;
        return entity instanceof LivingEntity && entity.isPickable();
    }
}
