package tnt.tarkovcraft.medsystem.common.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectSubmitter;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectWithDelay;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;

import java.util.Objects;
import java.util.function.Consumer;

public interface StatusEffectContext extends StatusEffectSubmitter {

    HealthContainer container();

    LivingEntity entity();

    @Nullable Limb limb();

    default Level level() {
        return this.entity().level();
    }

    default boolean isServerSide() {
        return !this.level().isClientSide();
    }

    default void ifLimbPresent(Consumer<Limb> event) {
        Limb limb = this.limb();
        if (limb != null) {
            event.accept(limb);
        }
    }

    static StatusEffectContext of(HealthContainer container, LivingEntity entity, StatusEffectSubmitter submitter, @Nullable Limb limb) {
        return new Impl(Objects.requireNonNull(container), Objects.requireNonNull(entity), Objects.requireNonNull(submitter), limb);
    }

    final class Impl implements StatusEffectContext {

        private final HealthContainer container;
        private final LivingEntity entity;
        private final StatusEffectSubmitter submitter;
        private final Limb limb;

        public Impl(HealthContainer container, LivingEntity entity, StatusEffectSubmitter submitter, Limb limb) {
            this.container = container;
            this.entity = entity;
            this.submitter = submitter;
            this.limb = limb;
        }

        @Override
        public HealthContainer container() {
            return this.container;
        }

        @Override
        public LivingEntity entity() {
            return this.entity;
        }

        @Override
        public void submit(int delay, StatusEffect template) {
            this.submitter.submit(delay, template);
        }

        @Override
        public void clear() {
            this.submitter.clear();
        }

        @Override
        public void accept(Consumer<StatusEffectWithDelay> consumer) {
            this.submitter.accept(consumer);
        }

        @Override
        public Limb limb() {
            return this.limb;
        }
    }

    final class MutableContext implements StatusEffectContext {

        private final HealthContainer container;
        private final LivingEntity entity;
        private StatusEffectSubmitter submitter;
        private Limb limb;

        public MutableContext(HealthContainer container, LivingEntity entity) {
            this.container = container;
            this.entity = entity;
        }

        @Override
        public HealthContainer container() {
            return this.container;
        }

        @Override
        public LivingEntity entity() {
            return this.entity;
        }

        @Override
        public void submit(int delay, StatusEffect template) {
            this.submitter.submit(delay, template);
        }

        @Override
        public void clear() {
            this.submitter.clear();
        }

        @Override
        public void accept(Consumer<StatusEffectWithDelay> consumer) {
            this.submitter.accept(consumer);
        }

        @Override
        public Limb limb() {
            return this.limb;
        }

        public void withEffectSubmitter(StatusEffectSubmitter submitter) {
            this.submitter = submitter;
        }

        public void withLimb(Limb limb) {
            this.limb = limb;
        }
    }
}
