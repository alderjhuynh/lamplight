package com.aura.lamplight.nullbloom.client.dialogue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.gui.Font;

final class GlyphLine {
	private static final float BASE_CHARS_PER_SECOND = 24.0F;

	final String text;
	final int length;
	final char[] chars;
	final int[] row;
	final float[] x;
	final float[] glyphWidth;
	final float[] rowWidth;
	final int rows;
	final long[] revealAtMs;
	final float[] phaseA;
	final float[] phaseB;
	final float[] phaseC;
	final long totalMs;

	private GlyphLine(String text, int rows, float[] rowWidth, int[] row, float[] x, float[] glyphWidth, long[] revealAtMs, long totalMs, long seed) {
		this.text = text;
		this.length = text.length();
		this.chars = text.toCharArray();
		this.rows = rows;
		this.rowWidth = rowWidth;
		this.row = row;
		this.x = x;
		this.glyphWidth = glyphWidth;
		this.revealAtMs = revealAtMs;
		this.totalMs = totalMs;
		this.phaseA = new float[length];
		this.phaseB = new float[length];
		this.phaseC = new float[length];
		Random random = new Random(seed);
		for (int i = 0; i < length; i++) {
			phaseA[i] = random.nextFloat();
			phaseB[i] = random.nextFloat();
			phaseC[i] = random.nextFloat();
		}
	}

	static GlyphLine build(Font font, String text, float speed, float maxWidth, boolean typed, long seed) {
		int n = text.length();
		int[] row = new int[n];
		float[] x = new float[n];
		float[] glyphWidth = new float[n];
		List<Float> widths = new ArrayList<>();
		int spaceWidth = font.width(" ");

		int rowIndex = 0;
		float cursor = 0;
		int i = 0;
		String[] words = text.split(" ", -1);
		for (int w = 0; w < words.length; w++) {
			String word = words[w];
			float wordWidth = font.width(word);
			if (cursor > 0 && cursor + wordWidth > maxWidth) {
				widths.add(Math.max(0, cursor - spaceWidth));
				rowIndex++;
				cursor = 0;
			}
			for (int c = 0; c < word.length(); c++) {
				row[i] = rowIndex;
				x[i] = cursor;
				glyphWidth[i] = font.width(String.valueOf(word.charAt(c)));
				cursor += glyphWidth[i];
				i++;
			}
			if (w < words.length - 1) {
				row[i] = rowIndex;
				x[i] = cursor;
				glyphWidth[i] = spaceWidth;
				cursor += spaceWidth;
				i++;
			}
		}
		widths.add(cursor);
		float[] rowWidths = new float[widths.size()];
		for (int r = 0; r < rowWidths.length; r++) {
			rowWidths[r] = widths.get(r);
		}

		long[] reveal = new long[n];
		float perChar = 1000.0F / (BASE_CHARS_PER_SECOND * Math.max(0.1F, speed));
		float t = 0;
		for (int k = 0; k < n; k++) {
			reveal[k] = (long) t;
			t += typed ? perChar + pauseAfter(text, k) * (1.0F / Math.max(0.5F, speed)) : 14.0F;
		}
		long total = (n == 0 ? 0 : reveal[n - 1]) + Motion.POP_MS;
		return new GlyphLine(text, rowWidths.length, rowWidths, row, x, glyphWidth, reveal, total, seed);
	}

	private static float pauseAfter(String text, int index) {
		char c = text.charAt(index);
		boolean last = index == text.length() - 1;
		char next = last ? ' ' : text.charAt(index + 1);
		return switch (c) {
			case '.' -> next == '.' ? 230.0F : (last || next == ' ' ? 420.0F : 0.0F);
			case '\u2026' -> 480.0F;
			case ',' -> 190.0F;
			case ';', ':' -> 240.0F;
			case '?', '!' -> 440.0F;
			case '\u2014' -> 220.0F;
			default -> 0.0F;
		};
	}

	boolean isRevealedAt(long ageMs) {
		return ageMs >= totalMs;
	}
}
