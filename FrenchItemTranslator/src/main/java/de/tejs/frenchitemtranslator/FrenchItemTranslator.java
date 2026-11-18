package de.tejs.frenchitemtranslator;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side tooltip translator. Minecraft must be set to Français (France)
 * so the item's hover name is French before the literal German gloss is added.
 *
 * The German gloss is shown in the inventory tooltip and also above the
 * item name that pops up over the hotbar when you select an item.
 */
@Mod(FrenchItemTranslator.MOD_ID)
public final class FrenchItemTranslator {
    public static final String MOD_ID = "frenchitemtranslator";

    public FrenchItemTranslator() {
        // Handlers are registered by the Forge event-bus subscriber below.
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ClientEvents {
        // Mirrors the vanilla "selected item name" timer so we know when to draw.
        private static ItemStack lastStack = ItemStack.EMPTY;
        private static int highlightTimer = 0;
        private static String currentGloss = null;

        private ClientEvents() {}

        /** Inventory / hover tooltip. */
        @SubscribeEvent
        public static void onItemTooltip(ItemTooltipEvent event) {
            if (event.getItemStack().isEmpty()) {
                return;
            }

            String frenchName = event.getItemStack().getHoverName().getString();
            if (frenchName.isBlank()) {
                return;
            }

            String germanGloss = FrenchWordTranslator.translateLiterally(frenchName);
            if (germanGloss == null || germanGloss.equalsIgnoreCase(frenchName)) {
                return;
            }

            // The first tooltip line is the item name; insert directly beneath it.
            int insertAt = Math.min(1, event.getToolTip().size());
            event.getToolTip().add(insertAt,
                    Component.literal(germanGloss).withStyle(ChatFormatting.GRAY));
        }

        /** Same timing logic as vanilla's selected-item-name popup above the hotbar. */
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
                highlightTimer = (int) (40.0D * mc.options.notificationDisplayTime().get());
                String french = stack.getHoverName().getString();
                String gloss = french.isBlank() ? null : FrenchWordTranslator.translateLiterally(french);
                currentGloss = (gloss == null || gloss.equalsIgnoreCase(french)) ? null : gloss;
            }
            lastStack = stack;
        }

        /**
         * Replaces the vanilla item-name popup (only when we have a German gloss):
         * French name at the vanilla position, German gloss one line above it.
         */
        @SubscribeEvent
        public static void onOverlayPre(RenderGuiOverlayEvent.Pre event) {
            if (event.getOverlay() != VanillaGuiOverlay.ITEM_NAME.type()) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.gameMode == null || mc.player.isSpectator()) {
                return;
            }
            if (currentGloss == null || lastStack.isEmpty() || highlightTimer <= 0) {
                return;
            }

            int alpha = (int) ((float) highlightTimer * 256.0F / 10.0F);
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
            Component germanLine = Component.literal(currentGloss).withStyle(ChatFormatting.GRAY);

            int y = height - 59;
            if (!mc.gameMode.canHurtPlayer()) {
                y += 14;
            }

            drawCentered(g, mc, frenchLine, width, y, alpha);
            drawCentered(g, mc, germanLine, width, y - 11, alpha);

            event.setCanceled(true); // skip the vanilla one, we drew it ourselves
        }

        private static void drawCentered(GuiGraphics g, Minecraft mc, Component text, int screenWidth, int y, int alpha) {
            int textWidth = mc.font.width(text);
            int x = (screenWidth - textWidth) / 2;
            int bg = ((int) (alpha * 0.4F)) << 24;
            g.fill(x - 2, y - 2, x + textWidth + 2, y + 9 + 2, bg);
            g.drawString(mc.font, text, x, y, 0xFFFFFF | (alpha << 24));
        }
    }
}
