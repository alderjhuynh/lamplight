package com.aura.lamplight.client.mixin.armory;

import com.aura.lamplight.armory.BackSlotAttachments;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// back slot hud
@Mixin(Hud.class)
public abstract class HudMixin {
	@Shadow
	@Final
	private static Identifier HOTBAR_SELECTION_SPRITE;

	@Shadow
	@Final
	private static Identifier HOTBAR_OFFHAND_LEFT_SPRITE;

	@Shadow
	@Final
	private static Identifier HOTBAR_OFFHAND_RIGHT_SPRITE;

	@Shadow
	protected abstract Player getCameraPlayer();

	@Shadow
	protected abstract void extractSlot(GuiGraphicsExtractor graphics, int x, int y, DeltaTracker tracker, Player player, ItemStack stack, int seed);

	// while wielding, selection is on the back slot, which draws its own
	// highlight, so vanilla hotbar highlight is supressed. 
	@Redirect(
			method = "extractItemHotbar",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
					ordinal = 1
			)
	)
	private void armory$suppressHotbarSelection(GuiGraphicsExtractor graphics, com.mojang.blaze3d.pipeline.RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height) {
		Player player = getCameraPlayer();
		if (player != null && BackSlotAttachments.isHolding(player)) {
			return;
		}
		graphics.blitSprite(pipeline, sprite, x, y, width, height);
	}

	@Inject(method = "extractItemHotbar", at = @At("TAIL"))
	private void armory$drawBackSlot(GuiGraphicsExtractor graphics, DeltaTracker tracker, CallbackInfo ci) {
		Player player = getCameraPlayer();
		if (player == null) {
			return;
		}
		ItemStack shown = BackSlotAttachments.getBackStack(player);
		if (shown.isEmpty()) {
			return;
		}

		int middle = graphics.guiWidth() / 2;
		int bgY = graphics.guiHeight() - 23;
		int itemY = graphics.guiHeight() - 19;

		// mirror the offhand slot onto the opposite side.
		Identifier bgSprite;
		int bgX;
		int itemX;
		if (player.getMainArm().getOpposite() == HumanoidArm.LEFT) {
			bgSprite = HOTBAR_OFFHAND_RIGHT_SPRITE;
			bgX = middle + 91;
			itemX = middle + 91 + 10;
		} else {
			bgSprite = HOTBAR_OFFHAND_LEFT_SPRITE;
			bgX = middle - 91 - 29;
			itemX = middle - 91 - 26;
		}

		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, bgSprite, bgX, bgY, 29, 24);
		if (BackSlotAttachments.isHolding(player)) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION_SPRITE, itemX - 3, itemY - 4, 24, 23);
		}
		extractSlot(graphics, itemX, itemY, tracker, player, shown, 11);
	}
}
