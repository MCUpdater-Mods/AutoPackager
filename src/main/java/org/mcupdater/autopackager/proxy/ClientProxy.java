package org.mcupdater.autopackager.proxy;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ClientProxy extends CommonProxy {

	public ClientProxy() {
		this.client = true;
	}
}
