package net.tjnewbry.skinhead.cli;

import net.tjnewbry.skinhead.core.ApiClient;
import net.tjnewbry.skinhead.core.ArmType;
import net.tjnewbry.skinhead.core.SkinFile;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {

        Config config = new Config("config.properties");

        if(config.isTokenExpired()) {
            System.out.println("TOKEN IS EXPIRED, please launch minecraft and then try again.");
            System.exit(1);
        }
        String token = config.getToken(); // fail before prompting for a skin, not after

        SkinFile skin = null;
        while (skin == null) {
            try {
                skin = SkinFile.load(expandHome(System.console().readLine("Enter the path to your skin file: ")));
            } catch (IllegalArgumentException e) {
                System.out.println("[!] " + e.getMessage());
            }
        }

        // Confirm skin type
        ArmType arm = skin.detectedArmType();
        String answer = System.console().readLine("It looks like this skin is designed to have " + arm.apiName() + " arms. Is this correct? (Y/IDK/N): ");
        if (answer.equalsIgnoreCase("N")) arm = arm.toggle();
        System.out.println("Selecting " + arm.apiName() + " arms...");

        try {
            ApiClient.Response response = new ApiClient().uploadSkin(token, skin.path(), arm);
            if (response.ok()) {
                System.out.println("Skin uploaded successfully!");
            } else {
                System.out.println("Failed to upload skin, response code: " + response.status());
                System.out.println(response.body());
            }
        } catch (Exception e) {
            System.out.println("[!] " + e.getMessage());
        }
    }

    // Expands a leading "~" to the user's home directory (shell behavior, so it lives in the CLI)
    private static Path expandHome(String input) {
        if (input.startsWith("~")) {
            input = System.getProperty("user.home") + input.substring(1); //I love linux
        }
        return Path.of(input);
    }
}
