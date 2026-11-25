# ChemCraft full-screen substance presentation renderer

## Result and reference footage

The attached archive's source matched the existing workspace source. The earlier `EXPERIENCE_RENDERING_UPGRADE.md` described a GUI-only composition engine and explicitly said there was no shader or framebuffer path; that was still true in the source before this revision.

I inspected the supplied MP4 by sampling the recording from start to finish, then checking full-sized frames around 80–130 seconds. The opening and the section around 80–100 seconds show menus, loading, the world, and additive inventory. The active effect begins around 109 seconds: the rendered world becomes a full-frame, center-focused radial vortex with evolving refraction and spectral color splitting. I used those broad presentation characteristics as a technical reference. No medical accuracy is claimed, and no reference assets or shader code were copied.

## Current architecture

- The existing registered item registry, profile catalog, server-to-client activation packets, lifecycle timer, additive item use/consumption, client modifier catalog, camera/audio hooks, GUI composition renderer, and accessibility controls remain in place.
- `ExperiencePresentationEvents` listens to Forge's 1.20.1 `RenderLevelStageEvent` and delegates at `AFTER_LEVEL`. Forge documents that stage as occurring after `LevelRenderer.renderLevel` completes. The effect therefore processes the full rendered 3D scene target before the later GUI composition. It does not process HUD pixels or later hand rendering.
- `FullScreenPostProcessor` creates and owns an independent Minecraft `PostChain` against the main render target. It does not use `GameRenderer.loadEffect`, which would replace Minecraft's current post effect. It uses `PostChain`'s public 1.20.1 constructor, `addTempTarget`, `addPass`, `resize`, and `process` APIs, and `PostPass.getEffect()` for per-frame uniforms. The ordered stage list is a small registry of stage specs, rather than pass construction being embedded in the event handler.
- The chain has two ordered full-frame stages. The first samples the complete world target and performs center-weighted radial swirl/refraction. The second reads that result and adds animated radial chromatic separation and a profile palette contribution, then writes back to the main target.
- The chain is compiled on the client resource reload thread's apply phase, disposed/recreated on reload, resized to the main target's dimensions, and disabled with an error log after resource or runtime failure until the next reload. The legacy GUI layers remain as a safe fallback.
- `ExperienceProfile.layers` remains the data-driven profile format. The fictional `echo_prism` profile now includes `post_vortex` and `post_spectral` stage definitions. Other profiles can request those stage types without renderer rewrites. Profile phase/intensity and layer color feed the post uniforms.
- Additive modifiers remain JSON-driven. The four modifier files now include generic `post_vortex` and `post_spectral` channels; shader source does not branch on additive IDs.

## Shader resources

Minecraft 1.20.1's older `PostChain`/`PostPass` system uses `assets/<namespace>/shaders/post` for the chain descriptor and program definitions under `assets/minecraft/shaders/program` for these post-pass `EffectInstance` names. The program names are uniquely prefixed `barklychem_post_*` to avoid overriding vanilla program IDs. This is specific to the 1.20.1 format and is not the newer `post_effect` schema used by recent Minecraft releases.

| Resource | Purpose |
| --- | --- |
| `assets/barklychemlab/shaders/post/chemcraft_perception.json` | Owned chain and intermediate target descriptor |
| `assets/minecraft/shaders/program/barklychem_post_vertex.vsh` | Full-screen post-pass vertex shader |
| `assets/minecraft/shaders/program/barklychem_post_vortex.json` | First stage program definition and uniforms |
| `assets/minecraft/shaders/program/barklychem_post_vortex.fsh` | Full-frame radial refraction/swirl |
| `assets/minecraft/shaders/program/barklychem_post_spectral.json` | Second stage program definition and uniforms |
| `assets/minecraft/shaders/program/barklychem_post_spectral.fsh` | Chromatic channel separation and profile palette |

Animation uniforms are supplied from the existing profile phase, local intensity/accessibility settings, main-target dimensions, frame time, weighted per-layer speed and scale, opacity, and profile/additive stage palettes. The shaders are ordinary Minecraft 1.20.1 GLSL 150 post programs; no external rendering dependency was added.

## Profile and additive schema

Profile JSON layers retain these bounded fields: `id`, `type`, `order`, `intensity`, `opacity`, `speed`, `scale`, `color`, and `accessibility`. In addition to GUI types, the two supported full-frame types are `post_vortex` and `post_spectral`. They share the profile onset/peak/recovery lifecycle and support additive channel adjustments. Existing flat profile fields and the legacy layer fallback remain for compatibility.

Additive definitions are resource-reloadable JSON with an ID, label, generic numeric `channels`, bounded `intensity_multiplier`, `tint`, and `duration_ticks`. The modifiers influence post stages through data channels, with the same clamping, server-side item-use gate, inventory consumption, and deterministic active-modifier cap as before.

## Accessibility

The post chain obeys the master simulation switch, visual-effects switch, maximum local intensity, profile transition envelope, full-screen post toggle, independent vortex/spectral toggles, and the chromatic-fringe toggle. Reduced motion removes animated shader movement and substantially reduces the vortex contribution. Flash reduction slows the animation. The existing immediate F8 reset removes active state so the post stages stop on the next render. HUD, audio, and camera switches remain independent.

The two shader stages are full-frame and can be intense. The post stage toggles and global intensity limit are the primary accessibility controls; the legacy GUI settings do not silently override those master controls.

## Rendering compatibility and remaining limits

- The processing input is the Minecraft main world render target at Forge's `AFTER_LEVEL` stage. It includes the rendered world scene, but not later HUD drawing; held-item rendering is also later in the vanilla frame and is not distorted by this hook.
- The supplied Addictives source archive was inspected separately; the architecture findings and compatibility distinctions are recorded in [ADDICTIVES_RENDERING_ANALYSIS.md](ADDICTIVES_RENDERING_ANALYSIS.md). ChemCraft reuses public PostChain concepts without copying decompiled code or replacing the shared GameRenderer post-effect slot.
- Full-screen effects from other mods may interact with this post pass depending on their stage/order. Mods that directly replace the main target or use incompatible post-processing can conflict. OptiFine/shader-pack compatibility is not established.
- There is no temporal framebuffer history, depth-aware blur, or post-processing of GUI/hand pixels. The implementation provides two real full-frame shader stages; it does not claim those other effects.
- The program resources live in Minecraft's legacy `shaders/program` namespace due to 1.20.1's post-pass loader behavior. Names are unique, but a resource pack can still override them.

## Verification status

- Resource/schema validator: see `scripts/verify_render_resources.py`; checks profile/modifier JSON, layer bounds and IDs, post-chain target descriptor, program JSON, shader file references, shader uniform declarations, and the fictional demo profile.
- The validator is run after the shader/resource changes. The video was visually inspected at sample points including 110, 120, and 130 seconds to distinguish active gameplay from inventory footage.
- The latest Gradle build could not start: the wrapper attempted to download Gradle 8.8 from `services.gradle.org` and the execution environment returned `Network is unreachable`. This environment also has no system Gradle binary. Therefore the new Java source has not been compiled here.
- No GLSL compiler or Minecraft client launch was available. The shader programs have static resource-reference checks, not driver compilation or live visual confirmation. Do not treat the implementation as runtime-verified.
- The uploaded ZIP includes source and documentation, not a newly rebuilt JAR. The prior JAR does not include this renderer change and should not be installed as the result of this revision.
