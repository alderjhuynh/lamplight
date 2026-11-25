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
    vec3 col = texture(InSampler, texCoord).rgb;

    // the edges of the world close in
    vec2 c = texCoord - 0.5;
    c.x *= OutSize.x / max(OutSize.y, 1.0);
    col *= 1.0 - Finish.z * smoothstep(0.25, 0.95, length(c));

    // film grain, restless in the midtones
    float lum = dot(col, vec3(0.299, 0.587, 0.114));
    float g = hash21(gl_FragCoord.xy + Center.w * 17.0);
    col += (g - 0.5) * Finish.x * 0.22 * (0.35 + 0.65 * (1.0 - abs(lum * 2.0 - 1.0)));

    // the white flash on the way in, the black blink on the way out
    col = mix(col, vec3(1.0, 0.985, 0.95), Finish.y);
    col *= 1.0 - Finish.w;

    // a hair of dither so the dark gradients don't band
    col += (hash21(gl_FragCoord.xy + 91.7) - 0.5) / 255.0;

    fragColor = vec4(col, 1.0);
}
