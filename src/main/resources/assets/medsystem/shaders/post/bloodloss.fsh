#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>

uniform sampler2D InSampler;

layout(std140) uniform DesaturationSettings {
    float DesaturationAmount;
};

layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

void main() {
    vec4 color = texture(InSampler, texCoord);
    float strength = DesaturationAmount * ColorModulator.a;
    float gray = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));
    vec3 desaturated = mix(color.rgb, vec3(gray), strength);
    fragColor = vec4(desaturated, color.a);
}