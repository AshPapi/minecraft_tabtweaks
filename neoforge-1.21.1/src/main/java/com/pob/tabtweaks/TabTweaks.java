package com.pob.tabtweaks;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.CreativeModeTabRegistry;

public final class TabTweaks {

    private static Field sortedTabsField;
    private static boolean fieldResolved;
    private static Field iconField;
    private static boolean iconFieldResolved;
    private static Field nameField;
    private static boolean nameFieldResolved;

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

        Map<String, TabRule> byId = TabLayout.indexById(rules);

        applyIcons(byId);
        applyNames(byId);

        List<CreativeModeTab> placed = TabLayout.reorder(tabs, byId, TabTweaks::nameOf);
        tabs.clear();
        tabs.addAll(placed);

        TabTweaksMod.LOGGER.info("Вкладки переставлены: {} правил, {} вкладок в списке",
                rules.size(), tabs.size());
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
            ResourceLocation iconId = ResourceLocation.tryParse(rule.icon);
            Item item = iconId == null ? null : BuiltInRegistries.ITEM.getOptional(iconId).orElse(null);
            if (item == null) {
                TabTweaksMod.LOGGER.warn("Предмет {} не найден — иконка вкладки {} оставлена прежней",
                        rule.icon, rule.id);
                continue;
            }
            setIcon(tab, new ItemStack(item));
        }
    }

    private static void applyNames(Map<String, TabRule> byId) {
        for (TabRule rule : byId.values()) {
            if (rule.name == null || rule.name.isBlank()) {
                continue;
            }
            CreativeModeTab tab = findTab(rule.id);
            if (tab == null) {
                TabTweaksMod.LOGGER.warn("Вкладка {} не найдена - название не заменено", rule.id);
                continue;
            }
            setName(tab, Component.literal(rule.name));
        }
    }

    static void setName(CreativeModeTab tab, Component name) {
        if (!nameFieldResolved) {
            nameFieldResolved = true;
            for (Field field : CreativeModeTab.class.getDeclaredFields()) {
                if (field.getType() == Component.class && !java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    nameField = field;
                    break;
                }
            }
            if (nameField == null) {
                TabTweaksMod.LOGGER.error("В CreativeModeTab нет поля типа Component - "
                        + "названия менять не получится");
            }
        }
        if (nameField == null) {
            return;
        }
        try {
            nameField.set(tab, name);
        } catch (ReflectiveOperationException e) {
            TabTweaksMod.LOGGER.warn("Не удалось заменить название вкладки {}", nameOf(tab), e);
        }
    }

    static void setIcon(CreativeModeTab tab, ItemStack stack) {
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
