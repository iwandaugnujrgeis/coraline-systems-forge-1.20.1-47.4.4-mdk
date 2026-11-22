package net.zharok01.coralinesystems.client.gui.creative_tabs;

import com.google.gson.Gson;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.mixin.accessor.CreativeModeTabAccessor;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("EmptyCatchBlock")
public final class CoralineCreativeTabLoader {

    public static final CoralineCreativeTabLoader INSTANCE = new CoralineCreativeTabLoader();

    private static final String SCAN_FOLDER = "creative_tabs";
    private static final String DISABLED_TABS_FILE = "disabled_tabs.json";
    private static final Gson GSON = new Gson();

    private final Map<String, List<ItemStack>> replacementTemplates = new HashMap<>();
    private final Map<String, ItemStack> replacementIcons = new HashMap<>();
    private final Set<Item> globallyHiddenItems = new HashSet<>();
    private final Set<String> disabledTabKeys = new HashSet<>();
    private boolean wasReloaded = false;

    private CoralineCreativeTabLoader() {}

    public void reload() {
        replacementTemplates.clear();
        replacementIcons.clear();
        globallyHiddenItems.clear();
        disabledTabKeys.clear();

        ResourceManager rm = Minecraft.getInstance().getResourceManager();

        @SuppressWarnings("removal")
        ResourceLocation disabledLoc = new ResourceLocation(
                CoralineSystems.MOD_ID, SCAN_FOLDER + "/" + DISABLED_TABS_FILE);
        rm.getResource(disabledLoc).ifPresent(res -> {
            try (var reader = new InputStreamReader(res.open())) {
                CoralineTabDefinition def = GSON.fromJson(reader, CoralineTabDefinition.class);
                disabledTabKeys.addAll(def.getDisabledTabs());
            } catch (Exception e) {
            }
        });

        Map<ResourceLocation, Resource> overrides = rm.listResources(
                SCAN_FOLDER,
                path -> path.getNamespace().equals(CoralineSystems.MOD_ID)
                        && path.getPath().endsWith(".json")
                        && !path.getPath().endsWith(DISABLED_TABS_FILE)
        );

        for (Map.Entry<ResourceLocation, Resource> entry : overrides.entrySet()) {
            processOverrideFile(entry.getKey(), entry.getValue());
        }

        overrideSearchTabGenerator();

        wasReloaded = true;
    }

    private void overrideSearchTabGenerator() {
        CreativeModeTab searchTab = BuiltInRegistries.CREATIVE_MODE_TAB.get(
                CreativeModeTabs.SEARCH.location());
        if (searchTab == null) {
            return;
        }

        ((CreativeModeTabAccessor) searchTab).coraline$setDisplayItemsGenerator(
                (params, output) -> {
                    Set<ItemStack> stacks = ItemStackLinkedSet.createTypeAndTagSet();
                    BuiltInRegistries.CREATIVE_MODE_TAB.holders()
                            .filter(h -> h.key().location().getNamespace().equals("minecraft"))
                            .filter(h -> !h.key().equals(CreativeModeTabs.OP_BLOCKS))
                            .map(net.minecraft.core.Holder::value)
                            .filter(tab -> tab.getType() != CreativeModeTab.Type.SEARCH)
                            .forEach(tab -> stacks.addAll(tab.getSearchTabDisplayItems()));
                    output.acceptAll(stacks);
                });
    }

    public boolean hasReplacement(CreativeModeTab tab) {
        return replacementTemplates.containsKey(normalisedKey(tab));
    }

    public boolean isDisabled(CreativeModeTab tab) {
        String key = rawTranslationKey(tab.getDisplayName());
        return disabledTabKeys.contains(key);
    }

    @Nullable
    public ItemStack getReplacementIcon(CreativeModeTab tab) {
        return replacementIcons.get(normalisedKey(tab));
    }

    public boolean wasReloaded() { return wasReloaded; }
    public void clearReloadedFlag() { wasReloaded = false; }

