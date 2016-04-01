package com.spinalcraft.spinalvote;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.spinalcraft.spinalpack.Co;
import com.spinalcraft.spinalpack.Spinalpack;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

public class VoteCommandExecutor implements CommandExecutor{
	private SpinalvoteListener listener;
	
	public VoteCommandExecutor(SpinalvoteListener listener){
		this.listener = listener;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
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
			if(sender instanceof Player){
				Player player = (Player)sender;
				
				Vote vote = new Vote();
				vote.setUsername(player.getName());
				vote.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
				vote.setServiceName("Test");
				
				Bukkit.getServer().getPluginManager().callEvent(new VotifierEvent(vote));
				return true;
			}
		}
		if(cmd.getName().equalsIgnoreCase("consecutive")){
			if(args.length == 0)
				return false;
			sender.sendMessage(args[0] + " voted " + listener.consecutiveDaysFromUsername(args[0]) + " consecutive days.");
			return true;
		}
		return false;
	}
}
