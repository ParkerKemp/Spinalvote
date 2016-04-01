package com.spinalcraft.spinalvote;

import com.spinalcraft.spinalpack.Spinalpack;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class Spinalvote extends JavaPlugin{
	
	public static final int NUM_WEBSITES = 2;
	ConsoleCommandSender console;
	SpinalvoteListener voteListener;
	CommandExecutor executor;
	
	@Override
	public void onEnable(){	
		console = Bukkit.getConsoleSender();
		
		console.sendMessage(ChatColor.BLUE + "Spinalvote online!");
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
	}
	
	private void createVoteTables(){
		String query = "CREATE TABLE IF NOT EXISTS Votes (ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY, username VARCHAR(31), date VARCHAR(63), service VARCHAR(63), uuid VARCHAR(36))";
		Spinalpack.update(query);
		query = "CREATE TABLE IF NOT EXISTS VoteRewards (hash VARCHAR(36) PRIMARY KEY, uuid VARCHAR(36), username VARCHAR(31), date VARCHAR(63), choice INT)";
		Spinalpack.update(query);
	}
	
	@Override
	public void onDisable(){
		HandlerList.unregisterAll(this);
	}
}
