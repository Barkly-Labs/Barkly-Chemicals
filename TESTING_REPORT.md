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
