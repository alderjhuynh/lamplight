package com.aura.lamplight.armory.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ScythePoseEditorStore {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public float armXDeg = (float) Math.toDegrees(ScytheArmPose.armXRot);
	public float armYDeg = (float) Math.toDegrees(ScytheArmPose.armYRot);
	public float armZDeg = (float) Math.toDegrees(ScytheArmPose.armZRot);
	public float fpRotX;
	public float fpRotY;
	public float fpRotZ;
	public float fpTransX;
	public float fpTransY;
	public float fpTransZ;
	public float fpScale = 1.0F;
	public float tpRotX;
	public float tpRotY;
	public float tpRotZ;
	public float tpTransX;
	public float tpTransY;
	public float tpTransZ;
	public float tpScale = 1.0F;
	public float tpLeftRotX;
	public float tpLeftRotY;
	public float tpLeftRotZ;
	public float tpLeftTransX;
	public float tpLeftTransY;
	public float tpLeftTransZ;
	public float tpLeftScale = 1.0F;

	private ScythePoseEditorStore() {
	}

	public static ScythePoseEditorStore load() {
		ScythePoseEditorStore store = new ScythePoseEditorStore();
		Path file = file();
		if (!Files.exists(file)) {
			return store;
		}
		try {
			ScythePoseEditorStore read = GSON.fromJson(Files.readString(file), ScythePoseEditorStore.class);
			if (read != null) {
				store.armXDeg = read.armXDeg;
				store.armYDeg = read.armYDeg;
				store.armZDeg = read.armZDeg;
				store.fpRotX = read.fpRotX;
				store.fpRotY = read.fpRotY;
				store.fpRotZ = read.fpRotZ;
				store.fpTransX = read.fpTransX;
				store.fpTransY = read.fpTransY;
				store.fpTransZ = read.fpTransZ;
				store.fpScale = read.fpScale;
				store.tpRotX = read.tpRotX;
				store.tpRotY = read.tpRotY;
				store.tpRotZ = read.tpRotZ;
				store.tpTransX = read.tpTransX;
				store.tpTransY = read.tpTransY;
				store.tpTransZ = read.tpTransZ;
				store.tpScale = read.tpScale;
				store.tpLeftRotX = read.tpLeftRotX;
				store.tpLeftRotY = read.tpLeftRotY;
				store.tpLeftRotZ = read.tpLeftRotZ;
				store.tpLeftTransX = read.tpLeftTransX;
				store.tpLeftTransY = read.tpLeftTransY;
				store.tpLeftTransZ = read.tpLeftTransZ;
				store.tpLeftScale = read.tpLeftScale;
			}
		} catch (Exception ignored) {
		}
		return store;
	}

	public void applyToLive() {
		ScytheArmPose.armXRot = (float) Math.toRadians(armXDeg);
		ScytheArmPose.armYRot = (float) Math.toRadians(armYDeg);
		ScytheArmPose.armZRot = (float) Math.toRadians(armZDeg);
		ScytheArmPose.fpRotX = fpRotX;
		ScytheArmPose.fpRotY = fpRotY;
		ScytheArmPose.fpRotZ = fpRotZ;
		ScytheArmPose.fpTransX = fpTransX;
		ScytheArmPose.fpTransY = fpTransY;
		ScytheArmPose.fpTransZ = fpTransZ;
		ScytheArmPose.fpScale = fpScale;
		ScytheArmPose.tpRotX = tpRotX;
		ScytheArmPose.tpRotY = tpRotY;
		ScytheArmPose.tpRotZ = tpRotZ;
		ScytheArmPose.tpTransX = tpTransX;
		ScytheArmPose.tpTransY = tpTransY;
		ScytheArmPose.tpTransZ = tpTransZ;
		ScytheArmPose.tpScale = tpScale;
		ScytheArmPose.tpLeftRotX = tpLeftRotX;
		ScytheArmPose.tpLeftRotY = tpLeftRotY;
		ScytheArmPose.tpLeftRotZ = tpLeftRotZ;
		ScytheArmPose.tpLeftTransX = tpLeftTransX;
		ScytheArmPose.tpLeftTransY = tpLeftTransY;
		ScytheArmPose.tpLeftTransZ = tpLeftTransZ;
		ScytheArmPose.tpLeftScale = tpLeftScale;
	}

	public void save() {
		try {
			Files.createDirectories(file().getParent());
			Files.writeString(file(), GSON.toJson(this));
		} catch (Exception ignored) {
		}
	}

	public void resetToDefaults() {
		armXDeg = (float) Math.toDegrees(-0.35F);
		armYDeg = (float) Math.toDegrees(0.10F);
		armZDeg = (float) Math.toDegrees(-0.15F);
		fpRotX = 0.0F;
		fpRotY = 0.0F;
		fpRotZ = 0.0F;
		fpTransX = 0.0F;
		fpTransY = 0.0F;
		fpTransZ = 0.0F;
		fpScale = 1.0F;
		tpRotX = 0.0F;
		tpRotY = 0.0F;
		tpRotZ = 0.0F;
		tpTransX = 0.0F;
		tpTransY = 0.0F;
		tpTransZ = 0.0F;
		tpScale = 1.0F;
		tpLeftRotX = 0.0F;
		tpLeftRotY = 0.0F;
		tpLeftRotZ = 0.0F;
		tpLeftTransX = 0.0F;
		tpLeftTransY = 0.0F;
		tpLeftTransZ = 0.0F;
		tpLeftScale = 1.0F;
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir()
				.resolve("lamplight").resolve("scythe_pose_dev.json");
	}
}
