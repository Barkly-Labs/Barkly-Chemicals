# Barkly Chem Lab Research — Existing Mod Audit

## Pack and method

The supplied manifest targets Minecraft 1.20.1 and Forge 47.4.28. The attached `mods.zip` contained the actual JARs. The audit below uses the requested four mods' `en_us.json` names, packaged recipe JSON, block names, and effect names. It also checks the rest of the supplied pack's English localization files for roster terms. This is a resource-level audit; it does not claim to reverse engineer every runtime behavior in each mod.

## Requested audit targets

| Mod JAR | Items | Blocks | Effects | JSON recipes | Summary |
|---|---:|---:|---:|---:|---|
| Addictives 4.0.5 | 72 | 19 | 20 | 76 | Broad set of cannabis/tobacco/cocaine/crack/meth/DMT/LSD/mushroom/opioid items, crop blocks, processing recipes, effects, and existing advancements. |
| Narcosis 1.5.2 | 85 | 13 | 12 | 37 | Cannabis variants, cocaine, mushrooms, tobacco, meth, salvia, custom processing blocks, and several existing status effects. |
| Sillyism 1.0.1 | 17 | 3 | 6 | 16 | Debilitum-themed items/effects and a separate humorous fictional system. |
| Deadout Medical Supplies 1.0.1 | 20 | 1 | 7 | 2 | Medical supplies, herbs, treatments, survival status effects, and a herbal plant; not a general substance-research system. |

The pack also includes Simple Drugs, CQTZS Drugs, and Salami's Substances Illicit, which further overlap with cannabis, cocaine, methamphetamine, heroin, morphine, fentanyl, and related items. The pack's OpenLoader `CYNX-Advancements` report documents 184 unique item IDs and 2,576 item-by-dimension advancement entries. This mod uses its own `barklychemlab` namespace and adds no competing dimension-wide item tree.

## Overlap and gap decisions

**Already represented; no new counterpart items were added:** cannabis/THC products, salvia, LSD/acid, DMT, magic mushrooms, cocaine/crack, methamphetamine, MDMA, Adderall/amphetamine, heroin, morphine, fentanyl, nicotine/tobacco, and Benadryl/diphenhydramine. Existing pack items, recipes, effects, and processing blocks are left intact.

**No matching substance item names were found in the supplied English item localizations for these candidate areas:** Kratom, Dextromethorphan (DXM), Ketamine, Nitrous Oxide, Mescaline, 2C-B, Mephedrone, GHB, Oxycodone, PCP, Methylphenidate, and standalone alcohol. For these uncovered areas the mod adds items whose *display names are the actual substance names*, with a short fictional experience profile attached. These are inventory artifacts in Minecraft; they are not consumables and their names do not imply real-world effects or instructions.

**Intentionally not implemented:** duplicate items for substances already present, real-world recipes or precursors, drug preparation, dose/combination mechanics, escalating effects, dependence, overdose rewards, or claims that vanilla effects reproduce pharmacology. No separate Datura item was added; its eerie/uncanny creative direction is covered by the fictional Diphenhydramine/Mothlight and PCP/Mirror Corridor experience themes without asserting equivalence.

## Implemented system

- 30 registered items: ten research materials plus twenty profile artifacts. Each item has an ID, inventory model, distinct PNG texture, English name, stack limit, tooltip, and creative-tab location.
- One registered Research Workbench block and block entity with a custom three-slot GUI, custom processing recipe type, progress display, persistent inputs/output/progress, server-side processing, slot validation, and content drops on block removal.
- 31 recipes: ten vanilla crafting recipes for the workbench/components and 21 custom workbench research recipes. Processing consumes one matching input from each input slot only when the result can fit and the work completes.
- Seven advancements: item-based research milestones, an all-profile archive challenge, and Nether/End exploration challenges. Research advancement criteria do not require consumption.
- Randomized localized challenge reactions, with independent config for message, sound, profile duration/effects, and particles.
- Optional JEI integration: custom recipe category and Workbench catalyst. The Gradle resource task generates a JEI catalog from the data recipes for the title-screen case where there is no server recipe manager.

## Integration limits

The mod avoids hard dependencies on Create, Ad Astra, or other pack mods. It uses vanilla materials, and dimension research currently uses vanilla Nether/End travel. JEI is the only optional runtime integration. This keeps the pack loadable if optional mod versions change, but it means there are no Create-machine processing recipes or Ad Astra-specific challenges yet.

## Verification status

See `TESTING_REPORT.md`. The Java source compiled and reobfuscated successfully with Java 17 and Gradle 8.8. Static resource checks passed. No Minecraft client/server or multiplayer game session was launched in this environment, so interaction and visual behavior still need in-game validation.
