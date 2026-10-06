package net.tjnewbry.skinhead.mod;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.ScrollableTextWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.tjnewbry.skinhead.core.ArmType;
import net.tjnewbry.skinhead.core.SkinFile;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Three columns: left picks the file and arm type, center previews the chosen skin with Upload
 * under it, right shows the skin Mojang's server serves (with Reload) above an output log.
 * Widgets are rebuilt from the fields below with clearAndInit() whenever they change.
 */
public class SkinChangerScreen extends Screen {

    private static final Identifier PREVIEW_TEXTURE = new Identifier("skinhead", "preview");
    private static final int MAX_COLUMN_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SPACING = 4;
    private static final int BUTTON_TEXT_PADDING = 4; // each side of a button sized to its label
    private static final int DONE_WIDTH = 200;        // same as the Done button on the Options screen

    private final Screen parent;
    private ThreePartsLayoutWidget layout;
    private SkinFile skin; // null until a valid file is chosen
    private ArmType armType = ArmType.CLASSIC;
    private boolean choosingFile; // the file dialog is open
    private boolean uploading;
    private String fileError; // shown in place of the file name while set
    private final List<Text> output = new ArrayList<>(); // right column log, oldest first
    private boolean serverSkinRequested;

    public SkinChangerScreen(Screen parent) {
        super(Text.literal("Change Skin"));
        this.parent = parent;
    }

    // Sizes depend only on the window size, so other screens (the title screen) can match them exactly

    static int columnWidth(int screenWidth) {
        return Math.min(MAX_COLUMN_WIDTH, screenWidth / 3 - SPACING * 4);
    }

    private static int previewHeight(int screenHeight) {
        int bodyHeight = screenHeight - ThreePartsLayoutWidget.DEFAULT_HEADER_FOOTER_HEIGHT * 2;
        return bodyHeight - BUTTON_HEIGHT - SPACING * 3;
    }

    /** Height of the small "what Mojang's server sees" preview. */
    static int serverPreviewHeight(int screenHeight) {
        return previewHeight(screenHeight) * 2 / 5;
    }

