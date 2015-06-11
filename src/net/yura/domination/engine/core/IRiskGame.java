package net.yura.domination.engine.core;

import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Vector;

public interface IRiskGame extends Serializable {

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

	PlayerManager getPlayerManager();

	void addCommand(String a);

	Vector getCommands();

	void setCommands(Vector replayCommands);

	int getMaxDefendDice();

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
	boolean addPlayer(int type, String name, int color, String a);

	/**
	 * This deletes a player in the game
	 * 
	 * @param name
	 *            Name of the player
	 * @return boolean Returns true if the player is deleted, returns false if
	 *         the player cannot be deleted
	 */
	boolean delPlayer(String name);

	/**
	 * Starts the game Risk
	 * 
	 * @param mode
	 *            This represents the mode of the game: normal, 2 player,
	 *            capital or mission
	 */
	void startGame(int mode, int card, boolean recycle, boolean threeDice)
			throws Exception;

	/**
	 * Sets the current player in the game
	 * 
	 * @param name
	 *            The name of the current player
	 * @return Player Returns the current player in the game
	 */
	Player setCurrentPlayer(int c);

	/**
	 * Gets the current player in the game
	 * 
	 * @return String Returns the name of a randomly picked player from the set
	 *         of players
	 */
	int getRandomPlayer();

	/**
	 * Checks whether the player deserves a card during at the end of their go
	 * 
	 * @return String Returns the name of the card if deserves a card, else else
	 *         returns empty speech-marks
	 */
	String getDesrvedCard();

	boolean isCapturedCountry();

	/**
	 * Ends a player's go
	 * 
	 * @return Player Returns the next player
	 */
	Player endGo();

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
	int trade(Card card1, Card card2, Card card3);

	int getTradeAbsValue(String c1, String c2, String c3, int cardMode);

	boolean canTrade();

	/**
	 * Find the best (highest) trade Simple greedy search using the various
	 * valid combinations
	 * 
	 * @param cards
	 * @return
	 */
	int getBestTrade(List<Card> cards, Card[] bestResult);

	int getNewCardState();

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
	boolean checkTrade(Card card1, Card card2, Card card3);

	/**
	 * Ends the trading phase by checking if the player has less than 5 cards
	 * 
	 * @return boolean Returns true if the player has ended the trade phase,
	 *         returns false if the player cannot end the trade phase
	 */
	boolean endTrade();

	boolean canEndTrade();

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
	int placeArmy(Country t, int n);

	/**
	 * Automatically places an army on an unoccupied country
	 * 
	 * @return int Returns the country id which an army was added to
	 */
	int getRandomCountry();

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
	boolean attack(Country t1, Country t2);

	/**
	 * Ends the attacking phase
	 * 
	 * @return boolean Returns true if the player ended the attacking phase,
	 *         returns false if the player cannot end the attacking phase
	 */
	boolean endAttack();

	/**
	 * Rolls the attackersdice
	 * 
	 * @param dice1
	 *            Number of dice to be used by the attacker
	 * @return boolean Return if the roll was successful
	 */
	boolean rollA(int dice1);

	boolean rollD(int dice2);

	int getAttackerDice();

	int getDefenderDice();

	/**
	 * Get the number of rolls that have taken place in the current attack
	 * 
	 * @return
	 */
	int getBattleRounds();

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
	int[] battle(int[] attackerResults, int[] defenderResults);

	/**
	 * Moves a number of armies from the attacking country to defending country
	 * 
	 * @param noa
	 *            Number of armies to be moved
	 * @return 1 or 2 if you can move the number of armies across (2 if you won
	 *         the game), returns 0 if you cannot
	 */
	int moveArmies(int noa);

	/**
	 * Moves all of armies from the attacking country to defending country
	 * 
	 * @return int Return trues if you can move the number of armies across,
	 *         returns false if you cannot
	 */
	int moveAll();

	int getMustMove();

	/**
	 * Retreats from attacking a country
	 * 
	 * @return boolean Returns true if you can retreat, returns false if you
	 *         cannot
	 */
	boolean retreat();

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
	boolean moveArmy(Country t1, Country t2, int noa);

	/**
	 * Choosing not to use the tactical move and moves to the end phase
	 * 
	 * @return boolean Returns true if you are in the right phase to use the
	 *         tactical move and returns false otherwise
	 */
	boolean noMove();

	void workOutEndGoStats(Player p);

	/**
	 * Sets the capital for a player - ONLY FOR CAPITAL RISK
	 * 
	 * @param c
	 *            The capital country
	 * @return boolean Returns true if the country is set as the capital,
	 *         returns false otherwise
	 */
	boolean setCapital(Country c);

	/**
	 * Check if a player has won the game
	 * 
	 * @return boolean Returns true if the player has won the game, returns
	 *         false otherwise
	 */
	boolean checkPlayerWon();

