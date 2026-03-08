package tnt.tarkovcraft.medsystem.common.health.calc;

import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.medsystem.common.health.distributor.DamageDistributor;
import tnt.tarkovcraft.medsystem.common.health.distributor.EvenDamageDistributor;
import tnt.tarkovcraft.medsystem.util.HitboxHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public final class HitCalculationResult {

    private static final HitCalculationResult MISS = new HitCalculationResult(null);
    private final @Nullable List<HitInfo> hits;
    private final List<Ray> raycasts = new ArrayList<>();
    private DamageDistributor damageDistributor = EvenDamageDistributor.INSTANCE;

    private HitCalculationResult(@Nullable List<HitInfo> hits) {
        this.hits = hits != null
                ? hits.stream().distinct().toList()
                : Collections.emptyList();
    }

    public static HitCalculationResult of(@Nullable List<HitInfo> hits) {
        return new HitCalculationResult(hits);
    }

    public static HitCalculationResult of(Stream<HitInfo> stream) {
        return of(stream.toList());
    }

    public static HitCalculationResult of(@Nullable HitInfo hit) {
        return of(hit == null ? null : Collections.singletonList(hit));
    }

    public static HitCalculationResult miss() {
        return MISS;
    }

    public static HitCalculationResult miss(Ray ray) {
        return new HitCalculationResult(null).withRayCast(ray);
    }

    public static HitCalculationResult simpleMappedResult(HitCalculationContext ctx, Predicate<LimbHitbox> filter, Function<LimbHitbox, HitInfo> mapper) {
        return HitCalculationResult.of(HitboxHelper.getEntityHitboxes(ctx)
                .filter(filter)
                .map(mapper)
        );
    }

    public static HitCalculationResult simpleResult(HitCalculationContext ctx, Predicate<LimbHitbox> filter) {
        return simpleMappedResult(ctx, filter, hitbox -> HitInfo.create(hitbox, ctx.entity()));
    }

    public static HitCalculationResult simpleMappedResult(HitCalculationContext ctx, Function<LimbHitbox, HitInfo> mapper) {
        return simpleMappedResult(ctx, t -> true, mapper);
    }

    public static HitCalculationResult simpleResult(HitCalculationContext ctx) {
        return simpleMappedResult(ctx, hitbox -> HitInfo.create(hitbox, ctx.entity()));
    }

    public HitCalculationResult withRayCast(Ray ray) {
        this.raycasts.add(ray);
        return this;
    }

    public HitCalculationResult withDamageDistributor(UnaryOperator<DamageDistributor> updater) {
        this.damageDistributor = Objects.requireNonNull(updater.apply(this.damageDistributor));
        return this;
    }

    public boolean isMiss() {
        return this.hits == null || this.hits.isEmpty();
    }

    public DamageDistributor getDamageDistributor() {
        return this.damageDistributor;
    }

    public List<HitInfo> getHits() {
        return this.hits;
    }

    public List<Ray> getRaycasts() {
        return raycasts;
    }
}
