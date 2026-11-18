
package de.tejs.frenchitemtranslator;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Mod(FrenchItemTranslator.MOD_ID)
public final class FrenchItemTranslator {

    public static final String MOD_ID = "frenchitemtranslator";

    private static final KeyMapping READ_KEY = new KeyMapping(
            "key.frenchitemtranslator.read",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.misc"
    );

    public FrenchItemTranslator() {}

    @Mod.EventBusSubscriber(
            modid = MOD_ID,
            bus = Mod.EventBusSubscriber.Bus.MOD,
            value = Dist.CLIENT
    )
    public static class KeyEvents {

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(READ_KEY);
        }
    }

    @Mod.EventBusSubscriber(
            modid = MOD_ID,
            bus = Mod.EventBusSubscriber.Bus.FORGE,
            value = Dist.CLIENT
    )
    public static class ClientEvents {

        private static ItemStack lastStack = ItemStack.EMPTY;
        private static int highlightTimer = 0;
        private static String currentGloss = null;

        @SubscribeEvent
        public static void onItemTooltip(ItemTooltipEvent event) {

            if (event.getItemStack().isEmpty()) {
                return;
            }

            String frenchName =
                    event.getItemStack().getHoverName().getString();

            if (frenchName.isBlank()) {
                return;
            }

            String germanGloss =
                    FrenchWordTranslator.translateLiterally(frenchName);

            if (germanGloss == null ||
                    germanGloss.equalsIgnoreCase(frenchName)) {
                return;
            }

            int insertAt = Math.min(1, event.getToolTip().size());

            event.getToolTip().add(
                    insertAt,
                    Component.literal(germanGloss)
                            .withStyle(ChatFormatting.GRAY)
            );
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {

            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;

            if (player == null) {
                highlightTimer = 0;
                currentGloss = null;
                lastStack = ItemStack.EMPTY;
                return;
            }

            // Vorlesefunktion: Taste V
            while (READ_KEY.consumeClick()) {

                ItemStack selected =
                        player.getInventory().getSelected();

                if (!selected.isEmpty()) {
                    speakFrench(
                            selected.getHoverName().getString()
                    );
                }
            }

            ItemStack stack = player.getInventory().getSelected();

            if (stack.isEmpty()) {
                highlightTimer = 0;
                currentGloss = null;

            } else if (!lastStack.isEmpty()
                    && stack.is(lastStack.getItem())
                    && stack.getHoverName().equals(lastStack.getHoverName())) {

                if (highlightTimer > 0) {
                    highlightTimer--;
                }

            } else {

                highlightTimer = (int) (
                        40.0D *
                        mc.options.notificationDisplayTime().get()
                );

                String french = stack.getHoverName().getString();

                String gloss = french.isBlank()
                        ? null
                        : FrenchWordTranslator.translateLiterally(french);

                currentGloss =
                        (gloss == null ||
                                gloss.equalsIgnoreCase(french))
                                ? null
                                : gloss;
            }

            lastStack = stack;
        }

        // WINDOWS TEXT-TO-SPEECH
        private static void speakFrench(String text) {

            if (text == null || text.isBlank()) {
                return;
            }

            String encoded = Base64.getEncoder()
                    .encodeToString(
                            text.getBytes(StandardCharsets.UTF_8)
                    );

            String script =
                    "$text = [Text.Encoding]::UTF8.GetString(" +
                    "[Convert]::FromBase64String('" + encoded + "')); " +

                    "Add-Type -AssemblyName System.Speech; " +

                    "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +

                    "$culture = [Globalization.CultureInfo]::GetCultureInfo('fr-FR'); " +

                    "$voice = $s.GetInstalledVoices() | " +
                    "Where-Object { $_.Enabled -and " +
                    "$_.VoiceInfo.Culture.Name -eq 'fr-FR' } | " +
                    "Select-Object -First 1; " +

                    "if ($null -ne $voice) { " +
                    "$s.SelectVoice($voice.VoiceInfo.Name); } " +

                    "$s.Speak($text); " +
                    "$s.Dispose();";

            Thread speechThread = new Thread(() -> {

                try {

                    Process process = new ProcessBuilder(
                            "powershell.exe",
                            "-NoProfile",
                            "-NonInteractive",
                            "-Command",
                            script
                    ).redirectErrorStream(true).start();

                    try (var input = process.getInputStream()) {
                        input.transferTo(
                                java.io.OutputStream.nullOutputStream()
                        );
                    }

                    process.waitFor();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }, "FrenchItemTranslator-TTS");

            speechThread.setDaemon(true);
            speechThread.start();
        }

        @SubscribeEvent
        public static void onOverlayPre(RenderGuiOverlayEvent.Pre event) {

            if (event.getOverlay() != VanillaGuiOverlay.ITEM_NAME.type()) {
                return;
            }

            Minecraft mc = Minecraft.getInstance();

            if (mc.player == null ||
                    mc.gameMode == null ||
                    mc.player.isSpectator()) {
                return;
            }

            if (currentGloss == null ||
                    lastStack.isEmpty() ||
                    highlightTimer <= 0) {
                return;
            }

            int alpha = (int) (
                    (float) highlightTimer * 256.0F / 10.0F
            );

            if (alpha > 255) {
                alpha = 255;
            }

            if (alpha <= 4) {
                return;
            }

            GuiGraphics g = event.getGuiGraphics();

            int width = event.getWindow().getGuiScaledWidth();
            int height = event.getWindow().getGuiScaledHeight();

            MutableComponent french = Component.empty()
                    .append(lastStack.getHoverName())
                    .withStyle(lastStack.getRarity().color);

            if (lastStack.hasCustomHoverName()) {
                french.withStyle(ChatFormatting.ITALIC);
            }

            Component frenchLine = lastStack.getHighlightTip(french);

            Component germanLine = Component.literal(currentGloss)
                    .withStyle(ChatFormatting.GRAY);

            int y = height - 59;

            if (!mc.gameMode.canHurtPlayer()) {
                y += 14;
            }

            drawCentered(g, mc, frenchLine, width, y, alpha);
            drawCentered(g, mc, germanLine, width, y - 11, alpha);

            event.setCanceled(true);
        }

        private static void drawCentered(
                GuiGraphics g,
                Minecraft mc,
                Component text,
                int screenWidth,
                int y,
                int alpha
        ) {

            int textWidth = mc.font.width(text);
            int x = (screenWidth - textWidth) / 2;

            int bg = ((int) (alpha * 0.4F)) << 24;

            g.fill(
                    x - 2,
                    y - 2,
                    x + textWidth + 2,
                    y + 11,
                    bg
            );

            g.drawString(
                    mc.font,
                    text,
                    x,
                    y,
                    0xFFFFFF | (alpha << 24)
            );
        }
    }
}
