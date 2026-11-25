# ChemCraft layered perception renderer upgrade

## Summary

The existing Forge 1.20.1 effect activation, packet delivery, profile lifecycle, accessibility configuration, and normal GUI/camera/audio hooks were retained. Rendering was separated from the event subscriber and profile data now composes independently ordered modules. Profile and modifier files are loaded through client resource reload listeners. The implementation remains an in-GUI presentation effect; it does not claim a post-process over the 3D world.

## Architecture

- `ExperiencePresentationEvents` owns Forge callbacks only: HOTBAR post overlay, menu post render, and camera angle feedback. It delegates drawing to the composition engine.
- `ExperienceProfile` now contains immutable `ExperienceVisualLayer` entries. Existing top-level strength, pattern and lifecycle timing fields remain supported; a compatibility fallback synthesizes simple layers for legacy profiles.
- `ExperienceLayerRenderers` owns a renderer registry keyed by declarative `type`, sorts layers by `order`, limits each frame to 96 submitted layers, and composes each active profile's layers. New client renderer types can be registered without expanding the Forge event handler.
- Built-in modules: `edge_tint`, `bands`, `motif`, `orbit`, `particles`, `motion_trail`, and `chromatic`.
- All GUI operations use the active `GuiGraphics` batch. Layers use profile intensity, lifecycle phase, local accessibility intensity, layer opacity, and bounded animation parameters.
- `ExperienceModifierCatalog` loads fictional additive definitions independently. Each additive exposes a map of bounded channels, a clamped intensity multiplier, tint, and active duration. Renderer code does not know additive IDs or encode additive-specific behavior.
- Active modifier IDs are retained, so resource reloads replace the effective data without leaving stale definitions active. Modifier collisions are deterministic: duplicate IDs refresh; when the configured limit is exceeded, modifiers are sorted by ID and the lexicographically later entries are removed.

## Resource schema

Each profile JSON may add a `layers` array. Fields:

| Field | Meaning | Validation |
| --- | --- | --- |
| `id` | Profile-local layer name | fallback generated if omitted |
| `type` | Registered renderer module | unknown values safely skipped |
| `order` | Composition order | clamped to -10000..10000 |
| `intensity`, `opacity` | Layer contribution and alpha factor | clamped to 0..1 |
| `speed` | Animation rate | clamped to 0..4 |
| `scale` | Size multiplier | clamped to 0.25..4 |
| `color` | RGB override | six hex digits; defaults to profile color |
| `accessibility` | `visual`, `hud`, `trails`, `chromatic`, or `motion` | unknown category falls back to visual |

A maximum of 16 layer definitions is accepted per profile; the composition pass processes at most 96 profile-layer pairs. Existing `onset_ticks`, `peak_ticks`, and `recovery_ticks` still control the shared phase envelope; every child layer receives that envelope. Legacy profiles without `layers` continue to work.

Modifier JSON lives under `assets/barklychemlab/experience_modifiers/<id>.json`:

```json
{
  "id": "prism_salt",
  "label": "Prism Salt",
  "channels": { "fringe": 0.22, "warp": 0.12, "strength": 0.04 },
  "intensity_multiplier": 1.10,
  "tint": "#D49EFF",
  "duration_ticks": 1200
}
```

Channels are data-only and clamped to -0.5..0.5; intensity multiplier is clamped to 0.5..1.5; duration is clamped to 20..12000 ticks. IDs must match the resource filename. The server continues to consume an additive only when its registered item is used while an experience is active; modifier content is resolved client-side for local presentation.

## Accessibility

All layers respect master simulation and visual switches, local intensity and transition scale. `hud`, `trails`, `chromatic`, and `motion` groups respect the existing HUD, trail, chromatic, and reduced-motion settings. New independent controls were added for edge tint, bands, motifs, orbits, particles, and the maximum active modifier count. Reduced motion freezes/disables moving modules. Flash prevention caps the edge animation variation and alpha. Existing F8 reset and master switch remain.

## Render pipeline limits

Forge's `RenderGuiOverlayEvent.Post` and `ScreenEvent.Render.Post` support composited screen-space graphics using the current GUI render target, and `ViewportEvent.ComputeCameraAngles` supports camera angle changes. These hooks are appropriate for GUI overlays, motifs, and bounded camera feedback. They do not provide captured scene pixels for blur, temporal framebuffer feedback, actual chromatic separation of the world, optical focus/depth manipulation, or world geometry warping. This code does not register a shader, custom framebuffer, or post-chain. Those capabilities require a separate post-processing pipeline and shader compatibility work; they are not represented as implemented here.

## Audit findings retained from the previous diagnostic

The prior renderer was one large event-handler method and selected one pattern string with a switch. Profiles otherwise contained useful per-effect channels and lifecycle timing. Additive definitions were hard-coded in a Java `Map`. Existing network activation is server-to-using-client, with duration/profile transmitted and local profile intensity/accessibility evaluated client-side. Camera and audio hooks are separate. The previous runtime diagnostic established no shader/framebuffer path and could not launch the game due missing offline Minecraft metadata.

## Verification

- `gradle build --no-daemon --offline`: **passed**, including Java compilation, resource processing, tests task, JAR packaging, and Forge reobfuscation. Gradle printed its known asset-index network warning and existing deprecation warnings.
- `scripts/verify_render_resources.py`: **passed**; validated 20 profiles, 71 layer definitions, 4 modifiers, renderer/accessibility IDs, profile and modifier path IDs, field bounds, and JSON parsing.
- Gradle `test`: **NO-SOURCE**; the repository has no JUnit suite. The Python resource validator is the new automated schema check.
- In-game launch and visual verification: **not performed**. Previous `runClient --offline` was blocked before launch by ForgeGradle `downloadMCMeta` (`Network is unreachable`); no Minecraft log was produced. The build proves API compilation, not visible rendering, resource reload timing, graphics-driver behavior, or multiplayer synchronization.
