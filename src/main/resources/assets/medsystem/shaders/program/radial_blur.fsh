#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 Center;
uniform float Strength;
uniform int Samples;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 uv = texCoord;
    vec4 color = vec4(0.0);
    float weightSum = 0.0;

    for (int i = 0; i < Samples; i++) {
        float f = float(i) / float(Samples - 1) * Strength;
        vec2 sampleUv = mix(uv, Center, f);
        sampleUv = clamp(sampleUv, 0.0, 1.0);

        float w = 1.0 - f;
        color += texture(DiffuseSampler, sampleUv) * w;
        weightSum += w;
    }

    fragColor = color / weightSum;
}