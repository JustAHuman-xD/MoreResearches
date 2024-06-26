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

    @Subcommand("config set-lang")
    @CommandCompletion("@languages")
    public void setLang(CommandSender sender, String lang) {
        if (MoreResearches.getInstance().getResource("lang/" + lang + ".yml") == null) {
            sender.sendMessage(ChatColor.RED + Utils.translated("warnings.command.invalid-lang", lang));
            return;
        }
        MoreResearches.getInstance().getConfig().set("lang", lang);
        MoreResearches.getInstance().saveConfig();
        Utils.unchacheLangFile();
        sender.sendMessage(ChatColor.GREEN + Utils.translated("command.set-lang", lang));
    }

    @Subcommand("config reload")
    public void reload(CommandSender sender) {
        MoreResearches.getInstance().reloadConfig();
        Utils.unchacheLangFile();
        sender.sendMessage(ChatColor.GREEN + Utils.translated("command.reload"));
        Utils.loadResearches();
    }

    @Subcommand("editor")
    public void editor(Player player) {
        ResearchEditor.openMainMenu(player);
    }

    @Subcommand("editor confirm")
    public void editorConfirm(Player player) {
        ResearchEditor.handleCallback(player, "confirm");
    }

    @Subcommand("editor cancel")
    public void editorCancel(Player player) {
        ResearchEditor.handleCallback(player, "cancel");
    }
}
