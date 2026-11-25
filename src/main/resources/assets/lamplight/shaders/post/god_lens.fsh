#version 330

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

// Layout must match god_vision.json and GodFxPass.java (std140, 176 bytes). All three passes share it.
layout(std140) uniform GodConfig {
    mat4 InvViewProj; // inverse(projection * viewRotation)
    vec4 CameraPos;   // xyz = camera world position, w = 1 if clip-space z is [0,1], 0 if [-1,1]
    vec4 Center;      // xy = lit circle centre RELATIVE TO THE CAMERA (x,z), z = seconds, w = frame seed (steps 12x a second)
    vec4 Radii;       // x gloomy radius, y dark radius, z brightness at centre, w brightness at gloomy radius
    vec4 Grade;       // x grayscale amount, y contrast, z terrain dimming (fake time of day), w tint strength
    vec4 Lens;        // x chromatic aberration, y lens pinch, z double vision, w tearing
    vec4 Finish;      // x grain, y white flash, z vignette, w blink to black
    vec4 Extra;       // x sky dimming, y brightness floor far from the centre, z speech pulse, w unused
};

float hash11(float p) {
    p = fract(p * 0.1031);
    p *= p + 33.33;
    p *= p + p;
    return fract(p);
}

float hash21(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float smooth01(float t) {
    t = clamp(t, 0.0, 1.0);
    return t * t * (3.0 - 2.0 * t);
}

in vec2 texCoord;

out vec4 fragColor;

void main() {
    float t = Center.z;
    float seed = Center.w;
    vec2 uv = texCoord;
    vec2 c = uv - 0.5;

    // lens breathing: the whole picture swells and sags
    uv = 0.5 + c * (1.0 + Lens.y * dot(c, c) * 4.0);

    // a slow liquid wobble that only shows up with the double vision
    float wob = Lens.z * 0.0016;
    uv += wob * vec2(sin(uv.y * 41.0 + t * 1.7), cos(uv.x * 37.0 + t * 1.3));

    // tearing: rows of the picture slide sideways, now and then a whole band jumps
    float glitch = Lens.w;
    if (glitch > 0.001) {
        float rows = 52.0;
        float row = floor(uv.y * rows);
        float h = hash21(vec2(row, seed));
        float torn = step(1.0 - glitch * 0.55, h);
        uv.x += torn * (hash21(vec2(row * 1.7, seed + 3.1)) - 0.5) * 0.18 * glitch;
        float band = step(0.985 - glitch * 0.10, hash21(vec2(floor(uv.y * 9.0), seed + 7.7)));
        uv.x += band * (hash11(seed + 1.0) - 0.5) * 0.25 * glitch;
    }

    // chromatic aberration, pushed outward from the middle
    vec2 dir = uv - 0.5;
    float len = length(dir);
    vec2 off = (len > 1.0e-5 ? dir / len : vec2(0.0)) * Lens.x * 0.012 * (0.35 + len * 1.6);
    vec3 col = vec3(
        texture(InSampler, uv + off).r,
        texture(InSampler, uv).g,
        texture(InSampler, uv - off).b);

    // double vision: a faint second picture that drifts, as if something is standing just behind you
    vec2 drift = vec2(sin(t * 0.53), cos(t * 0.41)) * 0.010 * Lens.z + vec2(0.006 * Lens.z, 0.0);
    vec3 echo = texture(InSampler, uv + drift).rgb;
    col = mix(col, echo, Lens.z * 0.35);

    fragColor = vec4(col, 1.0);
}
