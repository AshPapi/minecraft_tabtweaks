package com.pob.tabtweaks;

import java.util.List;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;

@Mod(TabTweaksMod.MOD_ID)
public class TabTweaksMod {

    public static final String MOD_ID = "tabtweaks";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static List<TabRule> rules = List.of();

    public TabTweaksMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static List<TabRule> rules() {
        return rules;
    }

    public static void reload() {
        rules = TabConfig.load();
        TabTweaks.apply(rules);
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ClientModSetup {

        private ClientModSetup() {
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            TabConfig.ensureConfigExists();
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static final class ClientHooks {

        private ClientHooks() {
        }

        @SubscribeEvent
        public static void onScreenOpen(ScreenEvent.Opening event) {
            if (event.getNewScreen() instanceof net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen) {
                reload();
            }
        }
    }

    public static String describe(CreativeModeTab tab, int index) {
        int page = index / TabRule.SLOTS_PER_PAGE + 1;
        int slot = index % TabRule.SLOTS_PER_PAGE + 1;
        return TabTweaks.nameOf(tab) + "  —  страница " + page + ", слот " + slot;
    }
}
