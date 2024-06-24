package me.justahuman.moreresearches;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.common.ChatColors;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerHead;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerSkin;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class ResearchEditor {
    private static final int[] FOOTER_BACKGROUND = { 45, 47, 51, 53 };
    private static final int PAGE_SIZE = 45;

    // Head From: https://minecraft-heads.com/custom-heads/head/9382-red-x
    private static final ItemStack RED_X = PlayerHead.getItemStack(PlayerSkin.fromHashCode("beb588b21a6f98ad1ff4e085c552dcb050efc9cab427f46048f18fc803475f7"));
    // Head From: https://minecraft-heads.com/custom-heads/head/8768-black-plus
    private static final ItemStack BLACK_PLUS = PlayerHead.getItemStack(PlayerSkin.fromHashCode("9a2d891c6ae9f6baa040d736ab84d48344bb6b70d7f1a280dd12cbac4d777"));
    // Head From: https://minecraft-heads.com/custom-heads/head/21771-lime-checkmark
    private static final ItemStack LIME_CHECKMARK = PlayerHead.getItemStack(PlayerSkin.fromHashCode("a92e31ffb59c90ab08fc9dc1fe26802035a3a47c42fee63423bcdb4262ecb9b6"));

    public static void openMainMenu(Player player) {
        openMainMenu(player, 1);
    }

    public static void openMainMenu(Player player, int page) {
        ChestMenu menu = new ChestMenu("MoreResearches Editor");

        FileConfiguration config = MoreResearches.getInstance().getConfig();
        ConfigurationSection researches = config.getConfigurationSection("researches") == null
                ? config.createSection("researches")
                : config.getConfigurationSection("researches");

        List<String> researchIds = researches.getKeys(false).stream()
                .filter(key -> Objects.nonNull(researches.getConfigurationSection(key)))
                .collect(Collectors.toList());
        int researchCount = researchIds.size();
        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, researchCount);
        int pages = (int) Math.ceil(researchCount / (double) PAGE_SIZE);
        researchIds.sort(Comparator.naturalOrder());

        int i = 0;
        List<String> pageResearchIds = researchIds.subList(start, end);

        for (String researchId : pageResearchIds) {
            ConfigurationSection researchConfig = researches.getConfigurationSection(researchId);
            String displayName = researchConfig.getString("display-name", "Error: No Display Name Provided");
            List<String> itemIds = researchConfig.getStringList("slimefun-items");
            ItemStack display = itemIds.stream()
                    .map(SlimefunItem::getById)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .map(SlimefunItem::getItem)
                    .orElse(new ItemStack(Material.BARRIER));

            menu.addItem(i, new CustomItemStack(
                    display,
                    "&f" + displayName + " &7(&e" + researchId + "&7)",
                    "&fLeft-Click &7to edit"
            ));

            menu.addMenuClickHandler(i, (o1, o2, o3, o4) -> {
                openResearchEditor(player, researchId);
                return false;
            });

            i++;
        }

        ChestMenuUtils.drawBackground(menu, FOOTER_BACKGROUND);

        menu.addItem(46, ChestMenuUtils.getPreviousButton(player, page, pages));
        menu.addMenuClickHandler(46, (o1, o2, o3, o4) -> {
            if (page > 1) {
                openMainMenu(player, page - 1);
            }
            return false;
        });

        menu.addItem(48, new CustomItemStack(
                RED_X,
                "&cDiscard Changes",
                "&cLeft-Click &7to discard all changes",
                "&7This effectively reloads the config"
        ));
        menu.addMenuClickHandler(48, (o1, o2, o3, o4) -> {
            player.closeInventory();
            MoreResearches.getInstance().reloadConfig();
            Utils.loadResearches();
            return false;
        });

        menu.addItem(49, new CustomItemStack(
                BLACK_PLUS,
                "&aNew Research",
                "&fLeft-Click &7to create a new Research"
        ));
        menu.addMenuClickHandler(49, (o1, o2, o3, o4) -> {
            String randomId = UUID.randomUUID().toString();
            ConfigurationSection researchConfig = researches.createSection(randomId);
            researchConfig.set("legacy-id", 0);
            researchConfig.set("display-name", "[Set Me!]");
            researchConfig.set("exp-cost", 0);
            researchConfig.set("slimefun-items", new ArrayList<>());
            openResearchEditor(player, randomId);
            return false;
        });

        menu.addItem(50, new CustomItemStack(
                LIME_CHECKMARK,
                "&aSave Changes",
                "&aLeft-Click &7to save all changes to the",
                "&7config file & load them in-game."
        ));
        menu.addMenuClickHandler(50, (o1, o2, o3, o4) -> {
            player.closeInventory();
            MoreResearches.getInstance().saveConfig();
            Utils.loadResearches();
            return false;
        });

        menu.addItem(52, ChestMenuUtils.getNextButton(player, page, pages));
        menu.addMenuClickHandler(52, (o1, o2, o3, o4) -> {
            if (page < pages) {
                openMainMenu(player, page + 1);
            }
            return false;
        });

        menu.open(player);
    }

    public static void openResearchEditor(Player player, String researchId) {
        FileConfiguration config = MoreResearches.getInstance().getConfig();
        ConfigurationSection researchConfig = config.getConfigurationSection("researches." + researchId);
        if (researchConfig == null) {
            return;
        }

        ConfigurationSection researches = config.getConfigurationSection("researches");
        ChestMenu menu = new ChestMenu("Research Editor");

        menu.addItem(1, new CustomItemStack(
                Material.ANVIL,
                "&eResearch Id &7(String)",
                "&7Current: &e" + researchId,
                "&eLeft-Click &7to edit"
        ));
        menu.addMenuClickHandler(1, (o1, o2, o3, o4) -> {
            player.closeInventory();
            player.sendMessage(ChatColors.color("&e&lEnter the new Research Id (ex. test_research):"));
            ChatUtils.awaitInput(player, newId -> {
                if (researches.getKeys(false).contains(newId)) {
                    player.sendMessage(ChatColors.color("&cA research already uses that id: " + newId));
                    openResearchEditor(player, researchId);
                    return;
                } else if (newId.isBlank()) {
                    player.sendMessage(ChatColors.color("&cResearch id cannot be blank"));
                    openResearchEditor(player, researchId);
                    return;
                } else if (!newId.matches("^[a-z0-9_]+$")) {
                    player.sendMessage(ChatColors.color("&cInvalid research id: " + newId));
                    player.sendMessage(ChatColors.color("&cOnly lowercase letters, numbers, and underscores are allowed"));
                    openResearchEditor(player, researchId);
                    return;
                }
                researches.set(newId, researchConfig);
                researches.set(researchId, null);
                openResearchEditor(player, newId);
            });
            return false;
        });

        menu.addItem(3, new CustomItemStack(
                Material.CLOCK,
                "&eLegacy Id &7(Integer)",
                "&7Current: &e" + researchConfig.getInt("legacy-id", -1),
                "&eLeft-Click &7to edit"
        ));
        menu.addMenuClickHandler(3, (o1, o2, o3, o4) -> {
            player.closeInventory();
            player.sendMessage(ChatColors.color("&e&lEnter the new Legacy Id (ex. -1):"));
            ChatUtils.awaitInput(player, newLegacyId -> {
                int legacyId;
                try {
                    legacyId = Integer.parseInt(newLegacyId);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColors.color("&cInvalid number: " + newLegacyId));
                    openResearchEditor(player, researchId);
                    return;
                }

                if (Slimefun.getRegistry().getResearches().stream().map(Research::getID).anyMatch(id -> id.equals(legacyId))
                        || MoreResearches.getInstance().getConfig().getConfigurationSection("researches").getValues(true).values().contains(legacyId)) {
                    player.sendMessage("A research already uses that legacyId: " + legacyId);
                    openResearchEditor(player, researchId);
                    return;
                }
                researchConfig.set("legacy-id", legacyId);
                openResearchEditor(player, researchId);
            });
            return false;
        });

        menu.addItem(5, new CustomItemStack(
                Material.NAME_TAG,
                "&eDisplay Name &7(String)",
                "&7Current: &e" + researchConfig.getString("display-name", "Error: No Display Name Provided"),
                "&eLeft-Click &7to edit"
        ));
        menu.addMenuClickHandler(5, (o1, o2, o3, o4) -> {
            player.closeInventory();
            player.sendMessage(ChatColors.color("&e&lEnter the new Display Name:"));
            ChatUtils.awaitInput(player, newName -> {
                if (newName.isBlank()) {
                    player.sendMessage(ChatColors.color("&cDisplay name cannot be blank"));
                    openResearchEditor(player, researchId);
                    return;
                }
                researchConfig.set("display-name", newName);
                openResearchEditor(player, researchId);
            });
            return false;
        });

        menu.addItem(7, new CustomItemStack(
                Material.EXPERIENCE_BOTTLE,
                "&eExperience Cost &7(Integer)",
                "&7Current: &e" + researchConfig.getInt("exp-cost", 0),
                "&eLeft-Click &7to edit"
        ));
        menu.addMenuClickHandler(7, (o1, o2, o3, o4) -> {
            player.closeInventory();
            player.sendMessage(ChatColors.color("&e&lEnter the new Experience Cost (ex. 10):"));
            ChatUtils.awaitInput(player, newExpCost -> {
                int expCost;
                try {
                    expCost = Integer.parseInt(newExpCost);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColors.color("&cInvalid number: " + newExpCost));
                    openResearchEditor(player, researchId);
                    return;
                }

                if (expCost < 0) {
                    player.sendMessage(ChatColors.color("&cExperience cost cannot be negative: " + expCost));
                    openResearchEditor(player, researchId);
                    return;
                }

                researchConfig.set("exp-cost", expCost);
                openResearchEditor(player, researchId);
            });
            return false;
        });


        List<String> idLore = new ArrayList<>();
        idLore.add("&7Current:");
        idLore.addAll(Utils.compressIds(researchConfig.getStringList("slimefun-items")));
        idLore.add(" ");
        idLore.add("&eLeft-Click &7to add a new id");
        idLore.add("&eRight-Click &7to remove an id");
        idLore.add("&eShift-Right-Click &7to clear all ids");

        menu.addItem(13, new CustomItemStack(
                Material.CHEST,
                "&eSlimefun Item Ids &7(String List)",
                idLore.toArray(new String[0])
        ));
        menu.addMenuClickHandler(13, ((o1, o2, o3, action) -> {
            player.closeInventory();
            if (action.isRightClicked() && action.isShiftClicked()) {
                researchConfig.set("slimefun-items", new ArrayList<>());
                openResearchEditor(player, researchId);
                return false;
            } else if (action.isRightClicked()) {
                player.sendMessage(ChatColors.color("&e&lEnter the id to remove:"));
                ChatUtils.awaitInput(player, id -> {
                    List<String> itemIds = researchConfig.getStringList("slimefun-items");
                    if (itemIds.remove(id)) {
                        researchConfig.set("slimefun-items", itemIds);
                    }
                    openResearchEditor(player, researchId);
                });
                return false;
            } else {
                player.sendMessage(ChatColors.color("&e&lEnter the new id:"));
                ChatUtils.awaitInput(player, id -> {
                    List<String> itemIds = researchConfig.getStringList("slimefun-items");
                    itemIds.add(id);
                    researchConfig.set("slimefun-items", itemIds);
                    openResearchEditor(player, researchId);
                });
                return false;
            }
        }));

        menu.addItem(17, new CustomItemStack(
                LIME_CHECKMARK,
                "&aDone",
                "&aLeft-Click &7to return to the main page"
        ));
        menu.addMenuClickHandler(17, (o1, o2, o3, o4) -> {
            openMainMenu(player);
            return false;
        });

        menu.open(player);
    }
}
