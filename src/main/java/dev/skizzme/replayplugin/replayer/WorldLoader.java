package dev.skizzme.replayplugin.replayer;

import dev.skizzme.replayplugin.actions.ReplayAction;
import dev.skizzme.replayplugin.actions.impl.BlockPlaceAction;
import dev.skizzme.replayplugin.util.RandomUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class WorldLoader {

    private Replayer replay;

    private Thread worldThread;

    public WorldLoader(Replayer replay) {
        this.replay = replay;
    }
    
    public void createWorld() {
        this.worldThread = new Thread(() -> {
            replay.world = this.copyWorld(replay.viewer.getWorld(), "ReplayWorld-" + RandomUtil.randomString(4));
            replay.worldCreated();
            worldThread.stop();
        });
        this.worldThread.setName("ReplayWorldCreatorThread");
        this.worldThread.run();
    }

    public void revertWorld() {
        for (ReplayAction action : replay.actions) {
            if (action instanceof BlockPlaceAction) {

            }
        }
    }

    public void deleteWorld() {
        replay.viewer.teleport(replay.viewerLocation);
        this.unloadWorld(replay.world);
        this.deleteWorldFile(replay.world.getWorldFolder());
    }

    private World copyWorld(World originalWorld, String newWorldName) {
        originalWorld.save();
        copyFileStructure(originalWorld.getWorldFolder(), new File(Bukkit.getWorldContainer(), newWorldName));
        return new WorldCreator(newWorldName).createWorld();
//        return null;
    }

    private boolean unloadWorld(World world) {
        return world!=null && Bukkit.getServer().unloadWorld(world, false);
    }

    private boolean deleteWorldFile(File file) {
        File files[] = file.listFiles();
        for(int i=0; i<files.length; i++) {
            if(files[i].isDirectory()) {
                deleteWorldFile(files[i]);
            } else {
                files[i].delete();
            }
        }
        return file.delete();
    }

    private void copyFileStructure(File source, File target) {
        try {
            ArrayList<String> ignore = new ArrayList<>(Arrays.asList("uid.dat", "session.lock"));
            if (!ignore.contains(source.getName())) {
                if (source.isDirectory()) {
                    if (!target.exists())
                        if (!target.mkdirs())
                            throw new IOException("Couldn't create world directory!");
                    String files[] = source.list();
                    for (String file : files) {
                        File srcFile = new File(source, file);
                        File destFile = new File(target, file);
                        copyFileStructure(srcFile, destFile);
                    }
                } else {
                    InputStream in = new FileInputStream(source);
                    OutputStream out = new FileOutputStream(target);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0)
                        out.write(buffer, 0, length);
                    in.close();
                    out.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            replay.viewer.sendMessage("Failed to create world");
            this.deleteWorldFile(replay.world.getWorldFolder());
            return;
        }
    }

}
