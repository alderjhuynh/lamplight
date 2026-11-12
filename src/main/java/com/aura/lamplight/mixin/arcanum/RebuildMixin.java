package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.server.RebuildHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Feeds the §5–§7 rebuild enchantments into RebuildHandler. */
@Mixin(LivingEntity.class)
public abstract class RebuildMixin {

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void lamplight$rebuildModifyIncoming(ServerLevel level, DamageSource source, float amount,
                                                 CallbackInfoReturnable<Boolean> cir) {
        if (RebuildHandler.isRecursing()) return;
        LivingEntity self = (LivingEntity) (Object) this;
        float adjusted = RebuildHandler.modifyIncoming(level, self, source, amount);
        if (adjusted != amount) {
            cir.setReturnValue(RebuildHandler.directHurt(level, self, source, adjusted));
        }
    }

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void lamplight$rebuildOnHurt(ServerLevel level, DamageSource source, float amount,
                                         CallbackInfoReturnable<Boolean> cir) {
        RebuildHandler.onHurt(level, (LivingEntity) (Object) this, source, amount, cir.getReturnValue());
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void lamplight$rebuildOnDeath(DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level() instanceof ServerLevel level) {
            RebuildHandler.onDeath(level, self, source);
        }
    }

    // --- plunder: re-run the vanilla loot table for an extra set of drops ---

    @Shadow
    protected abstract void dropFromLootTable(ServerLevel level, DamageSource source, boolean hitByExplosion);

    @Inject(method = "dropAllDeathLoot", at = @At("RETURN"))
    private void lamplight$plunderExtraLoot(ServerLevel level, DamageSource source, CallbackInfo ci) {
        if (RebuildHandler.consumePlunder(((LivingEntity) (Object) this).getUUID())) {
            this.dropFromLootTable(level, source, false);
        }
    }

    // --- tuition: +15/30% experience ---

    @Inject(method = "getExperienceReward", at = @At("RETURN"), cancellable = true)
    private void lamplight$tuitionXp(ServerLevel level, Entity killer, CallbackInfoReturnable<Integer> cir) {
        int tuitionLvl = RebuildHandler.consumeTuition(((LivingEntity) (Object) this).getUUID());
        int scholarLvl = killer instanceof Player player ? RebuildHandler.armorLvl(player,
                com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments.SCHOLAR) : 0;
        float multiplier = (1.0F + 0.15F * tuitionLvl) * (1.0F + 0.2F * scholarLvl);
        if (multiplier > 1.0F) {
            cir.setReturnValue(Math.round(cir.getReturnValue() * multiplier));
        }
    }
}
