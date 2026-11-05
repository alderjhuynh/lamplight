package com.aura.lamplight.armory.client;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.model.geom.ModelPart;

public final class ScytheArmPose {
	private static final TagKey<Item> SCYTHES =
			TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("melee_weapons/scythes"));

	public static float armXRot = 0.0F;
	public static float armYRot = 0.5689F; 
	public static float armZRot = 0.7614F;

	public static float fpRotX;
	public static float fpRotY;
	public static float fpRotZ;
	public static float fpTransX;
	public static float fpTransY;
	public static float fpTransZ;
	public static float fpScale = 1.0F;

	public static float tpRotX = -50F;
	public static float tpRotY = 0.0F;
	public static float tpRotZ = 0.0F;
	public static float tpTransX = 0.0F;
	public static float tpTransY = -0.16F;
	public static float tpTransZ = -0.045F;
	public static float tpScale = 1.0F;
	public static float tpLeftRotX = 50F;
	public static float tpLeftRotY = 180.0F;
	public static float tpLeftRotZ = 0.0F;
	public static float tpLeftTransX = 0.0F;
	public static float tpLeftTransY = -0.16F;
	public static float tpLeftTransZ = -0.045F;
	public static float tpLeftScale = 1.0F;

	public static boolean tpDeltaNeutral(boolean left) {
		if (left) {
			return tpLeftRotX == 0.0F && tpLeftRotY == 0.0F && tpLeftRotZ == 0.0F
					&& tpLeftTransX == 0.0F && tpLeftTransY == 0.0F && tpLeftTransZ == 0.0F
					&& tpLeftScale == 1.0F;
		}
		return tpRotX == 0.0F && tpRotY == 0.0F && tpRotZ == 0.0F
				&& tpTransX == 0.0F && tpTransY == 0.0F && tpTransZ == 0.0F
				&& tpScale == 1.0F;
	}

	private ScytheArmPose() {
	}

	public static boolean isScythe(ItemStack stack) {
		return !stack.isEmpty() && stack.is(SCYTHES);
	}

	public static void applyPose(ModelPart scytheArm, boolean left) {
		float side = left ? -1.0F : 1.0F;
		scytheArm.xRot = armXRot;
		scytheArm.yRot = armYRot * side;
		scytheArm.zRot = armZRot * side;
	}
}
