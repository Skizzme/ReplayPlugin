package dev.skizzme.replayplugin.listeners;

import dev.skizzme.replayplugin.ReplayPlugin;
import dev.skizzme.replayplugin.inventory.GuiManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class ClickListener implements Listener {

    private ReplayPlugin plugin;

    public ClickListener(ReplayPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (GuiManager.inventories.containsKey(event.getInventory())) GuiManager.inventories.get(event.getInventory()).handleItemClick(event);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (GuiManager.inventories.containsKey(event.getInventory())) GuiManager.inventories.remove(event.getInventory());
    }

}
