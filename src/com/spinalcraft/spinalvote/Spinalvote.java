package com.spinalcraft.spinalvote;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
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
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("vote")){
			Player player = (Player)sender;
			player.sendMessage(Spinalpack.code(Co.BLUE) + "You can support Spinalcraft by voting for us on different server lists. Currently we're listed on two websites:");
			player.sendMessage(Spinalpack.code(Co.GOLD) + "http://www.planetminecraft.com/server/spinalcraft/vote/");
			player.sendMessage(Spinalpack.code(Co.BLUE) + "and");
			player.sendMessage(Spinalpack.code(Co.GOLD) + "http://minecraft-server-list.com/server/177423/vote/");
			player.sendMessage(Spinalpack.code(Co.BLUE) + "Each vote earns you 15 experience levels!");
			return true;
		}
		return false;
	}
	
	@Override
	public void onDisable(){
		//Unregister vote listener (to avoid duplication)
		HandlerList.unregisterAll(this);
	}
}
