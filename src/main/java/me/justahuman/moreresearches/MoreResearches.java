package me.justahuman.moreresearches;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.PaperCommandManager;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.updater.BlobBuildUpdater;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.stream.Collectors;

public class MoreResearches extends JavaPlugin implements SlimefunAddon {
    private static MoreResearches instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new RegistryListener(), this);

        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.enableUnstableAPI("help");

        CommandCompletions<BukkitCommandCompletionContext> completions = commandManager.getCommandCompletions();
        completions.registerAsyncCompletion("slimefun_items", c -> Slimefun.getRegistry().getEnabledSlimefunItems()
                .stream().map(SlimefunItem::getId).collect(Collectors.toSet()));
        completions.registerStaticCompletion("languages", List.of("en_us"));

        commandManager.registerCommand(new ResearchCommands());

        if (getConfig().getBoolean("options.auto-update") && getDescription().getVersion().startsWith("Dev - ")) {
            BlobBuildUpdater updater = new BlobBuildUpdater(this, this.getFile(), "MoreResearches", "Dev");
            updater.start();
        }

        Metrics metrics = new Metrics(this, 22383);
        metrics.addCustomChart(new SimplePie("server_custom_research_count",
                () -> String.valueOf(Slimefun.getRegistry().getResearches().stream()
                        .filter(research -> research.getKey().getNamespace().equalsIgnoreCase("moreresearches"))
                        .count())));
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
