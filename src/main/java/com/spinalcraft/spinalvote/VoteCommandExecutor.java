package com.spinalcraft.spinalvote;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.spinalcraft.spinalpack.Co;
import com.spinalcraft.spinalpack.Spinalpack;
import com.spinalcraft.spinalvote.VoteRaffle.Winner;
import com.spinalcraft.usernamehistory.UUIDFetcher;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

public class VoteCommandExecutor implements CommandExecutor{
	private SpinalvoteListener listener;
	
	public VoteCommandExecutor(SpinalvoteListener listener){
		this.listener = listener;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		if(cmd.getName().equalsIgnoreCase("vote")){
			if(sender instanceof Player){
				Player player = (Player)sender;
				player.sendMessage("");
				player.sendMessage(Spinalpack.code(Co.GREEN) + "You can support Spinalcraft by voting for us on different server lists. Currently we're listed on two websites:");
				player.sendMessage(Spinalpack.code(Co.BLUE) + "http://minecraft-server-list.com/server/177423/vote/");
				player.sendMessage(Spinalpack.code(Co.GREEN) + "and");
				player.sendMessage(Spinalpack.code(Co.BLUE) + "http://www.planetminecraft.com/server/spinalcraft/vote/");
				player.sendMessage(Spinalpack.code(Co.GREEN) + "Vote for us on both sites and get a reward!");
				player.sendMessage("");
				return true;
			}
		}
		if(cmd.getName().equalsIgnoreCase("claimreward")){
			if(sender instanceof Player){
				Player player = (Player)sender;
				String hash = VoteBacklog.getRewardHash(player);
				if(hash == null){
					player.sendMessage(ChatColor.RED + "You have no unclaimed votes!");
					return true;
				}
				SpinalvoteListener.notifyPendingReward(player, hash);
				return true;
			}
			return false;
		}
		if(cmd.getName().equalsIgnoreCase("testvote")){
			Vote vote = new Vote();
			
			if (sender instanceof Player){
				Player player = (Player)sender;
				vote.setUsername(player.getName());
				sendTestVote(vote);
			} else {
				if (args.length < 1){
					return false;
				}
				new Thread(new Runnable(){
					@Override
					public void run(){
						testVoteByUsername(args[0], sender);
					}
				}).run();
			}
			return true;
		}
		if(cmd.getName().equalsIgnoreCase("consecutive")){
			if(args.length == 0)
				return false;
			sender.sendMessage(args[0] + " voted " + listener.consecutiveDaysFromUsername(args[0]) + " consecutive days.");
			return true;
		}
		/*
		if(cmd.getName().equalsIgnoreCase("testgive")){
			if (sender instanceof Player){
				new VoteRewardTask((Player) sender, 2, 0).runTask(sender.getServer().getPluginManager().getPlugin("Spinalvote"));
			}
			return true;
		}
		*/
		if(cmd.getName().equalsIgnoreCase("voteraffle")){
			if(sender instanceof Player || sender instanceof ConsoleCommandSender){
				if (args.length < 2){
					return false;
				}
				int month = Integer.parseInt(args[0]);
				int year = Integer.parseInt(args[1]);
				int cap = VoteRaffle.NO_CAP;
				if (args.length >= 3){
					cap = Integer.parseInt(args[2]);
					if (cap < 0){
						sender.sendMessage("Value of cap must be positive");
						return true;
					}
				}
				if (month < 1 || month > 12){
					sender.sendMessage("Value of month must be numerical between 1 and 12");
					return true;
				}
				if (year < 2000){
					sender.sendMessage("Value of year must be bigger than 2000");
					return true;
				}
				String query = "SELECT username, c FROM (SELECT username, uuid, COUNT(*) as c " + "FROM Votes " + "WHERE MONTH(FROM_UNIXTIME(date)) = ? AND YEAR(FROM_UNIXTIME(DATE)) = ? AND uuid IS NOT NULL " + "GROUP BY uuid " + "ORDER BY username) as V";
				
				try {
					PreparedStatement stmt = Spinalpack.prepareStatement(query);
					stmt.setInt(1, month);
					stmt.setInt(2, year);
					ResultSet rs = stmt.executeQuery();
					HashMap<String, Integer> hashmap = new HashMap<String, Integer>();
					while (rs.next()) {
						hashmap.put(rs.getString("username"), rs.getInt("c"));
					}
					Winner winner = VoteRaffle.pullWinner(hashmap, cap);
					if (winner == null){
						sender.sendMessage("There aren't any votes for the chosen month");
					} else {
						sender.sendMessage("The winner is " + winner.username + " with " + winner.votes + " votes and " + String.format("%.03f", winner.probability) + "% probability");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return true;
			}
		}
		return false;
	}
	
	private void testVoteByUsername(String username, CommandSender sender){
		Vote vote = new Vote();
		
		try {
			if (UUIDFetcher.getUUIDOf(username) == null){
				sender.sendMessage(username + " is not a valid player name!");
				return;
			};
		} catch (IOException e) {}
		
		vote.setUsername(username);

		sendTestVote(vote);
	}
	
	private void sendTestVote(Vote vote){
		vote.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
		vote.setServiceName("Test");
		
		Bukkit.getServer().getPluginManager().callEvent(new VotifierEvent(vote));
	}
}
