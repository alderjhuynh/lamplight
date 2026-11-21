#version 330

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
    vec2 InDepthSize;
};

// Layout must match impact_frame.json and ImpactFrame.java (std140).
layout(std140) uniform ImpactConfig {
    vec4 DarkColor;        // rgb used, a ignored
    vec4 LightColor;       // rgb used, a ignored
    vec2 Center;           // texCoord space, (0.5, 0.5) is screen centre
    float Progress;        // 0 = just hit, 1 = finished
    float Beat;            // whole beats since the hit (floor(seconds * beatsPerSecond))
    float Seed;            // per-trigger random seed
    float FlickerMask;     // bit n set = beat n is drawn negative (light ink on dark)
    float Threshold;       // split between dark and light, as a fraction of average scene luma
    float Softness;        // half-width of the threshold edge, same units (0 = razor)
    float LocalContrast;   // how hard local detail is pushed past the threshold
    float Halftone;        // dot-screen strength in mid-tones (0 = off)
    float InkWidth;        // contour half-width, screen-heights
    float InkLuma;         // luma step that counts as a contour, as a fraction of average scene luma
    float InkDepth;        // relative depth kink that counts as a contour
    float LineCount;       // speed-line slots around the circle
    float LineInner;       // radius where speed lines begin, screen-heights
    float LineWidth;       // max half-width of a speed line, in slot units (< 0.5)
    float Punch;           // zoom at the strike (0.05 = 5%)
    float Shake;           // random offset at the strike, screen-heights
    float ReleaseStart;    // Progress at which the release wipe begins
    float FrontWidth;      // thickness of the inverted band on the wipe edge
};

in vec2 texCoord;

out vec4 fragColor;

const float TAU = 6.28318530718;
const vec3 LUMA = vec3(0.2126, 0.7152, 0.0722);
const float SMALL_RADIUS = 0.004;   // screen-heights, light blur applied before thresholding
const float LOCAL_RADIUS = 0.016;   // screen-heights, ring used for local average
const float HALFTONE_CELL = 0.010;  // screen-heights
const float MIN_EXPOSURE = 0.03;   // floor so pitch black doesn't amplify noise

// Sampling happens around a punched (zoomed / shaken) base coordinate.
vec2 gBase;
vec2 gToUnits;
float gExposure; // average scene luma; all luma below is divided by it

float lumaAt(vec2 off) {
    return dot(texture(InSampler, gBase + off / gToUnits).rgb, LUMA) / gExposure;
}

// Average luma of the unpunched frame from a fixed 5x5 grid. Every fragment reads
// the same 25 texels, so this is almost free, and it lets one set of thresholds
// work in daylight, at night and in caves.
float averageLuma() {
    float sum = 0.0;
    for (int y = 0; y < 5; y++) {
        for (int x = 0; x < 5; x++) {
            vec2 uv = (vec2(float(x), float(y)) + 0.5) / 5.0;
            sum += dot(texture(InSampler, uv).rgb, LUMA);
        }
    }
    return max(sum / 25.0, MIN_EXPOSURE);
}

float depthAt(vec2 off) {
    return texture(InDepthSampler, gBase + off / gToUnits).r;
}

float easeOutCubic(float x) {
    float i = 1.0 - x;
    return 1.0 - i * i * i;
}

// PCG hash: stable across GPUs, unlike fract(sin()).
uint pcg(uint v) {
    uint s = v * 747796405u + 2891336453u;
    uint w = ((s >> ((s >> 28u) + 4u)) ^ s) * 277803737u;
    return (w >> 22u) ^ w;
}

vec4 hash4(uint a, uint b) {
    uint h = pcg(a ^ pcg(b + 0x9e3779b9u));
    vec4 r;
    r.x = float(h >> 8) / 16777215.0;
    h = pcg(h);
    r.y = float(h >> 8) / 16777215.0;
    h = pcg(h);
    r.z = float(h >> 8) / 16777215.0;
    h = pcg(h);
    r.w = float(h >> 8) / 16777215.0;
    return r;
}

// Tapered wedges radiating from the centre. Each slot gets its own start
// radius, thickness, angular offset and a chance to be empty, re-rolled
// whenever lineBeat changes.
float speedLines(vec2 p, float dist, float px, uint lineBeat, uint seed) {
    float n = LineCount;
    float u = atan(p.y, p.x) / TAU + 0.5;
    float cell = floor(u * n);
    float f = fract(u * n) - 0.5;

    vec4 h = hash4(uint(cell), lineBeat * 7919u + seed);

    float present = step(0.22, h.x);
    float r0 = LineInner * mix(0.75, 1.5, h.y);
    float widthJitter = mix(0.2, 1.0, h.z * h.z);
    float halfW = LineWidth * widthJitter * smoothstep(r0, r0 + 0.5, dist);
    float shift = (h.w - 0.5) * (1.0 - 2.0 * LineWidth);

    // One pixel, expressed in slot units at this radius.
    float aa = (px / max(dist, 1.0e-3)) * n / TAU * 1.2;
    float inside = 1.0 - smoothstep(halfW - aa, halfW + aa, abs(f - shift));
    return present * inside * step(r0, dist);
}

