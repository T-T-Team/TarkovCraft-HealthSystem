package tnt.tarkovcraft.medsystem.common.armor;

// TODO finish implementation
public class SimulatedArmorComponent extends ModularArmorComponent {

    public static final SimulatedArmorComponent INSTANCE = new SimulatedArmorComponent();
    public static final float DEFLECT_SCALE = 50.0F;
    public static final float MAX_DEFLECT_CHANCE = 0.75F;
    public static final float UNCONSCIOUS_ENERGY_THRESHOLD = 40.0F;
    public static final float CONCUSSION_ENERGY_THRESHOLD = 10.0F;

    /*@Override
    public boolean shouldDeflectIncomingHit(DamageSource source, LivingEntity entity, List<HitResult> hits) {
        HitResult first = hits.getFirst();
        Limb limb = first.limb();
        LimbType type = limb.getType();
        // in theory all limbs could have armor which is able to deflect projectiles, but for now we care only about helmets
        if (type != LimbType.HEAD)
            return false;
        Entity projectile = source.getDirectEntity();
        if (projectile != null && !source.isDirect() && projectile.hasData(MedSystemDataAttachments.PROJECTILE_ATTRIBUTES)) {
            EquipmentSlot slot = EquipmentSlot.HEAD;
            ItemStack helmetItem = entity.getItemBySlot(slot);
            if (helmetItem.isEmpty())
                return false;
            // better deflection calculation
            ArmorMaterial material = helmetItem.get(MedSystemItemComponents.ARMOR_MATERIAL);
            if (material == null) {
                return false;
            }
            // deflections should be based on armor material but should also consider projectile energy
            ProjectileAttributes projectileAttributes = projectile.getData(MedSystemDataAttachments.PROJECTILE_ATTRIBUTES);
            float baseDeflectionChance = material.deflectionChance();
            float rawEnergy = (float) projectile.getDeltaMovement().length() * projectileAttributes.massFactor();
            float deflectModifier = 1.0F / (1.0F + rawEnergy / DEFLECT_SCALE);
            float deflectProbability = Mth.clamp(baseDeflectionChance * deflectModifier, 0.0F, MAX_DEFLECT_CHANCE);
            RandomSource random = entity.getRandom();
            // apply deflection post-effects
            if (random.nextFloat() < deflectProbability) {
                if (rawEnergy >= UNCONSCIOUS_ENERGY_THRESHOLD && BloodSystem.hasBloodDataIntegration(entity)) {
                    BloodData bloodData = BloodSystem.getBloodData(entity);
                    bloodData.setOrExtendedUnconsciousTime(Mth.floor(rawEnergy) * 5, BloodData.UnconsciousInfo.PAIN);
                    bloodData.sync(entity);
                } else if (rawEnergy >= CONCUSSION_ENERGY_THRESHOLD && HealthSystem.hasCustomHealth(entity)) {
                    HealthContainer container = HealthSystem.getHealthData(entity);
                    StatusEffectHelper.addEffect(container.getGlobalStatusEffects(), entity, null, new ConcussionStatusEffect(Mth.floor(rawEnergy) * 5));
                    HealthSystem.synchronizeEntity(entity);
                }
                return true;
            }
        }
        return false;
    }*/
}
