#version 330

#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

in vec4 vertexColor;
in vec2 faceUv;
in vec3 faceNormal;
in float sphericalVertexDistance;
in float cylindricalVertexDistance;

out vec4 fragColor;

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 345.45));
    p += dot(p, p + 34.345);
    return fract(p.x * p.y);
}

float valueNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

// Hard-edged 16x16 pixel rectangle. The whole face is drawn on this grid, so
// the eyes and rune stay as chunky as a hand-made Minecraft texture.
float pixelRect(vec2 cell, vec2 minCell, vec2 maxCell) {
    vec2 inside = step(minCell, cell) * step(cell, maxCell);
    return inside.x * inside.y;
}

void main() {
    // UV0.x encodes the body part and the cube face ID in its integer part.
    float part = floor(faceUv.x);
    vec2 uv = vec2(fract(faceUv.x), faceUv.y);
    float softness = max(fwidth(uv.x), fwidth(uv.y));

    vec3 tint = vertexColor.rgb;
    float light = 0.90;
    if (faceNormal.y > 0.5) {
        light = 1.04;
    } else if (faceNormal.y < -0.5) {
        light = 0.78;
    } else if (faceNormal.x > 0.5) {
        light = 0.86;
    } else if (faceNormal.x < -0.5) {
        light = 0.84;
    }
    vec3 color = tint * light;

    if (part < 5.5) {
        // Body: a faint vertical wash, soft mottling and a few blocky spots.
        color *= mix(0.97, 1.03, uv.y);

        vec2 cell = floor(uv * 16.0);
        float mottle = valueNoise(cell * 0.10 + vec2(part * 17.0));
        color *= 0.95 + 0.08 * mottle;

        float spotNoise = valueNoise(cell * 0.16 + vec2(part * 13.0 + 4.0));
        float spots = step(0.72, spotNoise);
        vec3 spotColor = mix(tint, vec3(0.45, 0.62, 0.78), 0.45) * light;
        color = mix(color, spotColor, spots * 0.32);

        if (part < 0.5) {
            // Front face: two big pixel eyes and a tall pixel mouth.
            float eyes = max(
                    pixelRect(cell, vec2(4.0, 9.0), vec2(6.0, 12.0)),
                    pixelRect(cell, vec2(9.0, 9.0), vec2(11.0, 12.0)));
            float mouth = pixelRect(cell, vec2(7.0, 4.0), vec2(8.0, 7.0));
            float ink = max(eyes, mouth);
            vec3 inkColor = vec3(0.12, 0.21, 0.35) * (0.90 + 0.10 * light);
            color = mix(color, inkColor, ink);
        } else if (part > 0.5 && part < 1.5) {
            // Back face: a blocky square spiral.
            float rune = 0.0;
            rune = max(rune, pixelRect(cell, vec2(5.0, 5.0), vec2(10.0, 5.0)));
            rune = max(rune, pixelRect(cell, vec2(10.0, 5.0), vec2(10.0, 10.0)));
            rune = max(rune, pixelRect(cell, vec2(5.0, 10.0), vec2(10.0, 10.0)));
            rune = max(rune, pixelRect(cell, vec2(5.0, 7.0), vec2(5.0, 10.0)));
            rune = max(rune, pixelRect(cell, vec2(5.0, 7.0), vec2(8.0, 7.0)));
            color = mix(color, mix(tint, vec3(1.0), 0.55) * light, rune * 0.55);
        } else if (part > 3.5 && part < 4.5) {
            // Top face: a small, faint mark.
            float mark = max(
                    pixelRect(cell, vec2(6.0, 7.0), vec2(9.0, 7.0)),
                    pixelRect(cell, vec2(7.0, 6.0), vec2(8.0, 8.0)));
            color = mix(color, mix(tint, vec3(1.0), 0.50) * light, mark * 0.40);
        }

        // Pale rim around every face, about two pixels wide at any distance.
        // This is the screen-space outline that keeps the cube readable.
        float edge = min(min(uv.x, 1.0 - uv.x), min(uv.y, 1.0 - uv.y));
        float rimWidth = clamp(softness * 2.4, 0.025, 0.10);
        float rim = 1.0 - smoothstep(rimWidth * 0.5, rimWidth, edge);
        vec3 rimColor = mix(vec3(0.90, 0.97, 1.0), tint, 0.22);
        color = mix(color, rimColor, rim);
    } else if (part < 6.5) {
        // Hanging blocks are plain pale blue.
        color = mix(color, vec3(1.0), 0.08);
    } else {
        // Droplets catch a little more light than the body.
        color = mix(color, vec3(0.95, 0.99, 1.0), 0.30);
    }

    color *= ColorModulator.rgb;

    float fog = total_fog_value(
            sphericalVertexDistance,
            cylindricalVertexDistance,
            FogEnvironmentalStart,
            FogEnvironmentalEnd,
            FogRenderDistanceStart,
            FogRenderDistanceEnd);
    color = mix(color, FogColor.rgb, fog * FogColor.a);

    fragColor = vec4(color, 1.0);
}
