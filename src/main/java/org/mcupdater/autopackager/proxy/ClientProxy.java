package org.mcupdater.autopackager.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import org.mcupdater.autopackager.AutoPackager;

public class ClientProxy extends CommonProxy {

	public ClientProxy() {
		this.client = true;
	}

	@Override
	public void doClientRegistrations() {
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(Item.getItemFromBlock(AutoPackager.packagerBlock), 0, new ModelResourceLocation("autopackager:packagerblock", "inventory"));
	}
}
