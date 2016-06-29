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
import org.bukkit.event.player.PlayerLoginEvent;

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
	
	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerLoginEvent(final PlayerLoginEvent event){
		new Thread(new VoteBacklog(event.getPlayer())).start();
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
		
		Spinalpack spinalPack = (Spinalpack) Bukkit.getPluginManager().getPlugin("Spinalpack");
		if (spinalPack != null){
			spinalPack.broadcastMessage(ChatColor.GOLD + username + " just voted for Spinalcraft!");
		}
		if(uuid == null)
			return;
		if(!completeVotes(uuidString))
			return;
		registerPendingReward(username, uuid);
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
	
	public void registerPendingReward(String username, UUID uuid){
		String query = "INSERT INTO VoteRewards(hash, uuid, username, date, choice) VALUES (?, ?, ?, ?, 0)";
		UUID hash = UUID.randomUUID();
		try {
			PreparedStatement stmt = Spinalpack.prepareStatement(query);
			stmt.setString(1, hash.toString());
			stmt.setString(2, uuid.toString());
			stmt.setString(3, username);
			stmt.setLong(4, System.currentTimeMillis());
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		Player player = Bukkit.getPlayer(uuid);
		if (player != null){
			notifyPendingReward(player, hash.toString());
		}
	}
	
	public static void notifyPendingReward(Player player, String hash){
		player.sendMessage(ChatColor.BLUE + "Thanks for voting! Choose your reward!");
		player.sendMessage("");
		player.sendMessage(ChatColor.AQUA + "Click here for Haste II:");
		player.sendMessage(ChatColor.MAGIC + "http://vote.spinalcraft.com?hash=" + hash.toString() + "&choice=1");
		player.sendMessage(ChatColor.AQUA + "Click here for Exp Bottles:");
		player.sendMessage(ChatColor.MAGIC + "http://vote.spinalcraft.com?hash=" + hash.toString() + "&choice=2");
	}
	
	public void sendVoteReward(Player player){
		int multiplier = Math.min(12, consecutiveDays(player.getUniqueId().toString()));
		
		new VoteRewardTask(player, 1, multiplier).runTask(this.plugin);
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
	
	public static int consecutiveDays(String uuid){
		String query = "CALL CONSECUTIVEVOTES(?)";
		int i = 1;
		try {
			PreparedStatement stmt = Spinalpack.prepareStatement(query);
			stmt.setString(1, uuid);
			ResultSet rs = stmt.executeQuery();
			while(rs.next())
				if(rs.getInt("hourgap") <= 36)
					i++;
				else
					break;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return i;
	}
}
