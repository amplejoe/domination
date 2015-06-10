// Yura Mamyrin, Group D

package net.yura.domination.engine.core;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import net.yura.domination.engine.ColorUtil;
import net.yura.domination.engine.RiskObjectOutputStream;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.translation.MapTranslator;
import net.yura.domination.engine.translation.TranslationBundle;

/**
 * <p>
 * Risk Game Main Class
 * </p>
 * 
 * @author Yura Mamyrin
 */

public class RiskGame implements Serializable { // transient

	private static final long serialVersionUID = 8L;
	public final static String SAVE_VERSION = String.valueOf(serialVersionUID);

	public final static String NETWORK_VERSION = "12";

	public final static int MAX_PLAYERS = 6;
	public final static Continent ANY_CONTINENT = new Continent("any", "any",
			0, 0);

	public final static int STATE_NEW_GAME = 0;
	public final static int STATE_TRADE_CARDS = 1;
	public final static int STATE_PLACE_ARMIES = 2;
	public final static int STATE_ATTACKING = 3;
	public final static int STATE_ROLLING = 4;
	public final static int STATE_BATTLE_WON = 5;
	public final static int STATE_FORTIFYING = 6;
	public final static int STATE_END_TURN = 7;
	public final static int STATE_GAME_OVER = 8;
	public final static int STATE_SELECT_CAPITAL = 9;
	public final static int STATE_DEFEND_YOURSELF = 10;

	public final static int MODE_DOMINATION = 0;
	public final static int MODE_CAPITAL = 2;
	public final static int MODE_SECRET_MISSION = 3;

	public final static int CARD_INCREASING_SET = 0;
	public final static int CARD_FIXED_SET = 1;
	public final static int CARD_ITALIANLIKE_SET = 2;

	public final static int MAX_CARDS = 5;

	/*
	 * 
	 * // public final static int MODE_DOMINATION_2 = 1;
	 * 
	 * gameState:
	 * 
	 * nogame (-1 in gui) (current possible commands are: newgame, loadgame,
	 * closegame, savegame)
	 * 
	 * 9 - select capital (current possible commands are: capital) 10 - defend
	 * yourself! (current possible commands are: roll) 0 - new game just created
	 * (current possible commands are: newplayer, delplayer, startgame) 1 -
	 * trade cards (current possible commands are: showcards, trade, notrade) 2
	 * - placing new armies (current possible commands are: placearmies,
	 * autoplace) 3 - attacking (current possible commands are: attack
	 * endattack) 4 - rolling (current possible commands are: roll retreat) 5 -
	 * you have won! (current possible commands are: move) 6 - fortifying
	 * (current possible commands are: movearmy nomove) 7 - endturn (current
	 * possible commands are: endgo) 8 - game is over (current possible commands
	 * are: continue)
	 * 
	 * gameMode: 0 - WORLD DOMINATION RISK - 3 to 6 players //1 - WORLD
	 * DOMINATION RISK - 2 player 2 - CAPITAL RISK - 3 to 6 players 3 - SECRET
	 * MISSION RISK - 3 to 6 players
	 * 
	 * playerType: 0 - human 1 - AI (Easy) 2 - AI (Hard) 3 - AI (Crap)
	 * 
	 * transient - A keyword in the Java programming language that indicates
	 * that a field is not part of the serialized form of an object. When an
	 * object is serialized, the values of its transient fields are not included
	 * in the serial representation, while the values of its non-transient
	 * fields are included.
	 */

	private static String defaultMap;
	private static String defaultCards;

	// ---------------------------------------
	// THIS IS THE GAME INFO FOR Serialization
	// ---------------------------------------

	// cant use URL as that stores full URL to the map file on the disk,
	// and if the risk install dir changes the saves dont work
	private String mapfile;
	private String cardsfile;
	private int setup;

	private Country[] countries;
	private Continent[] continents;
	private Vector cards, usedCards;
	private Vector missions;

	private Player currentPlayer;
	private int gameState;
	private int cardState;
	private int mustmove;
	private boolean capturedCountry;
	private boolean tradeCap;
	private int gameMode;

	private Country attacker;
	private Country defender;

	private int attackerDice;
	private int defenderDice;

	private transient int battleRounds;

	private String imagePic;
	private String imageMap;
	private String previewPic;

	private Vector replayCommands;
	private int maxDefendDice;
	private int cardMode;

	private boolean runmaptest = false;
	private boolean recycleCards = false;

	/**
	 * The next fields should be injected.
	 */

	private PropertyManager propertyManager;
	
	private PlayerManager playerManager;

	private Parser parser;

	/**
	 * Creates a new RiskGame
	 */
	public RiskGame() throws Exception {
		// Should be injected instead
		propertyManager = new PropertyManager();
		playerManager = new PlayerManager();
		parser = new Parser(propertyManager);

		// try {

		setMapfile("default");
		setCardsfile("default");
		// }
		// catch (Exception e) {
		// RiskUtil.printStackTrace(e);
		// }

		setup = 0; // when setup reaches the number of players it goes into
					// normal mode

		gameState = STATE_NEW_GAME;
		cardState = 0;

		replayCommands = new Vector();

		// System.out.print("New Game created\n"); // testing

		// simone=true;//false;

		RiskUtil.RAND.setSeed(new Random().nextLong());
	}
	
	public PlayerManager getPlayerManager() {
		return playerManager;
	}

	public void addCommand(String a) {

		replayCommands.add(a);

	}

	public Vector getCommands() {

		return replayCommands;

	}

	public void setCommands(Vector replayCommands) {
		this.replayCommands = replayCommands;
	}

	public int getMaxDefendDice() {
		return maxDefendDice;
	}

	/**
	 * This adds a player to the game
	 * 
	 * @param type
	 *            Type of game (i.e World Domination, Secret Mission, Capital)
	 * @param name
	 *            Name of player
	 * @param color
	 *            Color of player
	 * @return boolean Returns true if the player is added, returns false if the
	 *         player can't be added.
	 */
	public boolean addPlayer(int type, String name, int color, String a) {
		if (gameState == STATE_NEW_GAME) { // && !name.equals("neutral") &&
											// !(color==Color.gray)
			return playerManager.addPlayer(type, name, color, a);
		} else
			return false;
	}

	/**
	 * This deletes a player in the game
	 * 
	 * @param name
	 *            Name of the player
	 * @return boolean Returns true if the player is deleted, returns false if
	 *         the player cannot be deleted
	 */
	public boolean delPlayer(String name) {
		if (gameState == STATE_NEW_GAME) {
			return playerManager.removePlayer(name);
		} else
			return false;

	}

