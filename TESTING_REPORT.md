# Testing report

## Environment and build

- Target: Minecraft 1.20.1, Forge 47.4.28, Java 17.
- Build tool: Gradle 8.8 wrapper; ForgeGradle 6.0.54.
- `gradle build` completed successfully, including `compileJava`, resource processing, JAR assembly, and Forge reobfuscation.
- Output: `build/libs/barklychemlab-0.1.0.jar`.
- The compiler reported deprecation warnings for Forge context access and JEI's legacy background method; there were no compilation errors.
- Gradle's `test` task reports `NO-SOURCE`; this project has no automated unit test suite yet.

## Static checks

- Parsed every recipe, advancement, model, blockstate, language, and loot-table JSON.
- Parsed `mods.toml` and `pack.mcmeta`.
- Checked registered IDs against recipes, advancement item criteria, translations, item models, and texture files.
- Checked that the workbench has blockstate/model/item model/texture/loot table and a custom GUI texture.
- Checked research recipe JSON references, output IDs, recipe IDs, and custom recipe serializer/type registration.
- Checked that the reobfuscated JAR includes mod metadata, resources, and compiled block entity/menu/JEI integration classes.
- Verified all 34 registered items map to distinct 16×16 RGBA PNG textures; models point to matching textures, including four new individually drawn additive icons.
- Verified 20 data profiles exactly match all 20 profile-token registry IDs; each includes rationale, a reference field, a unique palette and a unique presentation pattern.
- Verified four additive Research Workbench recipes, item models/textures/translations, creative-tab entries, packet registration and the additive collection advancement data.
- Added [EXPERIENCE_SIMULATION.md](EXPERIENCE_SIMULATION.md), with per-substance source records, research-to-effect mappings, uncertainty notes and implementation limits.
- Added `TEXTURE_MAPPING.md` with the full registry-to-texture table, per-icon visual rationale, and physical-appearance references for the 15 named substances.
- Verified the ZIP archive after packaging.

## Runtime coverage and limits

- The mod JAR archive and all JSON resources were checked, but no Minecraft instance was launched in this workspace.
- Block placement, GUI interaction, NBT persistence, recipe completion, advancement notifications, JEI rendering, profile reload behavior, camera/audio hook behavior and multiplayer synchronization have compile/static validation only; they have not been observed in-game.
- The four additive icons were inspected as enlarged nearest-neighbor pixel art. This verifies the asset files and shape separation, not the live Minecraft renderer.
- The workbench logic is server-side and uses Forge's standard menu/slot/container-data synchronization, but final playtesting remains necessary before treating the JAR as production-ready.

- Build command for this revision: `gradle clean build --no-daemon --offline`; succeeded. Gradle emitted an asset-index network warning because the build environment blocks the ForgeGradle metadata request, but cached dependencies allowed compilation, resource processing, and reobfuscation to complete.
- Gradle reports `test NO-SOURCE`; no automated behavioral tests are present. Runtime visual quality, actual sound echo playback and client-server game sessions remain unverified.

## Screen-space rendering investigation

- Traced the item-use packet through the client profile state and Forge overlay callback; added logging for catalog loads, activation/intensity, hook invocation, and active profile drawing.
- Checked Forge 47.4.28 mapped API signatures for `RenderGuiOverlayEvent.Post`, `VanillaGuiOverlay.HOTBAR`, and `RegisterClientReloadListenersEvent`.
- Found no shader registration, GLSL resources, or custom framebuffer code in the implementation. The current renderer is a GUI overlay.
- `runClient --offline` could not launch: ForgeGradle failed at `downloadMCMeta` with `Network is unreachable`; no game logs or visual observation were available. Do not treat rendering as in-game verified. See `EXPERIENCE_RENDER_DIAGNOSTIC.md`.

## Layered perception renderer upgrade