    public Collection<ItemStack> buildFinalItemList(CreativeModeTab tab,
                                                    CreativeModeTab.ItemDisplayParameters parameters) {
        List<ItemStack> template = replacementTemplates.getOrDefault(normalisedKey(tab), List.of());
        if (template.isEmpty()) return List.of();

        boolean hasSentinel = template.stream().anyMatch(ItemStack::isEmpty);

        if (!hasSentinel) {
            return template;
        }

        Collection<ItemStack> vanillaBaseItems = runVanillaGeneratorOnly(tab, parameters);

        Collection<ItemStack> filtered = vanillaBaseItems.stream()
                .filter(s -> !globallyHiddenItems.contains(s.getItem()))
                .collect(Collectors.toCollection(ItemStackLinkedSet::createTypeAndTagSet));

        Collection<ItemStack> result = ItemStackLinkedSet.createTypeAndTagSet();
        for (ItemStack entry : template) {
            if (entry.isEmpty()) {
                result.addAll(filtered);
            } else {
                result.add(entry);
            }
        }
        return result;
    }

    private static Collection<ItemStack> runVanillaGeneratorOnly(
            CreativeModeTab tab,
            CreativeModeTab.ItemDisplayParameters parameters) {

        Collection<ItemStack> collector = ItemStackLinkedSet.createTypeAndTagSet();

        CreativeModeTab.DisplayItemsGenerator generator =
                ((CreativeModeTabAccessor) tab).coraline$getDisplayItemsGenerator();

        CreativeModeTab.Output output = (stack, visibility) -> {
            if (!stack.isEmpty() && stack.getItem().isEnabled(parameters.enabledFeatures())) {
                collector.add(stack);
            }
        };

        try {
            generator.accept(parameters, output);
        } catch (Exception e) {
        }

        return collector;
    }

    private void processOverrideFile(ResourceLocation location, Resource resource) {
        String path = location.getPath();
        String filename = path.substring(path.lastIndexOf('/') + 1);
        String normKey = filename.replace(".json", "").toLowerCase(Locale.ROOT);

        try (var reader = new InputStreamReader(resource.open())) {
            CoralineTabDefinition def = GSON.fromJson(reader, CoralineTabDefinition.class);

            if (!def.isReplace()) {
                return;
            }

            if (def.getTabIcon() != null && !def.getTabIcon().isBlank()) {
                ItemStack icon = resolveItem(def.getTabIcon());
                if (!icon.isEmpty()) {
                    applyNbt(icon, def.getTabIconNbt());
                    replacementIcons.put(normKey, icon);
                }
            }

            List<ItemStack> template = new ArrayList<>();
            for (CoralineTabDefinition.TabItemEntry entry : def.getTabItems()) {
                if ("existing".equalsIgnoreCase(entry.getItem())) {
                    template.add(ItemStack.EMPTY);
                    continue;
                }
                ItemStack stack = resolveItem(entry.getItem());
                if (stack.isEmpty()) {
                    continue;
                }
                applyNbt(stack, entry.getNbt());
                if (entry.isHideFromOriginal()) {
                    globallyHiddenItems.add(stack.getItem());
                }
                template.add(stack);
            }

            replacementTemplates.put(normKey, template);

        } catch (Exception e) {
        }
    }

    private static void applyNbt(ItemStack stack, @Nullable String nbtString) {
        if (nbtString == null || nbtString.isBlank()) return;
        try {
            CompoundTag tag = TagParser.parseTag(nbtString);
            stack.setTag(tag);
            if (tag.contains("customName")) {
                stack.setHoverName(Component.literal(tag.getString("customName")));
            }
        } catch (CommandSyntaxException e) {
        }
    }

    @SuppressWarnings("removal")
    private static ItemStack resolveItem(String id) {
        try {
            return BuiltInRegistries.ITEM.getOptional(new ResourceLocation(id))
                    .map(Item::getDefaultInstance)
                    .orElse(ItemStack.EMPTY);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    public static String normalisedKey(CreativeModeTab tab) {
        return rawTranslationKey(tab.getDisplayName())
                .replace(".", "_")
                .toLowerCase(Locale.ROOT);
    }

    private static String rawTranslationKey(Component component) {
        if (component.getContents() instanceof TranslatableContents tc) {
            return tc.getKey();
        }
        return component.getString();
    }
}