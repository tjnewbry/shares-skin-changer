package net.tjnewbry.skinhead.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PlayerSkinWidget;
import net.minecraft.text.Text;
import net.tjnewbry.skinhead.core.ApiClient;

public class SkinheadClient implements ClientModInitializer {

    private static final ApiClient API = new ApiClient();

    private static boolean bootReloadDone;
    private static PlayerSkinWidget titlePreview; // the server preview on the title screen currently shown

    /** Shared by every SkinChangerScreen. */
    public static ApiClient api() {
        return API;
    }

    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof TitleScreen)) return;

            ButtonWidget button = ButtonWidget.builder(Text.literal("Change Skin"),
                            b -> client.setScreen(new SkinChangerScreen(screen)))
                    .dimensions(4, 4, 98, 20)
                    .build();
            Screens.getButtons(screen).add(button);

            // What Mojang's server sees, sized exactly like the one in the skin screen; hidden until the server answers
            titlePreview = new PlayerSkinWidget(SkinChangerScreen.columnWidth(scaledWidth),
                    SkinChangerScreen.serverPreviewHeight(scaledHeight), client.getEntityModelLoader(), ServerSkin::textures);
            // Centered under the button
            titlePreview.setPosition(button.getX() + (button.getWidth() - titlePreview.getWidth()) / 2,
                    button.getY() + button.getHeight() + 4);
            titlePreview.visible = ServerSkin.textures() != null;
            titlePreview.active = false; // display only: inactive widgets ignore clicks, so it can't be dragged
            Screens.getButtons(screen).add(titlePreview);

            // Silently fetch the server skin once the game first reaches the title screen
            if (!bootReloadDone) {
                bootReloadDone = true;
                ServerSkin.reload(client).thenRun(() -> titlePreview.visible = ServerSkin.textures() != null);
            }
        });
    }
}
