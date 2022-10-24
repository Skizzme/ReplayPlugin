package dev.skizzme.replayplugin.util;

import com.google.gson.JsonObject;
import dev.skizzme.replayplugin.http.HttpRequest;

import java.io.IOException;

public class Util {

    public static String[] getSkinProperties(String uuid) {
        HttpRequest request = new HttpRequest("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.replace("-", "") + "?unsigned=false");
        try {
            request.createConnection("GET");
            JsonObject response = request.sendRequest().getData().getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
            return new String[] {response.get("value").getAsString(), response.get("signature").getAsString()};
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
