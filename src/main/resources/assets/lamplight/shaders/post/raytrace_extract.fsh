#version 330

uniform sampler2D InSampler;

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
    vec3 c = texture(InSampler, texCoord).rgb;
    float luma = dot(c, vec3(0.2126, 0.7152, 0.0722));
    // Pale albedo like sand, sandstone and snow sits around 0.8 luma. The old
    // 0.55 threshold treated those surfaces as light emitters, so their bloom
    // was added straight back on top of them and clipped them to white. Only
    // genuinely hot pixels should seed bloom.
    float soft = smoothstep(0.78, 1.0, luma);
    fragColor = vec4(c * soft, 1.0);
}
