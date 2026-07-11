package com.replaceit.network;

import com.replaceit.ReplaceIT;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TogglePayload() implements CustomPacketPayload {
    public static final Type TYPE = new Type<>(Identifier.fromNamespaceAndPath(ReplaceIT.MOD_ID, "toggle_replace"));
    public static final StreamCodec CODEC = StreamCodec.unit(new TogglePayload());

    @Override
    public Type type() {
        return TYPE;
    }
}