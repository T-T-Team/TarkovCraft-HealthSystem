package tnt.tarkovcraft.medsystem.network.message;

import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.health.HealthContainerDefinition;
import tnt.tarkovcraft.medsystem.network.MedicalSystemNetwork;

import java.util.HashMap;
import java.util.Map;

// TODO use stream codecs
public record S2C_SendHealthDefinitions(Map<EntityType<?>, HealthContainerDefinition> definitionMap) implements CustomPacketPayload {

    public static final ResourceLocation PACKET_ID = MedicalSystemNetwork.createId(S2C_SendHealthDefinitions.class);
    public static final Type<S2C_SendHealthDefinitions> TYPE = new Type<>(PACKET_ID);
    public static final StreamCodec<FriendlyByteBuf, S2C_SendHealthDefinitions> CODEC = StreamCodec.of(
            (buffer, value) -> value.encode(buffer),
            S2C_SendHealthDefinitions::decode
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void encode(FriendlyByteBuf buf) {
        int pairs = this.definitionMap.size();
        buf.writeInt(pairs);
        for (Map.Entry<EntityType<?>, HealthContainerDefinition> entry : this.definitionMap.entrySet()) {
            buf.writeResourceLocation(BuiltInRegistries.ENTITY_TYPE.getKey(entry.getKey()));
            DataResult<Tag> result = HealthContainerDefinition.CODEC.encodeStart(NbtOps.INSTANCE, entry.getValue());
            CompoundTag tag = new CompoundTag();
            tag.put("data", result.getOrThrow());
            buf.writeNbt(tag);
        }
    }

    private static S2C_SendHealthDefinitions decode(FriendlyByteBuf buf) {
        int pairs = buf.readInt();
        Map<EntityType<?>, HealthContainerDefinition> map = new HashMap<>(pairs);
        for (int i = 0; i < pairs; i++) {
            ResourceLocation id = buf.readResourceLocation();
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
            CompoundTag tag = buf.readNbt();
            DataResult<HealthContainerDefinition> result = HealthContainerDefinition.CODEC.parse(NbtOps.INSTANCE, tag.get("data"));
            HealthContainerDefinition definition = result.getOrThrow();
            map.put(type, definition);
        }
        return new S2C_SendHealthDefinitions(map);
    }

    public void handleMessage(IPayloadContext context) {
        MedicalSystem.HEALTH_SYSTEM.importServerData(this.definitionMap);
    }
}
