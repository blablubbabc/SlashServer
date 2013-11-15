package com.minecraftdimensions.slashserver;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ServerCommand extends Command {

	private Set<String> tasks = new HashSet<String>();

	public ServerCommand(String name, String permission, String... aliases) {
		super(name, permission, aliases);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage("This command is only available for players.");
			return;
		}

		final ProxiedPlayer pp = (ProxiedPlayer) sender;
		final String serverName = this.getName();
		String currentServer = pp.getServer().getInfo().getName();
		if (currentServer.equalsIgnoreCase(serverName)) {
			pp.sendMessage(SlashServer.ALREADY_ON_SERVER.replace("{name}", serverName));
		} else {
			if (tasks.contains(pp)) {
				pp.sendMessage(SlashServer.ALREADY_TELEPORTING.replace("{name}", serverName));
				return;
			}

			ServerInfo server = ProxyServer.getInstance().getServerInfo(serverName);
			if (server == null) {
				pp.sendMessage(SlashServer.UNKNOWN_SERVER.replace("{name}", serverName));
				return;
			}

			// the waiting time depends on the server the player is currently connected to, rather than the server he
			// wants to connect to:
			int waitingTime = SlashServer.INSTANCE.getWaitingDuration(currentServer);
			if (waitingTime <= 0) {
				pp.sendMessage(SlashServer.TELEPORTING_NOW.replace("{name}", serverName));
				pp.connect(server);
			} else {
				pp.sendMessage(SlashServer.TELEPORTING_LATER.replace("{name}", serverName).replace("{time}", String.valueOf(waitingTime / 1000)));
				final String pplayerName = pp.getName();
				tasks.add(pplayerName);
				ProxyServer.getInstance().getScheduler().schedule(SlashServer.INSTANCE, new Runnable() {
					@Override
					public void run() {
						if (tasks.remove(pplayerName)) {
							if (pp != null) {
								ServerInfo server = ProxyServer.getInstance().getServerInfo(serverName);
								if (server == null) {
									pp.sendMessage(SlashServer.UNKNOWN_SERVER.replace("{name}", serverName));
									return;
								}
								pp.connect(server);
							}
						}
					}
				}, waitingTime, TimeUnit.MILLISECONDS);
			}
		}
	}
}
