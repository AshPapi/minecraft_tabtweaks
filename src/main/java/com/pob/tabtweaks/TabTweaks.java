package com.pob.tabtweaks;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.CreativeModeTabRegistry;
import net.minecraftforge.registries.ForgeRegistries;

public final class TabTweaks {

    private static Field sortedTabsField;
    private static boolean fieldResolved;
    private static Field iconField;
    private static boolean iconFieldResolved;

    private TabTweaks() {
    }

    public static void apply(List<TabRule> rules) {
        if (rules.isEmpty()) {
            return;
        }

        List<CreativeModeTab> tabs = mutableSortedTabs();
        if (tabs == null) {
            return;
        }

        Map<String, TabRule> byId = new LinkedHashMap<>();
        for (TabRule rule : rules) {
            byId.put(rule.id, rule);
        }

        applyIcons(byId);
        reorder(tabs, rules, byId);
    }

    private static void applyIcons(Map<String, TabRule> byId) {
        for (TabRule rule : byId.values()) {
            if (rule.icon == null || rule.icon.isBlank()) {
                continue;
            }
            CreativeModeTab tab = findTab(rule.id);
            if (tab == null) {
                TabTweaksMod.LOGGER.warn("Вкладка {} не найдена — иконка не заменена", rule.id);
                continue;
            }
            Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(rule.icon));
            if (item == null) {
                TabTweaksMod.LOGGER.warn("Предмет {} не найден — иконка вкладки {} оставлена прежней",
                        rule.icon, rule.id);
                continue;
            }
            setIcon(tab, new ItemStack(item));
        }
    }

    private static void setIcon(CreativeModeTab tab, ItemStack stack) {
        if (!iconFieldResolved) {
            iconFieldResolved = true;
            for (Field field : CreativeModeTab.class.getDeclaredFields()) {
                if (field.getType() == ItemStack.class) {
                    field.setAccessible(true);
                    iconField = field;
                    break;
                }
            }
            if (iconField == null) {
                TabTweaksMod.LOGGER.error("В CreativeModeTab нет поля типа ItemStack — "
                        + "иконки менять не получится");
            }
        }
        if (iconField == null) {
            return;
        }
        try {
            iconField.set(tab, stack);
        } catch (ReflectiveOperationException e) {
            TabTweaksMod.LOGGER.warn("Не удалось заменить иконку вкладки {}", nameOf(tab), e);
        }
    }

    private static void reorder(List<CreativeModeTab> tabs, List<TabRule> rules, Map<String, TabRule> byId) {
        List<CreativeModeTab> rest = new ArrayList<>();
        List<CreativeModeTab> placed = new ArrayList<>();
        Map<CreativeModeTab, TabRule> moving = new LinkedHashMap<>();

        for (CreativeModeTab tab : tabs) {
            TabRule rule = byId.get(nameOf(tab));
            if (rule != null && rule.hidden) {
                continue;
            }
            if (rule != null && rule.movesTab()) {
                moving.put(tab, rule);
            } else {
                rest.add(tab);
            }
        }

        placed.addAll(rest);

        moving.entrySet().stream()
                .sorted((a, b) -> Integer.compare(a.getValue().targetIndex(), b.getValue().targetIndex()))
                .forEach(entry -> {
                    int index = Math.min(entry.getValue().targetIndex(), placed.size());
                    placed.add(index, entry.getKey());
                });

        tabs.clear();
        tabs.addAll(placed);

        TabTweaksMod.LOGGER.info("Вкладки переставлены: {} правил, {} вкладок в списке",
                rules.size(), tabs.size());
    }

    @Nullable
    public static CreativeModeTab findTab(String id) {
        ResourceLocation key = ResourceLocation.tryParse(id);
        return key == null ? null : CreativeModeTabRegistry.getTab(key);
    }

    public static String nameOf(CreativeModeTab tab) {
        ResourceLocation name = CreativeModeTabRegistry.getName(tab);
        return name == null ? "?" : name.toString();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static List<CreativeModeTab> mutableSortedTabs() {
        if (!fieldResolved) {
            fieldResolved = true;
            try {
                sortedTabsField = CreativeModeTabRegistry.class.getDeclaredField("SORTED_TABS");
                sortedTabsField.setAccessible(true);
            } catch (ReflectiveOperationException e) {
                TabTweaksMod.LOGGER.error("CreativeModeTabRegistry.SORTED_TABS недоступен — "
                        + "порядок вкладок менять не получится", e);
                return null;
            }
        }
        if (sortedTabsField == null) {
            return null;
        }
        try {
            return (List<CreativeModeTab>) sortedTabsField.get(null);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
