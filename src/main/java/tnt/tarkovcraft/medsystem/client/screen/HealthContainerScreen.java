package tnt.tarkovcraft.medsystem.client.screen;

import net.neoforged.neoforge.attachment.IAttachmentHolder;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;

public interface HealthContainerScreen {

    void onHealthContainerUpdated(IAttachmentHolder holder, HealthContainer container);
}
