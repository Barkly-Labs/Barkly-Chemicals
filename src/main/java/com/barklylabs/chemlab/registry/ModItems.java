package com.barklylabs.chemlab.registry;

import com.barklylabs.chemlab.BarklyChemLab;
import com.barklylabs.chemlab.item.ExperienceTokenItem;
import com.barklylabs.chemlab.item.ResearchComponentItem;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BarklyChemLab.MOD_ID);

    // Equipment and documentation
    public static final RegistryObject<Item> FIELD_NOTEBOOK = component("field_notebook", "tooltip.barklychemlab.field_notebook", 1);
    public static final RegistryObject<Item> SAMPLE_VIAL = component("sample_vial", "tooltip.barklychemlab.sample_vial", 16);
    public static final RegistryObject<Item> CALIBRATION_LENS = component("calibration_lens", "tooltip.barklychemlab.calibration_lens", 16);

    // Exploration materials: recipes use vanilla finds so the pack stays optional-mod friendly.
    public static final RegistryObject<Item> ECHO_SHARD = component("echo_shard", "tooltip.barklychemlab.echo_shard", 64);
    public static final RegistryObject<Item> LUNAR_GLASS = component("lunar_glass", "tooltip.barklychemlab.lunar_glass", 64);
    public static final RegistryObject<Item> SKY_SILK = component("sky_silk", "tooltip.barklychemlab.sky_silk", 64);
    public static final RegistryObject<Item> PRISM_POLLEN = component("prism_pollen", "tooltip.barklychemlab.prism_pollen", 64);
    public static final RegistryObject<Item> STAR_PETALS = component("star_petals", "tooltip.barklychemlab.star_petals", 64);

    // Processing and research outputs
    public static final RegistryObject<Item> RESONANCE_CORE = component("resonance_core", "tooltip.barklychemlab.resonance_core", 16);
    public static final RegistryObject<Item> INSIGHT_FRAGMENT = component("insight_fragment", "tooltip.barklychemlab.insight_fragment", 64);

    // Reusable, non-consumable fantasy experience tokens. Use is cooldown-limited and configurable.
    public static final RegistryObject<Item> LUMEN_BLOOM = profile("lumen_bloom", MobEffects.NIGHT_VISION, "lumen", 30, ParticleTypes.WAX_ON);
    public static final RegistryObject<Item> ECHO_PRISM = profile("echo_prism", MobEffects.GLOWING, "echo", 10, ParticleTypes.END_ROD);
    public static final RegistryObject<Item> ORBIT_MARBLE = profile("orbit_marble", MobEffects.SLOW_FALLING, "orbit", 10, ParticleTypes.CLOUD);
    public static final RegistryObject<Item> HUSH_CHIME = profile("hush_chime", MobEffects.DOLPHINS_GRACE, "hush", 10, ParticleTypes.BUBBLE);
    public static final RegistryObject<Item> WARMTH_CRYSTAL = profile("warmth_crystal", MobEffects.HERO_OF_THE_VILLAGE, "warmth", 10, ParticleTypes.HEART);

    // Additional profiles cover candidate areas absent from the supplied modpack. They are fictional
    // experience artifacts, not substance items or representations of real pharmacology.
    public static final RegistryObject<Item> KRATOM_ARCHIVE = profile("kratom_archive", MobEffects.REGENERATION, "profile.kratom", 10, ParticleTypes.HAPPY_VILLAGER);
    public static final RegistryObject<Item> DXM_ECHO = profile("dxm_echo", MobEffects.GLOWING, "profile.dxm", 10, ParticleTypes.REVERSE_PORTAL);
    public static final RegistryObject<Item> KETAMINE_GEOMETRY = profile("ketamine_geometry", MobEffects.SLOW_FALLING, "profile.ketamine", 10, ParticleTypes.END_ROD);
    public static final RegistryObject<Item> NITROUS_CLOUD = profile("nitrous_cloud", MobEffects.JUMP, "profile.nitrous", 10, ParticleTypes.CLOUD);
    public static final RegistryObject<Item> MESCALINE_SUN = profile("mescaline_sun", MobEffects.NIGHT_VISION, "profile.mescaline", 15, ParticleTypes.WAX_ON);
    public static final RegistryObject<Item> TWOCB_PRISM = profile("twocb_prism", MobEffects.LUCK, "profile.twocb", 10, ParticleTypes.CHERRY_LEAVES);
    public static final RegistryObject<Item> MEPHEDRONE_SPARK = profile("mephedrone_spark", MobEffects.MOVEMENT_SPEED, "profile.mephedrone", 10, ParticleTypes.ELECTRIC_SPARK);
    public static final RegistryObject<Item> GHB_TIDE = profile("ghb_tide", MobEffects.DOLPHINS_GRACE, "profile.ghb", 10, ParticleTypes.BUBBLE);
    public static final RegistryObject<Item> BENZODIAZEPINE_LANTERN = profile("benzodiazepine_lantern", MobEffects.SLOW_FALLING, "profile.benzodiazepine", 10, ParticleTypes.ASH);
    public static final RegistryObject<Item> BARBITURATE_ARCHIVE = profile("barbiturate_archive", MobEffects.DAMAGE_RESISTANCE, "profile.barbiturate", 10, ParticleTypes.SOUL);
    public static final RegistryObject<Item> OXYCODONE_MOON = profile("oxycodone_moon", MobEffects.ABSORPTION, "profile.oxycodone", 10, ParticleTypes.SOUL_FIRE_FLAME);
    public static final RegistryObject<Item> DIPHENHYDRAMINE_MOTH = profile("diphenhydramine_moth", MobEffects.INVISIBILITY, "profile.diphenhydramine", 5, ParticleTypes.ASH);
    public static final RegistryObject<Item> ALCOHOL_CAMPFIRE = profile("alcohol_campfire", MobEffects.FIRE_RESISTANCE, "profile.alcohol", 10, ParticleTypes.FLAME);
    public static final RegistryObject<Item> PCP_MIRROR = profile("pcp_mirror", MobEffects.LUCK, "profile.pcp", 10, ParticleTypes.REVERSE_PORTAL);
    public static final RegistryObject<Item> METHYLPHENIDATE_CLOCKWORK = profile("methylphenidate_clockwork", MobEffects.DIG_SPEED, "profile.methylphenidate", 10, ParticleTypes.ELECTRIC_SPARK);

    private static RegistryObject<Item> component(String id, String tooltip, int stackSize) {
        return ITEMS.register(id, () -> new ResearchComponentItem(new Item.Properties().stacksTo(stackSize), tooltip));
    }
    private static RegistryObject<Item> profile(String id, MobEffect effect, String messageSuffix, int durationScale, net.minecraft.core.particles.ParticleOptions particles) {
        return ITEMS.register(id, () -> new ExperienceTokenItem(new Item.Properties().stacksTo(1), effect, "message.barklychemlab." + messageSuffix, durationScale, particles));
    }
    private ModItems() { }
}
