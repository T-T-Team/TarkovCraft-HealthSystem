#version 330

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D InSampler;

layout(std140) uniform DesaturationSettings {
    float DesaturationAmount;
};

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 color = texture(InSampler, texCoord);
    float strength = DesaturationAmount * ColorModulator.a;
    float gray = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));
    vec3 desaturated = mix(color.rgb, vec3(gray), strength);
    fragColor = vec4(desaturated, color.a);
}