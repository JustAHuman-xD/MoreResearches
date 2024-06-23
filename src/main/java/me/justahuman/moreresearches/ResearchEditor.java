package me.justahuman.moreresearches;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerHead;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerSkin;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class ResearchEditor {
    private static final Map<UUID, Pair<String, ConfigurationSection>> PLAYER_EDITING = new HashMap<>();
    private static final int[] FOOTER_BACKGROUND = { 45, 47, 51, 53 };
    // # < # D N S # > #
    private static final int PAGE_SIZE = 45;

    public static void openMainMenu(Player player) {
        openMainMenu(player, 0);
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
            Material display = itemIds.stream()
                    .map(SlimefunItem::getById)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .map(SlimefunItem::getItem)
                    .map(ItemStack::getType)
                    .orElse(Material.BARRIER);

            menu.addItem(i, new CustomItemStack(
                    display,
                    displayName + " &7(&e" + researchId + "&7)",
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
                // Head From: https://minecraft-heads.com/custom-heads/head/9382-red-x
                PlayerHead.getItemStack(PlayerSkin.fromHashCode("beb588b21a6f98ad1ff4e085c552dcb050efc9cab427f46048f18fc803475f7")),
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
                // Head From: https://minecraft-heads.com/custom-heads/head/8768-black-plus
                PlayerHead.getItemStack(PlayerSkin.fromHashCode("9a2d891c6ae9f6baa040d736ab84d48344bb6b70d7f1a280dd12cbac4d777")),
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
                // Head From: https://minecraft-heads.com/custom-heads/head/21771-lime-checkmark
                PlayerHead.getItemStack(PlayerSkin.fromHashCode("a92e31ffb59c90ab08fc9dc1fe26802035a3a47c42fee63423bcdb4262ecb9b6")),
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
        ConfigurationSection researchConfig = MoreResearches.getInstance().getConfig().getConfigurationSection("researches." + researchId);
        if (researchConfig == null) {
            return;
        }


        ChestMenu menu = new ChestMenu("Research Editor (" + researchId + ")");

        menu.addItem(1, new CustomItemStack(
                Material.ANVIL,
                "&eResearch Id &7(String)",
                "&7Current: &e" + researchId,
                "&eLeft-Click &7to edit"
        ));
        menu.addMenuClickHandler(1, (o1, o2, o3, o4) -> {
            player.closeInventory();
            TextComponent component = new TextComponent("CLICK here to set the new Research Id");
            component.setColor(ChatColor.YELLOW);
            component.setBold(true);
            component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mr editor session set id "));
            player.spigot().sendMessage(component);
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
            TextComponent component = new TextComponent("CLICK here to set the new Legacy Id");
            component.setColor(ChatColor.YELLOW);
            component.setBold(true);
            component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mr editor session set legacy-id "));
            player.spigot().sendMessage(component);
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
            TextComponent component = new TextComponent("CLICK here to set the new Display Name");
            component.setColor(ChatColor.YELLOW);
            component.setBold(true);
            component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mr editor session set display-name "));
            player.spigot().sendMessage(component);
            return false;
        });

        menu.addItem(7, new CustomItemStack(
                Material.EXPERIENCE_BOTTLE,
                "&eExperience Cost &7(Integer)",
                "&7Current: &e" + researchConfig.getInt("exp-cost", -1),
                "&eLeft-Click &7to edit"
        ));
        menu.addMenuClickHandler(7, (o1, o2, o3, o4) -> {
            player.closeInventory();
            TextComponent component = new TextComponent("CLICK here to set the new Experience Cost");
            component.setColor(ChatColor.YELLOW);
            component.setBold(true);
            component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mr editor session set exp-cost "));
            player.spigot().sendMessage(component);
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
            String display = "CLICK here to";
            String command = "/mr editor session ";
            if (action.isRightClicked()) {
                display += "remove an id";
                command += "remove-item ";
            } else if (action.isShiftClicked()) {
                display += "clear all ids";
                command += "clear-items ";
            } else {
                display += "add a new id";
                command += "add-item ";
            }
            TextComponent component = new TextComponent(display);
            component.setColor(ChatColor.YELLOW);
            component.setBold(true);
            component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
            player.spigot().sendMessage(component);
            return false;
        }));

        menu.open(player);
        PLAYER_EDITING.put(player.getUniqueId(), new Pair<>(researchId, researchConfig));
    }

    public static Pair<String, ConfigurationSection> getEditing(Player player) {
        return PLAYER_EDITING.get(player.getUniqueId());
    }
}
