package com.aura.lamplight.nullbloom.dialogue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import org.jspecify.annotations.Nullable;

public final class GodContext {
	private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\w+)}");
	private static final int TICKS_PER_DAY = 24000;

	public static final java.util.function.Predicate<ServerPlayer> DEATHLESS = player -> deaths(player) == 0;
	public static final java.util.function.Predicate<ServerPlayer> DIED_ONCE = player -> deaths(player) == 1;
	public static final java.util.function.Predicate<ServerPlayer> DIED_MANY = player -> deaths(player) > 1;
	public static final java.util.function.Predicate<ServerPlayer> HAS_DIED = player -> deaths(player) > 0;

	private GodContext() {
	}

	public static int deaths(ServerPlayer player) {
		return player.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS));
	}

	public static List<GodLine> resolve(ServerPlayer player, List<GodScript.ScriptLine> lines) {
		List<GodLine> out = new ArrayList<>();
		for (GodScript.ScriptLine line : lines) {
			if (line.condition() != null && !line.condition().test(player)) {
				continue;
			}
			String text = fill(player, line.text());
			if (text == null && line.fallback() != null) {
				text = fill(player, line.fallback());
			}
			if (text != null && !text.isBlank()) {
				out.add(new GodLine(text, line.speed(), line.pauseMs()));
			}
		}
		return out;
	}

	private static @Nullable String fill(ServerPlayer player, String template) {
		Matcher matcher = PLACEHOLDER.matcher(template);
		StringBuilder result = new StringBuilder();
		while (matcher.find()) {
			String value = lookup(player, matcher.group(1));
			if (value == null) {
				return null;
			}
			matcher.appendReplacement(result, Matcher.quoteReplacement(value));
		}
		matcher.appendTail(result);
		return result.toString();
	}

	private static @Nullable String lookup(ServerPlayer player, String key) {
		return switch (key) {
			case "deaths" -> Integer.toString(deaths(player));
			case "days" -> {
				int days = player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)) / TICKS_PER_DAY;
				yield days >= 1 ? Integer.toString(days) : null;
			}
			default -> null;
		};
	}
}
