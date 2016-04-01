package com.spinalcraft.spinalvote;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class VoteRaffle {
	public static class Winner{
		public String username;
		public int votes;
		public double probability;
		
		public Winner(String u, int v, double p){
			this.username = u;
			this.votes = v;
			this.probability = p;
		}
	}

	public static int NO_CAP = -1;
	
	public static Winner pullWinner(HashMap<String, Integer> map, int cap){
		if (map == null){
			return null;
		}
		ArrayList<String> votes = new ArrayList<String>();
		for (String key : map.keySet()){
			int count = map.get(key);
			if (cap != NO_CAP && count < cap){
				continue;
			}
			for (int i = 0; i < count; i++){
				votes.add(key);
			}
		}
		if (votes.isEmpty()){
			return null;
		}
		String winnerName = votes.get((int)(Math.random() * votes.size()));
		int freq = Collections.frequency(votes, winnerName);
		
		return new Winner(winnerName, freq, (double)freq / votes.size() * 100);
	}
}
