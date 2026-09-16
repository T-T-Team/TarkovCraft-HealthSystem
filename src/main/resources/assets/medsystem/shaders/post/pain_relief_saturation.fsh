#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>
#include <tarkovcraft_core:colors.glsl>

uniform sampler2D InSampler;

layout(std140) uniform SaturationSettings {
    float SaturationAmount;
};

layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

void main() {
    vec4 color = texture(InSampler, texCoord);
    vec3 hsv = rgb2hsv(color.rgb);
    float strength = SaturationAmount * ColorModulator.a;
    hsv.y = clamp(hsv.y * (1.0 + strength), 0.0, 1.0);
    vec3 saturated = hsv2rgb(hsv);
    fragColor = vec4(saturated, color.a);
}