	/**
	 * Starts the game Risk
	 * 
	 * @param mode
	 *            This represents the mode of the game: normal, 2 player,
	 *            capital or mission
	 */
	public void startGame(int mode, int card, boolean recycle, boolean threeDice)
			throws Exception {

		if (gameState == STATE_NEW_GAME) { // && ((mapfile !=null && cardsfile
											// !=null) || () )

			gameMode = mode;
			cardMode = card;

			recycleCards = recycle;
			maxDefendDice = threeDice ? 3 : 2;

			// 2 player human crap
			// if ( gameMode==1 && (
			// !(((Player)Players.elementAt(0)).getType()==0) ||
			// !(((Player)Players.elementAt(1)).getType()==0) ) ) { return; }

			// check if things need to be loaded, maybe already loaded, then
			// these will be null
			if (mapfile != null && cardsfile != null) {

				// try {

				loadMap();

				// }
				// catch (Exception e) {
				// RiskUtil.printStackTrace(e);
				// return;
				// }

				try {

					loadCards(false);

				} catch (Exception e) {

					if (runmaptest) {

						// System.out.println("LOAD FILE ERROR: " +
						// e.getMessage() +
						// "\n(This normally means you have selected the wrong set of cards for this map)");
						// // testing
						// RiskUtil.printStackTrace(e);
						throw new Exception(
								"LOAD FILE ERROR: "
										+ e.getMessage()
										+ "\n(This normally means you have selected the wrong set of cards for this map)",
								e);

					}

					return;
				}

				if (runmaptest) {

					// try {
					RiskUtil.testMap(countries); // testing maps
					// }
					// catch (Exception e) {
					// RiskUtil.printStackTrace(e);
					// return;
					// }
				}

			}

			if (countries == null) {
				return;
			}

			if (gameMode == MODE_SECRET_MISSION
					&& missions.size() < playerManager.getNoPlayers()) {
				return;
			}

			int armies = (10 - playerManager.getNoPlayers())
					* Math.round(countries.length * 0.12f);

			// System.out.print("armies="+ armies +"\n");
			//
			// if (gameMode==1) { // 2 player mode
			// Player player = new Player(3, "neutral", Color.gray , "all" );
			// Players.add(player);
			// }
			//
			// System.out.print("Game Started\n"); // testing

			for (int c = 0; c < playerManager.getNoPlayers(); c++) {
				playerManager.getPlayer(c).addArmies(armies);
			}

			gameState = STATE_PLACE_ARMIES;
			capturedCountry = false;
			tradeCap = false;

		}

	}

	/**
	 * Sets the current player in the game
	 * 
	 * @param name
	 *            The name of the current player
	 * @return Player Returns the current player in the game
	 */
	public Player setCurrentPlayer(int c) {
		currentPlayer = playerManager.getPlayer(c);
		return currentPlayer;

	}

	/**
	 * Gets the current player in the game
	 * 
	 * @return String Returns the name of a randomly picked player from the set
	 *         of players
	 */
	public int getRandomPlayer() {
		return RiskUtil.RAND.nextInt(playerManager.getNoPlayers());
	}

	/**
	 * Checks whether the player deserves a card during at the end of their go
	 * 
	 * @return String Returns the name of the card if deserves a card, else else
	 *         returns empty speech-marks
	 */
	public String getDesrvedCard() {
		// check to see if the player deserves a new risk card
		if (capturedCountry == true && cards.size() > 0) {

			Card c = (Card) cards
					.elementAt(RiskUtil.RAND.nextInt(cards.size()));
			if (c.getCountry() == null)
				return Card.WILDCARD;
			else
				return ((Country) c.getCountry()).getColor() + "";
		} else {
			return "";
		}
	}

	public boolean isCapturedCountry() {
		return capturedCountry;
	}

	/**
	 * Ends a player's go
	 * 
	 * @return Player Returns the next player
	 */
	public Player endGo() {

		if (gameState == STATE_END_TURN) {

			// System.out.print("go ended\n"); // testing

			// work out who is the next player

			int playersSize = playerManager.getNoPlayers();
			while (true) {
				for (int c = 0; c < playersSize; c++) {
					if (currentPlayer == playerManager.getPlayer(c)
							&& playersSize == (c + 1)) {
						currentPlayer = playerManager.getPlayer(0);
						c = playersSize;
					} else if (currentPlayer == playerManager.getPlayer(c)
							&& playersSize != (c + 1)) {
						currentPlayer = playerManager.getPlayer(c + 1);
						c = playersSize;
					}
				}

				if (!getSetupDone()) {
					break;
				}

				// && (currentPlayer.getType() != 3)

				else if (currentPlayer.getNoTerritoriesOwned() > 0) {
					break;
				}

			}

			// System.out.print("Curent Player: " + currentPlayer.getName() +
			// "\n"); // testing

			if (getSetupDone()
					&& !(gameMode == 2 && currentPlayer.getCapital() == null)) { // ie
																					// the
																					// initial
																					// setup
																					// has
																					// been
																					// compleated

				workOutEndGoStats(currentPlayer);
				currentPlayer.nextTurn();

				// add new armies for the Territories Owned
				if (currentPlayer.getNoTerritoriesOwned() < 9) {
					currentPlayer.addArmies(3);
				} else {
					currentPlayer.addArmies(currentPlayer
							.getNoTerritoriesOwned() / 3);
				}

				// add new armies for the Continents Owned
				for (int c = 0; c < continents.length; c++) {

					if (continents[c].isOwned(currentPlayer)) {
						currentPlayer.addArmies(continents[c].getArmyValue());
					}

				}

			}

			if (getSetupDone() && gameMode == 2
					&& currentPlayer.getCapital() == null) { // capital risk
																// setup not
																// finished
				gameState = STATE_SELECT_CAPITAL;
			} else if (canTrade() == false) { // ie the initial setup has not
												// been compleated or there are
												// no cards that can be traded
				gameState = STATE_PLACE_ARMIES;
			} else { // there are cards that can be traded
				gameState = STATE_TRADE_CARDS;
			}

			capturedCountry = false;
			tradeCap = false;

			return currentPlayer;

		} else {

			// System.out.println("lala "+gameState);

			return null;
		}
	}

	/**
	 * Trades a set of cards
	 * 
	 * @param card1
	 *            First card to trade
	 * @param card2
	 *            Second card to trade
	 * @param card3
	 *            Third card to trade
	 * @return int Returns the number of armies gained from the trade, returning
	 *         0 if the trade is unsuccessful
	 */
	public int trade(Card card1, Card card2, Card card3) {
		if (gameState != STATE_TRADE_CARDS)
			return 0;

		if (tradeCap && currentPlayer.getCards().size() < MAX_CARDS)
			throw new RuntimeException(
					"trying to do a trade when less then 5 cards and tradeCap is on");

		int armies = RiskUtil.getTradeAbsValue(card1.getName(),
				card2.getName(), card3.getName(), cardMode, cardState);

		if (armies <= 0)
			return 0;

		if (cardMode == CARD_INCREASING_SET) {
			cardState = armies;
		}

		currentPlayer.tradeInCards(card1, card2, card3);

		// Return the cards to the deck
		List used = getUsedCards();
		used.add(card1);
		used.add(card2);
		used.add(card3);

		recycleUsedCards();

		currentPlayer.addArmies(armies);

		// if tradeCap you must trade to redude your cards to 4 or fewer cards
		// but once your hand is reduced to 4, 3 or 2 cards, you must stop
		// trading
		if (!canTrade()
				|| (tradeCap && currentPlayer.getCards().size() < MAX_CARDS)) {
			gameState = STATE_PLACE_ARMIES;
			tradeCap = false;
		}

		return armies;
	}

