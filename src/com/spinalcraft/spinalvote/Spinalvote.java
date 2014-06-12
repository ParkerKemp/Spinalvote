package com.spinalcraft.spinalvote;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.spinalcraft.spinalpack.*;

public class Spinalvote extends JavaPlugin{
	
	ConsoleCommandSender console;
	
	@Override
	public void onEnable(){
		
		console = Bukkit.getConsoleSender();
		
		console.sendMessage(Spinalpack.code(Co.BLUE) + "Spinalvote online!");
		getServer().getPluginManager().registerEvents((Listener)new SpinalvoteListener(this),  this);
		Spinalpack.createVoteTable();
	}
	
	@Override
	public void onDisable(){
		//Unregister vote listener (to avoid duplication)
		HandlerList.unregisterAll(this);
	}
}
