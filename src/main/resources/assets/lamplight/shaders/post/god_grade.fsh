#version 330

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
    vec2 InDepthSize;
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

// Light around the player, falling away to (almost) nothing. Same shape as the nullbloom's darkness.
float lightAt(float r) {
    float gloomy = Radii.x;
    float dark = Radii.y;
    float b;
    if (r < gloomy) {
        b = mix(Radii.z, Radii.w, smooth01(r / gloomy));
    } else {
        float t = (r - gloomy) / max(dark - gloomy, 1.0e-4);
        b = Radii.w * pow(1.0 - smooth01(t), 2.0);
    }
    return max(b, Extra.y);
}

void main() {
    vec4 scene = texture(InSampler, texCoord);
    float depth = texture(InDepthSampler, texCoord).r;
    // Minecraft 26.2 uses reversed-Z: the depth buffer is cleared to 0.0, so untouched (sky) pixels read ~0.
    bool sky = depth <= 1.0e-6;

    // ---- drain the colour: a cold grey in the shadows, a faint candle-warm in the highlights ----
    vec3 col = scene.rgb;
    float lum = dot(col, vec3(0.2126, 0.7152, 0.0722));
    float lc = clamp((lum - 0.5) * Grade.y + 0.5, 0.0, 1.0);
    vec3 cool = vec3(0.78, 0.88, 1.00);
    vec3 warm = vec3(1.00, 0.95, 0.86);
    vec3 toned = mix(vec3(lc), mix(cool, warm, smoothstep(0.15, 0.85, lc)) * lc, Grade.w);
    col = mix(col, toned, Grade.x);

    if (sky) {
        // The sky is exempt from the darkness: it is the one thing that stays lit.
        col *= Extra.x;
    } else {
        float ndcZ = CameraPos.w > 0.5 ? depth : depth * 2.0 - 1.0;
        vec4 clip = vec4(texCoord * 2.0 - 1.0, ndcZ, 1.0);
        vec4 rel = InvViewProj * clip;
        vec3 relPos = rel.xyz / rel.w;               // relative to the camera
        float r = length(relPos.xz - Center.xy);     // distance from the lit circle's centre
        col *= lightAt(r) * Grade.z;
    }

    fragColor = vec4(col, 1.0);
}
