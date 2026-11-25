# Barkly Chem Lab Research

A Forge 1.20.1 mod for fictional, Minecraft-only research gameplay. The uploaded pack already contains extensive implementations for cannabis products, LSD, DMT, mushrooms, salvia, cocaine, methamphetamine, MDMA, opioids, nicotine/tobacco, and medical items. This mod complements those systems with abstract research materials, a dedicated processing workbench, and fictional experience-profile artifacts for underrepresented categories. It does not add real-world drug recipes, preparation, dosage, or pharmacology.

## Compatibility

- Minecraft 1.20.1
- Forge 47.4.28
- Java 17
- JEI is optional. When JEI is installed, the Research Workbench recipe category and catalyst are registered.
- Addictives, Narcosis, Sillyism, Deadout Medical Supplies, and the other pack mods are not required dependencies. The new recipes use vanilla Minecraft inputs so missing optional mods do not break recipe loading.

## Build

1. Install a Java 17 JDK.
2. Run `./gradlew build` on Linux/macOS or `gradlew.bat build` on Windows.
3. Find the reobfuscated mod JAR at `build/libs/barklychemlab-0.1.0.jar`.
4. Install the JAR on both client and server in the `mods/` folder. JEI integration activates automatically when JEI is present.

## Gameplay

1. Craft a **Research Workbench** from iron, a crafting table, and purple dye.
2. Craft exploration materials from Minecraft finds: amethyst, glowstone, glass, glow ink, flowers, string, and phantom membrane.
3. Right-click the workbench. Place two compatible research materials in the two input slots.
4. Watch the processing meter. The server consumes one of each input only when processing finishes, then places the output in the protected output slot.
5. Collect **Insight Fragments** and craft profile artifacts. Profile artifacts are reusable and cooldown-limited; using one does not consume it or increase its strength.
6. Research advancements unlock from possessing items, reaching Nether/End field sites, and collecting profile artifacts. They do not require consuming substances.

The workbench inventory and in-progress work are saved with the block entity. Processing is server-authoritative, input/output slots are validated, the output slot cannot be filled by the player, and breaking the workbench drops its contents.

## Item ecosystem

The mod registers 30 items and one block. Each item has a registry ID, English name, inventory texture, tooltip, stack limit, recipe use, and creative-tab placement. JEI displays the registered items and the custom workbench recipes.

- **Equipment and records:** Field Notebook, Sample Vial, Calibration Lens.
- **Exploration and botanical materials:** Echo Shard, Lunar Glass, Sky Silk, Prism Pollen, Star Petals.
- **Intermediate research materials:** Resonance Core, Insight Fragment.
- **Starter fantasy profiles:** Lumen Bloom, Echo Prism, Orbit Marble, Hush Chime, Warmth Crystal.
- **Additional uncovered-category profiles:** Kratom, Dextromethorphan (DXM), Ketamine, Nitrous Oxide, Mescaline, 2C-B, Mephedrone, GHB, Benzodiazepine, Barbiturate, Oxycodone, Diphenhydramine, Alcohol, PCP, and Methylphenidate. Their names use the actual category/substance names; effects, recipes, and messages remain fictional Minecraft content.
- **Equipment:** Research Workbench.

The additional profiles are fictional Minecraft artifacts. Their short vanilla effects and playful messages do not claim to represent real substances.

### Texture atlas and references

Every registered item has its own generated 16×16 pixel-art texture. The 15 named substance items use physical-form references such as dried leaves, tablets, capsules, crystalline powders, clear liquid, and a gas cylinder; the other items have distinct laboratory or fictional-material silhouettes. See [TEXTURE_MAPPING.md](TEXTURE_MAPPING.md) for the complete registry-to-file mapping and the visual reference rationale for each item.

## Configuration and accessibility

After first launch, edit `config/barklychemlab-common.toml`:

- `fictional_experiences.enabled`: enable or disable the status effects on profile artifacts.
- `fictional_experiences.durationTicks`: shared profile duration setting, 20–6000 ticks.
- `fictional_experiences.cooldownTicks`: profile reuse delay, 0–72000 ticks.
- `accessibility.advancementReactions`: enable or disable achievement reactions.
- `accessibility.reactionSounds`: enable or disable the quiet chime (off by default).
- `accessibility.profileParticles`: enable or disable decorative particles.

There are no shaders, camera distortions, or forced audio effects. The vanilla status effects, durations, particles, and achievement sounds can be reduced or disabled.

## Progression, challenges, and reactions

The advancement chain includes field notes, calibration, first insight, collecting the starter profiles, collecting the expanded archive, and cross-dimensional fieldwork. The archive challenge checks inventory possession of each profile item; it does not require use. On a research challenge, the server-side advancement event picks one of three distinct, localized Barkly reactions. Reactions are emitted once when the advancement is earned and can be switched off in configuration.

## Extending the mod

### Add a component

1. Register a `RegistryObject<Item>` in `registry/ModItems.java` with the component helper.
2. Add its creative-tab entry in `registry/ModCreativeTabs.java`.
3. Add an item model, PNG texture, English display name, and tooltip.
4. Add a vanilla JSON recipe under `data/barklychemlab/recipes/`.
5. Add an advancement criterion or recipe unlock if it advances research.

### Add a workbench recipe

Add a JSON recipe with type `barklychemlab:research`, two `first` and `second` Ingredient objects, a `result`, and a `work` duration in ticks (20–1200). Ingredients are consumed only on successful completion. The custom serializer and recipe type are registered in `recipe/ModRecipes.java`.

### Add a profile artifact

Register an `ExperienceTokenItem` in `ModItems.java`, select a vanilla `MobEffect`, localized message key, duration scale, and vanilla particle. Add its workbench recipe, item model/texture, name, tooltip, creative-tab entry, and inventory-based advancement criterion. Profile artifacts are reusable; do not add escalating or real-world preparation mechanics.

### Add achievement reactions

Add a message pool in `event/AdvancementReactions.java` keyed by the advancement path and translate each line in `assets/barklychemlab/lang/en_us.json`. The handler runs on the logical server and supports independent sound and reaction settings.

## Audit and known limits

`EXISTING_MOD_AUDIT.md` records the actual pack versions, localized content and overlap decisions. The mod intentionally does not duplicate the pack's existing cannabis, LSD, DMT, mushroom, salvia, cocaine, methamphetamine, MDMA, heroin, morphine, fentanyl, nicotine/tobacco, or Benadryl items. It also does not attempt to represent every candidate with real-substance items; uncovered categories are represented as abstract research profiles.

The profile effects are short vanilla status effects rather than custom shaders, camera changes, ambient soundscapes, or environmental hallucinations. There are no direct Create or Ad Astra item dependencies; dimension progression uses vanilla Nether/End travel to remain compatible if those optional mods change. Full client/server multiplayer runtime testing still requires launching the built mod inside a Minecraft instance.
