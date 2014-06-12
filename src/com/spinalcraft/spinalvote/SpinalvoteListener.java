package com.spinalcraft.spinalvote;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.spinalcraft.spinalpack.Co;
import com.spinalcraft.spinalpack.Spinalpack;
import com.vexsoftware.votifier.model.VotifierEvent;
import com.vexsoftware.votifier.model.Vote;

public class SpinalvoteListener implements Listener{
	JavaPlugin plugin;
	
	public SpinalvoteListener(JavaPlugin plugin){
		this.plugin = plugin;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority=EventPriority.NORMAL)
	public void onVotifierEvent(VotifierEvent event){
		Vote vote = event.getVote();
		Bukkit.broadcastMessage(Spinalpack.code(Co.GOLD) + vote.getUsername() + " just voted for Spinalcraft!");
		Spinalpack.insertVoteRecord(vote.getUsername(), vote.getTimeStamp(), vote.getServiceName());
		Player target;
		if((target = Bukkit.getPlayer(vote.getUsername())) != null){
			target.setLevel(target.getLevel() + 15);
			target.sendMessage(Spinalpack.code(Co.BLUE) + "You got 15 levels for voting!");
		}
	}
}
