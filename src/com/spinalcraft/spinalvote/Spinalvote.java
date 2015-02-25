package com.spinalcraft.spinalvote;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.spinalcraft.spinalpack.*;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

public class Spinalvote extends JavaPlugin{
	
	public static final int NUM_WEBSITES = 2;
	ConsoleCommandSender console;
	SpinalvoteListener voteListener;
	
	
	@Override
	public void onEnable(){	
		console = Bukkit.getConsoleSender();
		
		console.sendMessage(ChatColor.BLUE + "Spinalvote online!");
		voteListener = new SpinalvoteListener(this);
		getServer().getPluginManager().registerEvents((Listener)voteListener,  this);
		createVoteTable();
	}
	
	private void createVoteTable(){
		String query = "CREATE TABLE IF NOT EXISTS Votes (ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY, username VARCHAR(31), date VARCHAR(63), service VARCHAR(63), uuid VARCHAR(36))";
		Spinalpack.update(query);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("vote")){
			if(sender instanceof Player){
				Player player = (Player)sender;
				player.sendMessage("");
				player.sendMessage(Spinalpack.code(Co.GREEN) + "You can support Spinalcraft by voting for us on different server lists. Currently we're listed on two websites:");
				player.sendMessage(Spinalpack.code(Co.BLUE) + "http://www.planetminecraft.com/server/spinalcraft/vote/");
				player.sendMessage(Spinalpack.code(Co.GREEN) + "and");
				player.sendMessage(Spinalpack.code(Co.BLUE) + "http://minecraft-server-list.com/server/177423/vote/");
				player.sendMessage(Spinalpack.code(Co.GREEN) + "Vote for both and get a long haste buff!");
				player.sendMessage("");
				return true;
			}
		}
		if(cmd.getName().equalsIgnoreCase("testvote")){
			if(sender instanceof Player){
				Player player = (Player)sender;
				
				Vote vote = new Vote();
				vote.setUsername(player.getName());
				vote.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
				vote.setServiceName("Test");
				
				Bukkit.getServer().getPluginManager().callEvent(new VotifierEvent(vote));
				return true;
			}
		}
		if(cmd.getName().equalsIgnoreCase("consecutive")){
			if(args.length == 0)
				return false;
			sender.sendMessage(args[0] + " voted " + voteListener.consecutiveDaysFromUsername(args[0]) + " consecutive days.");
			return true;
		}
		return false;
	}
	
	@Override
	public void onDisable(){
		HandlerList.unregisterAll(this);
	}
}
