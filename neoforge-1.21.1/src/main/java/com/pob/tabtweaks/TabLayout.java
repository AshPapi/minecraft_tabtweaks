package com.pob.tabtweaks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class TabLayout {

    private TabLayout() {
    }

    public static Map<String, TabRule> indexById(List<TabRule> rules) {
        Map<String, TabRule> byId = new LinkedHashMap<>();
        for (TabRule rule : rules) {
            byId.put(rule.id, rule);
        }
        return byId;
    }

    public static <T> List<T> reorder(List<T> tabs, Map<String, TabRule> byId, Function<T, String> idOf) {
        List<T> placed = new ArrayList<>();
        Map<T, TabRule> moving = new LinkedHashMap<>();

        for (T tab : tabs) {
            TabRule rule = byId.get(idOf.apply(tab));
            if (rule != null && rule.hidden) {
                continue;
            }
            if (rule != null && rule.movesTab()) {
                moving.put(tab, rule);
            } else {
                placed.add(tab);
            }
        }

        moving.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getValue().targetIndex()))
                .forEach(entry -> {
                    int index = Math.min(entry.getValue().targetIndex(), placed.size());
                    placed.add(index, entry.getKey());
                });

        return placed;
    }
}
