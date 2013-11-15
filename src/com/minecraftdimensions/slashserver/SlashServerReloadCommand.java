package com.minecraftdimensions.slashserver;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class SlashServerReloadCommand extends Command {
	public SlashServerReloadCommand(String name, String permission, String... aliases) {
		super(name, permission, aliases);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		SlashServer.INSTANCE.reloadConfig();
		sender.sendMessage(ChatColor.GREEN + "Config reloaded");
	}
}
