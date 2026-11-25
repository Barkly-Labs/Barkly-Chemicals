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
- Verified all 30 registered items map to distinct 16×16 RGBA textures; texture hashes are unique and every item model points to its matching texture file.
- Added `TEXTURE_MAPPING.md` with the full registry-to-texture table, per-icon visual rationale, and physical-appearance references for the 15 named substances.
- Verified the ZIP archive after packaging.

## Runtime coverage and limits

- The supplied mod JAR archive was inspected for overlapping items and recipes, but no Minecraft instance was launched in this workspace.
- Block placement, GUI interaction, NBT persistence, recipe completion, advancement notifications, JEI rendering, and multiplayer synchronization therefore have compile/static validation only; they have not been observed in-game.
- Inventory texture silhouettes were reviewed from a contact sheet at 16×16 scale. This verifies the asset files and mapping, not the live Minecraft renderer.
- The workbench logic is server-side and uses Forge's standard menu/slot/container-data synchronization, but final playtesting remains necessary before treating the JAR as production-ready.
