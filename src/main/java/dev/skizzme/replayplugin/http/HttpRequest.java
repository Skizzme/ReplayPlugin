package dev.skizzme.replayplugin.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.function.BiConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class HttpRequest {

    private String data = "";
    private URL url;
    private HttpsURLConnection connection;
    private Gson gson = new GsonBuilder().create();

    public HttpRequest(String urlIn) {
        try {
            this.url = new URL(urlIn);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean createConnection(String requestMethod) {
        try{
            this.connection = (HttpsURLConnection) url.openConnection();
            this.connection.setRequestMethod(requestMethod);
            this.setHeader("User-Agent", "7w!zC&FJNcRfUjXn2r5u8xDG");
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //Requests
    public int post() {
        this.connection.setConnectTimeout(500);
        this.connection.setRequestProperty("Content-Length", "" + data.getBytes().length);

        this.connection.setDoOutput(true);

        try {
            connection.getOutputStream().write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
//        try {
//            connection.getOutputStream().write("e".getBytes("UTF-8"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        try {
            return this.connection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public JsonElement get() {
        this.connection.setRequestProperty("Content-Length", "" + data.getBytes().length);

        this.connection.setDoOutput(true);

        StringBuilder response = new StringBuilder();
        try (DataInputStream reader = new DataInputStream(connection.getInputStream())) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return gson.fromJson(response.toString(), JsonElement.class);
    }

    public InputStream download() {
        this.connection.setRequestProperty("Content-Length", "" + data.getBytes().length);

        this.connection.setDoOutput(true);

        try {
            return this.connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Setters
    public void setData(String contentType, String data) {
        this.connection.setRequestProperty("Content-Type", contentType);
        this.data = data;
    }

    public void setHeader(String key, String value) {
        this.connection.setRequestProperty(key, value);
    }

    //Getters
    public int getResponseCode() {
        int code = 0;
        try {
            code = connection.getResponseCode();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    public HttpResponse sendRequest() throws IOException {
        this.connection.setRequestProperty("Content-Length", "" + data.getBytes().length);

        this.connection.setDoOutput(true);
        this.connection.setDoInput(true);

        if (this.data.length() > 0) {
            try {
                connection.getOutputStream().write(data.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new HttpResponse(this.connection);
    }

    public boolean download(String destination, BiConsumer<Integer, Integer> updater) {
        this.connection.setRequestProperty("Content-Length", "" + data.getBytes().length);

        this.connection.setDoOutput(true);
        this.connection.setDoInput(true);

        if (this.data.length() > 0) {
            try {
                connection.getOutputStream().write(data.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int size = Integer.parseInt(this.connection.getHeaderField("Content-Length"));
        int progress = 0;
        try (BufferedInputStream in = new BufferedInputStream(this.connection.getInputStream()); FileOutputStream fileOutputStream = new FileOutputStream(destination)) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
               fileOutputStream.write(dataBuffer, 0, bytesRead);
               progress+=bytesRead;
               updater.accept(progress, size);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean downloadZip(String destination, BiConsumer<Integer, Integer> updater) throws IOException {

        this.connection.setRequestProperty("Content-Length", "" + data.getBytes().length);

        this.connection.setDoOutput(true);
        this.connection.setDoInput(true);

        if (this.data.length() > 0) {
            try {
                connection.getOutputStream().write(data.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int size = Integer.parseInt(this.connection.getHeaderField("Content-Length"));
        int progress = 0;
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(this.connection.getInputStream());
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File destFile = new File(destination, zipEntry.getName());

            String destDirPath = new File(destination).getCanonicalPath();
            String destFilePath = destFile.getCanonicalPath();

            if (!destFilePath.startsWith(destDirPath + File.separator)) {
                throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
            }
            if (zipEntry.isDirectory()) {
                if (!destFile.isDirectory() && !destFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + destFile);
                }
            } else {
                File parent = destFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                FileOutputStream fos = new FileOutputStream(destFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                    progress+=len;
                    updater.accept(progress, size);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();

        return false;
    }

    public HttpsURLConnection getConnection() {
        return connection;
    }

    //Checks
    public boolean isErrorResponse() {
        return this.getResponseCode() >= 400;
    }

}
