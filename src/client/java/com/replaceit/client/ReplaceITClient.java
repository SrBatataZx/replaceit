package com.replaceit.client;

import com.replaceit.network.TogglePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.glfw.GLFW;

public class ReplaceITClient implements ClientModInitializer {
    private static KeyMapping toggleKey;
    public static boolean isReplaceModeOn = false;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.replaceit.toggle",
                GLFW.GLFW_KEY_R,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                isReplaceModeOn = !isReplaceModeOn;
                String status = isReplaceModeOn ? "§aON" : "§cOFF";
//                if (client.player != null) {
//                    client.player.sendSystemMessage(Component.literal("Modo de Troca: " + status));
//                }
                ClientPlayNetworking.send(new TogglePayload());
            }
        });

        // NOVO: Predição do Cliente para evitar o Bloco Fantasma
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (isReplaceModeOn) {
                ItemStack handStack = player.getItemInHand(hand);

                if (handStack.getItem() instanceof BlockItem blockItem) {
                    BlockState oldState = world.getBlockState(hitResult.getBlockPos());

                    // Se for igual, deixa o Minecraft normal agir (PASS)
                    if (oldState.getBlock() == blockItem.getBlock()) {
                        return InteractionResult.PASS;
                    }

                    // Retorna SUCCESS no Cliente! Isso cancela a colocação vanilla do bloco,
                    // e apenas avisa ao Servidor: "Eu cliquei, faça a troca aí!".
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        });
    }
}