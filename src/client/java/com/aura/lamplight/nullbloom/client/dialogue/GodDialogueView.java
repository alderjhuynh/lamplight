package com.aura.lamplight.nullbloom.client.dialogue;

import com.aura.lamplight.nullbloom.dialogue.GodDialoguePayload;
import com.aura.lamplight.nullbloom.dialogue.GodDialogueResponsePayload;
import com.aura.lamplight.nullbloom.dialogue.GodLine;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import org.joml.Matrix3x2fStack;

public final class GodDialogueView implements HudElement {
	private static final Identifier FLOWER = Identifier.fromNamespaceAndPath("lamplight", "textures/block/nullbloom.png");

	private static final long MIN_HOLD_MS = 320;
	private static final long CHOICE_CONFIRM_DELAY_MS = 450;
	private static final long OPEN_FADE_MS = 1400;
	private static final long CLOSE_FADE_MS = 900;
	private static final long WAIT_TIMEOUT_MS = 10_000;
	private static final long HINT_AFTER_MS = 3_000;
	private static final float TAU = (float) (Math.PI * 2.0);

	private enum Phase {
		IDLE,
		LINE,
		CHOICE,
		WAITING,
		CLOSING
	}

	private record Fading(GlyphLine line, long startMs, long dissolveMs, float centerX, float top, float scale) {
	}

	private final ArrayDeque<GodLine> queue = new ArrayDeque<>();
	private final List<Fading> fading = new ArrayList<>();

	private Phase phase = Phase.IDLE;
	private int sessionId;
	private boolean expectChoice;
	private long openedAt;
	private long closingAt;
	private long lineCounter;

	private GodLine currentSource;
	private GlyphLine current;
	private float builtWidth;
	private long lineStart;

	private GlyphLine yes;
	private GlyphLine no;
	private int selected = -1;
	private long choiceStart;
	private long waitStart;

	public boolean isActive() {
		return phase != Phase.IDLE;
	}

	public boolean isAwaitingInput() {
		return phase == Phase.LINE || phase == Phase.CHOICE;
	}

	public void begin(GodDialoguePayload payload) {
		long now = Util.getMillis();
		if (phase == Phase.IDLE || phase == Phase.CLOSING || sessionId != payload.sessionId()) {
			reset();
			sessionId = payload.sessionId();
			openedAt = now;
		}
		queue.addAll(payload.lines());
		expectChoice = payload.expectChoice();
		if (phase == Phase.WAITING) {
			current = null;
			currentSource = null;
		}
		if (current == null && phase != Phase.CHOICE) {
			startNextLine(now);
		}
	}

	public void cancel(int id) {
		if (phase == Phase.IDLE || id != sessionId) {
			return;
		}
		long now = Util.getMillis();
		queue.clear();
		fadeEverything(now);
		close(now);
	}

	public void reset() {
		queue.clear();
		fading.clear();
		current = null;
		currentSource = null;
		yes = null;
		no = null;
		selected = -1;
		phase = Phase.IDLE;
	}

	public void advance() {
		long now = Util.getMillis();
		switch (phase) {
			case LINE -> {
				if (current == null || now < lineStart) {
					return;
				}
				long age = now - lineStart;
				if (!current.isRevealedAt(age)) {
					lineStart = now - current.totalMs - 400;
					return;
				}
				if (age - current.totalMs < MIN_HOLD_MS) {
					return;
				}
				if (!queue.isEmpty()) {
					fadeCurrent(now);
					startNextLine(now);
				} else if (expectChoice) {
					enterChoice(now);
				} else {
					fadeCurrent(now);
					send(GodDialogueResponsePayload.DONE);
					close(now);
				}
			}
			case CHOICE -> {
				if (selected >= 0 && now - choiceStart >= CHOICE_CONFIRM_DELAY_MS) {
					confirm(now);
				}
			}
			default -> {
			}
		}
	}

