package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;

import java.util.Objects;

public final class Limb {

    public static final Codec<Limb> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("code").forGetter(t -> t.limbCode),
            Codec.BOOL.fieldOf("vital").forGetter(t -> t.vital),
            Codec.FLOAT.fieldOf("health").forGetter(t -> t.health),
            Codec.FLOAT.fieldOf("maxHealth").forGetter(t -> t.maxHealth),
            Codec.FLOAT.fieldOf("originalMaxHealth").forGetter(t -> t.originalMaxHealth),
            Codec.FLOAT.fieldOf("parentDamageScale").forGetter(t -> t.parentDamageScale),
            Codec.FLOAT.fieldOf("damageScale").forGetter(t -> t.damageScale),
            Codecs.simpleEnumCodec(LimbType.class).fieldOf("group").forGetter(t -> t.type),
            StatusEffectMap.CODEC.fieldOf("statusEffects").forGetter(t -> t.statusEffects)
    ).apply(instance, Limb::new));

    private LimbDefinition definition;
    private final String limbCode;
    private final boolean vital;
    private final float originalMaxHealth;
    private float health;
    private float maxHealth;
    private final float parentDamageScale;
    private final float damageScale;
    private final LimbType type;
    private final Component displayName;
    private final StatusEffectMap statusEffects;

    public Limb(String limbCode, boolean vital, float maxHealth, float parentDamageScale, float damageScale, LimbType type) {
        this(limbCode, vital, maxHealth, maxHealth, maxHealth, parentDamageScale, damageScale, type, new StatusEffectMap());
    }

    private Limb(String limbCode, boolean vital, float health, float maxHealth, float originalMaxHealth, float parentDamageScale, float damageScale, LimbType type, StatusEffectMap statusEffects) {
        this.limbCode = limbCode;
        this.vital = vital;
        this.health = health;
        this.maxHealth = maxHealth;
        this.originalMaxHealth = originalMaxHealth;
        this.parentDamageScale = parentDamageScale;
        this.damageScale = damageScale;
        this.type = type;
        this.displayName = Component.translatable("medsystem.bodypart." + limbCode);
        this.statusEffects = statusEffects;
    }

    public void setDefinition(LimbDefinition definition) {
        this.definition = definition;
    }

    public String getLimbCode() {
        return limbCode;
    }

    public Component getDisplayName() {
        return this.displayName;
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

    public LimbType getType() {
        return type;
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

    public float getOriginalMaxHealth() {
        return originalMaxHealth;
    }

    public StatusEffectMap getStatusEffects() {
        return this.statusEffects;
    }

    public void trigger(HealthContainer container, LivingEntity entity, DamageSource source) {
        this.definition.getReactions().forEach(def -> def.react(container, entity, source, this));
    }

    public void tick(HealthContainer container, LivingEntity entity) {
        this.statusEffects.tick(container, entity, this);
        this.definition.getReactions().forEach(def -> def.react(container, entity, null, this));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Limb part)) return false;
        return Objects.equals(limbCode, part.limbCode);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(limbCode);
    }
}
