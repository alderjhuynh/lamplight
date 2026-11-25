package com.aura.lamplight.nullbloom.dialogue;

import com.aura.lamplight.nullbloom.dialogue.GodScript.ScriptLine;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

public final class GodScripts {
	private GodScripts() {
	}

	public static final GodScript FIRST_MEETING = GodScript.deal(
		List.of(
			ScriptLine.of("Hm. You're early.").pause(2200).speed(0.8F),
			ScriptLine.of("...Don't. Don't speak. Every one of you does the same thing, and it's tiresome.").pause(1400),

			ScriptLine.of("Died... once. How... ordinary. Disappointing, but, amusing.")
				.when(GodContext.DIED_ONCE).pause(1100),
			ScriptLine.of("Died... {deaths} times. How... ordinary. Disappointing, but, amusing.")
				.when(GodContext.DIED_MANY).pause(1100),
			ScriptLine.of("Died... {deaths} times. Interesting....")
				.when(GodContext.DEATHLESS).speed(0.7F).pause(1500),

			ScriptLine.of("I have watched you for {days} days. You are utterly boring. Nothing more than drivel.")
				.orElse("I have watched you since your first breath. You are utterly boring. Nothing more than drivel.")
				.pause(1300),
			ScriptLine.of("...Though you have been careful. I noticed.")
				.when(GodContext.DEATHLESS).speed(0.75F).pause(1500),
			ScriptLine.of("Something is coming. Someone must stand in its way, and I do not do errands.")
				.pause(1500),
			ScriptLine.of("So. A bargain. Go home, and be warm, and be small, and forget this. Or take what I offer, and I will take a heart from you in exchange.")
				.pause(1500),

			ScriptLine.of("I will give it back, if you live. You won't, but I'll give it back if you do.")
				.when(GodContext.HAS_DIED).speed(0.85F).pause(1300),
			ScriptLine.of("I will give it back, if you live. You... might, actually. Hm.")
				.when(GodContext.DEATHLESS).speed(0.7F).pause(1400),

			ScriptLine.of("Well?").speed(0.5F).pause(1800)
		),
		List.of(
			ScriptLine.of("Good. Try to be useful.").when(GodContext.HAS_DIED).pause(900).speed(0.9F),
			ScriptLine.of("Good. ...Try not to die. It would be a waste.").when(GodContext.DEATHLESS).pause(900).speed(0.8F)
		),
		List.of(
			ScriptLine.of("Pity. Another will come. They always do.").when(GodContext.HAS_DIED).pause(900).speed(0.9F),
			ScriptLine.of("Pity. Another will come. They always do... but I would have liked it to be you.")
				.when(GodContext.DEATHLESS).pause(900).speed(0.8F)
		)
	);

	// be honest do i have to change this float names
	private static final float MIDDLE_BEAT_CHANCE_VISITS_2_TO_4 = 0.35F;
	private static final float MIDDLE_BEAT_CHANCE_VISITS_5_PLUS = 0.60F;

	private static final List<String> MIDDLE_BEATS = List.of(
		"You are standing there. Why are you standing there.",
		"Whatever you're hoping I'll say, I won't.",
		"There is nothing here for you. Go and... do whatever it is you do. Cook something."
	);

	private static final List<String> FIFTH_VISIT_GREETINGS = List.of(
		"I have stopped pretending to be surprised.",
		"Of course. Of course you're here.",
		"Again. Naturally."
	);