	public void choose(boolean yesPressed) {
		if (phase != Phase.CHOICE) {
			return;
		}
		int target = yesPressed ? 0 : 1;
		long now = Util.getMillis();
		if (selected == target) {
			if (now - choiceStart >= CHOICE_CONFIRM_DELAY_MS) {
				confirm(now);
			}
		} else {
			selected = target;
		}
	}

	private void confirm(long now) {
		send(selected == 0 ? GodDialogueResponsePayload.YES : GodDialogueResponsePayload.NO);
		fadeEverything(now);
		selected = -1;
		phase = Phase.WAITING;
		waitStart = now;
	}

	private void send(int kind) {
		ClientPlayNetworking.send(new GodDialogueResponsePayload(sessionId, kind));
	}

	private void startNextLine(long now) {
		currentSource = queue.poll();
		if (currentSource == null) {
			return;
		}
		current = null;
		builtWidth = -1;
		lineStart = now + currentSource.pauseBeforeMs();
		lineCounter++;
		phase = Phase.LINE;
	}

	private void enterChoice(long now) {
		phase = Phase.CHOICE;
		choiceStart = now;
		selected = -1;
		Font font = Minecraft.getInstance().font;
		yes = GlyphLine.build(font, "yes", 1.6F, 200, true, sessionId * 7919L + 1);
		no = GlyphLine.build(font, "no", 1.6F, 200, true, sessionId * 7919L + 2);
	}

	private void fadeCurrent(long now) {
		if (current != null) {
			Minecraft mc = Minecraft.getInstance();
			float scale = textScale(mc.getWindow().getGuiScaledHeight());
			fading.add(new Fading(current, lineStart, now, mc.getWindow().getGuiScaledWidth() / 2.0F,
				blockTop(mc.getWindow().getGuiScaledHeight(), current.rows, scale), scale));
		}
		current = null;
		currentSource = null;
	}

	private void fadeEverything(long now) {
		Minecraft mc = Minecraft.getInstance();
		int h = mc.getWindow().getGuiScaledHeight();
		float w = mc.getWindow().getGuiScaledWidth();
		float scale = textScale(h);
		if (current != null) {
			fadeCurrent(now);
		}
		if (yes != null && no != null) {
			float top = optionTop(h, scale);
			fading.add(new Fading(yes, choiceStart + 350, now, w / 2.0F - w * 0.13F, top, scale));
			fading.add(new Fading(no, choiceStart + 350, now, w / 2.0F + w * 0.13F, top, scale));
		}
		yes = null;
		no = null;
	}

	private void close(long now) {
		phase = Phase.CLOSING;
		closingAt = now;
		current = null;
		currentSource = null;
	}
	private static float textScale(int guiHeight) {
		return Math.max(1.0F, Math.round(guiHeight / 270.0F));
	}

	private static float blockTop(int guiHeight, int rows, float scale) {
		return guiHeight * 0.40F - rows * 11.0F * scale / 2.0F;
	}

	private float optionTop(int guiHeight, float scale) {
		int rows = current != null ? current.rows : 2;
		return blockTop(guiHeight, rows, scale) + rows * 11.0F * scale + 26.0F * scale;
	}