void main() {
    // Work in "screen-height" units so everything stays circular on wide screens.
    gToUnits = vec2(OutSize.x / OutSize.y, 1.0);
    float px = 1.0 / OutSize.y;
    vec2 p = (texCoord - Center) * gToUnits;
    float dist = length(p);

    uint beat = uint(max(Beat, 0.0));
    uint seed = uint(max(Seed, 0.0));

    // 1 at the strike, 0 when the release starts.
    float t = clamp(Progress / max(ReleaseStart, 1.0e-3), 0.0, 1.0);
    float kick = (1.0 - t) * (1.0 - t);

    // Punch: zoom toward Center plus a stepped random shake, both decaying.
    vec2 shake = (hash4(beat, seed + 17u).xy - 0.5) * 2.0 * Shake * kick;
    gExposure = averageLuma();
    gBase = Center + (texCoord - Center) / (1.0 + Punch * kick) - shake / gToUnits;

    vec3 scene = texture(InSampler, gBase).rgb;

    // ---- Tone: local-contrast-boosted luma against a global threshold --------
    // Difference of Gaussians, roughly: a small blur (kills texel noise) against
    // a wide ring average. Contrast is boosted where the small blur departs from
    // its surroundings, so shapes split cleanly instead of speckling.
    float small = dot(scene, LUMA) / gExposure;
    for (int i = 0; i < 4; i++) {
        float a = (float(i) + 0.5) * (TAU / 4.0);
        small += lumaAt(vec2(cos(a), sin(a)) * SMALL_RADIUS);
    }
    small *= 0.2;
    float large = 0.0;
    for (int i = 0; i < 8; i++) {
        float a = float(i) * (TAU / 8.0);
        large += lumaAt(vec2(cos(a), sin(a)) * LOCAL_RADIUS);
    }
    large *= 0.125;
    float key = small + LocalContrast * (small - large);

    // Dot screen on a 45 degree grid, only visible where key sits near the threshold.
    vec2 hp = vec2(p.x + p.y, p.x - p.y) / HALFTONE_CELL;
    float dots = 0.5 + 0.25 * (cos(hp.x * TAU) + cos(hp.y * TAU));
    float th = Threshold + Halftone * (dots - 0.5);
    float tone = smoothstep(th - Softness, th + Softness, key);

    // ---- Ink contours: depth kinks (silhouettes) and strong luma steps -------
    // Contours are thickest at the strike and thin out as the punch decays.
    float w = InkWidth * (1.0 + 1.5 * kick);

    // Luma contours sample wider than depth ones: texel-scale noise is pixel-sized
    // and would otherwise read as hatching all over every surface.
    float wl = w * 2.0;
    float l00 = lumaAt(vec2(-wl, -wl));
    float l11 = lumaAt(vec2( wl,  wl));
    float l10 = lumaAt(vec2( wl, -wl));
    float l01 = lumaAt(vec2(-wl,  wl));
    float lumaStep = length(vec2(l11 - l00, l10 - l01));
    float edgeL = smoothstep(InkLuma * 0.8, InkLuma * 1.2, lumaStep);

    // Reverse-Z depth is linear in screen space across any flat surface, so a
    // second difference is ~0 on planes and spikes at silhouettes and creases.
    float dc = depthAt(vec2(0.0));
    float dl = depthAt(vec2(-w, 0.0));
    float dr = depthAt(vec2( w, 0.0));
    float dd = depthAt(vec2(0.0, -w));
    float du = depthAt(vec2(0.0,  w));
    float dmax = max(max(max(dl, dr), max(dd, du)), dc);
    float kink = max(abs(dl + dr - 2.0 * dc), abs(dd + du - 2.0 * dc));
    float edgeD = dmax > 1.0e-6 ? smoothstep(InkDepth * 0.8, InkDepth * 1.2, kink / dmax) : 0.0;

    tone *= 1.0 - max(edgeL, edgeD);

    // ---- Speed lines (XOR with the image so they read on any background) ----
    // Re-rolled every other beat: animation "on twos".
    float lines = speedLines(p, dist, px, beat / 2u, seed);
    tone = mix(tone, 1.0 - tone, lines);

    // ---- Flicker: negative on beats whose bit is set in FlickerMask ----------
    bool negative = beat < 24u && ((uint(max(FlickerMask, 0.0)) >> beat) & 1u) == 1u;
    if (negative) {
        tone = 1.0 - tone;
    }

    // ---- Release: hard-edged radial wipe back to the real scene --------------
    float releaseT = clamp((Progress - ReleaseStart) / max(1.0 - ReleaseStart, 1.0e-3), 0.0, 1.0);
    float maxDist = length(max(Center, 1.0 - Center) * gToUnits);
    float radius = releaseT > 0.0 ? easeOutCubic(releaseT) * (maxDist + FrontWidth * 2.0) : -1.0;
    float wave = radius - dist; // > FrontWidth: revealed scene, 0..FrontWidth: band, < 0: ink frame

    float band = smoothstep(-px, px, wave) * (1.0 - smoothstep(FrontWidth - px, FrontWidth + px, wave));
    tone = mix(tone, 1.0 - tone, band);

    vec3 ink = mix(DarkColor.rgb, LightColor.rgb, tone);
    float revealed = smoothstep(FrontWidth - px, FrontWidth + px, wave);

    // The revealed scene is sampled unpunched so the wipe never shows a seam.
    vec3 clean = texture(InSampler, texCoord).rgb;
    fragColor = vec4(mix(ink, clean, revealed), 1.0);
}
