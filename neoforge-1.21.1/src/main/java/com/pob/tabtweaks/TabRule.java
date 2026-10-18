package com.pob.tabtweaks;

import javax.annotation.Nullable;

public final class TabRule {

    public static final int SLOTS_PER_ROW = 5;
    public static final int SLOTS_PER_PAGE = SLOTS_PER_ROW * 2;

    public final String id;
    @Nullable
    public final String icon;
    public final int page;
    public final int slot;
    public final boolean hidden;
    @Nullable
    public final String name;

    public TabRule(String id, @Nullable String icon, int page, int slot, boolean hidden,
            @Nullable String name) {
        this.id = id;
        this.icon = icon;
        this.page = page;
        this.slot = slot;
        this.hidden = hidden;
        this.name = name;
    }

    public boolean movesTab() {
        return this.page > 0;
    }

    public int targetIndex() {
        int slotIndex = this.slot > 0 ? this.slot - 1 : 0;
        return (this.page - 1) * SLOTS_PER_PAGE + slotIndex;
    }

    @Override
    public String toString() {
        return "TabRule[" + this.id + (this.hidden ? " hidden" : "")
                + (this.name != null ? " name=" + this.name : "")
                + (this.icon != null ? " icon=" + this.icon : "")
                + (this.movesTab() ? " page=" + this.page + " slot=" + this.slot : "") + "]";
    }
}
