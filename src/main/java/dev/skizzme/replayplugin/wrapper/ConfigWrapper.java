package dev.skizzme.replayplugin.wrapper;

import dev.skizzme.replayplugin.ReplayPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;

public class ConfigWrapper {

    private ReplayPlugin plugin;
    private FileConfiguration config;
    private int saveInterval;

    public ConfigWrapper(ReplayPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();

        if (this.config.getCurrentPath().equals("")) {
            this.plugin.saveDefaultConfig();
            this.plugin.reloadConfig();
        }
    }

    public int getSaveInterval() {
        return saveInterval;
    }

    public void setSaveInterval(int interval) {
        this.saveInterval = interval;
        this.config.set("save-interval", interval);
        save();
    }

    private void save() {
        try {
            this.config.save(this.config.getCurrentPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
