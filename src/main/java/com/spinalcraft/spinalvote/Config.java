package com.spinalcraft.spinalvote;

import org.bukkit.configuration.file.FileConfiguration;

public class Config {
	
	public String announcementMessage;

	public Config(FileConfiguration configuration)
	{
		announcementMessage = configuration.getString("announcement-message");
	}
}
