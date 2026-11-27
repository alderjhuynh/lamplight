package com.aura.lamplight.mixin.folio;

import com.aura.lamplight.folio.effect.DamageEffectHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class PlayerEffectDamageMixin {

    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float folio$playerEffectDamage(
        final float damage,
        final ServerLevel level,
        final DamageSource source,
        final float originalDamage
    ) {
        return DamageEffectHooks.modifyIncomingDamage(level, (Player) (Object) this, source, damage);
    }
}