    @Override
    protected void init() {
        // Silently fetch what Mojang serves the first time the screen opens, so the server preview never shows a cached skin
        if (!serverSkinRequested) {
            serverSkinRequested = true;
            reloadServerSkin(false);
        }

        layout = new ThreePartsLayoutWidget(this);
        layout.addHeader(new TextWidget(title, textRenderer));
        layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, button -> close()).width(DONE_WIDTH).build());

        int columnWidth = columnWidth(width);
        int previewHeight = previewHeight(height);
        int columnHeight = previewHeight + SPACING + BUTTON_HEIGHT;
        int serverPreviewHeight = serverPreviewHeight(height);

        DirectionalLayoutWidget columns = layout.addBody(DirectionalLayoutWidget.horizontal().spacing(width / 3 - columnWidth));
        columns.getMainPositioner().alignTop();

        // Left: file picker, file name (or file error), arm type text (once a file is chosen)
        DirectionalLayoutWidget left = columns.add(DirectionalLayoutWidget.vertical().spacing(SPACING));
        ButtonWidget chooseButton = left.add(ButtonWidget.builder(Text.literal("Choose File..."), button -> chooseFile())
                .width(columnWidth).build());
        chooseButton.active = !choosingFile;
        // File name line, replaced by the error when the chosen file is unusable
        Text fileLabel = fileError != null
                ? Text.literal(fileError).formatted(Formatting.RED)
                : Text.literal(skin == null ? "No file selected" : skin.path().getFileName().toString()).formatted(Formatting.GRAY);
        left.add(new MultilineTextWidget(fileLabel, textRenderer).setMaxWidth(columnWidth).setCentered(true),
                Positioner::alignHorizontalCenter);
        // A little lower, and centered in the column as a block (its lines stay left-justified)
        if (skin != null) left.add(armText(), positioner -> positioner.alignHorizontalCenter().marginTop(SPACING * 2));

        // Center: preview of the chosen skin, hidden until a valid file is chosen (reads previewTextures()
        // every frame, so it matches the arm type text), then Upload
        DirectionalLayoutWidget center = columns.add(DirectionalLayoutWidget.vertical().spacing(SPACING));
        LinkedSkinWidget preview = center.add(
                new LinkedSkinWidget(columnWidth, previewHeight, client.getEntityModelLoader(), this::previewTextures));
        preview.visible = skin != null;
        ButtonWidget uploadButton = center.add(ButtonWidget.builder(Text.literal("Upload"), button -> upload()).width(columnWidth).build());
        uploadButton.active = skin != null && !uploading;

        // Right: what Mojang's server sees (turns with the center preview, name tag above it like in
        // multiplayer), Reload, then the output log
        int nameTagSpace = textRenderer.fontHeight + 2;
        DirectionalLayoutWidget right = columns.add(DirectionalLayoutWidget.vertical().spacing(SPACING));
        LinkedSkinWidget serverPreview = right.add(
                new LinkedSkinWidget(columnWidth, serverPreviewHeight, client.getEntityModelLoader(), ServerSkin::textures)
                        .nameTag(Text.literal(client.getGameProfile().getName())),
                positioner -> positioner.marginTop(nameTagSpace));
        serverPreview.visible = ServerSkin.textures() != null; // hidden until the server has answered
        LinkedSkinWidget.link(preview, serverPreview);
        Text reloadText = Text.literal("Reload");
        ButtonWidget reloadButton = right.add(ButtonWidget.builder(reloadText, button -> reloadServerSkin(true))
                .width(textRenderer.getWidth(reloadText) + BUTTON_TEXT_PADDING * 2).build(), Positioner::alignHorizontalCenter);
        reloadButton.active = !ServerSkin.isReloading();
        right.add(new ScrollableTextWidget(0, 0, columnWidth,
                columnHeight - nameTagSpace - serverPreviewHeight - BUTTON_HEIGHT - SPACING * 2, outputText(), textRenderer) {
            {
                setScrollY(getMaxScrollY()); // newest line in view
            }

            // Vanilla only draws the black box (and pads the text inside it) once the text overflows.
            // Do the same as soon as there's any text; the scrollbar still only appears on overflow.
            @Override
            public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                if (output.isEmpty() || textOverflows()) {
                    super.renderWidget(context, mouseX, mouseY, delta);
                    return;
                }
                drawBox(context, getX(), getY(), getWidth(), getHeight());
                renderContents(context, mouseX, mouseY, delta);
            }
        });

        layout.forEachChild(this::addDrawableChild);
        initTabNavigation();
    }

    @Override
    protected void initTabNavigation() {
        layout.refreshPositions();
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    @Override
    public void removed() {
        client.getTextureManager().destroyTexture(PREVIEW_TEXTURE);
    }

    /**
     * "3px / Slim arms detected. / Click here to change it." and, once toggled, "... selected. / Click here to undo."
     * Every line is clickable and toggles the arm type.
     */
    private DirectionalLayoutWidget armText() {
        boolean detected = armType == skin.detectedArmType();
        Text[] lines = {
                Text.literal(armType.pixels() + "px"),
                Text.literal((armType == ArmType.SLIM ? "Slim" : "Classic") + " arms " + (detected ? "detected." : "selected.")),
                clickLine(detected)
        };
        // Always as wide as "Click here to change it.", so the block doesn't shift when the text toggles
        int blockWidth = textRenderer.getWidth(clickLine(true));

        DirectionalLayoutWidget block = DirectionalLayoutWidget.vertical().spacing(1);
        for (Text line : lines) {
            // An explicit "no underline" keeps PressableTextWidget's hover underline off, so only "here" is underlined
            Text styled = line.copy().styled(style -> style.withUnderline(false));
            PressableTextWidget widget = block.add(new PressableTextWidget(0, 0, blockWidth,
                    textRenderer.fontHeight, styled, button -> {
                        armType = armType.toggle();
                        clearAndInit();
                    }, textRenderer));
            widget.active = !uploading;
        }
        return block;
    }

    /** "Click here to change it." (or "... to undo.") with "here" underlined. */
    private static Text clickLine(boolean detected) {
        return Text.literal("Click ")
                .append(Text.literal("here").formatted(Formatting.UNDERLINE))
                .append(detected ? " to change it." : " to undo.");
    }

    /** The chosen file with the current arm type. Only drawn while a valid file is chosen. */
    private SkinTextures previewTextures() {
        return new SkinTextures(PREVIEW_TEXTURE, null, null, null, SkinTextures.Model.fromName(armType.apiName()), false);
    }

    /**
     * The native dialog blocks whatever thread opens it. On the game thread that freezes the game loop,
     * which starves the music stream (it stops, then restarts), so it opens on a worker thread instead.
     */
    private void chooseFile() {
        if (choosingFile) return;
        choosingFile = true;
        clearAndInit(); // disables Choose File while the dialog is open

        CompletableFuture.supplyAsync(SkinChangerScreen::openFileDialog, Util.getIoWorkerExecutor())
                .whenCompleteAsync((result, error) -> {
                    choosingFile = false;
                    if (result != null) loadSkin(Path.of(result)); // null when cancelled
                    clearAndInit();
                }, client);
    }

    private static String openFileDialog() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filters = stack.mallocPointer(1);
            filters.put(stack.UTF8("*.png")).flip();
            return TinyFileDialogs.tinyfd_openFileDialog("Select skin", null, filters, "PNG images", false);
        }
    }

    private void loadSkin(Path path) {
        try {
            skin = SkinFile.load(path);
            armType = skin.detectedArmType();
            fileError = null;
            try (InputStream in = Files.newInputStream(skin.path())) {
                // registerTexture closes the texture previously registered under this id
                client.getTextureManager().registerTexture(PREVIEW_TEXTURE, new NativeImageBackedTexture(NativeImage.read(in)));
            }
        } catch (IllegalArgumentException | IOException e) {
            skin = null;
            fileError = e.getMessage();
        }
    }

    private void upload() {
        if (skin == null || uploading) return;

        uploading = true;
        log(Text.literal("Uploading skin...").formatted(Formatting.GRAY));

        String token = client.getSession().getAccessToken(); // read on the render thread
        Path path = skin.path();
        ArmType arm = armType;
        CompletableFuture.supplyAsync(() -> {
            try {
                return SkinheadClient.api().uploadSkin(token, path, arm);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, Util.getMainWorkerExecutor()).whenCompleteAsync((response, error) -> {
            uploading = false;
            if (error != null) {
                log(Text.literal("Error: " + causeOf(error).getMessage()).formatted(Formatting.RED));
            } else if (response.ok()) {
                log(Text.literal("Uploaded!").formatted(Formatting.YELLOW));
                reloadServerSkin(true);
            } else {
                log(Text.literal("HTTP " + response.status() + "\n" + response.body()).formatted(Formatting.RED));
            }
        }, client);
    }

    /** Reloads the server preview (see {@link ServerSkin#reload}), logging it unless it's a silent reload. */
    private void reloadServerSkin(boolean announce) {
        CompletableFuture<?> reload = ServerSkin.reload(client); // marks it as reloading before the rebuild below
        if (announce) log(Text.literal("Fetching skin...").formatted(Formatting.GRAY));

        reload.whenCompleteAsync((textures, error) -> {
            if (announce) {
                log(error == null
                        ? Text.literal("Reloaded!").formatted(Formatting.GREEN)
                        : Text.literal("Couldn't reload: " + causeOf(error).getMessage()).formatted(Formatting.RED));
            } else {
                clearAndInit();
            }
        }, client);
    }

    /** Adds a line to the output log and rebuilds the screen to show it. */
    private void log(Text line) {
        output.add(line);
        clearAndInit();
    }

    private Text outputText() {
        MutableText text = Text.empty();
        for (int i = 0; i < output.size(); i++) {
            if (i > 0) text.append("\n");
            text.append(output.get(i));
        }
        return text;
    }

    private static Throwable causeOf(Throwable error) {
        return error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
    }
}
