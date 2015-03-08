package com.spinalcraft.spinalvote;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import com.spinalcraft.spinalpack.Spinalpack;

public class SocketListener implements Runnable{
	private Spinalvote plugin;
	
	public SocketListener(Spinalvote plugin){
		this.plugin = plugin;
	}
	@Override
	public void run(){
		File socketFile = new File(System.getProperty("user.dir") + "/plugins/Spinalpack/vote.sock");
		try {
			AFUNIXServerSocket server = AFUNIXServerSocket.newInstance();
			server.bind(new AFUNIXSocketAddress(socketFile));
			
			while(!Thread.interrupted()){
				Socket sock = server.accept();
				InputStream is = sock.getInputStream();
				OutputStream os = sock.getOutputStream();
				byte[] buffer = new byte[128];
				int read = is.read(buffer);
				String input = new String(buffer, 0, read);
				String[] tokens = input.split(":");
				String hash = tokens[0];
				int choice = Integer.parseInt(tokens[1]);
				
				UUID uuid = uuidFromHash(hash);
				if(uuid == null){
					String response = "BAD";
					byte[] responseBuffer = response.getBytes();
					os.write(responseBuffer);
					continue;
				}
				else{
					String response = "GOOD";
					byte[] responseBuffer = response.getBytes();
					os.write(responseBuffer);
				}
				Player player = Bukkit.getPlayer(uuid);
				sendVoteReward(player, choice);
				setChoice(hash, choice);
			}
			
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
	}
	
	private UUID uuidFromHash(String hash) throws SQLException{
		String query = "SELECT uuid FROM VoteRewards WHERE hash = ? AND choice = 0";
		PreparedStatement stmt = Spinalpack.prepareStatement(query);
		stmt.setString(1, hash);
		ResultSet rs = stmt.executeQuery();
		if(!rs.first())
			return null;
		String uuidString = rs.getString("uuid");
		UUID uuid = UUID.fromString(uuidString);
		return uuid;
	}
	
	private void setChoice(String hash, int choice) throws SQLException{
		String query = "UPDATE VoteRewards SET choice = ? WHERE hash = ?";
		PreparedStatement stmt = Spinalpack.prepareStatement(query);
		stmt.setInt(1, choice);
		stmt.setString(2, hash);
		stmt.executeUpdate();
	}
	
	private void sendVoteReward(Player player, int choice){
		int multiplier = Math.min(12, SpinalvoteListener.consecutiveDays(player.getUniqueId().toString()));
		
		new VoteRewardTask(player, choice, multiplier).runTask(this.plugin);
	}
}
