#version 330

#moj_import <dynamictransforms.glsl>
#moj_import <tarkovcraft_core:colors.glsl>

uniform sampler2D InSampler;

layout(std140) uniform SaturationSettings {
    float SaturationAmount;
};

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 color = texture(InSampler, texCoord);
    vec3 hsv = rgb2hsv(color.rgb);
    float strength = SaturationAmount * ColorModulator.a;
    hsv.y = clamp(hsv.y * (1.0 + strength), 0.0, 1.0);
    vec3 saturated = hsv2rgb(hsv);
    fragColor = vec4(saturated, color.a);
}