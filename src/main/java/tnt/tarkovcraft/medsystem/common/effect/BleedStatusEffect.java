package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.client.config.BloodDecalConfig;
import tnt.tarkovcraft.medsystem.client.particle.BloodDecalParticleOptions;
import tnt.tarkovcraft.medsystem.client.particle.BloodDripParticleOptions;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectSubmitter;
import tnt.tarkovcraft.medsystem.common.health.EntityHitboxContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthContainerDefinition;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageTypes;
import tnt.tarkovcraft.medsystem.common.init.MedSystemParticleTypes;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public final class BleedStatusEffect extends EntityCausedStatusEffect {

    public static final MapCodec<BleedStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> commonEntity(instance).and(
            instance.group(
                    Codec.LONG.optionalFieldOf("added_at", 0L).forGetter(t -> t.addedAt),
                    Codec.FLOAT.optionalFieldOf("amount", 0.005F).forGetter(t -> t.bleedAmount),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("interval", 60).forGetter(t -> t.bleedInterval),
                    ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("wound_duration", 0).forGetter(t -> t.woundDuration)
            )
    ).apply(instance, BleedStatusEffect::new));
    public static final Component LIGHT_BLEED = Component.translatable("status_effect.medsystem.bleed.light");
    public static final Component HEAVY_BLEED = Component.translatable("status_effect.medsystem.bleed.heavy");
    public static final Identifier ICON_LIGHT_BLEED = MedicalSystem.createIdentifier("textures/icons/status_effect/light_bleed.png");
    public static final Identifier ICON_HEAVY_BLEED = MedicalSystem.createIdentifier("textures/icons/status_effect/heavy_bleed.png");
    public static final float LIGHT_BLEED_AMOUNT = 0.005F;
    public static final float HEAVY_BLEED_AMOUNT = 0.025F;
    public static final int LIGHT_BLEED_INTERVAL = 60;
    public static final int HEAVY_BLEED_INTERVAL = 30;
    private static final float HEAVY_BLEED_AMOUNT_THRESHOLD = getBleedAmount(HEAVY_BLEED_AMOUNT, HEAVY_BLEED_INTERVAL, 20);
    private static final float RAW_DAMAGE_SCALE = 40.0F;
    private static final Component HINT_LIGHT_BLEED = Component.translatable("status_effect.medsystem.bleed.light.heal_hint").withStyle(ChatFormatting.DARK_GRAY);
    private static final Component HINT_HEAVY_BLEED = Component.translatable("status_effect.medsystem.bleed.heavy.heal_hint").withStyle(ChatFormatting.DARK_GRAY);

    private long addedAt;
    private final float bleedAmount;
    private final int bleedInterval;
    private final int woundDuration;

    public BleedStatusEffect(int duration, Optional<UUID> owner, float bleedAmount, int bleedInterval, int woundDuration) {
        this(duration, owner, 0L, bleedAmount, bleedInterval, woundDuration);
    }

    private BleedStatusEffect(int duration, Optional<UUID> owner, long addedAt, float bleedAmount, int bleedInterval, int woundDuration) {
        super(duration, owner);
        this.addedAt = addedAt;
        this.bleedAmount = bleedAmount;
        this.bleedInterval = bleedInterval;
        this.woundDuration = woundDuration;
    }

    public static BleedStatusEffect defaultLightBleed(int duration, Optional<UUID> causingEntity) {
        return new BleedStatusEffect(duration, causingEntity, LIGHT_BLEED_AMOUNT, LIGHT_BLEED_INTERVAL, 0);
    }

    public static BleedStatusEffect defaultHeavyBleed(int duration, Optional<UUID> causingEntity) {
        return new BleedStatusEffect(duration, causingEntity, HEAVY_BLEED_AMOUNT, HEAVY_BLEED_INTERVAL, Duration.minutes(5).tickValue());
    }

    @Override
    public void apply(HealthContainer container, StatusEffectSubmitter submitter, LivingEntity entity, @Nullable Limb limb) {
        Level level = entity.level();
        long time = level.getGameTime();
        if (this.addedAt == 0L) {
            this.addedAt = time - 1L;
        }
        if (limb != null && (time - this.addedAt) % this.bleedInterval == 0L) {
            if (level instanceof ServerLevel serverLevel) {
                if (BloodSystem.hasBloodDataIntegration(entity)) {
                    BloodSystem.causeBloodLoss(entity, this.bleedAmount);
                } else {
                    RegistryAccess access = serverLevel.registryAccess();
                    DamageSource damageSource = MedSystemDamageTypes.causeBleedDamage(access, this.getCausingEntity(serverLevel));
                    float damage = this.bleedAmount * RAW_DAMAGE_SCALE;
                    entity.hurtServer(serverLevel, damageSource, damage);
                }
            } else {
                BloodDecalConfig config = MedicalSystem.getConfig().bloodDecals;
                if (!config.enableBloodDecals)
                    return;
                Vec3 position = this.getParticlePosition(entity, container, limb);
                Vec3 direction = entity.getDeltaMovement();
                float baseDir = 0.025F;
                int particleCount = isHeavyBleed(this) ? config.heavyBleedDecalCount : config.lightBleedDecalCount;
                RandomSource random = level.getRandom();
                for (int i = 0; i < particleCount; i++) {
                    // TODO custom color
                    level.addParticle(new BloodDripParticleOptions(MedSystemParticleTypes.BLOOD_DRIP, 0xB20000), position.x, position.y, position.z, random.nextFloat() * (baseDir * 2.0F) - baseDir + direction.x, 0.1F + direction.y, random.nextFloat() * (baseDir * 2.0F) - baseDir + direction.z);
                }
            }
        }
    }

    @Override
    public void onRemoved(StatusEffectSubmitter submitter, HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
        if (this.woundDuration > 0) {
            submitter.submit(
                    Duration.seconds(5),
                    new FreshWoundStatusEffect(this.woundDuration)
            );
        }
    }

    @Override
    public void addAdditionalInfo(Consumer<Component> tooltip) {
        tooltip.accept(isHeavyBleed(this) ? HINT_HEAVY_BLEED : HINT_LIGHT_BLEED);
    }

    @Override
    public Component getCustomDisplayName() {
        return isHeavyBleed(this) ? HEAVY_BLEED : LIGHT_BLEED;
    }

    @Override
    public Identifier getCustomIcon() {
        return isHeavyBleed(this) ? ICON_HEAVY_BLEED : ICON_LIGHT_BLEED;
    }

    @Override
    public StatusEffect copy() {
        return new BleedStatusEffect(this.getDuration(), Optional.ofNullable(this.getCausingEntity()), this.bleedAmount, this.bleedInterval, this.woundDuration);
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.BLEED.value();
    }

    @Override
    protected @Nullable Integer getCustomHealingPriority() {
        return isHeavyBleed(this) ? MedSystemConstants.HEAL_EFFECT_MAJOR : MedSystemConstants.HEAL_EFFECT_MINOR;
    }

    private Vec3 getParticlePosition(LivingEntity entity, HealthContainer container, Limb limb) {
        HealthContainerDefinition definition = container.getDefinition();
        EntityHitboxContainer hitboxContainer = definition.hitboxContainer();
        String state = definition.getCurrentEntityState(entity);
        EntityHitboxContainer.LimbHitboxDefinition hitbox = hitboxContainer.getLimbHitbox(limb.getLimbCode(), state);
        return hitbox.toWorldSpaceHitbox(entity).getCenter();
    }

    public static boolean isHeavyBleed(BleedStatusEffect effect) {
        float bleedAmountPerSecond = getBleedAmount(effect, 20);
        return bleedAmountPerSecond >= HEAVY_BLEED_AMOUNT_THRESHOLD;
    }

    public static float getBleedAmount(BleedStatusEffect effect, float scale) {
        return getBleedAmount(effect.bleedAmount, effect.bleedInterval, scale);
    }

    public static float getBleedAmount(float bleedAmount, int bleedInterval, float scale) {
        return (bleedAmount / bleedInterval) * scale;
    }

    public static BleedStatusEffect withHighestDamage(BleedStatusEffect ef1, BleedStatusEffect ef2) {
        int duration1 = ef1.isInfinite() ? 1 : ef1.getDuration();
        int duration2 = ef2.isInfinite() ? 1 : ef2.getDuration();
        return getBleedAmount(ef1, duration1) >= getBleedAmount(ef2, duration2) ? ef1 : ef2;
    }
}