	public int getTradeAbsValue(String c1, String c2, String c3, int cardMode) {
		return RiskUtil.getTradeAbsValue(c1, c2, c3, cardMode, cardState);
	}

	public boolean canTrade() {
		return getBestTrade(currentPlayer.getCards(), null) > 0;
	}

	/**
	 * Find the best (highest) trade Simple greedy search using the various
	 * valid combinations
	 * 
	 * @param cards
	 * @return
	 */
	public int getBestTrade(List<Card> cards, Card[] bestResult) {
		return RiskUtil.getBestTrade(cards, bestResult, getCardMode(),
				cardState);
	}

	public int getNewCardState() {
		return RiskUtil.getNewCardState(cardState);
	}

	/**
	 * checks if a set of cards can be traded
	 * 
	 * @param card1
	 *            First card to trade
	 * @param card2
	 *            Second card to trade
	 * @param card3
	 *            Third card to trade
	 * @return boolean true if they can be traded false if they can not
	 */
	public boolean checkTrade(Card card1, Card card2, Card card3) {
		return RiskUtil.getTradeAbsValue(card1.getName(), card2.getName(),
				card3.getName(), cardMode, cardState) > 0;
	}

	/**
	 * Ends the trading phase by checking if the player has less than 5 cards
	 * 
	 * @return boolean Returns true if the player has ended the trade phase,
	 *         returns false if the player cannot end the trade phase
	 */
	public boolean endTrade() {
		if (canEndTrade()) {
			gameState = STATE_PLACE_ARMIES;
			if (tradeCap) {
				throw new RuntimeException(
						"endTrade worked when tradeCap was true");
			}
			return true;
		}
		return false;
	}