- Added the in-place renderer and modifier architecture documented in [EXPERIENCE_RENDERING_UPGRADE.md](EXPERIENCE_RENDERING_UPGRADE.md).
- Latest `gradle build --no-daemon --offline`: passed (`BUILD SUCCESSFUL`), including Java compile, resource processing, `test` task, jar assembly and reobfuscation. Gradle emitted its cached-metadata asset-index network warning plus existing deprecation warnings.
- `gradle test`: `NO-SOURCE`; no behavioral Java tests exist.
- `python3 scripts/verify_render_resources.py`: passed; 20 profiles, 71 layers, 4 modifier records validated.
- Minecraft was not launched. ForgeGradle's prior offline `runClient` attempt stopped at `downloadMCMeta` with `Network is unreachable`. Rendering visibility and live resource reload remain unverified.
- The renderer uses GUI draw operations and camera hooks. It does not perform post-processing on rendered world pixels and has no custom framebuffer/shader chain.

## Full-screen post-processing revision (2026-11-25)

- Source archive was inspected and matched the previous layered GUI renderer; it had no shader, framebuffer, or post-chain implementation.
- Added the two-stage `PostChain`, Forge `AFTER_LEVEL` hook, resize/reload/failure handling, GLSL 150 resources, profile stage definitions, modifier channels, and accessibility toggles described in [EXPERIENCE_RENDERING_UPGRADE.md](EXPERIENCE_RENDERING_UPGRADE.md).
- `python3 scripts/verify_render_resources.py`: passed for 20 profiles, 73 GUI/post layers, 4 modifier files, the chain descriptor, 2 program definitions, vertex/fragment resource mappings, required uniforms, and the fictional demonstration profile.
- Latest `./gradlew build --no-daemon --offline`: **not run to compilation**. The wrapper tried to fetch Gradle 8.8 and failed with `java.net.SocketException: Network is unreachable`; no system Gradle binary is installed in this workspace. The earlier successful build covered the preceding GUI-only revision, not the new Java post-processor.
- GLSL validation: resource JSON and source mappings were statically checked. No GLSL compiler or Minecraft OpenGL context was available, so actual shader compilation is unverified.
- Minecraft client launch: **not performed**. Full-screen visual verification and multiplayer/render-mod compatibility testing remain outstanding.

## Reference source comparison follow-up (2026-11-25)

- Inspected the newly supplied `addictives-4.0.5-forge.jar.src.zip`, including `net/addictives/client/clienthandler.java`, `net/addictives/mixin/gamerenderermixin.java`, `net/addictives/client/drugeffectlayer.java`, and the `assets/addictives/shaders/post` and `shaders/program` resources. The independently written findings are in [ADDICTIVES_RENDERING_ANALYSIS.md](ADDICTIVES_RENDERING_ANALYSIS.md).
- Confirmed the reference's true post path: `AFTER_LEVEL`, Minecraft `PostChain`/`PostPass`, swap/previous targets, selected per-effect chain or dedicated composite, fades, and a `GameRenderer.checkEntityPostEffect` mixin to protect its shared renderer slot. Its handler reflects into private chain fields for uniforms and catches several errors silently. ChemCraft continues using its own `PostChain`, direct `PostPass.getEffect()` uniforms, and no reference code or mixin.
- Tightened ChemCraft stage resource checks to require chain, program JSON, fragment sources and vertex source before constructing a chain. Profile opacity now contributes to stage strength; per-stage speed/scale and modifier palette tints feed the GLSL uniforms.
- `python3 scripts/verify_render_resources.py`: passed (20 profiles, 73 layers, 4 modifiers, program/source/uniform references).
- Parsed all 109 resource JSON files successfully. No `src/test` files are present. No `glslangValidator` is installed.
- Retried `./gradlew build --no-daemon --offline`; it again stopped before compilation because Gradle 8.8 is not cached and `services.gradle.org` is unreachable. No Minecraft client was launched, so this is not a Java compile, GLSL driver compile, or in-game render verification.
