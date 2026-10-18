package com.aura.lamplight.puppeteer.client;

import com.aura.lamplight.puppeteer.Puppeteer;
import com.aura.lamplight.puppeteer.network.AnimationStatePayload;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * Client-side mirror of what {@code PlayerAnimationTracker} knows
 * server-side, keyed by entity id rather than {@code UUID} since that's
 * what's cheaply available while rendering (see the
 * entity-render-state mixin, which is where this actually gets read).
 */
public final class AnimationClientState {
    private AnimationClientState() {}

    public record Active(Identifier animationId, long startGameTime) {}

    public static final RenderStateDataKey<Active> ANIMATION_RENDER_DATA = RenderStateDataKey.create(
        () -> Puppeteer.id("active_animation").toString()
    );

    private static final Map<Integer, Active> BY_ENTITY_ID = new ConcurrentHashMap<>();

    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(AnimationStatePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.animationId().isPresent()) {
                    BY_ENTITY_ID.put(payload.entityId(), new Active(payload.animationId().get(), payload.startGameTime()));
                } else {
                    BY_ENTITY_ID.remove(payload.entityId());
                }
            });
        });
    }

    public static @Nullable Active get(final int entityId) {
        return BY_ENTITY_ID.get(entityId);
    }
}
