#version 150

#moj_import <minecraft:globals.glsl>
#moj_import <medsystem:colors.glsl>

uniform sampler2D InSampler;

layout(std140) uniform SaturateConfig {
    float Strength;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 color = texture(InSampler, texCoord);
    vec3 hsv = rgb2hsv(color.rgb);
    hsv.y = clamp(hsv.y * (1.0 + Strength), 0.0, 1.0);
    vec3 saturated = hsv2rgb(hsv);

    fragColor = vec4(saturated, color.a);
}