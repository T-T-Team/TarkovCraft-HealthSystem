package tnt.tarkovcraft.medsystem.common.armor;

public enum ArmorSystem {

    SIMULATED(SimulatedArmorComponent.INSTANCE),
    MODULAR(new ModularArmorComponent(1.0F)),
    MODULAR_BOOSTED(new ModularArmorComponent(2.5F)),
    VANILLA(VanillaArmorComponent.INSTANCE);

    private final ArmorComponent component;

    ArmorSystem(ArmorComponent component) {
        this.component = component;
    }

    public ArmorComponent getComponent() {
        return component;
    }
}
