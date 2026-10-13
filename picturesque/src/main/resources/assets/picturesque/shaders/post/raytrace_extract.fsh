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
    float soft = smoothstep(0.55, 0.95, luma);
    fragColor = vec4(c * soft, 1.0);
}
