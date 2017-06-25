package org.mcupdater.autopackager;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class ClearRecipeCacheCommand extends CommandBase
{
	@Override
	public String getName() {
		return "ap_clearcache";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) {
		AutoPackager.large.clear();
		AutoPackager.small.clear();
		AutoPackager.hollow.clear();
		AutoPackager.single.clear();
	}
}
