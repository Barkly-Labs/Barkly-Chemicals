# Create: Addictives rendering reference analysis

## Scope and method

The supplied `addictives-4.0.5-forge.jar.src.zip` was inspected as a technical reference. This project does not copy or redistribute its decompiled implementation. The notes below describe the observed architecture and the lessons used for ChemCraft's independent renderer.

## Observed reference path

| Responsibility | Decompiled source | Observed behavior |
| --- | --- | --- |
| Client state and post chain | `net/addictives/client/clienthandler.java` | Keeps current/pending shader IDs, active effect sets, intensity/fade state, and cached activation times. Chooses a single per-effect chain or `composite.json` when several effect types are active. |
| Render hook | `clienthandler.stage(RenderLevelStageEvent)` | Runs at `RenderLevelStageEvent.Stage.AFTER_LEVEL`; reloads the selected chain if Minecraft's current chain is absent/different, then assigns effect uniforms and lets Minecraft process its current entity post chain. |
| Chain switching | `clienthandler.tick(ClientTickEvent)` | Selects the active effect chain, applies a priority policy, cross-fades intensity, and clears the currently loaded chain once the effect has faded out. |
| Uniform updates | `clienthandler.uniform(PostChain, String, float)` | Reflects over `PostChain` fields to find the internal `List<PostPass>`, then sets a uniform by name. Exceptions are swallowed. |
| Vanilla post-effect coordination | `net/addictives/mixin/gamerenderermixin.java` | Cancels `GameRenderer.checkEntityPostEffect` while the mod's shader is active, preventing the entity post effect from replacing the mod's selected chain. |
| Full-frame shader resources | `assets/addictives/shaders/post/*.json`, `assets/addictives/shaders/program/*` | Individual shader chains sample `minecraft:main`, render to a swap target, and blit back. Some programs sample a `previous` target for temporal persistence; the composite program receives a separate uniform for each active effect. |
| Other client presentation | `clienthandler`, `drugeffectlayer.java`, `methhallucinations.java`, `dmtbreakthrough.java` | Separate event hooks control camera/input/audio and entity presentation. These are distinct from the post-chain full-frame image processing. |

### What it does well

- Uses Minecraft's actual `PostChain` / `PostPass` infrastructure instead of drawing a screen-sized GUI approximation.
- Runs image processing after the 3D world render at Forge's `AFTER_LEVEL` stage.
- Uses swap targets for full-frame shader passes and supports a previous-frame sampler for temporal effects.
- Separates shader assets into chain JSON, program JSON, vertex shader, and fragment shader resources.
- Has lifecycle transitions and explicitly unloads the selected post effect after recovery.

### Coupling and limitations observed

- It installs effects through Minecraft's shared `GameRenderer` post-effect slot, so it competes with vanilla entity effects and other mods that use that slot. The included mixin suppresses vanilla's entity-post-effect check while Addictives is active; it does not make that shared slot composable with arbitrary post-processing mods.
- It loads one selected chain at a time. Multiple active effects switch to a dedicated composite chain with per-effect uniforms; this is a purpose-built shader rather than a general pass registry.
- Uniform updates depend on reflection into `PostChain` internals and silently ignore failures.
- Multiple broad `catch` blocks also swallow chain-load and render errors, making failures difficult to diagnose.
- Several profile/effect decisions are hardcoded in the client handler, and the decompiled source includes movement/input mutation. These parts are not a suitable architecture for ChemCraft's separately bounded, accessibility-first visual simulation.
- Temporal persistence depends on a `previous` render target and explicit blit-back passes. That increases state/lifecycle complexity and requires careful clearing and resizing to avoid stale-frame artifacts.
- `AFTER_LEVEL` processes the world viewport before later hand and GUI rendering. It is full-frame world processing, not final compositing of the HUD or held item.

## ChemCraft application

ChemCraft adopts the reusable public-API concepts—`PostChain`, ordered `PostPass` stages, a main render target, temporary targets, `AFTER_LEVEL`, resize handling, resource reload and profile-driven uniforms—but not the reference's implementation. ChemCraft owns a private chain and calls `process` directly, so it does not replace Minecraft's active entity post effect or require the reference's `GameRenderer` mixin workaround. The current code also obtains `PostPass.getEffect().getUniform(...)` directly instead of reflecting over chain internals.

The ChemCraft chain currently declares two registered stage specifications in `FullScreenPostProcessor`: `post_vortex`, then `post_spectral`. Profile JSON controls stage selection, ordering metadata, strength/opacity, speed, scale, palette, accessibility group and lifecycle envelope. Additive JSON adjusts bounded stage channels and contributes its tint only to stages that the modifier declares. Shader IDs are not tied to substance or additive item IDs.

ChemCraft intentionally does not implement the reference's previous-frame feedback target yet. Its two stages process the current full-frame scene, which avoids temporal ghosting buffers and their extra stale-frame/resource risks. Adding a history buffer later should be an explicit stage capability with reset-on-profile-stop, reload, resize, pause and world-change behavior.

## Compatibility boundaries

- Target is Forge 47.x / Minecraft 1.20.1's legacy `shaders/post` and `shaders/program` format. Newer versions use different post-effect resource formats and must not be assumed compatible.
- Forge's `AFTER_LEVEL` event allows full-viewport world-scene processing, not final UI/hand processing.
- Other mods that write to the same main target around this stage, replace render targets, or install incompatible shader pipelines can conflict. Compatibility with OptiFine, shader packs, Oculus, and other post-processing mods requires runtime matrix testing; static source review cannot establish it.
- The reference mixin is not required by ChemCraft's separate `PostChain`, but competing GPU state changes from other mods remain a possible integration issue.
