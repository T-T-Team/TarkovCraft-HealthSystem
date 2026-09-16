package tnt.tarkovcraft.medsystem.common.damage;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DamageResolverManager extends SimpleJsonResourceReloadListener<DamageResolver> {

    public static final Marker MARKER = MarkerManager.getMarker("DamageResolver");
    public static final Identifier IDENTIFIER = MedicalSystem.createIdentifier("damage_resolver");
    public static final DamageResolver DEFAULT = DamageResolver.generic();
    private final List<DamageResolver> resolvers = new ArrayList<>();

    public DamageResolverManager() {
        super(DamageResolver.CODEC, FileToIdConverter.json("tarkovcraft/damage_resolver"));
    }

    public DamageResolver getResolver(HitCalculationContext context) {
        for (DamageResolver resolver : this.resolvers) {
            if (resolver.test(context)) {
                return resolver;
            }
        }
        return DEFAULT;
    }

    @Override
    protected void apply(Map<Identifier, DamageResolver> preparations, ResourceManager manager, ProfilerFiller profiler) {
        this.resolvers.clear();
        this.resolvers.addAll(preparations.values());
        this.resolvers.sort(null);
        MedicalSystem.LOGGER.info(MARKER, "Registered {} damage resolvers", this.resolvers.size());
    }
}
