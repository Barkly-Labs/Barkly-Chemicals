#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 InSize;
uniform float Time;
uniform float Strength;
uniform float Motion;
uniform float Phase;
uniform vec3 Tint;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 centered = texCoord - vec2(0.5);
    float aspect = InSize.x / max(InSize.y, 1.0);
    vec2 radial = vec2(centered.x * aspect, centered.y);
    float radius = length(radial);
    vec2 direction = normalize(radial + vec2(0.00001));
    float amount = Strength * (0.0020 + 0.0075 * smoothstep(0.02, 0.62, radius));
    float slowOrbit = 1.0 + Motion * 0.12 * sin(Time * 0.12 + Phase + radius * 8.0);
    vec2 offset = vec2(direction.x / aspect, direction.y) * amount * slowOrbit;
    vec3 separated;
    separated.r = texture(DiffuseSampler, clamp(texCoord + offset, vec2(0.001), vec2(0.999))).r;
    separated.g = texture(DiffuseSampler, texCoord).g;
    separated.b = texture(DiffuseSampler, clamp(texCoord - offset, vec2(0.001), vec2(0.999))).b;
    vec4 original = texture(DiffuseSampler, texCoord);
    float halo = (1.0 - smoothstep(0.03, 0.85, radius)) * Strength * 0.08;
    separated = mix(separated, separated * 0.78 + Tint * 0.22, halo);
    fragColor = vec4(separated, original.a);
}
