package com.replaceit;

import com.replaceit.network.TogglePayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ReplaceIT implements ModInitializer {
    public static final String MOD_ID = "replaceit";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Set activePlayers = new HashSet<>();

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.serverboundPlay().register(TogglePayload.TYPE, TogglePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TogglePayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                UUID playerId = context.player().getUUID();
                if (activePlayers.contains(playerId)) {
                    activePlayers.remove(playerId);
                    context.player().sendSystemMessage(Component.literal("§cModo de Troca: OFF"));
                } else {
                    activePlayers.add(playerId);
                    context.player().sendSystemMessage(Component.literal("§aModo de Troca: ON"));
                }
            });
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            if (!activePlayers.contains(player.getUUID())) return InteractionResult.PASS;

            ItemStack handStack = player.getItemInHand(hand);
            if (!(handStack.getItem() instanceof BlockItem blockItem)) return InteractionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            BlockState oldState = world.getBlockState(pos);

            // NOVA VALIDAÇÃO: Se o bloco alvo for igual ao bloco na mão, ignora a ação do mod!
            if (oldState.getBlock() == blockItem.getBlock()) {
                return InteractionResult.PASS;
            }

            if (oldState.getDestroySpeed(world, pos) < 0) return InteractionResult.PASS;

            BlockEntity blockEntity = world.getBlockEntity(pos);

            List drops = Block.getDrops(oldState, (ServerLevel) world, pos, blockEntity, player, handStack);
            for (Object dropObj : drops) {
                if (dropObj instanceof ItemStack drop) {
                    if (!player.getInventory().add(drop)) {
                        player.drop(drop, false);
                    }
                }
            }

            BlockState newState = blockItem.getBlock().defaultBlockState();
            world.setBlock(pos, newState, 3);
            world.playSound(null, pos, newState.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0f, 1.0f);

            if (!player.isCreative()) {
                handStack.shrink(1);
            }

            return InteractionResult.SUCCESS;
        });
    }
}