package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ExtraCodecs;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.health.reaction.ReactionDefinition;

import javax.annotation.Nullable;
import java.util.*;

public final class LimbDefinition {

    public static final Codec<LimbDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("vital", false).forGetter(t -> t.vital),
            Codec.STRING.optionalFieldOf("parent").forGetter(t -> Optional.ofNullable(t.parent)),
            Codec.floatRange(0.0F, 10.0F).optionalFieldOf("parentDamageScale", 1.0F).forGetter(t -> t.parentDamageScale),
            Codec.floatRange(0.0F, 10.0F).optionalFieldOf("damageScale", 1.0F).forGetter(t -> t.damageScale),
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("health").forGetter(t -> t.maxHealth),
            Codecs.enumCodec(LimbType.class).optionalFieldOf("group", LimbType.OTHER).forGetter(t -> t.limbType),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, ReactionDefinition.CODEC).optionalFieldOf("reactions", Collections.emptyMap()).forGetter(t -> t.reactions)
    ).apply(instance, LimbDefinition::new));

    private final boolean vital;
    @Nullable
    private final String parent;
    private final float parentDamageScale;
    private final float damageScale;
    private final float maxHealth;
    private final LimbType limbType;
    private final Map<UUID, ReactionDefinition> reactions;

    public LimbDefinition(boolean vital, Optional<String> parent, float parentDamageScale, float damageScale, float maxHealth, LimbType limbType, Map<UUID, ReactionDefinition> reactions) {
        this.vital = vital;
        this.parent = parent.orElse(null);
        this.parentDamageScale = parentDamageScale;
        this.damageScale = damageScale;
        this.maxHealth = maxHealth;
        this.limbType = limbType;
        this.reactions = reactions;
    }

    @Nullable
    public String getParent() {
        return parent;
    }

    public boolean isVital() {
        return vital;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public float getParentDamageScale() {
        return parentDamageScale;
    }

    public float getDamageScale() {
        return damageScale;
    }

    public Limb createLimbInstance(String code) {
        Limb part = new Limb(code, this.vital, this.maxHealth, this.parentDamageScale, this.damageScale, this.limbType);
        part.setDefinition(this);
        return part;
    }

    public Collection<ReactionDefinition> getReactions() {
        return reactions.values();
    }

    Map<UUID, ReactionDefinition> getReactionMap() {
        return reactions;
    }

    public LimbType getBodyPartGroup() {
        return limbType;
    }
}
