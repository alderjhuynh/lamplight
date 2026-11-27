package com.aura.lamplight.folio.client;

import com.aura.lamplight.folio.MulticastBarPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MulticastBarClientState {
    private MulticastBarClientState() {}

    private static final Map<UUID, Integer> SECTIONS = new HashMap<>();

    public static void apply(final MulticastBarPayload payload) {
        if (payload.sections() <= 1) {
            SECTIONS.remove(payload.bar());
        } else {
            SECTIONS.put(payload.bar(), payload.sections());
        }
    }

    public static int sections(final UUID bar) {
        return SECTIONS.getOrDefault(bar, 0);
    }

    public static void reset() {
        SECTIONS.clear();
    }
}
