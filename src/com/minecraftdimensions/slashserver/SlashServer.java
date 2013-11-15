package com.minecraftdimensions.slashserver;

import com.minecraftdimensions.slashserver.configlibrary.ConfigurationSection;
import com.minecraftdimensions.slashserver.configlibrary.YamlConfiguration;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SlashServer extends Plugin {
	public static SlashServer INSTANCE;
	
	private YamlConfiguration config;
	private Map<String, Integer> time;

	public static String ALREADY_ON_SERVER;
	public static String TELEPORTING_NOW;
	public static String TELEPORTING_LATER;
	public static String ALREADY_TELEPORTING;
	public static String UNKNOWN_SERVER;

	public void onEnable() {
		INSTANCE = this;
		reloadConfig();
		registerCommands();
	}
	
	public void reloadConfig() {
		File configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			configFile.getParentFile().mkdirs();
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		config = YamlConfiguration.loadConfiguration(configFile);
		
		// init config defaults:
		initConfigDefaultMessage("ALREADY_ON_SERVER", "&cYou are already connected to server '&e{name}&c'!");
		initConfigDefaultMessage("TELEPORTING_NOW", "&2You are now being sent to server '&e{name}&2'.");
		initConfigDefaultMessage("TELEPORTING_LATER", "&2You are sent to server '&e{name}&2' in &e{seconds} seconds&2'.");
		initConfigDefaultMessage("ALREADY_TELEPORTING", "&cYou are already in the progress of joining server '&e{name}&c'.");
		initConfigDefaultMessage("UNKNOWN_SERVER", "&cThis server is unknown: '&e{name}&c'");
		
		if (!config.isConfigurationSection("servers")) {
			for (String serverName : ProxyServer.getInstance().getServers().keySet()) {
				config.set("servers." + serverName, 0);
			}
		}
		
		// save the config:
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// read config:
		ConfigurationSection serversSection = config.getConfigurationSection("servers");
		assert serversSection != null;
		time = new HashMap<String, Integer>();
		for (String servername : serversSection.getKeys(false)){
			time.put(servername, serversSection.getInt(servername));
		}

		// messages:
		ALREADY_ON_SERVER = colorize(config.getString("ALREADY_ON_SERVER"));
		TELEPORTING_NOW = colorize(config.getString( "TELEPORTING_NOW"));
		TELEPORTING_LATER = colorize(config.getString( "TELEPORTING_LATER"));
		ALREADY_TELEPORTING = colorize(config.getString( "ALREADY_TELEPORTING"));
		UNKNOWN_SERVER = colorize(config.getString( "UNKNOWN_SERVER"));
	}
	
	private void initConfigDefaultMessage(String path, Object value) {
		if (!config.isString(path)) config.set(path, value);
	}

	public static String colorize(String input) {
		return ChatColor.translateAlternateColorCodes('&', input);
	}
	
	public int getWaitingDuration(String serverName) {
		Integer duration = time.get(serverName);
		return duration != null ? duration.intValue() : 0;
	}

	private void registerCommands() {
		for (String serverName : ProxyServer.getInstance().getServers().keySet()) {
			ProxyServer.getInstance().getPluginManager().registerCommand(this, new ServerCommand(serverName, "slashserver." + serverName));
		}

		ProxyServer.getInstance().getPluginManager().registerCommand( this, new SlashServerReloadCommand( "reloadss", "slashserver.reload", "reloadslashserver", "slashserverreload", "ssreload" ) );
	}
}
