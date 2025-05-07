package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.core.common.init.CoreRegistries;
import tnt.tarkovcraft.core.common.skill.tracker.SkillTriggerEvent;
import tnt.tarkovcraft.medsystem.MedicalSystem;

public final class MedSystemSkillEvents {

    public static final DeferredRegister<SkillTriggerEvent> REGISTRY = DeferredRegister.create(CoreRegistries.SKILL_TRIGGER_EVENT, MedicalSystem.MOD_ID);

    public static final Holder<SkillTriggerEvent> ARMOR_USE = REGISTRY.register("armor_use", SkillTriggerEvent::new);
    public static final Holder<SkillTriggerEvent> DAMAGE_TAKEN = REGISTRY.register("damage_taken", SkillTriggerEvent::new);
    public static final Holder<SkillTriggerEvent> HEALING_USED = REGISTRY.register("healing_used", SkillTriggerEvent::new);
}
