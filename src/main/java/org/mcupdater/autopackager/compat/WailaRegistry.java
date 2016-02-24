package org.mcupdater.autopackager.compat;

import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import org.mcupdater.autopackager.BlockPackager;

public class WailaRegistry {
	public static void initWaila(IWailaRegistrar registry) {
		IWailaDataProvider dataProvider = new AutoPackagerDataProvider();
		registry.registerStackProvider(dataProvider, BlockPackager.class);
		registry.registerBodyProvider(dataProvider, BlockPackager.class);
	}
}
