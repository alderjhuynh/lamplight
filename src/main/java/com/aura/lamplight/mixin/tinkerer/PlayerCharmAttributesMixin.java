package com.aura.lamplight.mixin.tinkerer;

import com.aura.lamplight.tinkerer.charm.CharmSlotKinds;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerCharmAttributesMixin {

	@com.llamalad7.mixinextras.injector.ModifyReturnValue(
			method = "createAttributes()Lnet/minecraft/world/entity/ai/attributes/AttributeSupplier$Builder;",
			at = @At("RETURN"))
	private static AttributeSupplier.Builder lamplight$addCharmAttributes(
			final AttributeSupplier.Builder original) {
		return CharmSlotKinds.addToPlayerAttributes(original);
	}
}
