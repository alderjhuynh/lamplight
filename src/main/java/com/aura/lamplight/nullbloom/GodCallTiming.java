package com.aura.lamplight.nullbloom;

public final class GodCallTiming {
	private GodCallTiming() {
	}

	public static final int[] TOLL_TICKS = {30, 144, 258, 371, 484};

	public static final int TIMELAPSE_START_TICKS = 484;
	public static final int TIMELAPSE_TICKS = 128;

	public static final int FLASH_TICKS = TIMELAPSE_START_TICKS + TIMELAPSE_TICKS;

	public static final int DIALOGUE_DELAY_TICKS = 24;
	public static final int FULL_PREROLL_TICKS = FLASH_TICKS + DIALOGUE_DELAY_TICKS;

	public static final int BRIEF_PREROLL_TICKS = 36;

	public static final int OUTRO_TICKS = 70;

	public static final int CALL_COOLDOWN_TICKS = 20 * 60;
}
