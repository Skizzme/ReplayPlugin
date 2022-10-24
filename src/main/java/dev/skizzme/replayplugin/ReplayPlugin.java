package dev.skizzme.replayplugin;

import dev.skizzme.replayplugin.commands.ReplayCommand;
import dev.skizzme.replayplugin.commands.testnpccommand;
import dev.skizzme.replayplugin.listeners.ClickListener;
import dev.skizzme.replayplugin.listeners.PacketListener;
import dev.skizzme.replayplugin.npc.PlayerNPC;
import dev.skizzme.replayplugin.wrapper.ConfigWrapper;
import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class ReplayPlugin extends JavaPlugin {

    public static ReplayPlugin INSTANCE;
    public ArrayList<PlayerNPC> npcs = new ArrayList<>();
    public ReplayManager replayManager = new ReplayManager();
    public ConfigWrapper config;

    @Override
    public void onEnable() {
        config = new ConfigWrapper(this);

        //Commands
        new testnpccommand(this);
        new ReplayCommand(this);

        //Events
        new ClickListener(this);

        this.npcs = new ArrayList<PlayerNPC>();
        startPacketEvents(this);
        handleBukkit(this);
    }

    @Override
    public void onLoad() {
        INSTANCE = this;
        loadPacketEvents(this);
    }

    private void loadPacketEvents(final JavaPlugin plugin) {
        PacketEvents.create(plugin).getSettings()
                .compatInjector(false)
                .checkForUpdates(false)
                .backupServerVersion(ServerVersion.v_1_7_10);
        PacketEvents.get().load();
    }

    private void handleBukkit(final JavaPlugin plugin) {
        PacketEvents.get().getEventManager().registerListener(new PacketListener());
    }

    private void startPacketEvents(final JavaPlugin plugin) {
        PacketEvents.get().init(plugin);
    }

}
