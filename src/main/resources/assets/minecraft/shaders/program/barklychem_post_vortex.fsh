#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 InSize;
uniform float Time;
uniform float Speed;
uniform float Scale;
uniform float Strength;
uniform float Motion;
uniform float Phase;
uniform vec3 Tint;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 centered = texCoord - vec2(0.5);
    float aspect = InSize.x / max(InSize.y, 1.0);
    vec2 plane = vec2(centered.x * aspect, centered.y);
    float radius = length(plane);
    float scaledRadius = radius / max(Scale, 0.25);
    float angle = atan(plane.y, plane.x);
    float falloff = 1.0 - smoothstep(0.04, 0.74, radius);
    float baseSwirl = Strength * falloff * (0.18 + 0.26 * (1.0 - smoothstep(0.02, 0.48, scaledRadius)));
    float animatedSwirl = Motion * Strength * falloff * sin(scaledRadius * 17.0 - Time * 0.20 * Speed + Phase * 2.0) * 0.035;
    angle += baseSwirl + animatedSwirl;
    float ripple = Motion * sin(scaledRadius * 24.0 - Time * 0.24 * Speed + Phase) * Strength * falloff * 0.009 * Scale;
    float warpedRadius = radius + ripple;
    vec2 warpedPlane = vec2(cos(angle), sin(angle)) * warpedRadius;
    vec2 uv = vec2(warpedPlane.x / aspect, warpedPlane.y) + vec2(0.5);
    uv = clamp(uv, vec2(0.001), vec2(0.999));
    vec4 scene = texture(DiffuseSampler, uv);
    float ring = pow(max(0.0, sin(scaledRadius * 28.0 - Time * 0.15 * Speed + Phase)), 8.0) * falloff * Strength * 0.10;
    scene.rgb = mix(scene.rgb, Tint, ring);
    fragColor = scene;
}
