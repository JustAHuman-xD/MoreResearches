package me.justahuman.moreresearches;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public class Utils {
    private static final Map<SlimefunItem, Research> ORIGINAL_RESEARCHES = new HashMap<>();
    private static final Field RESEARCH_NAME_FIELD;
    static {
        try {
            RESEARCH_NAME_FIELD = Research.class.getDeclaredField("name");
            RESEARCH_NAME_FIELD.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadResearches() {
        FileConfiguration config = MoreResearches.getInstance().getConfig();
        ConfigurationSection researches = config.getConfigurationSection("researches");
        Map<String, Research> customResearches = new HashMap<>();
        for (Research research : new ArrayList<>(Slimefun.getRegistry().getResearches())) {
            if (!isCustomResearch(research)) {
                continue;
            }

            for (SlimefunItem slimefunItem : new ArrayList<>(research.getAffectedItems())) {
                slimefunItem.setResearch(ORIGINAL_RESEARCHES.get(slimefunItem));
            }

            String researchId = research.getKey().getKey();
            if (researches == null || !researches.contains(researchId)) {
                Slimefun.getRegistry().getResearches().remove(research);
            }
            customResearches.put(researchId, research);
        }

        if (researches != null) {
            for (String researchId : researches.getKeys(false)) {
                Research research = customResearches.get(researchId);
                ConfigurationSection researchConfig = researches.getConfigurationSection(researchId);
                if (research == null && researchConfig != null) {
                    Utils.createResearch(researchId, researchConfig);
                } else if (research != null && researchConfig != null){
                    Utils.updateResearch(research, researchId, researchConfig);
                }
            }
        }
    }

    public static Research createResearch(String researchId, ConfigurationSection researchConfig) {
        MoreResearches plugin = MoreResearches.getInstance();
        int legacyId = researchConfig.getInt("legacy-id", -113132);
        if (legacyId == -113132) {
            getLogger().warning("Invalid research, you need to set a legacy-id, negative numbers are recommended: " + researchId);
            return null;
        }

        String displayName = researchConfig.getString("display-name", "Error: No Display Name Provided");
        int expCost = researchConfig.getInt("exp-cost", 0);
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

        for (SlimefunItem item : items) {
            if (!ORIGINAL_RESEARCHES.containsKey(item) && !isCustomResearch(item.getResearch())) {
                ORIGINAL_RESEARCHES.put(item, item.getResearch());
            }
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

        for (SlimefunItem item : items) {
            if (!ORIGINAL_RESEARCHES.containsKey(item) && !isCustomResearch(item.getResearch())) {
                ORIGINAL_RESEARCHES.put(item, item.getResearch());
            }
        }

        try {
            RESEARCH_NAME_FIELD.set(research, researchConfig.getString("display-name", "Error: No Display Name Provided"));
        } catch (Exception e) {
            getLogger().severe("Ran into a problem updating " + researchId + "'s name!");
            getLogger().throwing("Utils", "updateResearch", e);
            return;
        }

        research.setCost(researchConfig.getInt("exp-cost", 0));
        research.addItems(items);
    }

    public static List<String> compressIds(List<String> words) {
        int totalWords = words.size();
        int numGroups = (int) Math.ceil(Math.sqrt(totalWords));
        int targetGroupSize = (int) Math.ceil((double) totalWords / numGroups);

        List<String> compressedWords = new ArrayList<>();
        StringBuilder currentGroup = new StringBuilder("&e");

        for (int i = 0; i < totalWords; i++) {
            currentGroup.append(words.get(i)).append(", ");

            if ((i + 1) % targetGroupSize == 0 || i == totalWords - 1) {
                compressedWords.add(currentGroup.toString().trim());
                currentGroup = new StringBuilder("&e");
            }
        }

        return compressedWords;
    }

    public static boolean isCustomResearch(Research research) {
        return research != null && research.getKey().getNamespace().equalsIgnoreCase("moreresearches");
    }

    public static Logger getLogger() {
        return MoreResearches.getInstance().getLogger();
    }
}
