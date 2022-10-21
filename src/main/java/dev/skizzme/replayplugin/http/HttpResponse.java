package dev.skizzme.replayplugin.http;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;

public class HttpResponse {

    private int code = -1;
    private JsonElement data = null;
    private boolean hasResponseData = false;
    private String rawData = "";

    public HttpResponse(int code, JsonElement data) {
        this.code = code;
        this.data = data;
        this.hasResponseData = data != null;
    }

    public HttpResponse(HttpsURLConnection connection) {

        try {
            StringBuilder data = new StringBuilder();
            int responseCode = -1;

            responseCode = connection.getResponseCode();
            InputStream is;

            if (responseCode >= 400) {
                is = connection.getErrorStream();
            } else {
                is = connection.getInputStream();
            }

            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = rd.readLine()) != null) {
                if (data.length() > 0) data.append("\n");
                data.append(line);
            }
            rd.close();

            this.rawData = data.toString();
            this.data = new GsonBuilder().create().fromJson(data.toString(), JsonElement.class);
            this.code = responseCode;
        }catch (ConnectException e) {
            System.err.println("Failed to connect to: " + connection.getURL().toString());
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getCode() {
        return code;
    }

    public JsonElement getData() {
        return data;
    }

    public String getRawData() {
        return rawData;
    }

    public boolean hasResponseData() {
        return hasResponseData;
    }
}
