package com.aura.lamplight.nullbloom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class GodMemory extends SavedData {
	public record Entry(boolean met, int returnVisits, int deathsAtLastVisit, int contacts) {
		public static final Entry NEW = new Entry(false, 0, -1, 0);

		public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.BOOL.optionalFieldOf("met", false).forGetter(Entry::met),
			Codec.INT.optionalFieldOf("return_visits", 0).forGetter(Entry::returnVisits),
			Codec.INT.optionalFieldOf("deaths_at_last_visit", -1).forGetter(Entry::deathsAtLastVisit),
			Codec.INT.optionalFieldOf("contacts", 0).forGetter(Entry::contacts)
		).apply(i, Entry::new));
	}

	private static final Codec<GodMemory> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, Entry.CODEC)
		.xmap(GodMemory::new, GodMemory::asMap);

	public static final SavedDataType<GodMemory> TYPE = new SavedDataType<>(
		Nullbloom.id("god_memory"), GodMemory::new, CODEC, null);

	private final Map<UUID, Entry> entries = new HashMap<>();

	public GodMemory() {
	}

	private GodMemory(Map<UUID, Entry> loaded) {
		this.entries.putAll(loaded);
	}

	private Map<UUID, Entry> asMap() {
		return new HashMap<>(this.entries);
	}

	public static GodMemory get(MinecraftServer server) {
		return server.getDataStorage().computeIfAbsent(TYPE);
	}

	public Entry get(UUID player) {
		return this.entries.getOrDefault(player, Entry.NEW);
	}

	public void update(UUID player, UnaryOperator<Entry> change) {
		this.entries.put(player, change.apply(get(player)));
		this.setDirty();
	}

	public void forget(UUID player) {
		if (this.entries.remove(player) != null) {
			this.setDirty();
		}
	}
}
