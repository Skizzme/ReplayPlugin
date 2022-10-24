package dev.skizzme.replayplugin.util;

import org.bukkit.ChatColor;

public class ChatUtil {

    public static String chat(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

}
