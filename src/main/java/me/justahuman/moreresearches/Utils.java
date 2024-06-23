package me.justahuman.moreresearches;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class Utils {
    private static final Field RESEARCH_NAME_FIELD;
    static {
        try {
            RESEARCH_NAME_FIELD = Research.class.getDeclaredField("name");
            RESEARCH_NAME_FIELD.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(RESEARCH_NAME_FIELD, RESEARCH_NAME_FIELD.getModifiers() & ~Modifier.FINAL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadResearches() {
        FileConfiguration config = MoreResearches.getInstance().getConfig();
        ConfigurationSection researches = config.getConfigurationSection("researches");
        for (Research research : new ArrayList<>(Slimefun.getRegistry().getResearches())) {
            NamespacedKey key = research.getKey();
            if (!key.getNamespace().equalsIgnoreCase("moreresearches")) {
                continue;
            }

            research.getAffectedItems().forEach(slimefunItem -> slimefunItem.setResearch(null));

            String researchId = key.getKey();
            if (researches != null && researches.contains(researchId)) {
                Utils.updateResearch(research, researchId, researches.getConfigurationSection(researchId));
            } else {
                Slimefun.getRegistry().getResearches().remove(research);
            }
        }
    }

    public static Research createResearch(String researchId, ConfigurationSection researchConfig) {
        MoreResearches plugin = MoreResearches.getInstance();
        int legacyId = researchConfig.getInt("legacy-id", -113132);
        if (legacyId != -113132) {
            getLogger().warning("Invalid research, you need to set a legacy-id, negative numbers are recommended: " + researchId);
            return null;
        }

        String displayName = researchConfig.getString("display-name", "Error: No Display Name Provided");
        int expCost = researchConfig.getInt("exp-cost", -1);
        List<String> itemIds = researchConfig.getStringList("slimefun-items");

        if (itemIds.isEmpty()) {
            getLogger().warning("Invalid research, no items: " + researchId);
            return null;
        }

        SlimefunItem[] items = itemIds.stream()
                .map(SlimefunItem::getById)
                .filter(Objects::nonNull)
                .toArray(SlimefunItem[]::new);

        if (items.length == 0) {
            getLogger().warning("Invalid research, no valid items: " + researchId);
            return null;
        }

        Research research = new Research(new NamespacedKey(plugin, researchId), legacyId, displayName, expCost);
        research.addItems(items);
        research.register();

        return research;
    }

    public static void updateResearch(Research research, String researchId, ConfigurationSection researchConfig) {
        List<String> itemIds = researchConfig.getStringList("slimefun-items");
        if (itemIds.isEmpty()) {
            getLogger().warning("Invalid research, no items: " + researchId);
            return;
        }

        SlimefunItem[] items = itemIds.stream()
                .map(SlimefunItem::getById)
                .filter(Objects::nonNull)
                .toArray(SlimefunItem[]::new);

        if (items.length == 0) {
            getLogger().warning("Invalid research, no valid items: " + researchId);
            return;
        }

        try {
            RESEARCH_NAME_FIELD.set(research, researchConfig.getString("display-name", "Error: No Display Name Provided"));
        } catch (Exception e) {
            getLogger().severe("Ran into a problem updating " + researchId + "'s name!");
            getLogger().throwing("Utils", "updateResearch", e);
            return;
        }

        research.setCost(researchConfig.getInt("exp-cost", -1));
        research.addItems(items);
    }

    public static Logger getLogger() {
        return MoreResearches.getInstance().getLogger();
    }
}
