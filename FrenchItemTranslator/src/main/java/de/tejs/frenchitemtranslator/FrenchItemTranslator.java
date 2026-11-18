package de.tejs.frenchitemtranslator;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Client-side tooltip translator. Minecraft must be set to Français (France)
 * so the item's hover name is French before the literal German gloss is added.
 */
@Mod(FrenchItemTranslator.MOD_ID)
public final class FrenchItemTranslator {
    public static final String MOD_ID = "frenchitemtranslator";

    public FrenchItemTranslator() {
        // Tooltip handler is registered by the Forge event-bus subscriber below.
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ClientEvents {
        private ClientEvents() {}

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
    }
}
