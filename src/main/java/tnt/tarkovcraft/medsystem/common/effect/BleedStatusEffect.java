package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.core.common.statistic.StatisticTracker;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.client.particle.BloodDripParticleOptions;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.config.BleedConfiguration;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.config.StatusEffectConfig;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectHelper;
import tnt.tarkovcraft.medsystem.common.health.EntityHitboxContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthContainerDefinition;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageTypes;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStats;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;
import tnt.tarkovcraft.medsystem.util.HealthHelper;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class BleedStatusEffect extends EntityCausedStatusEffect {

    public static final MapCodec<BleedStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> commonEntity(instance).and(
            instance.group(
                    Codec.LONG.optionalFieldOf("added_at", 0L).forGetter(t -> t.addedAt),
                    BleedType.CODEC.optionalFieldOf("bleed_type", BleedType.LIGHT).forGetter(t -> t.bleedType)
            )
    ).apply(instance, BleedStatusEffect::new));
    public static final Component LIGHT_BLEED = Component.translatable("status_effect.medsystem.bleed.light");
    public static final Component MODERATE_BLEED = Component.translatable("status_effect.medsystem.bleed.moderate");
    public static final Component HEAVY_BLEED = Component.translatable("status_effect.medsystem.bleed.heavy");
    public static final Component CRITICAL_BLEED = Component.translatable("status_effect.medsystem.bleed.critical");
    public static final ResourceLocation ICON_LIGHT_BLEED = StatusEffectHelper.getTextureResource(MedSystemConstants.MOD_ID, "light_bleed");
    public static final ResourceLocation ICON_HEAVY_BLEED = StatusEffectHelper.getTextureResource(MedSystemConstants.MOD_ID, "heavy_bleed");
    private static final float RAW_DAMAGE_SCALE = 40.0F;
    private static final Component HINT_USE_BANDAGE = Component.translatable("status_effect.medsystem.bleed.bandage.heal_hint").withStyle(ChatFormatting.DARK_GRAY);
    private static final Component HINT_USE_TOURNIQUET = Component.translatable("status_effect.medsystem.bleed.tourniquet.heal_hint").withStyle(ChatFormatting.DARK_GRAY);

    private long addedAt;
    private final BleedType bleedType;

    public BleedStatusEffect(int duration, Optional<UUID> owner, BleedType bleedType) {
        this(duration, owner, 0L, bleedType);
    }

    private BleedStatusEffect(int duration, Optional<UUID> owner, long addedAt, BleedType bleedType) {
        super(duration, owner);
        this.addedAt = addedAt;
        this.bleedType = bleedType;
    }

    public static BleedStatusEffect lightBleed(int duration, Optional<UUID> causingEntity) {
        return new BleedStatusEffect(duration, causingEntity, BleedType.LIGHT);
    }

    public static BleedStatusEffect moderateBleed(int duration, Optional<UUID> causingEntity) {
        return new BleedStatusEffect(duration, causingEntity, BleedType.MODERATE);
    }

    public static BleedStatusEffect heavyBleed(int duration, Optional<UUID> causingEntity) {
        return new BleedStatusEffect(duration, causingEntity, BleedType.HEAVY);
    }

    public static BleedStatusEffect criticalBleed(int duration, Optional<UUID> causingEntity) {
        return new BleedStatusEffect(duration, causingEntity, BleedType.CRITICAL);
    }

    public static BleedStatusEffect createTemplate(int duration, BleedType bleedType) {
        return new BleedStatusEffect(duration, Optional.empty(), bleedType);
    }

    @Override
    public void apply(StatusEffectContext context) {
        Level level = context.level();
        long time = level.getGameTime();
        if (this.addedAt == 0L) {
            this.addedAt = time - 1L;
        }
        Limb limb = context.limb();
        LivingEntity entity = context.entity();
        BleedConfiguration.BleedStageConfig stageConfig = this.getStageConfiguration();
        if (limb != null && (time - this.addedAt) % stageConfig.bleedInterval == 0L) {
            if (level instanceof ServerLevel serverLevel) {
                // blood loss
                float bloodLost = BloodSystemManager.causeBloodLoss(entity, stageConfig.bleedAmount);
                if (bloodLost < 0.0F) { // negative -> no blood container exists
                    RegistryAccess access = serverLevel.registryAccess();
                    DamageSource damageSource = MedSystemDamageTypes.causeBleedDamage(access, this.getCausingEntity(serverLevel));
                    float damage = stageConfig.bleedAmount * RAW_DAMAGE_SCALE;
                    entity.hurt(damageSource, damage);
                }

                if (bloodLost > 0.0F) {
                    // blood loss stat
                    StatisticTracker.incrementOptional(entity, MedSystemStats.BLOOD_LOST, Mth.floor(bloodLost * 1000));
                    // bleed particles
                    float decalMultiplier = bloodLost / stageConfig.bleedAmount;
                    HealthContainer container = context.container();
                    Integer particleColor = container.getDefinition().decalSettings().getColor(entity);
                    if (particleColor != null) {
                        RandomSource random = level.getRandom();
                        BloodDripParticleOptions options = new BloodDripParticleOptions(particleColor);
                        Vec3 position = this.getParticlePosition(entity, container, limb);
                        Vec3 delta = entity.getDeltaMovement();
                        double baseDelta = 0.025;
                        double xd = random.nextFloat() * (baseDelta * 2.0F) - baseDelta + delta.x;
                        double yd = 0.1F + delta.y;
                        double zd = random.nextFloat() * (baseDelta * 2.0F) - baseDelta + delta.z;
                        HealthHelper.submitServerBleedParticles(options, Mth.ceil(stageConfig.decalCount * decalMultiplier), position.x, position.y, position.z, xd, yd, zd, 1.5, entity);
                    }
                }
            }
        }
    }

    @Override
    public void onRemoved(StatusEffectContext context) {
    }

    @Override
    public void addAdditionalInfo(Consumer<Component> tooltip) {
        tooltip.accept(needsTourniquet(this) ? HINT_USE_TOURNIQUET : HINT_USE_BANDAGE);
    }

    @Override
    @Nullable
    public Component getCustomDisplayName() {
        return this.bleedType.label.get();
    }

    @Override
    @Nullable
    public ResourceLocation getCustomIcon() {
        return this.bleedType.icon.get();
    }

    @Override
    public StatusEffect copy() {
        return new BleedStatusEffect(this.getDuration(), Optional.ofNullable(this.getCausingEntity()), this.bleedType);
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.BLEED.value();
    }

    public BleedType getBleedType() {
        return bleedType;
    }

    @Override
    protected @Nullable Integer getCustomHealingPriority() {
        return needsTourniquet(this) ? MedSystemConstants.HEAL_EFFECT_MAJOR : MedSystemConstants.HEAL_EFFECT_MINOR;
    }

    private Vec3 getParticlePosition(LivingEntity entity, HealthContainer container, Limb limb) {
        HealthContainerDefinition definition = container.getDefinition();
        EntityHitboxContainer hitboxContainer = definition.hitboxContainer();
        String state = definition.getCurrentEntityState(entity);
        EntityHitboxContainer.LimbHitboxDefinition hitbox = hitboxContainer.getLimbHitbox(limb.getLimbCode(), state);
        return hitbox.toWorldSpaceHitbox(entity).getCenter();
    }

    public static boolean needsTourniquet(BleedStatusEffect effect) {
        return effect.bleedType.requiresTourniquet;
    }

    public static BleedStatusEffect higherStage(BleedStatusEffect ef1, BleedStatusEffect ef2) {
        StatusEffectConfig config = MedicalSystem.getConfig().statusEffects;
        int duration = StatusEffectConfig.getStackedDuration(sumEffectDurations(ef1, ef2), config.maxBleedDuration);
        BleedType bleedType = ef1.bleedType.ordinal() > ef2.bleedType.ordinal() ? ef1.bleedType : ef2.bleedType;
        UUID causingEntity = ef1.getCausingEntity() != null ? ef1.getCausingEntity() : ef2.getCausingEntity();
        return new BleedStatusEffect(duration, Optional.ofNullable(causingEntity), bleedType);
    }

    public BleedConfiguration.BleedStageConfig getStageConfiguration() {
        MedSystemConfig config = MedicalSystem.getConfig();
        BleedConfiguration bleedConfiguration = config.statusEffects.bleedConfiguration;
        return this.bleedType.configProvider.apply(bleedConfiguration);
    }

    public enum BleedType implements StringRepresentable {

        LIGHT("light", () -> LIGHT_BLEED, () -> ICON_LIGHT_BLEED, false, BleedConfiguration::getLightBleed),
        MODERATE("moderate", () -> MODERATE_BLEED, () -> ICON_LIGHT_BLEED, false, BleedConfiguration::getModerateBleed),
        HEAVY("heavy", () -> HEAVY_BLEED, () -> ICON_HEAVY_BLEED, true, BleedConfiguration::getHeavyBleed),
        CRITICAL("critical", () -> CRITICAL_BLEED, () -> ICON_HEAVY_BLEED, true, BleedConfiguration::getCriticalBleed);

        public static final Codec<BleedType> CODEC = StringRepresentable.fromEnum(BleedType::values);

        private final String name;
        private final Supplier<Component> label;
        private final Supplier<ResourceLocation> icon;
        private final boolean requiresTourniquet;
        private final Function<BleedConfiguration, BleedConfiguration.BleedStageConfig> configProvider;

        BleedType(String name, Supplier<Component> label, Supplier<ResourceLocation> icon, boolean requiresTourniquet, Function<BleedConfiguration, BleedConfiguration.BleedStageConfig> configProvider) {
            this.name = name;
            this.label = label;
            this.icon = icon;
            this.requiresTourniquet = requiresTourniquet;
            this.configProvider = configProvider;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public Component getLabel() {
            return this.label.get();
        }
    }
}
