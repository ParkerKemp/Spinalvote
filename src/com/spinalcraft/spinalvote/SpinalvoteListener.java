package com.spinalcraft.spinalvote;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.spinalcraft.spinalpack.Spinalpack;
import com.spinalcraft.usernamehistory.UUIDFetcher;
import com.vexsoftware.votifier.model.VotifierEvent;
import com.vexsoftware.votifier.model.Vote;

public class SpinalvoteListener implements Listener{
	Spinalvote plugin;
	
	public SpinalvoteListener(Spinalvote plugin){
		this.plugin = plugin;
	}
	
	@EventHandler(priority=EventPriority.NORMAL)
	public void onVotifierEvent(VotifierEvent event){
		processVote(event.getVote());
	}
	
	@SuppressWarnings("deprecation")
	public void processVote(Vote vote){
		String username = vote.getUsername();
		String uuid = null;
		try {
			uuid = UUIDFetcher.getUUIDOf(username);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(uuid == null)
			plugin.console.sendMessage(ChatColor.RED + "Couldn't find UUID for user " + username + "!");
		insertVoteRecord(username, vote.getTimeStamp(), vote.getServiceName(), uuid);
		
		Bukkit.broadcastMessage(ChatColor.GOLD + username + " just voted for Spinalcraft!");
		if(!completeVotes(uuid))
			return;
		Player player;
		if((player = Bukkit.getPlayer(username)) != null)
			voteReward(player);
	}
	
	private void insertVoteRecord(String username, String timestamp, String service, String uuid){
		String query;
		query = "INSERT INTO Votes(username, date, service, uuid) values(?, ?, ?, ?)";
		try {
			PreparedStatement stmt = Spinalpack.prepareStatement(query);
			stmt.setString(1, username);
			stmt.setString(2, timestamp);
			stmt.setString(3, service);
			stmt.setString(4, uuid);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void voteReward(Player player){
		int multiplier = Math.min(12, consecutiveDays(player.getUniqueId().toString()));
		
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
	
	private boolean completeVotes(String uuid){
		//Assumes most recent vote has already been inserted
		
		String query = "SELECT COUNT(*) AS count FROM Votes WHERE uuid = ? AND DATEDIFF(FROM_UNIXTIME(date), NOW()) = 0";
		try {
			PreparedStatement stmt = Spinalpack.prepareStatement(query);
			ResultSet rs = stmt.executeQuery();
			rs.first();
			return rs.getInt("count") == Spinalvote.NUM_WEBSITES;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public int consecutiveDaysFromUsername(String username){
		String uuid = null;
		try {
			uuid = UUIDFetcher.getUUIDOf(username);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(uuid == null)
			return 0;
		return consecutiveDays(uuid.toString());
	}
	
	private int consecutiveDays(String uuid){
		String query = "CALL CONSECUTIVEVOTES(?)";
		int i = 1;
		try {
			PreparedStatement stmt = Spinalpack.prepareStatement(query);
			stmt.setString(1, uuid);
			ResultSet rs = stmt.executeQuery();
			while(rs.next())
				if(rs.getInt("dategap") == 1)
					i++;
				else
					break;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return i;
	}
}
