#version 330

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
    vec2 InDepthSize;
};

// Layout must match nullbloom_darkness.json and NullbloomDarkness.java (std140, 128 bytes).
layout(std140) uniform DarknessConfig {
    mat4 InvViewProj; // inverse(projection * viewRotation)
    vec4 CameraPos;   // xyz = camera world position, w = 1 if clip-space z is [0,1], 0 if [-1,1]
    vec4 Center;      // xy = world x,z of the point everything is darkened around
    vec4 Profile;     // x gloomy radius, y dark radius, z brightness at centre, w brightness at gloomy radius
    vec4 Shape;       // x falloff exponent between gloomy and dark radius, y debug mode
};

in vec2 texCoord;

out vec4 fragColor;

float smooth01(float t) {
    t = clamp(t, 0.0, 1.0);
    return t * t * (3.0 - 2.0 * t);
}

float brightnessAt(float r) {
    float gloomy = Profile.x;
    float dark = Profile.y;
    if (r < gloomy) {
        return mix(Profile.z, Profile.w, smooth01(r / gloomy));
    }
    float t = (r - gloomy) / max(dark - gloomy, 1.0e-4);
    return Profile.w * pow(1.0 - smooth01(t), Shape.x);
}

void main() {
    vec4 scene = texture(InSampler, texCoord);
    float depth = texture(InDepthSampler, texCoord).r;
    float ndcZ = CameraPos.w > 0.5 ? depth : depth * 2.0 - 1.0;
    vec4 clip = vec4(texCoord * 2.0 - 1.0, ndcZ, 1.0);
    vec4 rel = InvViewProj * clip;
    vec3 world = CameraPos.xyz + rel.xyz / rel.w;
    float r = length(world.xz - Center.xy);
    int mode = int(Shape.y + 0.5);
    if (mode == 1) {            // passthrough: the pass runs but changes nothing
        fragColor = scene;
    } else if (mode == 2) {     // distance from centre: black at 0, white at 20+ blocks. NaN shows magenta.
        fragColor = (r == r) ? vec4(vec3(clamp(r / 20.0, 0.0, 1.0)), 1.0) : vec4(1.0, 0.0, 1.0, 1.0);
    } else if (mode == 3) {     // raw depth, boosted
        fragColor = vec4(vec3(clamp(depth * 20.0, 0.0, 1.0)), 1.0);
    } else if (mode == 5) {     // distance from the camera to what each pixel shows: ground a few blocks away = dark, far/sky = white
        fragColor = vec4(vec3(clamp(length(rel.xyz / rel.w) / 40.0, 0.0, 1.0)), 1.0);
    } else if (mode == 6) {     // screen coordinates: red = x, green = y (red to the right, green upward means orientation is right)
        fragColor = vec4(texCoord, 0.0, 1.0);
    } else if (mode == 7) {     // camera x/z squashed to 0..1 over +-64 blocks, centre of screen = camera position on the map
        fragColor = vec4(fract(CameraPos.x / 64.0 + 0.5), fract(CameraPos.z / 64.0 + 0.5), CameraPos.w, 1.0);
    } else if (mode == 4) {     // world-locked stripes: red = x, green = z (should stay fixed in the world as you move)
        fragColor = vec4(fract(world.x), fract(world.z), 0.0, 1.0);
    } else {
        float b = brightnessAt(r);
        fragColor = (b == b) ? vec4(scene.rgb * b, scene.a) : scene;
    }
}
