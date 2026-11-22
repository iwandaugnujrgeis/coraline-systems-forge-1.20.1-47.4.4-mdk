package net.zharok01.coralinesystems.registry;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.forgespi.language.IModInfo;
import net.zharok01.coralinesystems.CoralineSystems;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CoralineIntegrityChecker {

    public record Issue(String reason, String displayName) {}

    private record BlacklistedMod(String modId, String reason) {}

    private static final String REASON_MISSING = "missing";
    private static final String REASON_BLACKLISTED = "blacklisted";

    private static final List<String> COMMON_REQUIRED_MODS = List.of(
            "alexsmobs",
            "amendments",
            "atmospheric",
            "caverns_and_chasms",
            "clayworks",
            "crafttweaker",
            "dummmmmmy",
            "easel_does_it",
            "etcetera",
            "flowerymooblooms",
            "friendsandfoes",
            "kubejs",
            "moderner_beta",
            "modernfix",
            "morejs",
            "openloader", // ?
            "quark",
            "quarkoddities",
            "rediscovered",
            "rosen",
            "spawn",
            "spelunkery",
            "supplementaries",
            "sweety_garden",
            "upgrade_aquatic",
            "nostalgic_tweaks"

    );

    private static final List<String> CLIENT_REQUIRED_MODS = List.of(
            "embeddium",
            "fancymenu",
            "foglooksgoodnow",
            "keybindjs",
            "dyed",
            "picturemode",
            "polytone",
            "resourcepackoverrides",
            "shouldersurfing",
            "subtle_effects"
    );

    private static final List<BlacklistedMod> BLACKLISTED_MODS = List.of(
            new BlacklistedMod("emi", "recipe_viewer"),
            new BlacklistedMod("essential", "essential"),
            new BlacklistedMod("jei", "recipe_viewer"),
            new BlacklistedMod("roughlyenoughitems", "recipe_viewer"),
            new BlacklistedMod("reliable_recipes", "recipe_viewer")
    );

    private static final List<Issue> ISSUES = new ArrayList<>();

    public static List<Issue> getIssues() {
        return ISSUES;
    }

    public static void verifyIntegrity() {

        // dumpAllModIds();

        ModList modList = ModList.get();
        ISSUES.clear();

        List<String> requiredHere = new ArrayList<>(COMMON_REQUIRED_MODS);
        if (FMLEnvironment.dist.isClient()) {
            requiredHere.addAll(CLIENT_REQUIRED_MODS);
        }

        for (String requiredModId : requiredHere) {
            if (!modList.isLoaded(requiredModId)) {
                ISSUES.add(new Issue(REASON_MISSING, requiredModId));
            }
        }

        for (BlacklistedMod blacklisted : BLACKLISTED_MODS) {
            if (modList.isLoaded(blacklisted.modId())) {
                String displayName = modList.getModContainerById(blacklisted.modId())
                        .map(container -> container.getModInfo().getDisplayName())
                        .orElse(blacklisted.modId());
                ISSUES.add(new Issue(blacklisted.reason(), displayName));
            }
        }

        if (!ISSUES.isEmpty() && FMLEnvironment.dist.isDedicatedServer()) {
            throw new RuntimeException("Critical Integrity Error: " + ISSUES.stream()
                    .map(issue -> issue.reason() + " -> " + issue.displayName())
                    .collect(Collectors.joining(", ")));
        }
    }

    /*
    public static void dumpAllModIds() {
        CoralineSystems.LOGGER.info("Coraline Mod Dump:");
        for (IModInfo mod : ModList.get().getMods()) {
            CoralineSystems.LOGGER.info("\"{}\", // {}", mod.getModId(), mod.getDisplayName());
        }
        CoralineSystems.LOGGER.info("---");
    }
    */
}