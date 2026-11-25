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
