package com.replaceit.network;

import com.replaceit.ReplaceIT;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncRotationPayload(int rotation) implements CustomPacketPayload {
    public static final Type TYPE = new Type<>(Identifier.fromNamespaceAndPath(ReplaceIT.MOD_ID, "sync_rot"));

    public static final StreamCodec CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SyncRotationPayload::rotation,
            SyncRotationPayload::new
    );

    @Override
    public Type type() {
        return TYPE;
    }
}