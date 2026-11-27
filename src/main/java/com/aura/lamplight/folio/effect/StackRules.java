package com.aura.lamplight.folio.effect;

public final class StackRules {
    private StackRules() {}

    public static final int MAX_STACKS = 10;
    public static final int BASE_DURATION_TICKS = 20 * 20;
    public static final int EXTENSION_TICKS = 10 * 20;
    public static final int NEAR_FULL_TICKS = BASE_DURATION_TICKS - 5 * 20;
    public static final int INFINITE = -1;

    public record Result(int stacks, int duration) {
        public boolean atMax() {
            return stacks >= MAX_STACKS;
        }
    }

    public static Result apply(final int currentStacks, final int currentDuration, final int added) {
        if (added < 1) {
            throw new IllegalArgumentException("added must be at least 1, was " + added);
        }
        int current = Math.max(0, Math.min(MAX_STACKS, currentStacks));
        int stacks = Math.min(MAX_STACKS, current + Math.min(added, MAX_STACKS));

        if (current == 0) {
            return new Result(stacks, BASE_DURATION_TICKS);
        }
        if (currentDuration == INFINITE) {
            return new Result(stacks, INFINITE);
        }
        if (current == MAX_STACKS && currentDuration >= NEAR_FULL_TICKS) {
            long extended = (long) currentDuration + EXTENSION_TICKS;
            return new Result(stacks, (int) Math.min(Integer.MAX_VALUE, extended));
        }
        return new Result(stacks, Math.max(currentDuration, BASE_DURATION_TICKS));
    }

    public static int shockChainTargets(final int stacks) {
        return (Math.max(0, Math.min(MAX_STACKS, stacks)) + 1) / 2;
    }

    public static int stacksFromAmplifier(final int amplifier) {
        return Math.max(1, Math.min(MAX_STACKS, amplifier + 1));
    }
}
