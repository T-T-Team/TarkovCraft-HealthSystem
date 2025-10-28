#version 150

uniform sampler2D DiffuseSampler;
uniform float Strength;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);

    float gray = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));
    vec3 desaturated = mix(color.rgb, vec3(gray), Strength);

    fragColor = vec4(desaturated, color.a);
}