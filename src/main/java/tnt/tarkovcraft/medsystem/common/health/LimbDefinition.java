package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;
import tnt.tarkovcraft.medsystem.common.init.MedSystemTags;

public record LimbDefinition(LimbType type, boolean vital, float baseHealth, DamageConfiguration damageConfiguration) {

    public static final Codec<LimbDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LimbType.CODEC.fieldOf("type").forGetter(LimbDefinition::type),
            Codec.BOOL.optionalFieldOf("vital", false).forGetter(LimbDefinition::vital),
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("base_health").forGetter(LimbDefinition::baseHealth),
            DamageConfiguration.CODEC.optionalFieldOf("damage_configuration", DamageConfiguration.DEFAULT).forGetter(LimbDefinition::damageConfiguration)
    ).apply(instance, LimbDefinition::new));

    public Limb createLimbInstance(String code) {
        return new Limb(this, code);
    }

    public record DamageConfiguration(float scale, float transferScale, TagKey<StatusEffectType<?>> excludedStatusEffects) {

        public static final DamageConfiguration DEFAULT = new DamageConfiguration(1.0F, 1.0F, MedSystemTags.StatusEffects.DISABLED);
        public static final Codec<DamageConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.optionalFieldOf("scale", 1.0F).forGetter(DamageConfiguration::scale),
                Codec.FLOAT.optionalFieldOf("transfer_scale", 1.0F).forGetter(DamageConfiguration::transferScale),
                TagKey.codec(MedSystemRegistries.Keys.STATUS_EFFECT).optionalFieldOf("excluded_status_effects", MedSystemTags.StatusEffects.DISABLED).forGetter(DamageConfiguration::excludedStatusEffects)
        ).apply(instance, DamageConfiguration::new));

        public boolean isStatusEffectAllowed(StatusEffectType<?> type) {
            return !type.is(this.excludedStatusEffects);
        }
    }
}
