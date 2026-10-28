package com.aura.lamplight.mixin.tinkerer;

import com.aura.lamplight.tinkerer.charm.CharmAggregate;
import com.aura.lamplight.tinkerer.charm.CharmAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class CharmHurtServerMixin {

	@Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
	private void lamplight$charmDodge(final ServerLevel level, final DamageSource source,
	                                  final float amount, final CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof Player victim) {
			@Nullable CharmAggregate aggregate = CharmAggregate.of(CharmAttachments.wornEntries(victim));
			if (aggregate.dodgeChance() <= 0.0) {
				return;
			}
			for (TagKey<DamageType> tag : aggregate.cannotDodge()) {
				if (source.is(tag)) {
					return; 
				}
			}
			if (level.getRandom().nextFloat() < aggregate.dodgeChance()) {
				cir.setReturnValue(Boolean.FALSE);
			}
		}
	}

	@ModifyVariable(method = "hurtServer", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private float lamplight$charmResist(final float amount, final ServerLevel level, final DamageSource source, final float captured) {
		if ((Object) this instanceof Player victim) {
			@Nullable CharmAggregate aggregate = CharmAggregate.of(CharmAttachments.wornEntries(victim));
			double fraction = aggregate.resist(source.typeHolder());
			if (fraction <= 0.0) {
				return amount;
			}
			float reduced = amount * (float) (1.0 - fraction);
			return Math.max(0.0F, Math.min(amount, reduced));
		}
		return amount;
	}
}
