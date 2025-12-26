#version 330

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D InSampler;
in vec2 texCoord;

layout(std140) uniform BlurSettings {
    vec2 Center;
    int Samples;
};

out vec4 fragColor;

void main() {
    vec4 color = vec4(0);
    float weight = 0.0;
    float strength = ColorModulator.r;

    for (int i = 0; i < Samples; i++) {
        float f = float(i) / float(Samples - 1) * strength;
        vec2 sampleUv = mix(texCoord, Center, f);
        float w = 1.0 - f;
        color += texture(InSampler, sampleUv) * w;
        weight += w;
    }

    fragColor = color / weight;
}