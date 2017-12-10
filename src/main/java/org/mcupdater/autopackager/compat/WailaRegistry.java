package org.mcupdater.autopackager.compat;

import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;
import org.mcupdater.autopackager.BlockPackager;

@WailaPlugin("autopackager")
public class WailaRegistry implements IWailaPlugin {

	@Override
	public void register(IWailaRegistrar registry) {
		IWailaDataProvider dataProvider = new AutoPackagerDataProvider();
		registry.registerNBTProvider(dataProvider, BlockPackager.class);
		registry.registerBodyProvider(dataProvider, BlockPackager.class);
	}
}
