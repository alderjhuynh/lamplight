package com.aura.lamplight.folio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aura.lamplight.folio.effect.StackRules;
import org.junit.jupiter.api.Test;

public final class StackRulesTest {

    @Test
    void firstApplicationLastsTwentySeconds() {
        assertEquals(new StackRules.Result(1, 400), StackRules.apply(0, 0, 1));
        assertEquals(new StackRules.Result(2, 400), StackRules.apply(0, 0, 2));
    }

    @Test
    void reapplyingAddsStacksAndRefreshes() {
        assertEquals(new StackRules.Result(4, 400), StackRules.apply(3, 12, 1));
    }

    @Test
    void stacksCapAtTen() {
        assertEquals(10, StackRules.apply(9, 400, 2).stacks());
        assertEquals(10, StackRules.apply(0, 0, 50).stacks());
        assertTrue(StackRules.apply(9, 400, 1).atMax());
    }

    @Test
    void reachingTenDoesNotExtendYet() {
        assertEquals(new StackRules.Result(10, 400), StackRules.apply(9, 400, 1));
    }

    @Test
    void atMaxAndNearFullExtendsByTenSeconds() {
        assertEquals(new StackRules.Result(10, 600), StackRules.apply(10, 400, 1));
        assertEquals(new StackRules.Result(10, 500), StackRules.apply(10, StackRules.NEAR_FULL_TICKS, 1));
         assertEquals(new StackRules.Result(10, 600), StackRules.apply(10, 400, 2));
        assertEquals(new StackRules.Result(10, 800), StackRules.apply(10, 600, 1));
    }

    @Test
    void atMaxButNotNearFullOnlyRefreshes() {
        assertEquals(new StackRules.Result(10, 400), StackRules.apply(10, StackRules.NEAR_FULL_TICKS - 1, 1));
    }

    @Test
    void neverShortensAnEffect() {
        assertEquals(new StackRules.Result(5, 900), StackRules.apply(4, 900, 1));
        assertEquals(StackRules.INFINITE, StackRules.apply(4, StackRules.INFINITE, 1).duration());
        assertEquals(StackRules.INFINITE, StackRules.apply(10, StackRules.INFINITE, 1).duration());
    }

    @Test
    void extensionCannotOverflow() {
        assertEquals(Integer.MAX_VALUE, StackRules.apply(10, Integer.MAX_VALUE - 5, 1).duration());
    }

    @Test
    void rejectsNonPositiveApplications() {
        assertThrows(IllegalArgumentException.class, () -> StackRules.apply(3, 100, 0));
    }

    @Test
    void shockReachesFiveTargetsAtTenStacks() {
        int[] expected = {0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5};
        for (int stacks = 0; stacks <= 10; stacks++) {
            assertEquals(expected[stacks], StackRules.shockChainTargets(stacks), "stacks " + stacks);
        }
    }

    @Test
    void commandAmplifiersAreClamped() {
        assertEquals(1, StackRules.stacksFromAmplifier(0));
        assertEquals(10, StackRules.stacksFromAmplifier(255));
    }
}
