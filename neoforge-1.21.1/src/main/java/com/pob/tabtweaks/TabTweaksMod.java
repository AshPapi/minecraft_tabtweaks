package com.pob.tabtweaks;

import java.util.List;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.slf4j.Logger;

@Mod(TabTweaksMod.MOD_ID)
public class TabTweaksMod {

    public static final String MOD_ID = "tabtweaks";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static List<TabRule> rules = List.of();

    public static List<TabRule> rules() {
        return rules;
    }

    public static void reload() {
        rules = TabConfig.load();
        TabTweaks.apply(rules);
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class ClientModSetup {

        private ClientModSetup() {
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            TabConfig.ensureConfigExists();
        }
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static final class ClientHooks {

        private ClientHooks() {
        }

        @SubscribeEvent
        public static void onScreenOpen(ScreenEvent.Opening event) {
            if (event.getNewScreen() instanceof CreativeModeInventoryScreen) {
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