	boolean canContinue();

	boolean continuePlay();

	int getClosestCountry(int x, int y);

	/**
	 * Loads the map
	 * 
	 * @param filename
	 *            The map filename
	 * @throws Exception
	 *             There was a error
	 */
	void loadMap() throws Exception;

	void loadMap(boolean cleanLoad, BufferedReader bufferin) throws Exception;

	/**
	 * Sets the filename of the map file
	 * 
	 * @param f
	 *            The name of the new file
	 * @return boolean Return trues if missions are supported
	 * @throws Exception
	 *             The file cannot be found
	 */
	boolean setMapfile(String f) throws Exception;

	/**
	 * we need to call this if we do not want to reload data from disk when we
	 * start a game
	 */
	void setMemoryLoad();

	void setupNewMap();

	void setCountries(Country[] a);

	void setContinents(Continent[] a);

	/**
	 * Loads the cards
	 * 
	 * @param filename
	 *            The cards filename
	 * @throws Exception
	 *             There was a error
	 */
	void loadCards(boolean rawLoad) throws Exception;

	/**
	 * Sets the filename of the cards file
	 * 
	 * @param f
	 *            The name of the new file
	 * @return boolean Return trues if missions are supported
	 * @throws Exception
	 *             The file cannot be found
	 */
	boolean setCardsfile(String f) throws Exception;

	/**
	 * Shuffles the countries
	 */
	List shuffleCountries();

	/**
	 * Saves the current game to a file
	 * 
	 * @param file
	 *            The filename of the save
	 * @return boolean Return trues if you saved, returns false if you cannot
	 */
	void saveGame(OutputStream file) throws Exception;

	/**
	 * Gets the state of the game
	 * 
	 * @return int Returns the game state
	 */
	int getState();

	/**
	 * Checks if there are any empty countries
	 * 
	 * @return boolean Return trues if no empty countries, returns false
	 *         otherwise
	 */
	boolean NoEmptyCountries();

	/**
	 * Checks if the set up is completely
	 * 
	 * @return boolean Return trues if the set up is complete, returns false
	 *         otherwise
	 */
	boolean getSetupDone();

	/**
	 * get the value od the trade-cap
	 * 
	 * @return boolean Return trues if tradecap is true and false otherwise
	 */
	boolean getTradeCap();

	/**
	 * Gets the game mode
	 * 
	 * @return int Return the game mode
	 */
	int getGameMode();

	/**
	 * Gets the current player
	 * 
	 * @return player Return the current player
	 */
	Player getCurrentPlayer();

	/**
	 * Gets all the players
	 * 
	 * @return Vector Return all the players
	 */
	List<Player> getPlayersStats();

	/**
	 * Gets the attacking country
	 * 
	 * @return Country the attacking country
	 */
	Country getAttacker();

	/**
	 * Gets the defending country
	 * 
	 * @return Country the defending country
	 */
	Country getDefender();

	/**
	 * Gets the ImagePic
	 * 
	 * @return URL ImagePic
	 */
	String getImagePic();

	String getPreviewPic();

	void setPreviewPic(String prv);

	PropertyManager getPropertyManager();

	/**
	 * Gets the ImageMap
	 * 
	 * @return URL ImageMap
	 */
	String getImageMap();

	String getCardsFile();

	String getMapFile();

	Vector getCards();

	Vector getUsedCards();

	/**
	 * Gets the number of continents which are owned by a player
	 * 
	 * @param p
	 *            The player you want to find continents for
	 * @return int Return the number of continents a player owns
	 */
	int getNoContinentsOwned(Player p);

	/**
	 * returns the country with the given color (ID)
	 */
	Country getCountryInt(int color);

	/**
	 * Gets a cards
	 * 
	 * @param name
	 * @return Card Return the card you are looking for, if it exists. Otherwise
	 *         returns null
	 */
	Card[] getCards(String name1, String name2, String name3);

	Card findCardAndRemoveIt(String name);

	/**
	 * Gets the countries in the game
	 * 
	 * @return Vector Return the Countries in the current game
	 */
	Country[] getCountries();

	/**
	 * Gets the continents in the game
	 * 
	 * @return Vector Return the Continents in the current game
	 */
	Continent[] getContinents();

	/**
	 * Gets the number of countries in the game
	 * 
	 * @return int Return the number of countries in the current game
	 */
	int getNoCountries();

	int getNoContinents();

	/**
	 * Gets the allocated Missions in the game
	 * 
	 * @return Vector Return the Missions in the current game
	 */
	Vector getMissions();

	/**
	 * Gets the number of Missions in the game
	 * 
	 * @return int Return the number of Missions in the game
	 */
	int getNoMissions();

	int getNoCards();

	boolean isRecycleCards();

	/**
	 * @return the current Card Mode
	 */
	int getCardMode();

	int getNoAttackDice();

	int getNoDefendDice();

}