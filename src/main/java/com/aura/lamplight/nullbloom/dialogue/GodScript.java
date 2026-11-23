package com.aura.lamplight.nullbloom.dialogue;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

public record GodScript(List<ScriptLine> intro, boolean askChoice, List<ScriptLine> onYes, List<ScriptLine> onNo) {

	public static GodScript monologue(List<ScriptLine> lines) {
		return new GodScript(lines, false, List.of(), List.of());
	}

	public static GodScript deal(List<ScriptLine> intro, List<ScriptLine> onYes, List<ScriptLine> onNo) {
		return new GodScript(intro, true, onYes, onNo);
	}

	public record ScriptLine(
		String text, @Nullable String fallback, float speed, int pauseMs, @Nullable Predicate<ServerPlayer> condition
	) {
		public static ScriptLine of(String text) {
			return new ScriptLine(text, null, 1.0F, 900, null);
		}

		public ScriptLine orElse(String fallbackText) {
			return new ScriptLine(text, fallbackText, speed, pauseMs, condition);
		}

		public ScriptLine speed(float newSpeed) {
			return new ScriptLine(text, fallback, newSpeed, pauseMs, condition);
		}

		public ScriptLine pause(int ms) {
			return new ScriptLine(text, fallback, speed, Math.max(500, ms), condition);
		}

		public ScriptLine when(Predicate<ServerPlayer> newCondition) {
			return new ScriptLine(text, fallback, speed, pauseMs, newCondition);
		}
	}
}