	private void ensureBuilt(Font font, int guiWidth, float scale) {
		if (currentSource == null) {
			return;
		}
		float maxWidth = guiWidth * 0.70F / scale;
		if (current == null || Math.abs(maxWidth - builtWidth) > 0.5F) {
			current = GlyphLine.build(font, currentSource.text(), currentSource.speed(), maxWidth, true,
				sessionId * 104729L + lineCounter);
			builtWidth = maxWidth;
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, DeltaTracker deltaTracker) {
		if (phase == Phase.IDLE) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null) {
			reset();
			return;
		}
		long now = Util.getMillis();
		Font font = mc.font;
		int w = g.guiWidth();
		int h = g.guiHeight();
		float scale = textScale(h);

		if (phase == Phase.WAITING && now - waitStart > WAIT_TIMEOUT_MS) {
			close(now);
		}
		if (phase == Phase.CLOSING && now - closingAt > CLOSE_FADE_MS + 200) {
			reset();
			return;
		}

		float open = Motion.clamp01((now - openedAt) / (float) OPEN_FADE_MS);
		float closing = phase == Phase.CLOSING ? Motion.clamp01((now - closingAt) / (float) CLOSE_FADE_MS) : 0.0F;
		float overlay = open * (1.0F - closing);
		g.fill(0, 0, w, h, ARGB.colorFromFloat(0.38F * overlay, 0.0F, 0.0F, 0.0F));

		ensureBuilt(font, w, scale);

		fading.removeIf(f -> now - f.dissolveMs() > Motion.DISSOLVE_MS + 100);
		for (Fading f : fading) {
			drawLine(g, font, f.line(), now, f.startMs(), f.centerX(), f.top(), f.scale(), 1.0F, f.dissolveMs(), 1.0F, 0.0F);
		}

		if (current != null && phase != Phase.CLOSING) {
			float top = blockTop(h, current.rows, scale);
			drawLine(g, font, current, now, lineStart, w / 2.0F, top, scale, 1.0F, -1, 1.0F, 0.0F);
			long done = lineStart + current.totalMs;
			if (phase == Phase.LINE && now >= done) {
				drawContinueMark(g, now, done, w / 2.0F, top + current.rows * 11.0F * scale + 14.0F * scale, scale);
				if (now - done > HINT_AFTER_MS) {
					drawHint(g, font, "[" + keyName(GodDialogueClient.ADVANCE) + "] to continue", w,
						top + current.rows * 11.0F * scale + 30.0F * scale, 0.5F);
				}
			}
		}

		if (phase == Phase.CHOICE && yes != null && no != null) {
			float top = optionTop(h, scale);
			float yesX = w / 2.0F - w * 0.13F;
			float noX = w / 2.0F + w * 0.13F;
			long start = choiceStart + 350;
			drawLine(g, font, yes, now, start, yesX, top, scale, selected == 0 ? 1.0F : 0.42F, -1,
				1.0F, selected == 0 ? 0.35F : 0.0F);
			drawLine(g, font, no, now, start, noX, top, scale, selected == 1 ? 1.0F : 0.42F, -1,
				1.0F, selected == 1 ? 0.35F : 0.0F);
			if (selected >= 0 && now > start) {
				GlyphLine target = selected == 0 ? yes : no;
				float markX = (selected == 0 ? yesX : noX) - target.rowWidth[0] / 2.0F * scale - 10.0F * scale;
				drawFlower(g, now, markX, top + 4.5F * scale, scale, 0.85F);
			}
			String hint = selected < 0
				? "[" + keyName(GodDialogueClient.YES) + "] yes      [" + keyName(GodDialogueClient.NO) + "] no"
				: "[" + keyName(GodDialogueClient.ADVANCE) + "] to confirm";
			drawHint(g, font, hint, w, top + 9.0F * scale + 22.0F * scale, 0.7F);
		}
	}

