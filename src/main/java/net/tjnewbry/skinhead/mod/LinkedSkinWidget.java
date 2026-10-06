package net.tjnewbry.skinhead.mod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.PlayerSkinWidget;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Text;

import java.util.function.Supplier;

/**
 * A vanilla PlayerSkinWidget whose drag rotation is passed on to a partner, so two previews turn together,
 * with an optional multiplayer-style name tag above the head.
 */
class LinkedSkinWidget extends PlayerSkinWidget {

    private LinkedSkinWidget partner;
    private Text nameTag;

    LinkedSkinWidget(int width, int height, EntityModelLoader models, Supplier<SkinTextures> textures) {
        super(width, height, models, textures);
    }

    static void link(LinkedSkinWidget a, LinkedSkinWidget b) {
        a.partner = b;
        b.partner = a;
    }

    /** Draws this text just above the widget, which is where the model's head is (it fills the widget's height). */
    LinkedSkinWidget nameTag(Text nameTag) {
        this.nameTag = nameTag;
        return this;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        if (nameTag == null) return;

        // Drawn the way the game draws name tags: white text over the player's text background opacity
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        int background = (int) (client.options.getTextBackgroundOpacity(0.25f) * 255) << 24;
        float x = getX() + (getWidth() - textRenderer.getWidth(nameTag)) / 2f;
        float y = getY() - textRenderer.fontHeight - 1;
        textRenderer.draw(nameTag, x, y, 0xFFFFFFFF, false, context.getMatrices().peek().getPositionMatrix(),
                context.getVertexConsumers(), TextRenderer.TextLayerType.NORMAL, background, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        context.draw();
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        super.onDrag(mouseX, mouseY, deltaX, deltaY);
        if (partner != null) partner.rotateWithPartner(mouseX, mouseY, deltaX, deltaY);
    }

    private void rotateWithPartner(double mouseX, double mouseY, double deltaX, double deltaY) {
        super.onDrag(mouseX, mouseY, deltaX, deltaY);
    }
}
