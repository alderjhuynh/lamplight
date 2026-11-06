package com.aura.lamplight.puppeteer.client;

import com.aura.lamplight.puppeteer.Puppeteer;
import com.aura.lamplight.puppeteer.animation.AnimationDefinition;
import com.aura.lamplight.puppeteer.animation.AnimationDefinitions;
import com.aura.lamplight.puppeteer.network.AnimationStatePayload;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public final class AnimationClientState {
    private AnimationClientState() {}

    public static final float FADE_TICKS = 3.0F;

    public record Layer(Identifier animationId, long startGameTime) {}

    public record Seen(float firstSeen, float lastPresent, float layerAnchor) {}

    public record PhaseState(float phaseTicks, float lastClock) {}

    public record ActiveSet(int entityId, List<Layer> layers, float playbackRate, float measuredSpeed) {}

    public record Captured(@Nullable ActiveSet set, float clock) {}

    public static final RenderStateDataKey<Captured> ANIMATION_RENDER_DATA = RenderStateDataKey.create(
        () -> Puppeteer.id("active_animation").toString()
    );

    private static final Map<Integer, ActiveSet> BY_ENTITY_ID = new ConcurrentHashMap<>();
    private static final Map<Integer, Map<Identifier, Seen>> LAYER_SEEN = new HashMap<>();
    private static final Map<Integer, Map<Identifier, PhaseState>> LAYER_PHASE = new HashMap<>();

    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(AnimationStatePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                List<Layer> layers = payload.entries().stream()
                    .map(entry -> new Layer(entry.animationId(), entry.startGameTime()))
                    .toList();
                ActiveSet set = new ActiveSet(payload.entityId(), layers, payload.playbackRate(), payload.measuredSpeed());
                BY_ENTITY_ID.put(payload.entityId(), set);
            });
        });
    }

    public static @Nullable ActiveSet get(final int entityId) {
        return BY_ENTITY_ID.get(entityId);
    }

    public static Map<Identifier, Seen> trackSeen(final ActiveSet activeSet, final float clock) {
        Map<Identifier, Seen> seen = LAYER_SEEN.computeIfAbsent(activeSet.entityId(), id -> new HashMap<>());
        Map<Identifier, PhaseState> phase = LAYER_PHASE.computeIfAbsent(activeSet.entityId(), id -> new HashMap<>());
        for (Layer layer : activeSet.layers()) {
            Seen entry = seen.get(layer.animationId());
            boolean fresh = false;
            boolean keepPhase = false;
            boolean resetPhase = false;
            if (entry == null) {
                seen.put(layer.animationId(), new Seen(clock, clock, clock));
                fresh = true;
            } else if (clock - entry.lastPresent() > FADE_TICKS) {
                AnimationDefinition definition = AnimationDefinitions.byId(layer.animationId());
                boolean cycling = definition != null && definition.loop() && !definition.holdLastFrame();
                seen.put(layer.animationId(), new Seen(clock, clock, cycling ? entry.layerAnchor() : clock));
                keepPhase = cycling;
                resetPhase = !cycling;
            } else {
                seen.put(layer.animationId(), new Seen(entry.firstSeen(), clock, entry.layerAnchor()));
            }

            AnimationDefinition definition = AnimationDefinitions.byId(layer.animationId());
            float effectiveRate = definition != null && definition.playbackRate().isPresent()
                ? activeSet.playbackRate() : 1.0F;
            PhaseState priorPhase = resetPhase ? null : phase.get(layer.animationId());
            float phaseTicks = 0.0F;
            if (keepPhase && priorPhase != null) {
                phaseTicks = priorPhase.phaseTicks();
            } else if (!fresh && priorPhase != null) {
                float moved = clock - priorPhase.lastClock();
                phaseTicks = moved > 0.0F && moved < 4.0F
                    ? priorPhase.phaseTicks() + moved * effectiveRate
                    : priorPhase.phaseTicks();
            }
            phase.put(layer.animationId(), new PhaseState(phaseTicks, clock));
        }
        return seen;
    }

    public static float phaseOf(final ActiveSet activeSet, final Identifier id) {
        Map<Identifier, PhaseState> phases = LAYER_PHASE.get(activeSet.entityId());
        if (phases == null) {
            return 0.0F;
        }
        PhaseState phase = phases.get(id);
        return phase == null ? 0.0F : phase.phaseTicks();
    }

    public static void setPhase(final ActiveSet activeSet, final Identifier id, final float phaseTicks, final float clock) {
        LAYER_PHASE.computeIfAbsent(activeSet.entityId(), key -> new HashMap<>())
            .put(id, new PhaseState(phaseTicks, clock));
    }

    public static void pruneSeen(final @Nullable ActiveSet activeSet, final float clock) {
        Map<Identifier, Seen> seen = activeSet == null ? null : LAYER_SEEN.get(activeSet.entityId());
        if (seen == null) {
            return;
        }
        var iterator = seen.keySet().iterator();
        while (iterator.hasNext()) {
            Identifier id = iterator.next();
            if (clock - seen.get(id).lastPresent() > FADE_TICKS * 2.0F) {
                iterator.remove();
                Map<Identifier, PhaseState> phases = LAYER_PHASE.get(activeSet.entityId());
                if (phases != null) {
                    phases.remove(id);
                }
            }
        }
    }
}
