package com.spinalcraft.spinalvote;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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
		insertVoteRecord(vote.getUsername(), vote.getTimeStamp(), vote.getServiceName());
		Player player;
		if((player = Bukkit.getPlayer(vote.getUsername())) != null){
			voteReward(player);
		}
	}
	
	private void insertVoteRecord(String username, String timestamp, String service){
		String query;
		query = "INSERT INTO Votes(username, date, service) values(?, ?, ?)";
		try {
			PreparedStatement stmt = Spinalpack.prepareStatement(query);
			stmt.setString(1, username);
			stmt.setString(2, timestamp);
			stmt.setString(3, service);
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void voteReward(Player player){
		PlayerInventory inventory = player.getInventory();
		inventory.addItem(new ItemStack(Material.EXP_BOTTLE, 24));
		player.sendMessage(Spinalpack.code(Co.BLUE) + "You got 24 exp bottles for voting!");
	}
}
