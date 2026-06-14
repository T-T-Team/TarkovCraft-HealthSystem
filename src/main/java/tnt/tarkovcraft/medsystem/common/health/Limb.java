package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.config.HealthConfig;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectContext;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthEventSources;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class Limb {

    public static final Codec<Limb> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LimbDefinition.CODEC.fieldOf("definition").forGetter(t -> t.definition),
            Codec.STRING.fieldOf("code").forGetter(t -> t.limbCode),
            Codec.FLOAT.fieldOf("health").forGetter(t -> t.health),
            Codec.FLOAT.fieldOf("max_health").forGetter(t -> t.maxHealth),
            StatusEffectMap.CODEC.fieldOf("status_effects").forGetter(t -> t.statusEffects),
            Codec.unboundedMap(Identifier.CODEC, Codec.FLOAT).optionalFieldOf("reductions", Collections.emptyMap()).forGetter(t -> t.reductions)
    ).apply(instance, Limb::new));

    private final LimbDefinition definition;
    private final String limbCode;
    private float health;
    private float maxHealth;
    private final Component displayName;
    private final StatusEffectMap statusEffects;
    private final Object2FloatMap<Identifier> reductions;

    Limb(LimbDefinition definition, String code) {
        this.definition = definition;
        this.limbCode = code;
        this.health = this.definition.baseHealth();
        this.maxHealth = this.definition.baseHealth();
        this.displayName = getDisplayName(this.limbCode);
        this.statusEffects = new StatusEffectMap();
        this.reductions = new Object2FloatArrayMap<>();
    }

    private Limb(LimbDefinition definition, String limbCode, float health, float maxHealth, StatusEffectMap statusEffects, Map<Identifier, Float> reductions) {
        this.definition = definition;
        this.limbCode = limbCode;
        this.health = health;
        this.maxHealth = maxHealth;
        this.displayName = getDisplayName(this.limbCode);
        this.statusEffects = statusEffects;
        this.reductions = new Object2FloatArrayMap<>(reductions);
    }

    public Identifier getUniqueIdentifier() {
        return MedicalSystem.createIdentifier("limb/" + this.limbCode);
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
        return this.health / this.getMaxHealth();
    }

    public void setHealth(float health) {
        this.health = Mth.clamp(health, 0, this.getMaxHealth());
    }

    public void heal(float amount) {
        this.setHealth(this.health + amount);
    }

    public void healUpTo(float amount) {
        this.setHealth(Math.max(amount, this.health));
    }

    public void hurt(float amount) {
        this.setHealth(this.health - amount);
    }

    public float getMaxHealth() {
        return this.getRawMaxHealth() - this.getTotalReduction();
    }

    public float getRawMaxHealth() {
        return this.maxHealth;
    }

    public float getInitialHealth() {
        return this.definition.baseHealth();
    }

    public void restoreHealthLimit() {
        this.maxHealth = this.getInitialHealth();
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
        this.setHealth(this.health);
    }

    public float getMaxHealAmount() {
        return this.getMaxHealth() - this.health;
    }

    public StatusEffectMap getStatusEffects() {
        return this.statusEffects;
    }

    public float getScaledDamage(float incomingDamage) {
        HealthConfig config = MedicalSystem.getConfig().health;
        float configuredScaledDamage = incomingDamage * this.definition.damageConfiguration().scale();
        return config.applyDamageMultipliers(configuredScaledDamage, this.definition.type());
    }

    public float getScaledTransferDamage(float leftoverDamage) {
        return leftoverDamage * this.definition.damageConfiguration().transferScale();
    }

    public void tick(StatusEffectContext.MutableContext context) {
        this.statusEffects.tick(context);
        LivingEntity entity = context.entity();
        if (entity.level().getGameTime() % 20 == 0) {
            HealthContainer container = context.container();
            HealthEventContext ctx = HealthEventContext.simple(entity, container, this);
            MedicalSystem.HEALTH_EVENT.triggerEvent(MedSystemHealthEventSources.UPDATE, ctx);
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

    public void addReduction(Identifier identifier, float amount) {
        this.reductions.put(identifier, amount);
    }

    public void removeReduction(Identifier identifier) {
        this.reductions.removeFloat(identifier);
    }

    public float getReduction(Identifier identifier) {
        return this.reductions.getFloat(identifier);
    }

    public float getTotalReduction() {
        float reductionsSum = (float) this.reductions.values().doubleStream().sum();
        return Math.min(this.maxHealth - 1, reductionsSum);
    }

    public boolean isTagged(Identifier tag) {
        return this.definition.isTagged(tag);
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
