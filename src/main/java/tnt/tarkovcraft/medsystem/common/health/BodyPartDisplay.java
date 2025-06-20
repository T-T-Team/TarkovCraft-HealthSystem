package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import org.joml.Vector2f;
import org.joml.Vector4f;
import tnt.tarkovcraft.core.util.ObjectCache;

public record BodyPartDisplay(String source, Vec2 pos, Vec2 size) {

    public static final ObjectCache<String, Component> DISPLAY_NAME = new ObjectCache<>(name -> Component.translatable("label.tarkovcraft.bodypart." + name));
    public static final Codec<BodyPartDisplay> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("source").forGetter(BodyPartDisplay::source),
            Vec2.CODEC.fieldOf("pos").forGetter(BodyPartDisplay::pos),
            Vec2.CODEC.fieldOf("size").forGetter(BodyPartDisplay::size)
    ).apply(instance, BodyPartDisplay::new));

    public Vector4f getGuiPosition(float scale, Vector2f center) {
        float sizeX = size.x * scale;
        float sizeY = size.y * scale;
        return new Vector4f(
                center.x + pos.x * scale - sizeX / 2.0F,
                center.y + pos.y * scale,
                sizeX,
                sizeY
        );
    }

    public Component getDisplayName() {
        return DISPLAY_NAME.get(this.source);
    }
}
