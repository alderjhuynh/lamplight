package com.aura.lamplight.nullbloom.dialogue;

import com.aura.lamplight.nullbloom.dialogue.GodScript.ScriptLine;
import java.util.List;

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
}
