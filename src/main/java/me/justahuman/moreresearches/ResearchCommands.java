package me.justahuman.moreresearches;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Private;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@CommandAlias("more_researches|mr")
@CommandPermission("moreresearches.admin")
public class ResearchCommands extends BaseCommand {
    @Syntax("")
    @Private @HelpCommand
    public void helpCommand(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("reload")
    public void reload(CommandSender sender) {
        sender.sendMessage("Reloading MoreResearches Config!");
        MoreResearches.getInstance().reloadConfig();
        Utils.loadResearches();
    }

    @Subcommand("editor")
    public void editor(Player player) {
        ResearchEditor.openMainMenu(player);
    }

    @Subcommand("editor session set id")
    @CommandCompletion("example-research-id|different-example-research-id")
    public void editorSetId(Player player, String newId) {
        ConfigurationSection researches = MoreResearches.getInstance().getConfig().getConfigurationSection("researches");
        if (ResearchEditor.getEditing(player) == null) {
            player.sendMessage("You aren't editing any research!");
            return;
        } else if (researches.getKeys(false).contains(newId)) {
            player.sendMessage("A research already uses that id: " + newId);
            return;
        }

        Pair<String, ConfigurationSection> editing = ResearchEditor.getEditing(player);
        String oldId = editing.getFirstValue();
        ConfigurationSection researchConfig = editing.getSecondValue();
        researches.set(newId, researchConfig);
        researches.set(oldId, null);

        ResearchEditor.openResearchEditor(player, newId);
    }

    @Subcommand("editor session set legacy-id")
    @CommandCompletion("-1")
    public void editorSetLegacyId(Player player, int legacyId) {
        if (ResearchEditor.getEditing(player) == null) {
            player.sendMessage("You aren't editing any research!");
            return;
        } else if (Slimefun.getRegistry().getResearches().stream().map(Research::getID).anyMatch(id -> id.equals(legacyId))
                || MoreResearches.getInstance().getConfig().getConfigurationSection("researches").getValues(true).values().contains(legacyId)) {
            player.sendMessage("A research already uses that legacyId: " + legacyId);
            return;
        }

        Pair<String, ConfigurationSection> editing = ResearchEditor.getEditing(player);
        String researchId = editing.getFirstValue();
        ConfigurationSection researchConfig = editing.getSecondValue();
        researchConfig.set("legacy-id", legacyId);

        ResearchEditor.openResearchEditor(player, researchId);
    }

    @Subcommand("editor session set display-name")
    @CommandCompletion("Example")
    public void editorSetName(Player player, String name) {
        if (ResearchEditor.getEditing(player) == null) {
            player.sendMessage("You aren't editing any research!");
            return;
        } else if (name.trim().isEmpty()) {
            player.sendMessage("Display Name can't be blank!");
            return;
        }

        Pair<String, ConfigurationSection> editing = ResearchEditor.getEditing(player);
        String researchId = editing.getFirstValue();
        ConfigurationSection researchConfig = editing.getSecondValue();
        researchConfig.set("display-name", name.trim());

        ResearchEditor.openResearchEditor(player, researchId);
    }

    @Subcommand("editor session set exp-cost")
    @CommandCompletion("1|2|3|5|10|15|20|30|40|60")
    public void editorSetExpCost(Player player, int expCost) {
        if (ResearchEditor.getEditing(player) == null) {
            player.sendMessage("You aren't editing any research!");
            return;
        } else if (expCost < 0) {
            player.sendMessage("Exp Cost can't be negative: " + expCost);
            return;
        }

        Pair<String, ConfigurationSection> editing = ResearchEditor.getEditing(player);
        String researchId = editing.getFirstValue();
        ConfigurationSection researchConfig = editing.getSecondValue();
        researchConfig.set("exp-cost", expCost);

        ResearchEditor.openResearchEditor(player, researchId);
    }

    @Subcommand("editor session add-item")
    @CommandCompletion("@slimefun_items")
    public void editorAddItem(Player player, String itemId) {
        if (ResearchEditor.getEditing(player) == null) {
            player.sendMessage("You aren't editing any research!");
            return;
        } else if (SlimefunItem.getById(itemId) == null) {
            player.sendMessage("Invalid Slimefun Item: " + itemId);
            return;
        }

        Pair<String, ConfigurationSection> editing = ResearchEditor.getEditing(player);
        String researchId = editing.getFirstValue();
        ConfigurationSection researchConfig = editing.getSecondValue();

        List<String> itemIds = researchConfig.getStringList("slimefun-items");
        itemIds.add(itemId);
        researchConfig.set("slimefun-items", itemIds);

        ResearchEditor.openResearchEditor(player, researchId);
    }

    @Subcommand("editor session remove-item")
    @CommandCompletion("@slimefun_items")
    public void editorRemoveItem(Player player, String itemId) {
        if (ResearchEditor.getEditing(player) == null) {
            player.sendMessage("You aren't editing any research!");
            return;
        } else if (SlimefunItem.getById(itemId) == null) {
            player.sendMessage("Invalid Slimefun Item: " + itemId);
            return;
        }

        Pair<String, ConfigurationSection> editing = ResearchEditor.getEditing(player);
        String researchId = editing.getFirstValue();
        ConfigurationSection researchConfig = editing.getSecondValue();

        List<String> itemIds = researchConfig.getStringList("slimefun-items");
        itemIds.remove(itemId);
        researchConfig.set("slimefun-items", itemIds);

        ResearchEditor.openResearchEditor(player, researchId);
    }

    @Subcommand("editor session clear-items")
    public void editorClearItems(Player player) {
        if (ResearchEditor.getEditing(player) == null) {
            player.sendMessage("You aren't editing any research!");
            return;
        }

        Pair<String, ConfigurationSection> editing = ResearchEditor.getEditing(player);
        String researchId = editing.getFirstValue();
        ConfigurationSection researchConfig = editing.getSecondValue();
        researchConfig.set("slimefun-items", new ArrayList<>());

        ResearchEditor.openResearchEditor(player, researchId);
    }
}
