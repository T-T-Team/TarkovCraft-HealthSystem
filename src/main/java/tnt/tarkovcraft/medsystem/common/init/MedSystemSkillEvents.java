package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.core.common.init.CoreRegistries;
import tnt.tarkovcraft.core.common.skill.trigger.SkillTrigger;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;

public final class MedSystemSkillEvents {

    public static final DeferredRegister<SkillTrigger> REGISTRY = DeferredRegister.create(CoreRegistries.Keys.SKILL_TRIGGER_EVENT, MedSystemConstants.MOD_ID);

    public static final Holder<SkillTrigger> ARMOR_USE = REGISTRY.register("armor_use", SkillTrigger::new);
    public static final Holder<SkillTrigger> DAMAGE_TAKEN = REGISTRY.register("damage_taken", SkillTrigger::new);
    public static final Holder<SkillTrigger> HEALING_USED = REGISTRY.register("healing_used", SkillTrigger::new);
    public static final Holder<SkillTrigger> LIMB_FIXED = REGISTRY.register("limb_fixed", SkillTrigger::new);
}
