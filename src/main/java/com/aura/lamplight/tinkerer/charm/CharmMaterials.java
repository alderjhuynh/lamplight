package com.aura.lamplight.tinkerer.charm;

import com.aura.lamplight.tinkerer.Tinkerer;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import com.mojang.serialization.Codec;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings ("all")
public class CharmMaterials extends SimpleJsonResourceReloadListener<CharmMaterial>
		implements IdentifiableResourceReloadListener {

	private static final AtomicReference<Map<Identifier, CharmMaterial>> LOADED = new AtomicReference<>(Map.of());

	public CharmMaterials() {
		super(CharmMaterial.CODEC, FileToIdConverter.json("charm_material"));
	}

	@Override
	protected void apply(final Map<Identifier, CharmMaterial> data, final ResourceManager manager,
	                     final ProfilerFiller profiler) {
		LOADED.set(Map.copyOf(data));
		Tinkerer.LOGGER.info("Loaded {} charm material definitions", data.size());
	}

	@Override
	public Identifier getFabricId() {
		return Tinkerer.id("charm_materials");
	}

	public static Map<Identifier, CharmMaterial> all() {
		return LOADED.get();
	}

	public static void applySnapshot(final Map<Identifier, CharmMaterial> data) {
		LOADED.set(Map.copyOf(data));
	}

	public static CharmMaterial byId(final Identifier id) {
		return LOADED.get().get(id);
	}

	public static @Nullable Identifier idOf(final CharmMaterial material) {
		for (Map.Entry<Identifier, CharmMaterial> entry : LOADED.get().entrySet()) {
			if (entry.getValue() == material) {
				return entry.getKey();
			}
		}
		return null;
	}

	public static List<CharmMaterial> orderedMaterials() {
		return LOADED.get().entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.map(Map.Entry::getValue)
				.toList();
	}

	public static CharmMaterial byItem(final net.minecraft.world.item.ItemStack stack) {
		if (stack.isEmpty()) {
			return null;
		}
		for (CharmMaterial material : LOADED.get().values()) {
			if (material.matches(stack)) {
				return material;
			}
		}
		return null;
	}

	public static void initialize() {
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new CharmMaterials());
	}
}
