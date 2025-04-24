package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.util.Codecs;

public final class BodyPartHealth {

    public static final Codec<BodyPartHealth> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("vital").forGetter(t -> t.vital),
            Codec.FLOAT.fieldOf("health").forGetter(t -> t.health),
            Codec.FLOAT.fieldOf("maxHealth").forGetter(t -> t.maxHealth),
            Codecs.simpleEnumCodec(BodyPartGroup.class).fieldOf("group").forGetter(t -> t.group)
    ).apply(instance, BodyPartHealth::new));

    private final boolean vital;
    private float health;
    private float maxHealth;
    private final BodyPartGroup group;

    public BodyPartHealth(boolean vital, float maxHealth, BodyPartGroup group) {
        this(vital, maxHealth, maxHealth, group);
    }

    private BodyPartHealth(boolean vital, float health, float maxHealth, BodyPartGroup group) {
        this.vital = vital;
        this.health = health;
        this.maxHealth = maxHealth;
        this.group = group;
    }

    public boolean shouldOwnerDie() {
        return this.vital && this.health <= 0.0F;
    }

    public boolean isVital() {
        return vital;
    }

    public BodyPartGroup getGroup() {
        return group;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
    }
}
