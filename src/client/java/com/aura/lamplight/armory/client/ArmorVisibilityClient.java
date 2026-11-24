package com.aura.lamplight.armory.client;

import com.aura.lamplight.armory.ArmorVisibility;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public final class ArmorVisibilityClient {
	private ArmorVisibilityClient() {
	}

	public static boolean isVisible(HumanoidRenderState state, EquipmentSlot slot) {
		if (state instanceof AvatarRenderState ars) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.level != null) {
				net.minecraft.world.entity.Entity entity = mc.level.getEntity(ars.id);
				if (entity instanceof Player player) {
					return ArmorVisibility.isVisible(player, slot);
				}
			}
		}
		return true;
	}

	public static boolean isBackVisible(AvatarRenderState state) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null) {
			net.minecraft.world.entity.Entity entity = mc.level.getEntity(state.id);
			if (entity instanceof Player player) {
				return ArmorVisibility.isBackVisible(player);
			}
		}
		return true;
	}
}
