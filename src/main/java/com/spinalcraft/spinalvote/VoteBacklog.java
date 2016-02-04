package com.spinalcraft.spinalvote;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.spinalcraft.spinalpack.Spinalpack;

public class VoteBacklog implements Runnable{
	
	private Player player;
	
	public VoteBacklog(Player player){
		this.player = player;
	}
	
	@Override
	public void run(){
		int count = getCount();
		
		if(count > 0){
			reportBacklog(count);
		}
	}
	
	private void reportBacklog(int count){
		String number = (count > 1) ? "rewards" : "reward";
		String pronoun = (count > 1) ? "them" : "it";
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		player.sendMessage(ChatColor.BLUE + "You have " + count + " unclaimed vote " + number + "! Use " + ChatColor.AQUA + "/claimvote" + ChatColor.BLUE + " to claim " + pronoun + "!");
	}
	
	private int getCount(){
		int count = 0;
		String query = "SELECT count(1) AS c FROM VoteRewards WHERE uuid = ? AND choice = 0";
		try {
			PreparedStatement stmt = Spinalpack.prepareStatement(query);
			
			stmt.setString(1, player.getUniqueId().toString());
			ResultSet rs = stmt.executeQuery();
			
			rs.first();
			count = rs.getInt("c");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}

}
