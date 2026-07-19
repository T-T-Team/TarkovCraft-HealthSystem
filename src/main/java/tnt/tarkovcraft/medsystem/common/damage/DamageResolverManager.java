package tnt.tarkovcraft.medsystem.common.damage;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.event.AddBuiltInDamageResolversEvent;
import tnt.tarkovcraft.medsystem.common.damage.condition.DamageSourceCondition;
import tnt.tarkovcraft.medsystem.common.damage.condition.DamageTypeCondition;
import tnt.tarkovcraft.medsystem.common.damage.condition.IsSpecificLimbDamage;
import tnt.tarkovcraft.medsystem.common.damage.function.BrokenLimbDamageFunction;
import tnt.tarkovcraft.medsystem.common.damage.function.GenericDamageFunction;
import tnt.tarkovcraft.medsystem.common.damage.function.PoisonDamageFunction;
import tnt.tarkovcraft.medsystem.common.damage.function.SpecificLimbDamageFunction;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemTags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DamageResolverManager extends SimpleJsonResourceReloadListener<DamageResolver> {

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
        this.registerBuiltIn(this.resolvers::add);
        int defaultResolvers = this.resolvers.size();
        this.resolvers.addAll(preparations.values());
        this.resolvers.sort(null);
        MedicalSystem.LOGGER.info(MARKER, "Registered {} damage resolvers ({} built in)", this.resolvers.size(), defaultResolvers);
    }

    private void registerBuiltIn(Consumer<DamageResolver> registration) {
        registration.accept(DamageResolver.create(Integer.MIN_VALUE, new SpecificLimbDamageFunction(), new IsSpecificLimbDamage()));
        registration.accept(DamageResolver.create(-500, new BrokenLimbDamageFunction(), DamageSourceCondition.fromTag(MedSystemTags.DamageTypes.IS_MOVEMENT_RESTRICTED, true)));
        registration.accept(DamageResolver.create(-500, new PoisonDamageFunction(), new DamageTypeCondition(Collections.singletonList(NeoForgeMod.POISON_DAMAGE))));
        registration.accept(DamageResolver.create(100, new GenericDamageFunction(), DamageSourceCondition.fromTag(MedSystemTags.DamageTypes.IS_GENERIC, true)));
        NeoForge.EVENT_BUS.post(new AddBuiltInDamageResolversEvent(registration));
    }
}
