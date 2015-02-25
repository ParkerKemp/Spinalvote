package com.spinalcraft.spinalvote;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

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
	public void onVotifierEvent(final VotifierEvent event){
		new Thread(){
			public void run(){
				processVote(event.getVote());
			}
		}.start();
	}
	
	public void processVote(Vote vote){
		String username = vote.getUsername();
		UUID uuid = null;
		String uuidString = null;
		try {
			uuid = UUIDFetcher.getUUIDOf(username);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(uuid != null)
			uuidString = uuid.toString();
		else
			plugin.console.sendMessage(ChatColor.RED + "Couldn't find UUID for user " + username + "!");
		insertVoteRecord(username, vote.getTimeStamp(), vote.getServiceName(), uuidString);
		
		Bukkit.broadcastMessage(ChatColor.GOLD + username + " just voted for Spinalcraft!");
		if(!completeVotes(uuidString))
			return;
		Player player;
		if((player = Bukkit.getPlayer(uuid)) != null)
			sendVoteReward(player);
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
	
	public void sendVoteReward(Player player){
		int multiplier = Math.min(12, consecutiveDays(player.getUniqueId().toString()));
		
		new VoteRewardTask(player, multiplier).runTask(this.plugin);
	}
	
	private boolean completeVotes(String uuid){
		//Assumes most recent vote has already been inserted
		
		String query = "SELECT COUNT(*) AS count FROM Votes WHERE uuid = ? AND DATEDIFF(FROM_UNIXTIME(date), NOW()) = 0";
		try {
			PreparedStatement stmt = Spinalpack.prepareStatement(query);
			stmt.setString(1, uuid);
			ResultSet rs = stmt.executeQuery();
			rs.first();
			return rs.getInt("count") >= Spinalvote.NUM_WEBSITES;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public int consecutiveDaysFromUsername(String username){
		UUID uuid = null;
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
