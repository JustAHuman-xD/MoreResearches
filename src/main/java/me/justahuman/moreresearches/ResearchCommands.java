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
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
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

    @Subcommand("editor session add-item")
    @CommandCompletion("@slimefun_items")
    public void editorAddItem(Player player, String itemId) {
        if (SlimefunItem.getById(itemId) == null) {
            player.sendMessage("Invalid Slimefun Item: " + itemId);
            return;
        } else if (ResearchEditor.getEditing(player) == null) {
            player.sendMessage("You aren't editing any research!");
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
}
