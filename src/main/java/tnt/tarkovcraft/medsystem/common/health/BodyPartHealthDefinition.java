package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import tnt.tarkovcraft.core.util.Codecs;

public final class BodyPartHealthDefinition {

    public static final Codec<BodyPartHealthDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("vital", false).forGetter(t -> t.vital),
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("health").forGetter(t -> t.maxHealth),
            Codecs.enumCodec(BodyPartGroup.class).optionalFieldOf("group", BodyPartGroup.OTHER).forGetter(t -> t.bodyPartGroup)
    ).apply(instance, BodyPartHealthDefinition::new));

    private final boolean vital;
    private final float maxHealth;
    private final BodyPartGroup bodyPartGroup;

    public BodyPartHealthDefinition(boolean vital, float maxHealth, BodyPartGroup bodyPartGroup) {
        this.vital = vital;
        this.maxHealth = maxHealth;
        this.bodyPartGroup = bodyPartGroup;
    }

    public BodyPartHealth createContainer() {
        return new BodyPartHealth(this.vital, this.maxHealth, this.bodyPartGroup);
    }

    public BodyPartGroup getBodyPartGroup() {
        return bodyPartGroup;
    }
}
