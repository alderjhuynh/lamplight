package com.aura.lamplight.armory.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BackSlotTransform {
	private static final Logger LOGGER = LoggerFactory.getLogger("lamplight");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = "armory_backslot.json";

	public static final float DEFAULT_OFFSET_X = 0.0F;
	public static final float DEFAULT_OFFSET_Y = 0.35F;
	public static final float DEFAULT_OFFSET_Z = 0.18F;
	public static final float DEFAULT_ROT_X = 0.0F;
	public static final float DEFAULT_ROT_Y = 180.0F;
	public static final float DEFAULT_ROT_Z = 0.0F;
	public static final float DEFAULT_SCALE = 0.8F;

	public static final float HAND_OFFSET_X = -0.1F;
	public static final float HAND_OFFSET_Y = 0.35F;
	public static final float HAND_OFFSET_Z = 0.18F;
	public static final float HAND_ROT_X = 45.0F;
	public static final float HAND_ROT_Y = 90.0F;
	public static final float HAND_ROT_Z = 0.0F;
	public static final float HAND_SCALE = 1.0F;

	private static final BackSlotTransform INSTANCE = new BackSlotTransform();
	private static final BackSlotTransform HAND_INSTANCE = createHandInstance();

	public float offsetX = DEFAULT_OFFSET_X;
	public float offsetY = DEFAULT_OFFSET_Y;
	public float offsetZ = DEFAULT_OFFSET_Z;
	public float rotX = DEFAULT_ROT_X;
	public float rotY = DEFAULT_ROT_Y;
	public float rotZ = DEFAULT_ROT_Z;
	public float scale = DEFAULT_SCALE;

	public static BackSlotTransform get() {
		return INSTANCE;
	}

	public static BackSlotTransform hand() {
		return HAND_INSTANCE;
	}

	private static BackSlotTransform createHandInstance() {
		BackSlotTransform transform = new BackSlotTransform();
		transform.offsetX = HAND_OFFSET_X;
		transform.offsetY = HAND_OFFSET_Y;
		transform.offsetZ = HAND_OFFSET_Z;
		transform.rotX = HAND_ROT_X;
		transform.rotY = HAND_ROT_Y;
		transform.rotZ = HAND_ROT_Z;
		transform.scale = HAND_SCALE;
		return transform;
	}

	public static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
	}

	public static void load() {
		INSTANCE.resetToDefaults();
		Path path = file();
		if (!Files.isRegularFile(path)) {
			return;
		}
		try {
			String json = Files.readString(path);
			BackSlotTransform loaded = GSON.fromJson(json, BackSlotTransform.class);
			if (loaded != null) {
				INSTANCE.copyFrom(loaded);
			}
		} catch (IOException | RuntimeException e) {
			LOGGER.warn("[armory] Failed to read {}, using defaults", path, e);
		}
	}

	public void save() {
		try {
			Path path = file();
			Files.createDirectories(path.getParent());
			Files.writeString(path, GSON.toJson(this));
		} catch (IOException e) {
			LOGGER.warn("Failed to write {}", file(), e);
		}
	}

	public void resetToDefaults() {
		offsetX = DEFAULT_OFFSET_X;
		offsetY = DEFAULT_OFFSET_Y;
		offsetZ = DEFAULT_OFFSET_Z;
		rotX = DEFAULT_ROT_X;
		rotY = DEFAULT_ROT_Y;
		rotZ = DEFAULT_ROT_Z;
		scale = DEFAULT_SCALE;
	}

	private void copyFrom(BackSlotTransform other) {
		offsetX = other.offsetX;
		offsetY = other.offsetY;
		offsetZ = other.offsetZ;
		rotX = other.rotX;
		rotY = other.rotY;
		rotZ = other.rotZ;
		scale = other.scale;
	}
}
