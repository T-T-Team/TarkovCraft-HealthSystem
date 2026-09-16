#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>

uniform sampler2D InSampler;

layout(std140) uniform BlurSettings {
    vec2 Center;
    int Samples;
    float Strength;
};

layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

void main() {
    vec4 color = vec4(0);
    float weight = 0.0;
    float effectStrength = Strength * ColorModulator.a;

    for (int i = 0; i < Samples; i++) {
        float f = float(i) / float(Samples - 1) * effectStrength;
        vec2 sampleUv = mix(texCoord, Center, f);
        float w = 1.0 - f;
        color += texture(InSampler, sampleUv) * w;
        weight += w;
    }

    fragColor = color / weight;
}