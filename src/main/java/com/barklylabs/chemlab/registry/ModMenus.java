package com.barklylabs.chemlab.registry;

import com.barklylabs.chemlab.BarklyChemLab;
import com.barklylabs.chemlab.menu.ResearchWorkbenchMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, BarklyChemLab.MOD_ID);
    public static final RegistryObject<MenuType<ResearchWorkbenchMenu>> RESEARCH = TYPES.register("research_workbench", () -> IForgeMenuType.create(ResearchWorkbenchMenu::new));
    private ModMenus() { }
}
