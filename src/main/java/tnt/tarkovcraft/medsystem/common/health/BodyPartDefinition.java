package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import tnt.tarkovcraft.core.util.Codecs;

import javax.annotation.Nullable;
import java.util.Optional;

public final class BodyPartDefinition {

    public static final Codec<BodyPartDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("vital", false).forGetter(t -> t.vital),
            Codec.STRING.optionalFieldOf("parent").forGetter(t -> Optional.ofNullable(t.parent)),
            Codec.floatRange(0.0F, 10.0F).optionalFieldOf("parentDamageScale", 1.0F).forGetter(t -> t.parentDamageScale),
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("health").forGetter(t -> t.maxHealth),
            Codecs.enumCodec(BodyPartGroup.class).optionalFieldOf("group", BodyPartGroup.OTHER).forGetter(t -> t.bodyPartGroup)
    ).apply(instance, BodyPartDefinition::new));

    private final boolean vital;
    @Nullable
    private final String parent;
    private final float parentDamageScale;
    private final float maxHealth;
    private final BodyPartGroup bodyPartGroup;

    public BodyPartDefinition(boolean vital, Optional<String> parent, float parentDamageScale, float maxHealth, BodyPartGroup bodyPartGroup) {
        this.vital = vital;
        this.parent = parent.orElse(null);
        this.parentDamageScale = parentDamageScale;
        this.maxHealth = maxHealth;
        this.bodyPartGroup = bodyPartGroup;
    }

    @Nullable
    public String getParent() {
        return parent;
    }

    public float getParentDamageScale() {
        return parentDamageScale;
    }

    public boolean isVital() {
        return vital;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public BodyPart createContainer() {
        return new BodyPart(this.vital, this.maxHealth, this.parentDamageScale, this.bodyPartGroup);
    }

    public BodyPartGroup getBodyPartGroup() {
        return bodyPartGroup;
    }
}
