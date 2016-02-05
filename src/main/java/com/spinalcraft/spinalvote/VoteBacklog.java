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
		player.sendMessage(ChatColor.GREEN + "You have " + count + " unclaimed vote " + number + "! Use " + ChatColor.AQUA + "/claimreward" + ChatColor.GREEN + " to claim " + pronoun + "!");
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

	public static String getRewardHash(Player player){
		String uuid = player.getUniqueId().toString();
		String query = "SELECT hash FROM VoteRewards WHERE uuid = ? AND choice = 0";
		String hash = null;
		try {
			PreparedStatement stmt = Spinalpack.prepareStatement(query);
			stmt.setString(1, uuid);
			ResultSet rs = stmt.executeQuery();
			if(!rs.first())
				return null;
			hash = rs.getString("hash");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return hash;
	}
}



