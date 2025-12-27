#version 330

#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D InSampler;

layout(std140) uniform ConcussionConfig {
    float Speed;
    int Replicas;
    float Radius;
    float ReplicaBlur;
};

in vec2 texCoord;
out vec4 fragColor;

float rand(vec2 co) {
    return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec2 uv = texCoord;
    float Time = GameTime * 24000;
    vec3 scene = texture(InSampler, uv).rgb;
    float angle = Time * Speed;
    vec2 dir = vec2(cos(angle), sin(angle));
    vec3 accum = scene;
    for (int i = 1; i <= Replicas; i++) {
        float t = float(i) / float(Replicas);
        float len = length(uv - 0.5);
        float radius = Radius * t + 0.03 * len * t;
        float replicaBlur = (rand(uv * (Time + float(i))) - 0.5) * ReplicaBlur * ColorModulator.a;
        vec2 offset = ColorModulator.a * dir * radius + replicaBlur;
        accum += texture(InSampler, uv + offset).rgb;
    }

    vec3 color = accum / float(Replicas + 1);
    fragColor = vec4(color, 1.0);
}