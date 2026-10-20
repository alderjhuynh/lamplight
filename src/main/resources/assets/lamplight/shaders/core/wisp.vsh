#version 330

#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:fog.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in vec3 Normal;

out vec4 vertexColor;
out vec2 faceUv;
out vec3 faceNormal;
out float sphericalVertexDistance;
out float cylindricalVertexDistance;

const float TAU = 6.28318530718;

void main() {
    // UV0.x carries the body part: 0..5 are cube faces, 6 is a hanging
    // block and 7 is a floating droplet. All of it stays in one draw call.
    float part = floor(UV0.x);

    // Shared with WispPulseControl: a 40 tick breath with a sharp squeeze.
    float phase = fract(GameTime / 40.0);
    float bloom = sin(phase * TAU);
    float squeezeDelta = (phase - 0.60) / 0.065;
    float squeeze = exp(-squeezeDelta * squeezeDelta);

    vec3 pos = Position;

    // The cube and its hanging blocks breathe together.
    if (part < 6.5) {
        pos *= 1.0 + 0.035 * bloom - 0.045 * squeeze;
    }

    // Droplets drift independently of the body's breathing.
    if (part > 6.5) {
        float phaseOffset = Position.x * 40.0 + Position.z * 55.0;
        pos.y += sin(GameTime * 0.11 + phaseOffset) * 0.012;
        pos.x += cos(GameTime * 0.08 + phaseOffset) * 0.006;
    }

    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    vertexColor = Color;
    faceUv = UV0;
    faceNormal = Normal;
    sphericalVertexDistance = fog_spherical_distance(pos);
    cylindricalVertexDistance = fog_cylindrical_distance(pos);
}
