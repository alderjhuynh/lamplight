package com.aura.lamplight.wayfarer.waystone;

import com.aura.lamplight.wayfarer.Wayfarer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class WaystoneNetwork extends SavedData {

    private static final Codec<Map<ResourceKey<Level>, List<Long>>> VALUES_CODEC =
            Codec.unboundedMap(ResourceKey.codec(Registries.DIMENSION), Codec.LONG.listOf());

    private static final Codec<Map<Long, String>> NAMES_BY_POS_CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.STRING).xmap(
                    from -> {
                        Map<Long, String> out = new HashMap<>();
                        from.forEach((key, name) -> out.put(Long.parseLong(key), name));
                        return out;
                    },
                    to -> {
                        Map<String, String> out = new HashMap<>();
                        to.forEach((pos, name) -> out.put(Long.toString(pos), name));
                        return out;
                    });

    private static final Codec<Map<ResourceKey<Level>, Map<Long, String>>> NAMES_CODEC =
            Codec.unboundedMap(ResourceKey.codec(Registries.DIMENSION), NAMES_BY_POS_CODEC);

    private static final Codec<WaystoneNetwork> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    VALUES_CODEC.fieldOf("stones").forGetter(WaystoneNetwork::asMap),
                    NAMES_CODEC.optionalFieldOf("names", Map.of()).forGetter(WaystoneNetwork::namesAsMap))
                    .apply(instance, WaystoneNetwork::new));

    public static final SavedDataType<WaystoneNetwork> TYPE = new SavedDataType<>(
            Wayfarer.id("waystones"), WaystoneNetwork::new, CODEC, null);

    private final Map<ResourceKey<Level>, Set<Long>> stones = new HashMap<>();
    private final Map<ResourceKey<Level>, Map<Long, String>> names = new HashMap<>();

    public WaystoneNetwork() {
    }

    private WaystoneNetwork(Map<ResourceKey<Level>, List<Long>> values,
                            Map<ResourceKey<Level>, Map<Long, String>> namedStones) {
        for (Map.Entry<ResourceKey<Level>, List<Long>> entry : values.entrySet()) {
            this.stones.put(entry.getKey(), new HashSet<>(entry.getValue()));
            Map<Long, String> named = namedStones.get(entry.getKey());
            if (named != null) {
                this.names.put(entry.getKey(), new HashMap<>(named));
            }
        }
    }

    public static WaystoneNetwork get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    private Map<ResourceKey<Level>, List<Long>> asMap() {
        Map<ResourceKey<Level>, List<Long>> map = new HashMap<>();
        for (Map.Entry<ResourceKey<Level>, Set<Long>> entry : this.stones.entrySet()) {
            map.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return map;
    }

    private Map<ResourceKey<Level>, Map<Long, String>> namesAsMap() {
        Map<ResourceKey<Level>, Map<Long, String>> map = new HashMap<>();
        for (Map.Entry<ResourceKey<Level>, Map<Long, String>> entry : this.names.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                map.put(entry.getKey(), new HashMap<>(entry.getValue()));
            }
        }
        return map;
    }

    public void add(ResourceKey<Level> dimension, BlockPos base) {
        if (this.stones.computeIfAbsent(dimension, key -> new HashSet<>()).add(base.asLong())) {
            this.setDirty();
        }
    }

    public void rememberIfMissing(ResourceKey<Level> dimension, BlockPos base) {
        Set<Long> set = this.stones.get(dimension);
        if (set == null || !set.contains(base.asLong())) {
            this.add(dimension, base);
        }
    }

    public boolean contains(ResourceKey<Level> dimension, BlockPos base) {
        Set<Long> set = this.stones.get(dimension);
        return set != null && set.contains(base.asLong());
    }

    public void rename(ResourceKey<Level> dimension, BlockPos base, String name) {
        if (!this.contains(dimension, base)) {
            return;
        }
        Map<Long, String> named = this.names.computeIfAbsent(dimension, key -> new HashMap<>());
        long packed = base.asLong();
        if (name.isEmpty()) {
            if (named.remove(packed) != null) {
                this.setDirty();
            }
        } else {
            if (!name.equals(named.put(packed, name))) {
                this.setDirty();
            }
        }
    }

    public String name(ResourceKey<Level> dimension, BlockPos base) {
        Map<Long, String> named = this.names.get(dimension);
        return named == null ? "" : named.getOrDefault(base.asLong(), "");
    }

    public List<WaystoneTravelListPayload.Entry> travelStones(ServerPlayer traveler, BlockPos source, ResourceKey<Level> within) {
        ServerLevel level = traveler.level().getServer().getLevel(within);
        if (level == null) {
            return List.of();
        }
        List<WaystoneTravelListPayload.Entry> result = new ArrayList<>();
        Set<Long> known = this.stones.get(within);
        if (known != null && !known.isEmpty()) {
            for (Long packed : new ArrayList<>(known)) {
                BlockPos base = BlockPos.of(packed);
                if (!WaystoneBlock.isLitLower(level.getBlockState(base))) {
                    this.remove(within, base);
                    continue;
                }
                if (!base.equals(source)) {
                    result.add(new WaystoneTravelListPayload.Entry(within, base, this.name(within, base)));
                }
            }
            result.sort(Comparator.comparingDouble(entry -> distanceSq(traveler, entry.pos())));
        }
        return result;
    }

    private boolean remove(ResourceKey<Level> dimension, BlockPos base) {
        Set<Long> set = this.stones.get(dimension);
        if (set != null && set.remove(base.asLong())) {
            Map<Long, String> named = this.names.get(dimension);
            if (named != null) {
                named.remove(base.asLong());
            }
            this.setDirty();
            return true;
        }
        return false;
    }

    private static double distanceSq(ServerPlayer traveler, BlockPos base) {
        double dx = base.getX() + 0.5 - traveler.getX();
        double dy = base.getY() + 1.5 - (traveler.getY() + 1.5);
        double dz = base.getZ() + 0.5 - traveler.getZ();
        return dx * dx + dy * dy + dz * dz;
    }
}
