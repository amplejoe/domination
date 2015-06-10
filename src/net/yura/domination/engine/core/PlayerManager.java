package net.yura.domination.engine.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlayerManager implements Serializable {
	private static final long serialVersionUID = 563083159626703904L;

	private final List<Player> players = new ArrayList<Player>();

	public boolean addPlayer(int type, String name, int color, String a) {
		for (int c = 0; c < players.size(); c++) {
			if ((name.equals(players.get(c).getName()))
					|| (color == players.get(c).getColor()))
				return false;
		}

		// System.out.print("Player added. Type: " +type+ "\n"); // testing
		Player player = new Player(type, name, color, a);
		players.add(player);

		return true;
	}

	public boolean removePlayer(String name) {
		int n = -1;

		for (int c = 0; c < players.size(); c++) {
			if (name.equals(players.get(c).getName()))
				n = c;
		}
		if (n == -1) {
			// System.out.print("Error: No player found\n"); // testing
			return false;
		} else {
			players.remove(n);
			// System.out.print("Player removed\n"); // testing
			return true;
		}
	}

	public List<Player> getPlayers() {
		return players;
	}

	/**
	 * Gets the number of players in the game
	 * 
	 * @return int Return the number of number of players
	 */
	public int getNoPlayers() {
		return players.size();
	}

	public Player getPlayer(int index) {
		return players.get(index);
	}

	public Player getPlayer(String name) {
		for (Player player : (List<Player>) players) {
			if (player.getName().equals(name)) {
				return player;
			}
		}
		return null;
	}

	public Player getPlayerByCapital(Country c) {
		for (Player player : players) {
			if (player.getCapital() == c) {
				return player;
			}
		}
		return null;
	}
}
