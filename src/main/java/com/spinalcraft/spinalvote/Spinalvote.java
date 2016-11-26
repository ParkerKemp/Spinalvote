package com.spinalcraft.spinalvote;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.spinalcraft.skull.SpinalcraftPlugin;

public class Spinalvote extends SpinalcraftPlugin{
	
	public static final int NUM_WEBSITES = 2;
	public ConsoleCommandSender console;
	private SpinalvoteListener voteListener;
	private CommandExecutor executor;
	
	public Config config;
	
	@Override
	public void onEnable(){
		super.onEnable();
		
		console = Bukkit.getConsoleSender();
		
		voteListener = new SpinalvoteListener(this);
		getServer().getPluginManager().registerEvents((Listener)voteListener,  this);
		
		createVoteTables();
		
		new Thread(new SocketListener(this)).start();
		
		executor = new VoteCommandExecutor(voteListener);
		getCommand("vote").setExecutor(executor);
		getCommand("testvote").setExecutor(executor);
		getCommand("consecutive").setExecutor(executor);
		getCommand("claimreward").setExecutor(executor);
		getCommand("voteraffle").setExecutor(executor);
		//getCommand("testgive").setExecutor(executor);
		
		saveDefaultConfig();
		loadConfig();
	}
	
	private void loadConfig(){
		reloadConfig();
		FileConfiguration configuration = getConfig();
		
		config = new Config(configuration);
	}
	
	private void createVoteTables(){
		String query = "CREATE TABLE IF NOT EXISTS Votes (ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY, username VARCHAR(31), date VARCHAR(63), service VARCHAR(63), uuid VARCHAR(36))";
		update(query);
		query = "CREATE TABLE IF NOT EXISTS VoteRewards (hash VARCHAR(36) PRIMARY KEY, uuid VARCHAR(36), username VARCHAR(31), date VARCHAR(63), choice INT)";
		update(query);
	}
	
	@Override
	public void onDisable(){
		HandlerList.unregisterAll(this);
	}
}