	public static GodScript returnVisit(ServerPlayer player, int returnVisit, boolean diedSinceLastVisit, boolean accepted) {
		RandomSource random = player.getRandom();
		if (returnVisit <= 1) {
			return GodScript.monologue(firstReturn(random, diedSinceLastVisit, accepted));
		}
		boolean fifthPlus = returnVisit >= 5;
		float middleChance = fifthPlus ? MIDDLE_BEAT_CHANCE_VISITS_5_PLUS : MIDDLE_BEAT_CHANCE_VISITS_2_TO_4;
		ScriptLine greeting = fifthPlus
			? ScriptLine.of(FIFTH_VISIT_GREETINGS.get(random.nextInt(FIFTH_VISIT_GREETINGS.size()))).speed(0.9F).pause(1700)
			: ScriptLine.of("You again. Did I not say what I needed from you?").speed(0.9F).pause(1700);

		if (accepted) {
			List<ScriptLine> lines = new ArrayList<>();
			lines.add(greeting);
			lines.add(ScriptLine.of("Is the heart not enough?").speed(0.8F).pause(1600));
			maybeMiddleBeat(random, middleChance, lines);
			lines.add(dismissal(random, true));
			return GodScript.monologue(lines);
		}

		List<ScriptLine> intro = List.of(
			greeting,
			ScriptLine.of("Changed your mind then?").speed(0.7F).pause(1800)
		);
		List<ScriptLine> onYes = new ArrayList<>(List.of(
			ScriptLine.of("...Hm. So you learned something. Fine. Hold still.")
				.when(GodContext.HAS_DIED).speed(0.85F).pause(1200),
			ScriptLine.of("...Hm. Careful, and apparently curious. Fine. Hold still.")
				.when(GodContext.DEATHLESS).speed(0.85F).pause(1200)
		));
		onYes.add(dismissal(random, true));

		List<ScriptLine> onNo = new ArrayList<>();
		onNo.add(ScriptLine.of("Then what is the point of you coming back?").speed(0.85F).pause(1300));
		maybeMiddleBeat(random, middleChance, onNo);
		onNo.add(dismissal(random, false));
		return GodScript.deal(intro, onYes, onNo);
	}

	private static List<ScriptLine> firstReturn(RandomSource random, boolean diedSinceLastVisit, boolean accepted) {
		List<ScriptLine> lines = new ArrayList<>();
		if (diedSinceLastVisit) {
			lines.add(ScriptLine.of("Back so soon? ...Ah. Died again. {deaths} now. How... consistent.").speed(0.9F).pause(1700));
		} else {
			lines.add(ScriptLine.of("Back so soon? ...Still 0 deaths. Hm.")
				.when(GodContext.DEATHLESS).speed(0.85F).pause(1700));
			lines.add(ScriptLine.of("Back so soon?")
				.when(GodContext.HAS_DIED).speed(0.9F).pause(1700));
		}
		lines.add(dismissal(random, accepted));
		return lines;
	}

	private static void maybeMiddleBeat(RandomSource random, float chance, List<ScriptLine> into) {
		if (random.nextFloat() < chance) {
			into.add(ScriptLine.of(MIDDLE_BEATS.get(random.nextInt(MIDDLE_BEATS.size()))).speed(0.9F).pause(1300));
		}
	}

	private static ScriptLine dismissal(RandomSource random, boolean heartTaken) {
		List<ScriptLine> pool = new ArrayList<>(List.of(
			ScriptLine.of("Out.").speed(1.1F).pause(1100),
			ScriptLine.of("That's enough of you.").speed(1.0F).pause(1100),
			ScriptLine.of("Go home.").speed(1.0F).pause(1100)
		));
		if (heartTaken) {
			pool.add(ScriptLine.of("Go before you manage to lose another heart.").speed(0.9F).pause(1200));
		}
		return pool.get(random.nextInt(pool.size()));
	}

	public static GodScript contact(int contact) {
		ScriptLine line;
		if (contact <= 1) {
			line = ScriptLine.of("Oh. It's you.").speed(0.8F).pause(2200);
		} else if (contact == 2) {
			line = ScriptLine.of("Again?").speed(0.9F).pause(2000);
		} else if (contact == 3) {
			line = ScriptLine.of("Is this entertainment to you?").speed(0.9F).pause(2000);
		} else if (contact <= 6) {
			line = ScriptLine.of("I do not do errands, and I do not do company.").speed(0.9F).pause(2000);
		} else {
			line = ScriptLine.of("...Fine. What. What do you want.").speed(0.8F).pause(2200);
		}
		return GodScript.monologue(List.of(line));
	}

	public static final GodScript TOO_SOON = GodScript.monologue(List.of(
		ScriptLine.of("Quiet. Quiet. I am thinking.").speed(1.1F).pause(900)
	));

	public static final GodScript NO_DEAL = GodScript.monologue(List.of(
		ScriptLine.of("There is no deal between us. You have no right to call.").speed(0.9F).pause(1200)
	));
}
