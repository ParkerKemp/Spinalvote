package com.spinalcraft.spinalvote;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.spinalcraft.spinalpack.Co;
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
		
		String uuidString = null;
		try {
			UUID uuid = UUIDFetcher.getUUIDOf(username);
			if(uuid != null)
				uuidString = uuid.toString();
			else{
				plugin.console.sendMessage(ChatColor.RED + "Couldn't find UUID for user " + username + "!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		insertVoteRecord(username, vote.getTimeStamp(), vote.getServiceName(), uuidString);
		
		if(uuidString == null)
			return;
		
		Bukkit.broadcastMessage(ChatColor.GOLD + username + " just voted for Spinalcraft!");
		Player player;
		if((player = Bukkit.getPlayer(vote.getUsername())) != null)
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
		PlayerInventory inventory = player.getInventory();
		inventory.addItem(new ItemStack(Material.EXP_BOTTLE, 24));
		player.sendMessage(Spinalpack.code(Co.BLUE) + "You got 24 exp bottles for voting!");
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
	
	private int consecutiveDays(String uuid){
		String query = "CALL CONSECUTIVEVOTES(?)";
		int i = 1;
		try {
			PreparedStatement stmt = Spinalpack.prepareStatement(query);
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
