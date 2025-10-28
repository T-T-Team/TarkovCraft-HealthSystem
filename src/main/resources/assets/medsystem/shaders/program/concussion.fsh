#version 150

uniform sampler2D DiffuseSampler;
uniform float Speed;
uniform int Replicas;
uniform float Radius;
uniform float ReplicaBlur;
uniform float GameTime;

in vec2 texCoord;
out vec4 fragColor;

float rand(vec2 co) {
    return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec2 uv = texCoord;
    float Time = GameTime * 24000;
    vec3 scene = texture(DiffuseSampler, uv).rgb;
    float angle = Time * Speed;
    vec2 dir = vec2(cos(angle), sin(angle));
    vec3 accum = scene;
    for (int i = 1; i <= Replicas; i++) {
        float t = float(i) / float(Replicas);
        float len = length(uv - 0.5);
        float radius = Radius * t + 0.03 * len * t;
        float replicaBlur = (rand(uv * (Time + float(i))) - 0.5) * ReplicaBlur;
        vec2 offset = dir * radius + replicaBlur;
        accum += texture(DiffuseSampler, uv + offset).rgb;
    }

    vec3 color = accum / float(Replicas + 1);
    fragColor = vec4(color, 1.0);
}