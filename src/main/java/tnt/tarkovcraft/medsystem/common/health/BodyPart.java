package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import tnt.tarkovcraft.core.util.Codecs;

public final class BodyPart {

    public static final Codec<BodyPart> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("vital").forGetter(t -> t.vital),
            Codec.FLOAT.fieldOf("health").forGetter(t -> t.health),
            Codec.FLOAT.fieldOf("maxHealth").forGetter(t -> t.maxHealth),
            Codec.FLOAT.fieldOf("parentDamageScale").forGetter(t -> t.parentDamageScale),
            Codec.FLOAT.fieldOf("damageScale").forGetter(t -> t.damageScale),
            Codecs.simpleEnumCodec(BodyPartGroup.class).fieldOf("group").forGetter(t -> t.group)
    ).apply(instance, BodyPart::new));

    private final boolean vital;
    private float health;
    private float maxHealth;
    private final float parentDamageScale;
    private final float damageScale;
    private final BodyPartGroup group;

    public BodyPart(boolean vital, float maxHealth, float parentDamageScale, float damageScale, BodyPartGroup group) {
        this(vital, maxHealth, maxHealth, parentDamageScale, damageScale, group);
    }

    private BodyPart(boolean vital, float health, float maxHealth, float parentDamageScale, float damageScale, BodyPartGroup group) {
        this.vital = vital;
        this.health = health;
        this.maxHealth = maxHealth;
        this.parentDamageScale = parentDamageScale;
        this.damageScale = damageScale;
        this.group = group;
    }

    public float getParentDamageScale() {
        return parentDamageScale;
    }

    public float getDamageScale() {
        return damageScale;
    }

    public boolean shouldOwnerDie() {
        return this.vital && this.health <= 0.0F;
    }

    public boolean isDead() {
        return this.health <= 0.0F;
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

    public float getHealthPercent() {
        return this.health / this.maxHealth;
    }

    public void setHealth(float health) {
        this.health = Mth.clamp(health, 0, maxHealth);
    }

    public void heal(float amount) {
        this.setHealth(this.health + amount);
    }

    public void hurt(float amount) {
        this.setHealth(this.health - amount);
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
        this.setHealth(this.health);
    }

    public float getMaxHealAmount() {
        return this.maxHealth - this.health;
    }
}