	private void drawLine(GuiGraphicsExtractor g, Font font, GlyphLine line, long now, long start, float centerX, float top,
		float scale, float alphaMul, long dissolveAt, float boost, float tremble) {
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(centerX, top);
		pose.scale(scale, scale);
		float t = now / 1000.0F;

		for (int i = 0; i < line.length; i++) {
			char c = line.chars[i];
			if (c == ' ') {
				continue;
			}
			long age = now - start - line.revealAtMs[i];
			if (age < 0) {
				continue;
			}
			float pop = Motion.clamp01(age / (float) Motion.POP_MS);
			float alpha = Motion.clamp01(age / Motion.POP_FADE_MS) * alphaMul;
			float glitch = (1.0F - pop) * (1.0F - pop);
			float split = Motion.ABERRATION_BASE * boost + Motion.ABERRATION_POP * glitch;
			float rise = 0.0F;
			if (dissolveAt >= 0) {
				float dp = Motion.clamp01((now - dissolveAt) / Motion.DISSOLVE_MS);
				float fade = 1.0F - dp;
				alpha *= fade;
				rise = dp * Motion.DISSOLVE_RISE;
				split *= fade;
			}
			if (alpha < 0.02F) {
				continue;
			}

			float a = line.phaseA[i];
			float b = line.phaseB[i];
			float d = line.phaseC[i];
			float floatY = (float) (Math.sin(t * (1.1F + a * 0.9F) + b * TAU) * Motion.FLOAT_Y
				+ Math.sin(t * (0.35F + d * 0.4F) + a * 9.0F) * Motion.DRIFT_Y);
			float floatX = (float) (Math.sin(t * (0.7F + b * 0.6F) + d * TAU) * Motion.FLOAT_X);
			if (tremble > 0.0F) {
				floatX += (float) Math.sin(t * 43.0F + a * 20.0F) * tremble;
				floatY += (float) Math.cos(t * 37.0F + b * 20.0F) * tremble;
			}

			float gw = line.glyphWidth[i];
			float gx = line.x[i] - line.rowWidth[line.row[i]] / 2.0F + floatX;
			float gy = line.row[i] * 11.0F + floatY - rise + (1.0F - pop) * (1.0F - pop) * Motion.POP_START_DROP;
			float s = pop < 1.0F ? Motion.POP_START_SCALE + (1.0F - Motion.POP_START_SCALE) * Motion.easeOutBack(pop) : 1.0F;

			String glyph = String.valueOf(c);
			pose.pushMatrix();
			pose.translate(gx + gw / 2.0F, gy + 4.5F);
			pose.scale(s, s);
			pose.translate(-gw / 2.0F, -4.5F);
			glyphLayer(g, font, glyph, -split, 0.0F, ARGB.colorFromFloat(alpha * Motion.ABERRATION_ALPHA,
				Motion.RED[0], Motion.RED[1], Motion.RED[2]));
			glyphLayer(g, font, glyph, split, split * 0.12F, ARGB.colorFromFloat(alpha * Motion.ABERRATION_ALPHA,
				Motion.BLUE[0], Motion.BLUE[1], Motion.BLUE[2]));
			glyphLayer(g, font, glyph, 0.0F, 0.0F, ARGB.colorFromFloat(alpha,
				Motion.TEXT[0], Motion.TEXT[1], Motion.TEXT[2]));
			pose.popMatrix();
		}
		pose.popMatrix();
	}

	private static void glyphLayer(GuiGraphicsExtractor g, Font font, String glyph, float dx, float dy, int color) {
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(dx, dy);
		g.text(font, glyph, 0, 0, color, false);
		pose.popMatrix();
	}

	private void drawContinueMark(GuiGraphicsExtractor g, long now, long doneAt, float cx, float cy, float scale) {
		float fadeIn = Motion.clamp01((now - doneAt) / 500.0F);
		float pulse = 0.35F + 0.25F * (float) Math.sin(now / 520.0);
		drawFlower(g, now, cx, cy, scale * 0.75F, fadeIn * pulse);
	}

	private void drawFlower(GuiGraphicsExtractor g, long now, float cx, float cy, float scale, float alpha) {
		if (alpha < 0.02F) {
			return;
		}
		float bob = (float) Math.sin(now / 700.0) * 1.5F * scale;
		Matrix3x2fStack pose = g.pose();
		pose.pushMatrix();
		pose.translate(cx, cy + bob);
		pose.scale(scale * 0.5F, scale * 0.5F);
		g.blit(RenderPipelines.GUI_TEXTURED, FLOWER, -8, -8, 0.0F, 0.0F, 16, 16, 16, 16,
			ARGB.colorFromFloat(alpha, 1.0F, 1.0F, 1.0F));
		pose.popMatrix();
	}

	private void drawHint(GuiGraphicsExtractor g, Font font, String text, int guiWidth, float y, float alpha) {
		int width = font.width(text);
		g.text(font, text, (guiWidth - width) / 2, Math.round(y), ARGB.colorFromFloat(alpha * 0.6F, 0.8F, 0.8F, 0.85F), false);
	}

	private static String keyName(KeyMapping key) {
		return key.getTranslatedKeyMessage().getString();
	}
}
