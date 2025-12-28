package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.Vector4i;

import java.util.Map;
import java.util.function.BiConsumer;

public record HealthContainerDisplay(Map<String, DisplayData> displayDataMap) {

    public static final Codec<HealthContainerDisplay> CODEC = Codec.unboundedMap(Codec.STRING, DisplayData.CODEC)
            .xmap(HealthContainerDisplay::new, HealthContainerDisplay::displayDataMap);

    public void accept(BiConsumer<String, DisplayData> consumer) {
        this.displayDataMap.forEach(consumer);
    }

    public record DisplayData(Vec2 pos, Vec2 size) {

        public static final Codec<DisplayData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Vec2.CODEC.fieldOf("pos").forGetter(DisplayData::pos),
                Vec2.CODEC.fieldOf("size").forGetter(DisplayData::size)
        ).apply(instance, DisplayData::new));

        public Vector4f getPos(float scale, Vector2f center) {
            float sizeX = size.x * scale;
            float sizeY = size.y * scale;
            return new Vector4f(
                    center.x + pos.x * scale - sizeX / 2.0F,
                    center.y + pos.y * scale,
                    sizeX,
                    sizeY
            );
        }

        public Vector4i getGuiPos(float scale, Vector2f center) {
            float sizeX = size.x * scale;
            float sizeY = size.y * scale;
            return new Vector4i(
                    Mth.floor(center.x + pos.x * scale - sizeX / 2.0F),
                    Mth.floor(center.y + pos.y * scale),
                    Mth.ceil(sizeX),
                    Mth.ceil(sizeY)
            );
        }
    }
}
