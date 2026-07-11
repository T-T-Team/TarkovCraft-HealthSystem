package tnt.tarkovcraft.medsystem.common.damage.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.predicates.DamageSourcePredicate;
import net.minecraft.advancements.predicates.TagPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationContext;

public record DamageSourceCondition(DamageSourcePredicate predicate, EntityFilter filter) implements DamageCondition {

    public static final MapCodec<DamageSourceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DamageSourcePredicate.CODEC.fieldOf("predicate").forGetter(DamageSourceCondition::predicate),
            EntityFilter.CODEC.optionalFieldOf("filter", EntityFilter.ANY).forGetter(DamageSourceCondition::filter)
    ).apply(instance, DamageSourceCondition::new));

    public static DamageSourceCondition fromTag(TagKey<DamageType> tag, boolean include) {
        DamageSourcePredicate predicate = DamageSourcePredicate.Builder.damageType()
                .tag(include ? TagPredicate.is(tag) : TagPredicate.isNot(tag))
                .build();
        return new DamageSourceCondition(predicate, EntityFilter.ANY);
    }

    @Override
    public boolean test(HitCalculationContext context) {
        if (!this.filter.validate(context)) {
            return false;
        }
        DamageSource damageSource = context.source();
        LivingEntity entity = context.entity();
        Level level = entity.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        Vec3 position = damageSource.getSourcePosition();
        return this.predicate.matches(serverLevel, position, damageSource);
    }

    @Override
    public MapCodec<? extends DamageCondition> codec() {
        return CODEC;
    }

    public enum EntityFilter implements StringRepresentable {

        ANY("any"),
        DIRECT("direct"),
        PROJECTILE("projectile");

        public static final Codec<EntityFilter> CODEC = StringRepresentable.fromEnum(EntityFilter::values);
        private final String serializedName;

        EntityFilter(String serializedName) {
            this.serializedName = serializedName;
        }

        public boolean validate(HitCalculationContext context) {
            return switch (this) {
                case ANY -> true;
                case DIRECT -> context.getAttackingEntity() != null;
                case PROJECTILE -> context.getProjectile() != null;
            };
        }

        @Override
        public String getSerializedName() {
            return this.serializedName;
        }
    }
}
