package dev.skizzme.replayplugin.listeners;

import dev.skizzme.replayplugin.ReplayPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class RegistrationListener implements Listener {

    public ReplayPlugin plugin;

    public RegistrationListener(ReplayPlugin plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
    }

}
