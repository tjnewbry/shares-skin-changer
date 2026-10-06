package net.tjnewbry.skinhead.mod;

import com.mojang.authlib.properties.Property;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/** The skin Mojang's session server currently serves for this player, shared by the title screen and the skin screen. */
final class ServerSkin {

    // Every reload is written to the game log, including the ones that are silent on screen
    private static final Logger LOGGER = LoggerFactory.getLogger("skinhead");

    private static SkinTextures textures; // null until the first reload answers
    private static boolean reloading;

    private ServerSkin() {
    }

    static SkinTextures textures() {
        return textures;
    }

    static boolean isReloading() {
        return reloading;
    }

    /**
     * Fetches the signed profile Mojang's server now serves and loads its skin. The game reuses the
     * profile it fetched at startup (singleplayer hosts with it) and keys its skin cache by the
     * "textures" property, so swapping in the server's copy updates the in-game skin too.
     * The returned future completes on the game thread, after {@link #textures()} is updated.
     */
    static CompletableFuture<SkinTextures> reload(MinecraftClient client) {
        reloading = true;
        LOGGER.info("Fetching skin...");
        UUID uuid = client.getSession().getUuidOrNull();
        return CompletableFuture.supplyAsync(() -> client.getSessionService().fetchProfile(uuid, true), Util.getMainWorkerExecutor())
                .thenComposeAsync(result -> {
                    if (result == null) throw new CompletionException(new IOException("Mojang's server returned no profile"));
                    Property packed = client.getSessionService().getPackedTextures(result.profile());
                    if (packed != null) client.getGameProfile().getProperties().replaceValues("textures", List.of(packed));
                    return client.getSkinProvider().fetchSkinTextures(result.profile());
                }, client)
                .whenCompleteAsync((loaded, error) -> {
                    reloading = false;
                    if (error == null) {
                        textures = loaded;
                        LOGGER.info("Reloaded!");
                    } else {
                        LOGGER.warn("Couldn't reload skin from server", error);
                    }
                }, client);
    }
}
