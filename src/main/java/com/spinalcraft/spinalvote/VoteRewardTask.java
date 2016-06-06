package com.spinalcraft.spinalvote;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class VoteRewardTask extends BukkitRunnable {
	private Player player;
	private int choice;
	private int multiplier;

	public VoteRewardTask(Player player, int choice, int multiplier) {
		this.player = player;
		this.choice = choice;
		this.multiplier = multiplier;
	}

	@Override
	public void run() {
		int hasteMinutes = 15 * multiplier;

		switch (choice) {
		case 1:
			player.removePotionEffect(PotionEffectType.FAST_DIGGING);
			player.addPotionEffect(new PotionEffect(
					PotionEffectType.FAST_DIGGING, 20 * 60 * hasteMinutes, 1));
			if (multiplier == 1) {
				player.sendMessage(ChatColor.BLUE
						+ "You earned " + ChatColor.AQUA
						+ "Haste II" + ChatColor.BLUE + " for " + hasteMinutes
						+ " minutes!");
				player.sendMessage(ChatColor.BLUE
						+ "(Vote again tomorrow and it will be longer!)");
			} else {
				player.sendMessage(ChatColor.BLUE
						+ "You earned " + ChatColor.AQUA
						+ "Haste II" + ChatColor.BLUE + " for " + hasteMinutes
						+ " minutes!");
				player.sendMessage(ChatColor.BLUE
						+ "(The duration increases every day you vote, up to a maximum of 3 hours.)");
			}
			break;
		case 2:
			PlayerInventory inv = player.getInventory();
			HashMap<Integer, ItemStack> map = inv.addItem(new ItemStack(Material.EXP_BOTTLE, 48));
			if (!map.isEmpty()){
				for (ItemStack stack : map.values()){
					player.getWorld().dropItemNaturally(player.getLocation(), stack);
				}
			}
			player.sendMessage(ChatColor.BLUE + "You earned 48 exp bottles!");
			break;
		}
	}
}
