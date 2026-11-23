package com.aura.lamplight.client.mixin.nullbloom;

import com.aura.lamplight.nullbloom.GodsBargain;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class HudMissingHeartMixin {
	private static final Identifier MISSING_HEART = Identifier.fromNamespaceAndPath("lamplight", "textures/gui/sprites/hud/heart/missing.png");

	@Inject(method = "extractHearts", at = @At("TAIL"))
	private void lamplight$drawMissingHeart(
		final GuiGraphicsExtractor graphics,
		final Player player,
		final int xLeft,
		final int yLineBase,
		final int healthRowHeight,
		final int heartOffsetIndex,
		final float maxHealth,
		final int currentHealth,
		final int oldHealth,
		final int absorption,
		final boolean blink,
		final CallbackInfo ci
	) {
		if (!GodsBargain.hasAccepted(player)) {
			return;
		}
		int healthContainerCount = (int) Math.ceil(maxHealth / 2.0);
		int absorptionContainerCount = (int) Math.ceil(absorption / 2.0);
		int containerIndex = healthContainerCount + absorptionContainerCount;
		int row = containerIndex / 10;
		int column = containerIndex % 10;
		int xo = xLeft + column * 8;
		int yo = yLineBase - row * healthRowHeight;
		if (currentHealth + absorption <= 4) {
			yo += player.getRandom().nextInt(2);
		}
		if (containerIndex == heartOffsetIndex) {
			yo -= 2;
		}
		boolean hardcore = player.level().getLevelData().isHardcore();
		String container = "container";
		if (hardcore) {
			container = "container_hardcore";
		}
		if (blink) {
			container = container + "_blinking";
		}
		graphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("textures/gui/sprites/hud/heart/" + container + ".png"), xo, yo, 0, 0, 9, 9, 9, 9);
		graphics.blit(RenderPipelines.GUI_TEXTURED, MISSING_HEART, xo, yo, 0, 0, 9, 9, 9, 9);
	}
}
