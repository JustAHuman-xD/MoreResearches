package me.justahuman.moreresearches;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.PaperCommandManager;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.stream.Collectors;

public class MoreResearches extends JavaPlugin implements SlimefunAddon {
    private static MoreResearches instance;

    @Override
    public void onEnable() {
        instance = this;

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new RegistryListener(), this);

        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.enableUnstableAPI("help");

        CommandCompletions<BukkitCommandCompletionContext> completions = commandManager.getCommandCompletions();
        completions.registerAsyncCompletion("slimefun_items", c -> Slimefun.getRegistry().getEnabledSlimefunItems()
                .stream().map(SlimefunItem::getId).collect(Collectors.toSet()));

        commandManager.registerCommand(new ResearchCommands());
    }

    @Override
    public void onDisable() {

    }

    public static MoreResearches getInstance() {
        return instance;
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Override
    public String getBugTrackerURL() {
        return "https://github.com/JustAHuman-xD/MoreResearches/issues";
    }
}
