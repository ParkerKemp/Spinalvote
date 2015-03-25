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

import main.java.com.spinalcraft.spinalpack.Spinalpack;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

public class SocketListener implements Runnable {
	private Spinalvote plugin;
	private Socket sock;

	public SocketListener(Spinalvote plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		File socketFile = new File(System.getProperty("user.dir") + "/plugins/Spinalpack/sockets/vote.sock");
		socketFile.delete();
		
		AFUNIXServerSocket server;
		try {
			server = AFUNIXServerSocket.newInstance();
			server.bind(new AFUNIXSocketAddress(socketFile));
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}

		while (!Thread.interrupted()) {
			try {
				sock = server.accept();
				InputStream is = sock.getInputStream();
				byte[] buffer = new byte[128];
				int read = is.read(buffer);
				if (read == -1) {
					System.err.println("(Socket) Received empty message.");
					respond("ERR");
					continue;
				}
				String input = new String(buffer, 0, read);
				String[] tokens = input.split(":");
				if (tokens.length < 2) {
					System.err.println("(Socket) Improperly formatted message: \"" + input + "\"");
					respond("ERR");
					continue;
				}
				String hash = tokens[0];
				int choice;
				try {
					choice = Integer.parseInt(tokens[1]);
				} catch (NumberFormatException e) {
					System.err.println("(Socket) Second argument not an integer: \"" + input + "\"");
					respond("ERR");
					continue;
				}

				UUID uuid = uuidFromHash(hash);
				if (uuid == null) {
					respond("BAD");
					continue;
				} else {
					respond("GOOD");
				}
				Player player = Bukkit.getPlayer(uuid);
				sendVoteReward(player, choice);
				setChoice(hash, choice);

			} catch (IOException | SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void respond(String response) throws IOException {
		OutputStream os = sock.getOutputStream();
		byte[] responseBuffer = response.getBytes();
		os.write(responseBuffer);
	}

	private UUID uuidFromHash(String hash) throws SQLException {
		String query = "SELECT uuid FROM VoteRewards WHERE hash = ? AND choice = 0";
		PreparedStatement stmt = Spinalpack.prepareStatement(query);
		stmt.setString(1, hash);
		ResultSet rs = stmt.executeQuery();
		if (!rs.first())
			return null;
		String uuidString = rs.getString("uuid");
		UUID uuid = UUID.fromString(uuidString);
		return uuid;
	}

	private void setChoice(String hash, int choice) throws SQLException {
		String query = "UPDATE VoteRewards SET choice = ? WHERE hash = ?";
		PreparedStatement stmt = Spinalpack.prepareStatement(query);
		stmt.setInt(1, choice);
		stmt.setString(2, hash);
		stmt.executeUpdate();
	}

	private void sendVoteReward(Player player, int choice) {
		int multiplier = Math.min(12, SpinalvoteListener.consecutiveDays(player.getUniqueId().toString()));

		new VoteRewardTask(player, choice, multiplier).runTask(this.plugin);
	}
}
