#version 330

uniform sampler2D InSampler;
uniform sampler2D BloomSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform RayConfig {
    float Strength;
    float Vibrance;
    float Bloom;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 base = texture(InSampler, texCoord);
    vec3 bloom = texture(BloomSampler, texCoord).rgb;

    vec3 c = base.rgb + bloom * (0.55 * Bloom * Strength);

    float luma = dot(c, vec3(0.2126, 0.7152, 0.0722));
    float bell = exp(-pow((luma - 0.5) / 0.42, 2.0));
    float sat = 1.0 + bell * 0.5 * Vibrance * Strength;
    c = clamp(luma + (c - luma) * sat, 0.0, 1.0);

    float shoulder = 0.18 * Strength;
    c = c * (1.0 + shoulder) / (1.0 + shoulder * c);

    fragColor = vec4(c, base.a);
}
