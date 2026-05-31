package tnt.tarkovcraft.medsystem.common.armor;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.health.LimbType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public record ArmorProtectionArea(Set<LimbType> protectedLimbs) {

    public static final Codec<ArmorProtectionArea> CODEC = Codecs.enumSet(LimbType.CODEC)
            .xmap(ArmorProtectionArea::new, ArmorProtectionArea::protectedLimbs);
    public static final StreamCodec<ByteBuf, ArmorProtectionArea> STREAM_CODEC = LimbType.STREAM_CODEC.apply(ByteBufCodecs.list())
            .map(ArmorProtectionArea::new, armorProtectionArea -> new ArrayList<>(armorProtectionArea.protectedLimbs));

    private ArmorProtectionArea(Collection<LimbType> collection) {
        this(EnumSet.copyOf(collection));
    }

    public boolean isProtected(LimbType limb) {
        return this.protectedLimbs.contains(limb);
    }

    public static void checkCustomProtectionAreas(LivingEntity entity, LimbType limb, Set<EquipmentSlot> output) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor()) {
                continue;
            }
            ItemStack itemStack = entity.getItemBySlot(slot);
            if (!itemStack.isEmpty()) {
                ArmorProtectionArea protectionArea = itemStack.get(MedSystemItemComponents.ARMOR_PROTECTION_AREA);
                if (protectionArea != null && protectionArea.isProtected(limb)) {
                    output.add(slot);
                }
            }
        }
    }
}
