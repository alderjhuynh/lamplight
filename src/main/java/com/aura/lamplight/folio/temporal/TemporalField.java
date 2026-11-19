package com.aura.lamplight.folio.temporal;

import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class TemporalField {
    private final ResourceKey<Level> dimension;
    private final Vec3 center;
    private final @Nullable Entity anchor;
    private final double radius;
    private final double radiusSq;
    private final float tickRate;
    private final long expiresAtGameTime;
    private final Predicate<Entity> filter;
    private final @Nullable ParticleOptions particle;
    private final int particleIntervalTicks;
    private final int particleCount;

    private TemporalField(Builder builder) {
        this.dimension = builder.level.dimension();
        this.center = builder.center;
        this.anchor = builder.anchor;
        this.radius = builder.radius;
        this.radiusSq = builder.radius * builder.radius;
        this.tickRate = builder.tickRate;
        this.expiresAtGameTime = builder.expiresAtGameTime;
        this.filter = builder.filter;
        this.particle = builder.particle;
        this.particleIntervalTicks = Math.max(1, builder.particleIntervalTicks);
        this.particleCount = builder.particleCount;
    }

    public static Builder builder(ServerLevel level) {
        return new Builder(level);
    }

    public ResourceKey<Level> dimension() {
        return dimension;
    }

    public Vec3 center() {
        Entity anchor = this.anchor;
        if (anchor != null && !anchor.isRemoved()) {
            return anchor.position();
        }
        return center;
    }

    public double radius() {
        return radius;
    }

    public float tickRate() {
        return tickRate;
    }

    public boolean contains(Entity entity) {
        if (!entity.level().dimension().equals(dimension)) {
            return false;
        }
        Entity anchor = this.anchor;
        if (anchor != null && (anchor.isRemoved() || !anchor.level().dimension().equals(dimension))) {
            return false;
        }
        if (entity.position().distanceToSqr(center()) > radiusSq) {
            return false;
        }
        return filter.test(entity);
    }

    public @Nullable ParticleOptions particle() {
        return particle;
    }

    public int particleIntervalTicks() {
        return particleIntervalTicks;
    }

    public int particleCount() {
        return particleCount;
    }

    boolean isExpired(ServerLevel level) {
        return expiresAtGameTime != Long.MAX_VALUE && level.getGameTime() >= expiresAtGameTime;
    }

    boolean isAnchorInvalid(ServerLevel level) {
        Entity anchor = this.anchor;
        if (anchor == null) {
            return false;
        }
        return anchor.isRemoved() || !anchor.level().dimension().equals(dimension);
    }

    @Override
    public String toString() {
        return "TemporalField[" + dimension + " r=" + radius + " rate=" + tickRate + "]";
    }

    public static final class Builder {
        private final ServerLevel level;
        private Vec3 center = Vec3.ZERO;
        private @Nullable Entity anchor;
        private double radius = 6.0D;
        private float tickRate = 10.0F;
        private long durationTicks = Long.MAX_VALUE;
        private long expiresAtGameTime = Long.MAX_VALUE;
        private Predicate<Entity> filter = entity -> true;
        private @Nullable ParticleOptions particle;
        private int particleIntervalTicks = 4;
        private int particleCount = 20;

        private Builder(ServerLevel level) {
            this.level = Objects.requireNonNull(level, "level");
        }

        public Builder center(Vec3 center) {
            this.center = Objects.requireNonNull(center, "center");
            this.anchor = null;
            return this;
        }

        public Builder center(Entity entity) {
            return follow(entity);
        }

        public Builder follow(Entity entity) {
            this.anchor = Objects.requireNonNull(entity, "entity");
            this.center = entity.position();
            return this;
        }

        public Builder radius(double radius) {
            if (radius <= 0.0D) {
                throw new IllegalArgumentException("radius must be positive");
            }
            this.radius = radius;
            return this;
        }

        public Builder tickRate(float tickRate) {
            if (tickRate < 1.0F) {
                throw new IllegalArgumentException("tickRate must be >= 1");
            }
            this.tickRate = tickRate;
            return this;
        }

        public Builder duration(int ticks) {
            return durationTicks(ticks);
        }

        public Builder durationTicks(long ticks) {
            if (ticks <= 0L) {
                throw new IllegalArgumentException("duration must be positive");
            }
            this.durationTicks = ticks;
            long now = level.getGameTime();
            this.expiresAtGameTime = (ticks == Long.MAX_VALUE) ? Long.MAX_VALUE : now + ticks;
            return this;
        }

        public Builder filter(Predicate<Entity> filter) {
            this.filter = Objects.requireNonNull(filter, "filter");
            return this;
        }

        public Builder affectOnly(Predicate<Entity> predicate) {
            return filter(predicate);
        }

        public Builder exclude(Entity entity) {
            Predicate<Entity> previous = this.filter;
            this.filter = e -> !e.equals(entity) && previous.test(e);
            return this;
        }

        public Builder visual(ParticleOptions particle, int intervalTicks, int count) {
            this.particle = Objects.requireNonNull(particle, "particle");
            this.particleIntervalTicks = intervalTicks;
            this.particleCount = count;
            return this;
        }

        public Builder visual(ParticleOptions particle, int intervalTicks) {
            return visual(particle, intervalTicks, 20);
        }

        public TemporalField build() {
            return new TemporalField(this);
        }
    }
}
