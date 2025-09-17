#version 150

uniform sampler2D InSampler;

layout(std140) uniform BrightnessConfig {
    float Brightness;
};

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 color = texture(InSampler, texCoord);
    color.rgb *= Brightness;
    fragColor = color;
}