	public boolean canEndTrade() {
		if (gameState == STATE_TRADE_CARDS) {
			// in italian rules there isn't a limit to the number of risk cards
			// that you can hold in your hand.
			if (cardMode == CARD_ITALIANLIKE_SET
					|| currentPlayer.getCards().size() < MAX_CARDS) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Places an army on the Country
	 * 
	 * @param t
	 *            Country that the player wants to add armies to
	 * @param n
	 *            Number of armies the player wants to add to the country
	 * @return boolean Returns true if the number of armies are added the
	 *         country, returns false if the armies cannot be added to the
	 *         territory
	 */
	public int placeArmy(Country t, int n) {

		int done = 0;

		if (gameState == STATE_PLACE_ARMIES) {

			if (!getSetupDone()) { // ie the initial setup has not been
									// compleated
				if (n != 1)
					return 0;
				// if it has the player as a owner
				if (t.getOwner() == currentPlayer) {

					if (NoEmptyCountries()) { // no empty country are found
						t.addArmy();
						currentPlayer.loseExtraArmy(1);
						done = 1;
						// System.out.print("army placed in: " + t.getName() +
						// "\n"); // testing
					}

				}
				// if there is no owner
				else if (t.getOwner() == null) {

					t.setOwner(currentPlayer);
					currentPlayer.newCountry(t);
					t.addArmy();
					currentPlayer.loseExtraArmy(1);
					done = 1;
					// System.out.print("country taken and army placed in: " +
					// t.getName() + "\n"); // testing
				}

			} else { // initial setup is completed

				// if it has the player as a owner
				if (t.getOwner() == currentPlayer
						&& currentPlayer.getExtraArmies() >= n) {

					currentPlayer.currentStatistic.addReinforcements(n);

					t.addArmies(n);
					currentPlayer.loseExtraArmy(n);
					// System.out.print("army placed in: " + t.getName() +
					// "\n"); // testing
					done = 1;

				}
			}

			if (done == 1) {

				if (getSetupDone()) { // ie the initial setup has been
										// compleated
					if (currentPlayer.getExtraArmies() == 0) {
						gameState = STATE_ATTACKING;
					} else {
						gameState = STATE_PLACE_ARMIES;
					}
				} else { // initial setup is not compleated
					if (currentPlayer.getExtraArmies() == 0) {
						setup++; // another player has finished initial setup

					}

					gameState = STATE_END_TURN;

				}

				if (checkPlayerWon()) {
					done = 2;
				}

			}

		}
		return done;

	}

	/**
	 * Automatically places an army on an unoccupied country
	 * 
	 * @return int Returns the country id which an army was added to
	 */
	public int getRandomCountry() {
		if (gameState == STATE_PLACE_ARMIES) {
			if (NoEmptyCountries()) {
				List countries = currentPlayer.getTerritoriesOwned();
				return ((Country) countries.get(RiskUtil.RAND.nextInt(countries
						.size()))).getColor();
			} else {
				// find a empty country
				int a = RiskUtil.RAND.nextInt(countries.length);
				boolean done = false;
				for (int c = a; c < countries.length; c++) {
					if (countries[c].getOwner() == null) {
						return countries[c].getColor();
					} else if (c == countries.length - 1 && !done) {
						c = -1;
						done = true;
					} else if (c == countries.length - 1 && done) {
						break;
					}
				}
			}
		}
		throw new IllegalStateException();
	}

	/**
	 * Attacks one country and another
	 * 
	 * @param t1
	 *            Attacking country
	 * @param t2
	 *            Defending country
	 * @return int[] Returns an array which determines if the player is allowed
	 *         to roll dice
	 */
	public boolean attack(Country t1, Country t2) {

		boolean result = false;

		if (gameState == STATE_ATTACKING) {

			if (t1 != null && t2 != null && t1.getOwner() == currentPlayer
					&& t2.getOwner() != currentPlayer && t1.isNeighbours(t2) &&
					// t2.isNeighbours(t1) && // not needed as there is code to
					// check this
					t1.getArmies() > 1) {

				currentPlayer.currentStatistic.addAttack();
				((Player) t2.getOwner()).currentStatistic.addAttacked();

				result = true;

				attacker = t1;
				defender = t2;
				battleRounds = 0;
				gameState = STATE_ROLLING;
				// System.out.print("Attacking "+t2.getName()+" ("+t2.getArmies()+") with "+t1.getName()+" ("+t1.getArmies()+").\n");
				// // testing
			}
		}
		return result;
	}

	/**
	 * Ends the attacking phase
	 * 
	 * @return boolean Returns true if the player ended the attacking phase,
	 *         returns false if the player cannot end the attacking phase
	 */
	public boolean endAttack() {

		if (gameState == STATE_ATTACKING) { // if we were in the attack phase

			// YURA:TODO check if there are any countries with more then 1 amy,
			// maybe even check that a move can be made

			gameState = STATE_FORTIFYING; // go to move phase
			// System.out.print("Attack phase ended\n");
			return true;
		}
		return false;
	}

	/**
	 * Rolls the attackersdice
	 * 
	 * @param dice1
	 *            Number of dice to be used by the attacker
	 * @return boolean Return if the roll was successful
	 */
	public boolean rollA(int dice1) {

		if (gameState == STATE_ROLLING) { // if we were in the attacking phase

			if (attacker.getArmies() > 4) {
				if (dice1 <= 0 || dice1 > 3)
					return false;
			} else {
				if (dice1 <= 0 || dice1 > (attacker.getArmies() - 1))
					return false;
			}

			attackerDice = dice1; // 5 2 0

			// System.out.print("NUMBER OF DICE: " + dice1 + " or " +
			// attackerDice.length + "\n");

			currentPlayer = defender.getOwner();
			gameState = STATE_DEFEND_YOURSELF;
			return true;

		} else
			return false;

	}

	public boolean rollD(int dice2) {

		if (gameState == STATE_DEFEND_YOURSELF) { // if we were in the defending
													// phase

			if (defender.getArmies() > maxDefendDice) {
				if (dice2 <= 0 || dice2 > maxDefendDice)
					return false;
			} else {
				if (dice2 <= 0 || dice2 > (defender.getArmies()))
					return false;
			}

			currentPlayer = attacker.getOwner();

			defenderDice = dice2; // 4 3

			return true;

		} else
			return false;

	}

	public int getAttackerDice() {
		return attackerDice;
	}

	public int getDefenderDice() {
		return defenderDice;
	}

	/**
	 * Get the number of rolls that have taken place in the current attack
	 * 
	 * @return
	 */
	public int getBattleRounds() {
		return battleRounds;
	}

	/**
	 * Rolls the defenders dice
	 * 
	 * @param attackerResults
	 *            The results for the attacker
	 * @param defenderResults
	 *            The results for the defender
	 * @return int[] Returns an array which will determine the results of the
	 *         attack
	 */
	public int[] battle(int[] attackerResults, int[] defenderResults) {

		int[] result = new int[6];
		result[0] = 0; // worked or not
		result[1] = 0; // no of armies attacker lost
		result[2] = 0; // no of armies defender lost
		result[3] = 0; // did you win
		result[4] = 0; // min move
		result[5] = 0; // max move

		if (gameState == STATE_DEFEND_YOURSELF) { // if we were in the defending
													// phase
			battleRounds++;

			for (int aResult : attackerResults) {
				attacker.getOwner().currentStatistic.addDice(aResult);
			}
			for (int aResult : defenderResults) {
				defender.getOwner().currentStatistic.addDice(aResult);
			}

			// battle away!
			for (int c = 0; c < Math.min(attackerResults.length,
					defenderResults.length); c++) {

				if (attackerResults[c] > defenderResults[c]) {
					defender.looseArmy();
					defender.getOwner().currentStatistic.addCasualty();
					attacker.getOwner().currentStatistic.addKill();
					result[2]++;
				} else {
					attacker.looseArmy();
					attacker.getOwner().currentStatistic.addCasualty();
					defender.getOwner().currentStatistic.addKill();
					result[1]++;
				}

			}

			// if all the armies have been defeated
			if (defender.getArmies() == 0) {

				((Player) attacker.getOwner()).currentStatistic
						.addCountriesWon();
				((Player) defender.getOwner()).currentStatistic
						.addCountriesLost();

				result[5] = attacker.getArmies() - 1;

				capturedCountry = true;

				Player lostPlayer = (Player) defender.getOwner();

				lostPlayer.lostCountry(defender);
				currentPlayer.newCountry(defender);

				defender.setOwner((Player) attacker.getOwner());
				result[3] = 1;
				gameState = STATE_BATTLE_WON;
				mustmove = attackerResults.length;

				result[4] = mustmove;

				// if the player has been eliminated
				if (lostPlayer.getNoTerritoriesOwned() == 0) {

					result[3] = 2;

					currentPlayer.addPlayersEliminated(lostPlayer);

					while (lostPlayer.getCards().size() > 0) {

						// System.out.print("Hes got a card .. i must take it!\n");
						currentPlayer.giveCard(lostPlayer.takeCard());

					}

					// in italian rules there is no limit to the number of cards
					// you can hold
					// if winning the other players cards gives you 6 or more
					// cards you must immediately trade
					if (cardMode != CARD_ITALIANLIKE_SET
							&& currentPlayer.getCards().size() > MAX_CARDS) {
						// gameState=STATE_BATTLE_WON;
						tradeCap = true;
					}

				}

			} else if (attacker.getArmies() == 1) {
				gameState = STATE_ATTACKING;
				// System.out.print("Retreating (FORCED)\n");
				currentPlayer.currentStatistic.addRetreat();
			} else {
				gameState = STATE_ROLLING;
			}

			defenderDice = 0;
			attackerDice = 0;
			result[0] = 1;

		}

		return result;
	}

	/**
	 * Moves a number of armies from the attacking country to defending country
	 * 
	 * @param noa
	 *            Number of armies to be moved
	 * @return 1 or 2 if you can move the number of armies across (2 if you won
	 *         the game), returns 0 if you cannot
	 */
	public int moveArmies(int noa) {

		if (gameState == STATE_BATTLE_WON && mustmove > 0 && noa >= mustmove
				&& noa < attacker.getArmies()) {

			attacker.removeArmies(noa);
			defender.addArmies(noa);

			gameState = tradeCap ? STATE_TRADE_CARDS : STATE_ATTACKING;

			attacker = null;
			defender = null;
			mustmove = 0;

			return checkPlayerWon() ? 2 : 1;
		}
		return 0;
	}

	/**
	 * Moves all of armies from the attacking country to defending country
	 * 
	 * @return int Return trues if you can move the number of armies across,
	 *         returns false if you cannot
	 */
	public int moveAll() {

		if (gameState == STATE_BATTLE_WON && mustmove > 0) {

			return attacker.getArmies() - 1;

		}
		return -1;

	}

	public int getMustMove() {
		return mustmove;
	}

	/**
	 * Retreats from attacking a country
	 * 
	 * @return boolean Returns true if you can retreat, returns false if you
	 *         cannot
	 */
	public boolean retreat() {

		if (gameState == STATE_ROLLING) { // if we were in the attacking phase

			currentPlayer.currentStatistic.addRetreat();

			gameState = STATE_ATTACKING; // go to attack phase
			// System.out.print("Retreating\n");

			attacker = null;
			defender = null;

			return true;
		}
		return false;
	}

	/**
	 * Moves armies from one country to an adjacent country and goes to the end
	 * phase
	 * 
	 * @param t1
	 *            Country where the armies are moving from
	 * @param t2
	 *            Country where the armies are moving to
	 * @param noa
	 *            Number of Armies to move
	 * @return boolean Returns true if the tactical move is allowed, returns
	 *         false if the tactical move is not allowed
	 */
	public boolean moveArmy(Country t1, Country t2, int noa) {
		if (gameState == STATE_FORTIFYING) {

			// do they exist //check if they belong to the player //check if
			// they are neighbours //check if there are enough troops in
			// country1
			if (t1 != null && t2 != null && t1.getOwner() == currentPlayer
					&& t2.getOwner() == currentPlayer && t1.isNeighbours(t2) &&
					// t2.isNeighbours(t1) && // not needed as there is code to
					// check this
					t1.getArmies() > noa && noa > 0) {

				t1.removeArmies(noa);
				t2.addArmies(noa);
				gameState = STATE_END_TURN;

				checkPlayerWon();

				// System.out.println("Armies Moved. "+gameState); // testing
				return true;

			}
		}
		return false;

	}

	/**
	 * Choosing not to use the tactical move and moves to the end phase
	 * 
	 * @return boolean Returns true if you are in the right phase to use the
	 *         tactical move and returns false otherwise
	 */
	public boolean noMove() {

		if (gameState == STATE_FORTIFYING) { // if we were in the move phase
			gameState = STATE_END_TURN; // go to end phase

			// System.out.print("No Move.\n"); // testing
			return true;
		} else
			return false;

	}

	public void workOutEndGoStats(Player p) {

		int countries = p.getNoTerritoriesOwned();
		int armies = p.getNoArmies();
		int continents = getNoContinentsOwned(p);
		int conectedEmpire = RiskUtil.getConnectedEmpire(p).size();
		int cards = p.getCards().size();

		p.currentStatistic.endGoStatistics(countries, armies, continents,
				conectedEmpire, cards);

	}

	/**
	 * For backwards compatibility (Android).
	 * 
	 * @param p
	 * @return
	 * @deprecated Use {@link RiskUtil#getConnectedEmpire(Player)} instead.
	 */
	@Deprecated
	public List getConnectedEmpire(Player p) {
		return RiskUtil.getConnectedEmpire(p);
	}

	/**
	 * For backwards compatibility (Android).
	 * 
	 * @param t
	 * @param a
	 * @param n
	 * @param p
	 * @deprecated Use
	 *             {@link RiskUtil#getConnectedEmpire(List, List, List, Player)}
	 *             instead.
	 */
	@Deprecated
	public void getConnectedEmpire(List t, List a, List n, Player p) {
		RiskUtil.getConnectedEmpire(t, a, n, p);
	}

	/**
	 * Sets the capital for a player - ONLY FOR CAPITAL RISK
	 * 
	 * @param c
	 *            The capital country
	 * @return boolean Returns true if the country is set as the capital,
	 *         returns false otherwise
	 */
	public boolean setCapital(Country c) {

		if (gameState == STATE_SELECT_CAPITAL && gameMode == 2
				&& c.getOwner() == currentPlayer
				&& currentPlayer.getCapital() == null) {

			currentPlayer.setCapital(c);

			for (int b = 0; b < cards.size(); b++) {

				if (c == ((Card) cards.elementAt(b)).getCountry()) {
					cards.removeElementAt(b);
					// System.out.print("card removed because it is a capital\n");
				}

			}

			gameState = STATE_END_TURN;

			return true;

		}
		return false;

	}

	/**
	 * Check if a player has won the game
	 * 
	 * @return boolean Returns true if the player has won the game, returns
	 *         false otherwise
	 */
	public boolean checkPlayerWon() {

		boolean result = false;

		// check if the player has won
		int won = 0;
		for (int c = 0; c < continents.length; c++) {

			if (continents[c].isOwned(currentPlayer)) {
				won++;
			}

		}
		if (won == continents.length) {

			result = true;
			// System.out.print("The Game Is Over, "+currentPlayer.getName()+" has won!\n");

		}

		// check if the player has won 2 player risk

		/*
		 * @todo: maybe add this back, as crap player can never win
		 * 
		 * else if (getSetupDone() && gameMode==1) {
		 * 
		 * Player target=null;
		 * 
		 * for (int c=0; c< Players.size() ; c++) {
		 * 
		 * 
		 * // ((Player)Players.elementAt(c)).getType() !=3 &&
		 * 
		 * if ( (Player)Players.elementAt(c) != currentPlayer ) { target =
		 * (Player)Players.elementAt(c); }
		 * 
		 * }
		 * 
		 * if ( target.getNoTerritoriesOwned()==0 ) {
		 * 
		 * result=true;
		 * 
		 * }
		 * 
		 * }
		 */

		// check if the player has won capital risk!
		else if (getSetupDone() && gameMode == MODE_CAPITAL
				&& currentPlayer.getCapital() != null) {

			int capitalcount = 0;

			if (currentPlayer == ((Country) currentPlayer.getCapital())
					.getOwner()) {

				for (int c = 0; c < playerManager.getNoPlayers(); c++) {

					if (((Vector) currentPlayer.getTerritoriesOwned())
							.contains((Country) playerManager.getPlayer(c)
									.getCapital())) {
						capitalcount++;
					}

				}

			}

			if (capitalcount == playerManager.getNoPlayers()) {
				result = true;
			}

		}
		// check if the player has won mission risk!
		else if (getSetupDone() && gameMode == MODE_SECRET_MISSION) {

			Mission m = currentPlayer.getMission();

			if (m.getPlayer() != null && // check is this is indeed a Elim
											// Player card
					m.getPlayer() != currentPlayer && // check if its not the
														// current player u need
														// to eliminate
					((Player) m.getPlayer()).getNoTerritoriesOwned() == 0 && // chack
																				// if
																				// that
																				// player
																				// has
																				// been
																				// eliminated
					((Vector) currentPlayer.getPlayersEliminated()).contains(m
							.getPlayer()) // check if it was you who eliminated
											// them
			) {

				// yay you have won
				result = true;

			} else if (m.getNoofcountries() != 0
					&& m.getNoofarmies() != 0
					&& // check if this card has a value for capture teretories
					(m.getPlayer() == null
							|| ((Player) m.getPlayer()).getNoTerritoriesOwned() == 0 || (Player) m
							.getPlayer() == currentPlayer)
					&& m.getNoofcountries() <= currentPlayer
							.getNoTerritoriesOwned() // do you have that number
														// of countries captured
			) {

				int n = 0;

				for (int c = 0; c < currentPlayer.getNoTerritoriesOwned(); c++) {
					if (((Country) ((Vector) currentPlayer
							.getTerritoriesOwned()).elementAt(c)).getArmies() >= m
							.getNoofarmies())
						n++;

				}
				if (n >= m.getNoofcountries()) {

					// yay you have won
					result = true;

				}

			} else if ((m.getContinent1() != null)
					&& // this means its a continent mission

					checkPlayerOwnesContinentForMission(m.getContinent1(), 1)
					&& checkPlayerOwnesContinentForMission(m.getContinent2(), 2)
					&& checkPlayerOwnesContinentForMission(m.getContinent3(), 3)

			) {

				// yay you have won
				result = true;

			}

		}

		if (result == true) {
			gameState = STATE_GAME_OVER;
		}

		return result;

	}

	private boolean checkPlayerOwnesContinentForMission(Continent c, int n) {

		if (ANY_CONTINENT.equals(c)) {

			return (getNoContinentsOwned(currentPlayer) >= n);

		} else if (c != null) {

			return c.isOwned(currentPlayer);

		} else {

			return true;

		}

	}

	public boolean canContinue() {

		if (gameState == STATE_GAME_OVER && gameMode != MODE_DOMINATION
				&& gameMode != 1) {

			int oldGameMode = gameMode;
			gameMode = MODE_DOMINATION;
			boolean playerWon = checkPlayerWon();
			gameMode = oldGameMode;

			return !playerWon; // we CAN continue if someone has NOT won

		}
		return false;

	}

	public boolean continuePlay() {

		if (canContinue()) {

			gameMode = MODE_DOMINATION;

			if (tradeCap == true) {
				gameState = STATE_TRADE_CARDS;
			} else if (currentPlayer.getExtraArmies() == 0) {
				gameState = STATE_ATTACKING;
			} else {
				gameState = STATE_PLACE_ARMIES;
			}

			return true;

		}
		return false;
	}

	public int getClosestCountry(int x, int y) {
		Country closestCountryCanvas = null;
		int closestDistance = Integer.MAX_VALUE;

		for (int index = 0; index < countries.length; index++) {
			int distance = countries[index].getDistanceTo(x, y);
			if (distance < closestDistance) {
				// we have a country closer to the point (x,y)
				closestCountryCanvas = countries[index];
				closestDistance = distance;
			}
		}
		return closestCountryCanvas.getColor();
	}

	/**
	 * Loads the map
	 * 
	 * @param filename
	 *            The map filename
	 * @throws Exception
	 *             There was a error
	 */
	public void loadMap() throws Exception {
		loadMap(true, null);
	}

	public void loadMap(boolean cleanLoad, BufferedReader bufferin)
			throws Exception {
		Parser.MapResult result = parser.parseMap(mapfile, bufferin, cleanLoad, countries, continents);

		imagePic = result.getImagePicture();
		imageMap = result.getImageMap();
		countries = (Country[]) result.getCountries().toArray(
				new Country[result.getCountries().size()]);
		continents = (Continent[]) result.getContinents().toArray(
				new Continent[result.getContinents().size()]);
	}

	/**
	 * Sets the filename of the map file
	 * 
	 * @param f
	 *            The name of the new file
	 * @return boolean Return trues if missions are supported
	 * @throws Exception
	 *             The file cannot be found
	 */
	public boolean setMapfile(String f) throws Exception {
		Parser.MapFileResult result = parser.parseMapFile(f, defaultMap,
				defaultCards);
		mapfile = result.getMapFile();
		imagePic = result.getImagePicture();
		previewPic = result.getPreviewPicture();
		return result.isMissions();
	}

	/**
	 * we need to call this if we do not want to reload data from disk when we
	 * start a game
	 */
	public void setMemoryLoad() {

		mapfile = null;
		cardsfile = null;

		imagePic = null;
		imageMap = null;
	}

	public void setupNewMap() {

		countries = new Country[0];
		continents = new Continent[0];

		cards = new Vector();
		usedCards = new Vector();
		missions = new Vector();

		propertyManager.clear();

		runmaptest = false;
		previewPic = null;

		setMemoryLoad();

	}

	public void setCountries(Country[] a) {
		countries = a;
	}

	public void setContinents(Continent[] a) {
		continents = a;
	}

	/**
	 * Loads the cards
	 * 
	 * @param filename
	 *            The cards filename
	 * @throws Exception
	 *             There was a error
	 */
	public void loadCards(boolean rawLoad) throws Exception {

		StringTokenizer st = null;

		cards = new Vector();
		usedCards = new Vector();
		missions = new Vector();

		// System.out.print("Starting load cards and missions...\n");

		BufferedReader bufferin = RiskUtil.readMap(RiskUtil
				.openMapStream(cardsfile));

		String input = bufferin.readLine();
		String mode = "none";

		while (input != null) {

			if (input.equals("") || input.charAt(0) == ';') {
				// do nothing
				// System.out.print("Nothing\n"); // testing
			} else {

				// System.out.print("Something found\n"); // testing

				if (input.charAt(0) == '['
						&& input.charAt(input.length() - 1) == ']') {
					// System.out.print("Something beggining with [ and ending with ] found\n");
					// // testing
					mode = "newsection";
				} else {
					st = new StringTokenizer(input);
				}

				if (mode.equals("cards")) {
					// System.out.print("Adding cards\n"); // testing

					String name = st.nextToken(); // System.out.print(name+"\n");
													// // testing

					if (name.equals(Card.WILDCARD)) {
						Card card = new Card(name, null);
						cards.add(card);
					} else if (name.equals(Card.CAVALRY)
							|| name.equals(Card.INFANTRY)
							|| name.equals(Card.CANNON)) {
						int country = Integer.parseInt(st.nextToken());

						// System.out.print( Countries[ country - 1 ].getName()
						// +"\n"); // testing
						Card card = new Card(name, countries[country - 1]);
						cards.add(card);
					} else {
						throw new Exception(
								"unknown item found in cards file: " + name);
					}

					if (st.hasMoreTokens()) {
						throw new Exception(
								"unknown item found in cards file: "
										+ st.nextToken());
					}

				} else if (mode.equals("missions")) {
					// System.out.print("Adding Mission\n"); // testing

					// boolean add=true;

					int s1 = Integer.parseInt(st.nextToken());
					Player p;

					if (s1 == 0 || s1 > playerManager.getNoPlayers()) {
						p = null;
					} else {
						p = playerManager.getPlayer(s1 - 1);
					}

					int noc = Integer.parseInt(st.nextToken());
					int noa = Integer.parseInt(st.nextToken());

					String s4 = st.nextToken();
					String s5 = st.nextToken();
					String s6 = st.nextToken();

					Continent c1 = getMissionContinentfromString(s4);
					Continent c2 = getMissionContinentfromString(s5);
					Continent c3 = getMissionContinentfromString(s6);

					String missioncode = s1 + "-" + noc + "-" + noa + "-" + s4
							+ "-" + s5 + "-" + s6;
					String description = rawLoad ? null : MapTranslator
							.getTranslatedMissionName(missioncode);

					if (description == null) {
						description = "";
						while (st.hasMoreElements()) {
							description = description
									+ ("".equals(description) ? "" : " ")
									+ st.nextToken();
						}
					}

					if (p != null && !rawLoad) {

						String name = p.getName();

						String color = "color."
								+ ColorUtil.getStringForColor(p.getColor());
						java.util.ResourceBundle trans = TranslationBundle
								.getBundle();
						try { // in Java 1.4 no if (trans.containsKey(color))
							name = trans.getString(color) + " " + name;
						} catch (Exception ex) {
						}

						String oldkey = "PLAYER" + s1;
						String newkey = "{" + oldkey + "}";
						if (description.indexOf(newkey) >= 0) {
							// DefaultCards_XX.properties uses this format
							description = RiskUtil.replaceAll(description,
									newkey, name);
						} else if (description.indexOf(oldkey) >= 0) {
							// many maps still have this format for missions
							description = RiskUtil.replaceAll(description,
									oldkey, name);
						} else {
							System.err.println("newkey: " + newkey
									+ " and oldkey: " + oldkey
									+ " not found in mission: " + description);
						}

					}

					if (rawLoad || s1 <= playerManager.getNoPlayers()) {

						// System.out.print(description+"\n"); // testing
						Mission mission = new Mission(p, noc, noa, c1, c2, c3,
								description);
						missions.add(mission);
					} else {
						// System.out.print("NOT adding this mission as it refures to an unused player\n");
						// // testing
					}

				} else if (mode.equals("newsection")) {

					mode = input.substring(1, input.length() - 1); // set mode
																	// to the
																	// name of
																	// the
																	// section

					if (mode.equals("cards")) {
						// System.out.print("Section: cards found\n"); //
						// testing
					} else if (mode.equals("missions")) {
						// System.out.print("Section: missions found\n"); //
						// testing
					} else {
						throw new Exception(
								"unknown section found in cards file: " + mode);
					}

				} else {

					throw new Exception("unknown item found in cards file: "
							+ input);

				}

			}

			input = bufferin.readLine(); // get next line

		}
		bufferin.close();

		// System.out.print("Cards and missions loaded.\n");

	}

	private Continent getMissionContinentfromString(String a) {

		if (a.equals("*")) {
			return ANY_CONTINENT;
		} else {
			int s = Integer.parseInt(a);
			if (s == 0) {
				return null;
			} else {
				return continents[s - 1];
			}

		}
	}

	/**
	 * Sets the filename of the cards file
	 * 
	 * @param f
	 *            The name of the new file
	 * @return boolean Return trues if missions are supported
	 * @throws Exception
	 *             The file cannot be found
	 */
	public boolean setCardsfile(String f) throws Exception {
		Parser.CardsResult result = parser.parseCards(f, defaultCards);
		cardsfile = result.getCardsFile();
		return result.isMissions();
	}

	/**
	 * Shuffles the countries
	 */
	public List shuffleCountries() {

		// we create a COPY of the Countries array, so that we do not mess up
		// the real one
		List oldCountries = new Vector(Arrays.asList(countries));

		// Vector newCountries = new Vector();
		// while(oldCountries.size() > 0) {
		// int a = r.nextInt(oldCountries.size()) ;
		// newCountries.add ( oldCountries.remove(a) );
		// }
		// return newCountries;

		Collections.shuffle(oldCountries);
		return oldCountries;

	}

	/**
	 * Creates a new game
	 * 
	 * @return RiskGame Returns the new game created
	 * 
	 *         public static RiskGame newGame() { RiskGame game = new
	 *         RiskGame();
	 *         //System.out.print("Game State: "+game.getState()+"\n"); //
	 *         testing return game; }
	 */

	/**
	 * Loads a saved game
	 * 
	 * @param file
	 *            The saved game's filename
	 * @return Riskgame Return the saved game object if it loads, returns null
	 *         if it doe not load
	 */
	public static RiskGame loadGame(String file) throws Exception {
		RiskGame game = null;
		// try {
		InputStream filein = RiskUtil.getLoadFileInputStream(file);
		ObjectInputStream objectin = new ObjectInputStream(filein);
		game = (RiskGame) objectin.readObject();
		objectin.close();

		// XMLDecoder d = new XMLDecoder( new BufferedInputStream( new
		// FileInputStream(file)));
		// game = (RiskGame)d.readObject();
		// d.close();

		// }
		// catch (Exception e) {
		// System.out.println(e.getMessage());
		// }
		return game;
	}

	/**
	 * Closes the current game
	 * 
	 * @return Riskgame Returns the game, which is already set to null / public
	 *         static RiskGame closeGame() { RiskGame game = null; return game;
	 *         }
	 */

	/**
	 * Saves the current game to a file
	 * 
	 * @param file
	 *            The filename of the save
	 * @return boolean Return trues if you saved, returns false if you cannot
	 */
	public void saveGame(OutputStream file) throws Exception { // added RiskGame
																// parameter g,
																// so remember
																// to change in
																// parser

		ObjectOutputStream out = new RiskObjectOutputStream(file);
		out.writeObject(this);
		// out.flush(); not needed if we do a close
		out.close();

		// XMLEncoder e = new XMLEncoder( new BufferedOutputStream( new
		// FileOutputStream(file)));
		// e.writeObject(this);
		// e.close();
	}

	/**
	 * Gets the state of the game
	 * 
	 * @return int Returns the game state
	 */
	public int getState() {
		return gameState;
	}

	/**
	 * Checks if there are any empty countries
	 * 
	 * @return boolean Return trues if no empty countries, returns false
	 *         otherwise
	 */
	public boolean NoEmptyCountries() {

		// find out if there are any empty countries

		Country empty = null;

		for (int c = 0; c < countries.length; c++) {

			if (countries[c].getOwner() == null) {
				empty = countries[c];
				c = countries.length;
			}

		}
		if (empty != null) {
			return false;
		} else {
			return true;
		}

	}

	/**
	 * Checks if the set up is completely
	 * 
	 * @return boolean Return trues if the set up is complete, returns false
	 *         otherwise
	 */
	public boolean getSetupDone() {
		return setup == playerManager.getNoPlayers();
	}

	/**
	 * get the value od the trade-cap
	 * 
	 * @return boolean Return trues if tradecap is true and false otherwise
	 */
	public boolean getTradeCap() {
		return tradeCap;
	}

	/**
	 * Gets the game mode
	 * 
	 * @return int Return the game mode
	 */
	public int getGameMode() {
		return gameMode;
	}

	/**
	 * Gets the current player
	 * 
	 * @return player Return the current player
	 */
	public Player getCurrentPlayer() {
		return currentPlayer;
	}

	/**
	 * Gets all the players
	 * 
	 * @return Vector Return all the players
	 */
	public List<Player> getPlayersStats() {

		for (int c = 0; c < playerManager.getNoPlayers(); c++) {
			workOutEndGoStats((Player) playerManager.getPlayer(c));
		}

		return playerManager.getPlayers();
	}

	/**
	 * Gets the attacking country
	 * 
	 * @return Country the attacking country
	 */
	public Country getAttacker() {
		return attacker;
	}

	/**
	 * Gets the defending country
	 * 
	 * @return Country the defending country
	 */
	public Country getDefender() {
		return defender;
	}

	/**
	 * Gets the ImagePic
	 * 
	 * @return URL ImagePic
	 */
	public String getImagePic() {
		return imagePic;
	}

	public String getPreviewPic() {
		return previewPic;
	}

	public void setPreviewPic(String prv) {
		previewPic = prv;
	}

	public PropertyManager getPropertyManager() {
		return propertyManager;
	}

	/**
	 * Gets the ImageMap
	 * 
	 * @return URL ImageMap
	 */
	public String getImageMap() {
		return imageMap;
	}

	public String getCardsFile() {
		return cardsfile; // .getFile().substring(
							// cardsfile.getFile().lastIndexOf("/")+1 );
	}

	public String getMapFile() {
		return mapfile; // .getFile().substring(
						// mapfile.getFile().lastIndexOf("/")+1 );
	}

	public Vector getCards() {
		return cards;
	}

	public Vector getUsedCards() {
		return usedCards;
	}

	/**
	 * Gets the number of continents which are owned by a player
	 * 
	 * @param p
	 *            The player you want to find continents for
	 * @return int Return the number of continents a player owns
	 */
	public int getNoContinentsOwned(Player p) {

		int total = 0;

		for (int c = 0; c < continents.length; c++) {

			if (continents[c].isOwned(p)) {
				total++;
			}

		}
		return total;
	}

	/**
	 * Gets a country
	 * 
	 * @param name
	 *            The name of the country
	 * @return Country Return the country you are looking for, if it exists.
	 *         Otherwise returns null
	 *
	 *         // * @deprecated
	 * 
	 *         public Country getCountry(String name) {
	 * 
	 *         for (int c=0; c< Countries.length ; c++) {
	 * 
	 *         if ( name.equals(Countries.[c].getName()) ) { return
	 *         Countries[c]; }
	 * 
	 *         } System.out.println( "ERROR: Country not found: " + name );
	 *         return null;
	 * 
	 *         }
	 */

	/**
	 * Tries to find a country by its name. This function should only be used if
	 * a user has entered the name manually!
	 *
	 * @param name
	 *            The name of the country
	 * @return Country Return the country you are looking for, if it exists.
	 *         Otherwise returns null
	 * 
	 *         public Country getCountryByName(String name) {
	 * 
	 *         for (int c=0; c< Countries.length ; c++) {
	 * 
	 *         if ( name.equals(Countries[c].getName()) ) { return Countries[c];
	 *         }
	 * 
	 *         } System.out.println( "ERROR: Country not found: " + name );
	 *         return null;
	 * 
	 *         }//public Country getCountryByName(String name)
	 */

	/**
	 * returns the country with the given color (ID)
	 */
	public Country getCountryInt(int color) {

		if (color <= 0 || color > countries.length) {
			return null;
		} else
			return countries[color - 1];

	}

	/**
	 * returns the country with the given color (ID) the string is converted to
	 * an int value
	 * 
	 * public Country getCountryInt(String strId) { int nId = -1; try { nId =
	 * Integer.parseInt( strId); } catch( NumberFormatException e) {
	 * System.out.println( "ERROR: Can't convert number \"" + strId +
	 * "\" to a number." ); return null; }
	 * 
	 * return getCountryInt(nId); }//public Country getCountryInt(String nId)
	 */

	/**
	 * Gets a cards
	 * 
	 * @param name
	 * @return Card Return the card you are looking for, if it exists. Otherwise
	 *         returns null
	 */
	public Card[] getCards(String name1, String name2, String name3) {

		Card[] c = new Card[3];

		Vector playersCards = new Vector(currentPlayer.getCards());

		jumppoint: for (int a = 0; a < 3; a++) {

			String name;

			if (a == 0) {
				name = name1;
			} else if (a == 1) {
				name = name2;
			} else {
				name = name3;
			} // if (a==2)

			for (int b = 0; b < playersCards.size(); b++) {

				if (name.equals(Card.WILDCARD)
						&& name.equals(((Card) playersCards.elementAt(b))
								.getName())) {
					c[a] = (Card) playersCards.remove(b);
					continue jumppoint;
				} else if ((Country) ((Card) playersCards.elementAt(b))
						.getCountry() != null
						&& name.equals(((Country) ((Card) playersCards
								.elementAt(b)).getCountry()).getColor() + "")) {
					c[a] = (Card) playersCards.remove(b);
					continue jumppoint;
				}

			}

		}

		return c;

	}

	public Card findCardAndRemoveIt(String name) {

		int cardIndex = -1;

		for (int c = 0; c < cards.size(); c++) {
			Card theCard = ((Card) cards.elementAt(c));

			// if we are looking for a wildcard, and this card is also a
			// wildcard
			if (name.equals(Card.WILDCARD) && name.equals(theCard.getName())) {
				cardIndex = c;
				break;
			}
			// if we are not looking for a wildcard and the card matches the
			// country
			else if (theCard.getCountry() != null
					&& name.equals(String.valueOf(theCard.getCountry()
							.getColor()))) {
				cardIndex = c;
				break;
			}

		}

		// find the card and remove it
		Card theCard = (Card) cards.remove(cardIndex);
		cards.trimToSize(); // not sure if this is needed

		recycleUsedCards();

		return theCard;

	}

	/**
	 * This method should be called after:
	 * <ul>
	 * <li>a card was removed from normal cards
	 * <li>a card was added to the used cards
	 * </ul>
	 */
	private boolean recycleUsedCards() {
		// if we have removed the last card, and we want to reuse our cards,
		// then we add all the used ones into the current cards vector
		Vector used = getUsedCards();
		if (cards.isEmpty() && recycleCards && !used.isEmpty()) {
			cards.addAll(used);
			used.clear();
			return true;
		}
		return false;
	}

	/**
	 * Gets the countries in the game
	 * 
	 * @return Vector Return the Countries in the current game
	 */
	public Country[] getCountries() {

		return countries;
	}

	/**
	 * Gets the continents in the game
	 * 
	 * @return Vector Return the Continents in the current game
	 */
	public Continent[] getContinents() {
		return continents;
	}

	/**
	 * Gets the number of countries in the game
	 * 
	 * @return int Return the number of countries in the current game
	 */
	public int getNoCountries() {
		return countries.length;
	}

	public int getNoContinents() {

		return continents.length;

	}

	/**
	 * Gets the allocated Missions in the game
	 * 
	 * @return Vector Return the Missions in the current game
	 */
	public Vector getMissions() {
		return missions;
	}

	/**
	 * Gets the number of Missions in the game
	 * 
	 * @return int Return the number of Missions in the game
	 */
	public int getNoMissions() {
		return missions.size();
	}

	public int getNoCards() {
		return cards.size();
	}

	public boolean isRecycleCards() {
		return recycleCards;
	}

	/**
	 * Set the Default Map and Cards File
	 */
	public static void setDefaultMapAndCards(String a, String b) {

		defaultMap = a;
		defaultCards = b;

		// not needed as is reset each time a new RiskGame object is created
		// net.yura.domination.engine.translation.MapTranslator.setMap( a );
		// net.yura.domination.engine.translation.MapTranslator.setCards( b );

	}

	public static String getDefaultMap() {
		return defaultMap;
	}

	public static String getDefaultCards() {
		return defaultCards;
	}

	/**
	 * @return the current Card Mode
	 */
	public int getCardMode() {
		return cardMode;
	}

	public int getNoAttackDice() {
		if (attacker.getArmies() > 4) {
			return 3;
		} else {
			return attacker.getArmies() - 1;
		}
	}

	public int getNoDefendDice() {
		if (defender.getArmies() > maxDefendDice) {
			return maxDefendDice;
		} else {
			return defender.getArmies();
		}
	}
}
