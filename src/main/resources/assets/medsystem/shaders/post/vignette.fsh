#version 150

#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

layout(std140) uniform VignetteConfig {
    float Speed;
    float Strength;
};

in vec2 texCoord;

out vec4 fragColor;

float vignette(vec2 uv, float intensity, float smoothness) {
    vec2 coord = uv * 2.0 - 1.0;
    float dist = length(coord);
    return 1.0 - smoothstep(intensity, intensity + smoothness, dist);
}

void main() {
    vec4 color = texture(InSampler, texCoord);

    float intensity = 0.5;
    float smoothness = 0.3;
    float pulse = sin(GameTime * 24000.0 * Speed);
    float blended = mix(intensity, intensity * Strength, pulse);
    float vig = vignette(texCoord, blended, smoothness);

    color.rgb *= vig;
    fragColor = color;
}