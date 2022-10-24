package dev.skizzme.replayplugin.commands;

import dev.skizzme.replayplugin.ReplayPlugin;
import dev.skizzme.replayplugin.handlers.PacketHandler;
import dev.skizzme.replayplugin.inventory.GuiInventory;
import dev.skizzme.replayplugin.replayer.Replayer;
import dev.skizzme.replayplugin.util.RandomUtil;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ReplayCommand implements CommandExecutor {

    public ReplayPlugin replayPlugin;

    public ReplayCommand(ReplayPlugin replayPlugin) {
        this.replayPlugin = replayPlugin;
        replayPlugin.getCommand("replay").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandName, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length > 0) {
                String subC = args[0].toLowerCase();
                if (subC.equals("play")) {
                    if (replayPlugin.replayManager.hasReplay(player)) {
                        player.sendMessage("You are already in a replay!");
                    } else {
                        player.sendMessage("Creating replay");
                        System.out.println(PacketHandler.packets.size());
                        Replayer replayer = new Replayer(PacketHandler.packets, player);
                        replayPlugin.replayManager.addPlayer(player, replayer);
                        player.sendMessage("Created replay");
//                player.sendMessage("" + ReplayPlugin.INSTANCE.replayer.actions);
                        replayer.runReplay();
                        replayer.startQueue();
                        replayer.showNPCs();
                    }
                }
                if (subC.equals("end")) {
                    replayPlugin.replayManager.getReplay(player).replayController.end();
                    replayPlugin.replayManager.removePlayer(player);
                    player.sendMessage("Ended replay");
                }
                if (subC.equals("pause")) {
                    replayPlugin.replayManager.getReplay(player).replayController.pause();
                    player.sendMessage("Paused replay");
                }
                if (subC.equals("resume")) {
                    replayPlugin.replayManager.getReplay(player).replayController.play();
                    player.sendMessage("Resumed replay");
                }
                return true;
            } else {
                GuiInventory inv = new GuiInventory(player, 3, "&7Replays").setBackground(Material.GRAY_STAINED_GLASS_PANE);
                player.openInventory(inv.getInventory());
            }
        }

        return false;
    }
}
