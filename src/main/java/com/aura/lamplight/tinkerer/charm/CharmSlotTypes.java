package com.aura.lamplight.tinkerer.charm;

import com.aura.lamplight.tinkerer.Tinkerer;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings ("all")
public class CharmSlotTypes extends SimpleJsonResourceReloadListener<CharmSlotDefinition>
		implements IdentifiableResourceReloadListener {

	private static final AtomicReference<List<CharmSlotType>> LOADED = new AtomicReference<>(List.of());

	public record CharmSlotType(Identifier id, CharmSlotDefinition def) {}

	public CharmSlotTypes() {
		super(CharmSlotDefinition.CODEC, FileToIdConverter.json("charm_slot"));
	}

	@Override
	protected void apply(final Map<Identifier, CharmSlotDefinition> data, final ResourceManager manager,
	                     final ProfilerFiller profiler) {
		List<CharmSlotType> sorted = data.entrySet().stream()
				.map(entry -> new CharmSlotType(entry.getKey(), entry.getValue()))
				.sorted(Comparator.comparingInt(type -> type.def().order()))
				.toList();
		LOADED.set(List.copyOf(sorted));
		Tinkerer.LOGGER.info("Loaded {} charm slot definitions", data.size());
	}

	@Override
	public Identifier getFabricId() {
		return Tinkerer.id("charm_slots");
	}

	public static List<CharmSlotType> all() {
		return LOADED.get();
	}

	public static void applySnapshot(final Map<Identifier, CharmSlotDefinition> data) {
		List<CharmSlotType> sorted = data.entrySet().stream()
				.map(entry -> new CharmSlotType(entry.getKey(), entry.getValue()))
				.sorted(Comparator.comparingInt(type -> type.def().order()))
				.toList();
		LOADED.set(List.copyOf(sorted));
	}

	public static int count() {
		return all().size();
	}

	public static int definedCount(final String slug) {
		int defined = 0;
		for (CharmSlotType slot : LOADED.get()) {
			if (slot.def().slotType().equals(slug)) {
				defined++;
			}
		}
		return defined;
	}

	public static CharmSlotType byId(final Identifier id) {
		for (CharmSlotType slot : LOADED.get()) {
			if (slot.id().equals(id)) {
				return slot;
			}
		}
		return null;
	}

	public static int rankOf(final CharmSlotType slot) {
		int rank = 0;
		for (CharmSlotType other : LOADED.get()) {
			if (!other.def().slotType().equals(slot.def().slotType())) {
				continue;
			}
			rank++;
			if (other.id().equals(slot.id())) {
				return rank;
			}
		}
		return 0;
	}

	public static Map<Identifier, CharmSlotDefinition> snapshots() {
		Map<Identifier, CharmSlotDefinition> snapshot = new java.util.HashMap<>();
		for (CharmSlotType slot : LOADED.get()) {
			snapshot.put(slot.id(), slot.def());
		}
		return snapshot;
	}

	public static void initialize() {
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new CharmSlotTypes());
	}
}
