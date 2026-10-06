package net.tjnewbry.skinhead.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class ApiClient {

    private static final String SKINS_URL = "https://api.minecraftservices.com/minecraft/profile/skins";

    private final HttpClient client = HttpClient.newHttpClient();

    public record Response(int status, String body) {
        public boolean ok() {
            return status == 200;
        }
    }

    /** Blocking call; callers decide which thread to run it on. */
    public Response uploadSkin(String token, Path skin, ArmType arm) throws IOException, InterruptedException {
        String boundary = UUID.randomUUID().toString();

        ByteArrayOutputStream body = new ByteArrayOutputStream();
        writeAscii(body, "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"variant\"\r\n\r\n"
                + arm.apiName() + "\r\n");
        writeAscii(body, "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"skin.png\"\r\n"
                + "Content-Type: image/png\r\n\r\n");
        body.write(Files.readAllBytes(skin));
        writeAscii(body, "\r\n--" + boundary + "--\r\n");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SKINS_URL))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return new Response(response.statusCode(), response.body());
    }

    private static void writeAscii(ByteArrayOutputStream out, String s) {
        out.writeBytes(s.getBytes(StandardCharsets.US_ASCII));
    }
}
