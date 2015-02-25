package com.spinalcraft.spinalvote;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class VoteRewardTask extends BukkitRunnable{
	private Player player;
	private int multiplier;
	
	public VoteRewardTask(Player player, int multiplier){
		this.player = player;
		this.multiplier = multiplier;
	}
	
	@Override
	public void run(){
		int hasteMinutes = 15 * multiplier;
		
		player.removePotionEffect(PotionEffectType.FAST_DIGGING);
		player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 20 * 60 * hasteMinutes, 1));
		if(multiplier == 1){
			player.sendMessage(ChatColor.BLUE + "Thanks for voting! You earned " + ChatColor.AQUA + "Haste" + ChatColor.BLUE + " for " + hasteMinutes + " minutes!");
			player.sendMessage(ChatColor.BLUE + "(Vote again tomorrow and it will be longer!)");
		}
		else{
			player.sendMessage(ChatColor.BLUE + "Thanks for voting! You earned " + ChatColor.AQUA + "Haste" + ChatColor.BLUE + " for a bonus " + hasteMinutes + " minutes!");
			player.sendMessage(ChatColor.BLUE + "(The duration increases every day you vote, up to a maximum of 3 hours.)");
		}	
	}
}
