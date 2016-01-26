package org.mcupdater.autopackager;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class ClearRecipeCacheCommand extends CommandBase
{
	@Override
	public String getCommandName() {
		return "ap_clearcache";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] params) {
		AutoPackager.large.clear();
		AutoPackager.small.clear();
		AutoPackager.hollow.clear();
		AutoPackager.single.clear();
	}
}
