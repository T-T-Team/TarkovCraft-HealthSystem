package tnt.tarkovcraft.medsystem.common.armor;

public enum ArmorSystem {

    SIMULATED(SimulatedArmorComponent.INSTANCE),
    MODULAR(ModularArmorComponent.INSTANCE),
    MODULAR_BOOSTED(ModularBoostedArmorComponent.INSTANCE),
    VANILLA(VanillaArmorComponent.INSTANCE);

    private final ArmorComponent component;

    ArmorSystem(ArmorComponent component) {
        this.component = component;
    }

    public ArmorComponent getComponent() {
        return component;
    }
}
