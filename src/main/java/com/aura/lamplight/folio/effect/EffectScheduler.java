package com.aura.lamplight.folio.effect;

import java.util.ArrayDeque;
import java.util.Deque;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

final class EffectScheduler {
    private EffectScheduler() {}

    private static final Deque<Runnable> QUEUE = new ArrayDeque<>();

    static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> run());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> QUEUE.clear());
    }

    static void schedule(final Runnable task) {
        QUEUE.add(task);
    }

    private static void run() {
        for (int remaining = QUEUE.size(); remaining > 0 && !QUEUE.isEmpty(); remaining--) {
            QUEUE.poll().run();
        }
    }
}
