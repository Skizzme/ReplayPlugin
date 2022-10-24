package dev.skizzme.replayplugin.commands;

import com.mojang.authlib.GameProfile;
import dev.skizzme.replayplugin.ReplayPlugin;
import dev.skizzme.replayplugin.ncp.PlayerNPC;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.UUID;

public class testnpccommand implements CommandExecutor {

    public ReplayPlugin plugin;

    public testnpccommand(ReplayPlugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("testnpccommand").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandName, String[] args) {

        if (sender instanceof Player) {
            String name = args[1];
            Player player = (Player)sender;
            if (args[0].equals("create")) {
                PlayerNPC playerNPC = new PlayerNPC(new GameProfile(UUID.randomUUID(), args[1]), ((CraftWorld)player.getWorld()).getHandle());
                ReplayPlugin.INSTANCE.npcs.add(playerNPC);
//                playerNPC.addPlayer(player);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    playerNPC.addPlayer(p);
                }
                player.sendMessage("" + player.getLocation().getYaw());
                playerNPC.setLocation(player.getLocation());
                playerNPC.showToPlayers();
                player.sendMessage("Spawned npc with name '" + args[1] + "' at your location");
            }
            if (args[0].equals("remove")) {
                for (PlayerNPC npc : ReplayPlugin.INSTANCE.npcs) {
                    if (npc.gameProfile.getName().equals(name)) {
                        npc.hideFromPlayers();
                        ReplayPlugin.INSTANCE.npcs.remove(npc);
                        break;
                    }
                }
                player.sendMessage("Removed npc with name '" + args[1] + "'.");
            }
            if (args[0].equals("tp")) {
                for (PlayerNPC npc : ReplayPlugin.INSTANCE.npcs) {
                    if (npc.gameProfile.getName().equals(name)) {
                        npc.teleportToLocation(player.getLocation());
                        break;
                    }
                }
                player.sendMessage("TPed npc with name '" + args[1] + "' to u");
            }
            if (args[0].equals("mkfollow")) {
                for (PlayerNPC npc : ReplayPlugin.INSTANCE.npcs) {
                    if (npc.gameProfile.getName().equals(name)) {
                        npc.followPlayer(player);
                        break;
                    }
                }
                player.sendMessage("NPC '" + args[1] + "' is following u");
            }
            if (args[0].equals("swing")) {
                for (PlayerNPC npc : ReplayPlugin.INSTANCE.npcs) {
                    if (npc.gameProfile.getName().equals(name)) {
                        npc.swingArm();
                        break;
                    }
                }
                player.sendMessage("NPC '" + args[1] + "' swung their arm");
            }
            if (args[0].equals("rotate")) {
                for (PlayerNPC npc : ReplayPlugin.INSTANCE.npcs) {
                    if (npc.gameProfile.getName().equals(name)) {
                        npc.rotateEntity(Double.parseDouble(args[2]), Double.parseDouble(args[3]));
                        break;
                    }
                }
                player.sendMessage("Set NPC's rotation to " + args[2] + ", " + args[3]);
            }
            if (args[0].equals("rotate_follow")) {
                for (PlayerNPC npc : ReplayPlugin.INSTANCE.npcs) {
                    if (npc.gameProfile.getName().equals(name)) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                npc.rotateEntity(player.getLocation().getYaw(), player.getLocation().getPitch());
                            }
                        }.runTaskTimerAsynchronously(ReplayPlugin.INSTANCE, 0, 1);
                        break;
                    }
                }
                player.sendMessage("NPC will follow your rotations!");
            }
            return true;
        }

        return false;
    }
}
