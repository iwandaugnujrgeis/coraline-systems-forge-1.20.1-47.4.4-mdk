package net.zharok01.coralinesystems.client.gui.creative_tabs;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CoralineTabDefinition {

    @SerializedName("replace")
    private boolean replace;

    @SerializedName("tab_icon")
    private String tabIcon;

    @SerializedName("tab_icon_nbt")
    private String tabIconNbt;

    @SerializedName("tab_items")
    private List<TabItemEntry> tabItems;

    @SerializedName("disabled_tabs")
    private List<String> disabledTabs;

    public boolean isReplace() { return replace; }
    public String getTabIcon() { return tabIcon; }
    public String getTabIconNbt() { return tabIconNbt; }
    public List<TabItemEntry> getTabItems() { return tabItems != null ? tabItems : List.of(); }
    public List<String> getDisabledTabs() { return disabledTabs != null ? disabledTabs : List.of(); }

    public static class TabItemEntry {

        @SerializedName("item")
        private String item;

        @SerializedName("hide_from_original")
        private boolean hideFromOriginal;

        @SerializedName("nbt")
        private String nbt;

        public String getItem() { return item; }
        public boolean isHideFromOriginal() { return hideFromOriginal; }
        public String getNbt() { return nbt; }
    }
}