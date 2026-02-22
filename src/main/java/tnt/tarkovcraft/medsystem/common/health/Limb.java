package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventSources;

import java.util.Objects;

public final class Limb {

    public static final Codec<Limb> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LimbDefinition.CODEC.fieldOf("definition").forGetter(t -> t.definition),
            Codec.STRING.fieldOf("code").forGetter(t -> t.limbCode),
            Codec.FLOAT.fieldOf("health").forGetter(t -> t.health),
            Codec.FLOAT.fieldOf("maxHealth").forGetter(t -> t.maxHealth),
            Codec.FLOAT.fieldOf("originalMaxHealth").forGetter(t -> t.originalMaxHealth),
            StatusEffectMap.CODEC.fieldOf("statusEffects").forGetter(t -> t.statusEffects)
    ).apply(instance, Limb::new));

    private final LimbDefinition definition;
    private final String limbCode;
    private final float originalMaxHealth;
    private float health;
    private float maxHealth;
    private final Component displayName;
    private final StatusEffectMap statusEffects;

    Limb(LimbDefinition definition, String code) {
        this.definition = definition;
        this.limbCode = code;
        this.health = this.definition.baseHealth();
        this.maxHealth = this.definition.baseHealth();
        this.originalMaxHealth = this.definition.baseHealth();
        this.displayName = getDisplayName(this.limbCode);
        this.statusEffects = new StatusEffectMap();
    }

    private Limb(LimbDefinition definition, String limbCode, float health, float maxHealth, float originalMaxHealth, StatusEffectMap statusEffects) {
        this.definition = definition;
        this.limbCode = limbCode;
        this.health = health;
        this.maxHealth = maxHealth;
        this.originalMaxHealth = originalMaxHealth;
        this.displayName = getDisplayName(this.limbCode);
        this.statusEffects = statusEffects;
    }

    public String getLimbCode() {
        return limbCode;
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    public boolean shouldOwnerDie() {
        return this.isVital() && this.health <= 0.0F;
    }

    public boolean isDead() {
        return this.health <= 0.0F;
    }

    public boolean isAlive() {
        return !this.isDead();
    }

    public boolean isVital() {
        return this.definition.vital();
    }

    public LimbType getType() {
        return this.definition.type();
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

    public float getScaledDamage(float incomingDamage) {
        return incomingDamage * this.definition.damageConfiguration().scale();
    }

    public float getScaledTransferDamage(float leftoverDamage) {
        return leftoverDamage * this.definition.damageConfiguration().transferScale();
    }

    public void tick(HealthContainer container, LivingEntity entity) {
        this.statusEffects.tick(container, entity, this);
        if (entity.level().getGameTime() % 20 == 0) {
            StatusEffectEventContext ctx = StatusEffectEventContext.simple(entity, container, this);
            MedicalSystem.STATUS_EFFECT_EVENTS.triggerEvent(MedSystemStatusEffectEventSources.UPDATE, ctx);
        }
    }

    public boolean canApplyStatusEffect(StatusEffectType<?> statusEffect) {
        return this.definition.damageConfiguration().isStatusEffectAllowed(statusEffect);
    }

    public boolean is(LimbType type) {
        return this.definition.type() == type;
    }

    public boolean isLeg() {
        return this.is(LimbType.LEG);
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

    @Override
    public String toString() {
        return this.limbCode;
    }

    public static Component getDisplayName(String code) {
        return Component.translatable("medsystem.limb." + code);
    }
}
