package me.justahuman.moreresearches;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Private;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        sender.sendMessage(ChatColor.GREEN + "Reloading MoreResearches Config!");
        MoreResearches.getInstance().reloadConfig();
        Utils.loadResearches();
    }

    @Subcommand("editor")
    public void editor(Player player) {
        ResearchEditor.openMainMenu(player);
    }
}
