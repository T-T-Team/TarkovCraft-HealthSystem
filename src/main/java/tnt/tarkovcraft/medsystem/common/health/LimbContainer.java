package tnt.tarkovcraft.medsystem.common.health;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.checkerframework.checker.nullness.qual.NonNull;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectContext;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectSubmitter;
import tnt.tarkovcraft.medsystem.util.HealthHelper;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class LimbContainer implements Iterable<Limb> {

    public static final LimbContainer EMPTY = new LimbContainer(Collections.emptyMap(), "");
    public static final Codec<LimbContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Limb.CODEC).optionalFieldOf("limbs", Collections.emptyMap()).forGetter(t -> t.limbs),
            Codec.STRING.optionalFieldOf("root_limb", "").forGetter(t -> t.rootLimbCode)
    ).apply(instance, LimbContainer::new));

    private final Map<String, Limb> limbs;
    private final String rootLimbCode;

    private LimbContainer(Map<String, Limb> map, String rootLimbCode) {
        this.limbs = ImmutableMap.copyOf(map);
        this.rootLimbCode = rootLimbCode;
    }

    public static LimbContainer create(@Nullable HealthContainerDefinition definition) {
        Map<String, Limb> map = definition != null ? definition.limbConfiguration().buildLimbInstances() : Collections.emptyMap();
        String rootLimbCode = definition != null ? definition.limbConfiguration().rootLimb() : "";
        return new LimbContainer(map, rootLimbCode);
    }

    public void update(StatusEffectContext.MutableContext context) {
        float initialHealth = this.getHealth();
        this.limbs.values().forEach(limb -> {
            context.withLimb(limb);
            limb.tick(context);
        });
        float currentHealth = this.getHealth();
        if (currentHealth != initialHealth) {
            HealthHelper.synchronizeHealth(context.entity(), context.container());
        }
    }

    public float getHealth() {
        float total = 0.0F;
        for (Limb limb : this.limbs.values()) {
            if (limb.shouldOwnerDie())
                return 0.0F;
            total += limb.getHealth();
        }
        return total;
    }

    public float getMaxHealth() {
        return (float) this.getLimbs().mapToDouble(Limb::getMaxHealth).sum();
    }

    public boolean hasLimb(String limbCode) {
        return this.limbs.containsKey(limbCode);
    }

    public Limb getLimb(@Nullable String limbCode) {
        return limbCode == null || limbCode.isBlank() ? this.limbs.get(this.rootLimbCode) : this.limbs.get(limbCode);
    }

    public Limb getRootLimb() {
        return this.getLimb(this.rootLimbCode);
    }

    public boolean isEmpty() {
        return this.limbs.isEmpty() || this.rootLimbCode.isBlank();
    }

    @Override
    public @NonNull Iterator<Limb> iterator() {
        return this.limbs.values().iterator();
    }

    public Stream<Limb> getLimbs() {
        return this.limbs.values().stream();
    }

    public int getLimbCount() {
        return this.limbs.size();
    }

    public Stream<StatusEffect> getStatusEffects() {
        return this.getLimbs()
                .flatMap(limb -> limb.getStatusEffects().getEffectsStream());
    }

    public boolean removeMatchingStatusEffects(TagKey<StatusEffectType<?>> tag, LivingEntity entity, HealthContainer container) {
        boolean modified = false;
        for (Limb limb : this.limbs.values()) {
            StatusEffectContext ctx = StatusEffectContext.of(container, entity, StatusEffectSubmitter.NOOP, limb);
            modified |= limb.getStatusEffects().removeMatching(tag, ctx);
        }
        return modified;
    }

    public Collection<Limb> getVitalLimbs() {
        return this.getLimbs().filter(Limb::isVital).toList();
    }

    public boolean hasLimb(Predicate<Limb> predicate) {
        return this.getLimbs().anyMatch(predicate);
    }

    public void hurt(DamageContext damageContext, float incomingDamage) {
        Map<Limb, Float> distributedDamage = damageContext.getDamage(incomingDamage);
        for (Map.Entry<Limb, Float> entry : distributedDamage.entrySet()) {
            Limb limb = entry.getKey();
            float amount = entry.getValue();
            this.hurtLimb(damageContext, limb, amount);
        }
    }

    public float heal(float amount, @Nullable Limb limb) {
        if (limb != null && !limb.isDead()) {
            return this.healSpecificLimb(limb, amount);
        } else {
            return this.healGeneric(amount);
        }
    }

    public void restoreHealthLimits() {
        this.limbs.values().forEach(Limb::restoreHealthLimit);
    }

    public void clearData(HealthContainer container, LivingEntity entity) {
        for (Limb limb : this.limbs.values()) {
            StatusEffectMap map = limb.getStatusEffects();
            StatusEffectContext ctx = StatusEffectContext.of(container, entity, StatusEffectSubmitter.NOOP, limb);
            if (!map.isEmpty())
                map.removeAll(ctx);
        }
    }

    private void hurtLimb(DamageContext context, Limb limb, float limbDamage) {
        float damage = Math.min(limb.getHealth(), limb.getScaledDamage(limbDamage));
        float leftover = limbDamage - damage;
        boolean wasDead = limb.isDead();
        limb.hurt(damage);
        if (!limb.isVital() && limb.isDead() != wasDead) {
            context.addLostLimb(limb);
        }
        // no need to redistribute damage from vital parts
        if (!limb.isVital() && leftover > 0) {
            Collection<Limb> aliveLimbs = this.getLimbs().filter(Limb::isAlive).toList();
            if (aliveLimbs.isEmpty()) {
                return;
            }
            DamageSource source = context.getSource();
            float pooledDamage = (source.is(DamageTypeTags.BYPASSES_ARMOR) ? leftover : limb.getScaledTransferDamage(leftover)) / aliveLimbs.size();
            for (Limb liveLimb : aliveLimbs) {
                this.hurtLimb(context, liveLimb, pooledDamage);
            }
        }
    }

    private float healSpecificLimb(Limb limb, final float amount) {
        float healAmount = Math.min(amount, limb.getMaxHealAmount());
        limb.heal(healAmount);
        return amount - healAmount;
    }

    private float healGeneric(final float amount) {
        Limb currentLimb;
        float leftover = amount;
        while (leftover > 0.0F && (currentLimb = HealthHelper.selectLimbForHealing(this)) != null) {
            float healAmount = Math.min(leftover, currentLimb.getMaxHealAmount());
            currentLimb.heal(healAmount);
            leftover -= healAmount;
        }
        return leftover;
    }
}
