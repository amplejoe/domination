package net.yura.domination.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.PushbackInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import net.yura.domination.engine.core.Card;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.IRiskGame;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.engine.translation.MapTranslator;

public class RiskUtil {

	public static final Object SUCCESS = "SUCCESS";
	public static final Object ERROR = "ERROR";

	public static final String RISK_VERSION_URL;
	public static final String RISK_LOBBY_URL;
	// public static final String RISK_POST_URL; // look in Grasshopper.jar
	// now
	public static final String GAME_NAME;
	public static final String RISK_VERSION;
	// private static final String DEFAULT_MAP;

	private static final Logger logger = Logger.getLogger(RiskUtil.class
			.getName());
	public static RiskIO streamOpener;
	public static final Random RAND = new Random();

	static {

		Properties settings = new Properties();

		try {
			settings.load(RiskUtil.class
					.getResourceAsStream("settings.ini"));
		} catch (Exception ex) {
			throw new RuntimeException(
					"can not find settings.ini file!", ex);
		}

		RISK_VERSION_URL = settings.getProperty("VERSION_URL");
		RISK_LOBBY_URL = settings.getProperty("LOBBY_URL");
		// RISK_POST_URL = settings.getProperty("POST_URL");
		GAME_NAME = settings.getProperty("name");
		// DEFAULT_MAP = settings.getProperty("defaultmap");
		RISK_VERSION = settings.getProperty("version");

		String dmap = settings.getProperty("defaultmap");
		String dcards = settings.getProperty("defaultcards");

		RiskGame.setDefaultMapAndCards(dmap, dcards);

	}

	public static InputStream openMapStream(String a) throws IOException {
		return streamOpener.openMapStream(a);
	}

	public static InputStream openStream(String a) throws IOException {
		return streamOpener.openStream(a);
	}

	public static ResourceBundle getResourceBundle(Class c, String n,
			Locale l) {
		return streamOpener.getResourceBundle(c, n, l);
	}

	public static void openURL(URL url) throws Exception {
		streamOpener.openURL(url);
	}

	public static void openDocs(String docs) throws Exception {
		streamOpener.openDocs(docs);
	}

	public static void saveFile(String file, IRiskGame aThis)
			throws Exception {
		streamOpener.saveGameFile(file, aThis);
	}

	public static InputStream getLoadFileInputStream(String file)
			throws Exception {
		return streamOpener.loadGameFile(file);
	}

	/**
	 * option string looks like this:
	 * 
	 * 0 2 2 choosemap luca.map startgame domination increasing
	 */
	public static String createGameString(int easyAI, int averageAI,
			int hardAI, int gameMode, int cardsMode,
			boolean AutoPlaceAll, boolean recycle, String mapFile) {

		String players = averageAI + "\n" + easyAI + "\n" + hardAI
				+ "\n";

		String type = "";

		switch (gameMode) {
		case IRiskGame.MODE_DOMINATION:
			type = "domination";
			break;
		case IRiskGame.MODE_CAPITAL:
			type = "capital";
			break;
		case IRiskGame.MODE_SECRET_MISSION:
			type = "mission";
			break;
		}

		switch (cardsMode) {
		case IRiskGame.CARD_INCREASING_SET:
			type += " increasing";
			break;
		case IRiskGame.CARD_FIXED_SET:
			type += " fixed";
			break;
		case IRiskGame.CARD_ITALIANLIKE_SET:
			type += " italianlike";
			break;
		}

		if (AutoPlaceAll)
			type += " autoplaceall";
		if (recycle)
			type += " recycle";

		return players + "choosemap " + mapFile + "\nstartgame " + type;
	}

	public static String getMapNameFromLobbyStartGameOption(String options) {
		String[] lines = options.split(RiskUtil.quote("\n"));
		String choosemap = lines[3];
		return choosemap.substring("choosemap ".length()).intern();
	}

	public static String getGameDescriptionFromLobbyStartGameOption(
			String options) {
		String[] lines = options.split(RiskUtil.quote("\n"));
		int ai = 0;
		for (int c = 0; c < 3; c++) {
			ai = ai + Integer.parseInt(lines[c]);
		}
		return "AI:" + ai + " "
				+ lines[4].substring("startgame ".length());
	}

	public static void printStackTrace(Throwable ex) {
		logger.log(Level.WARNING, null, ex);
	}

	public static void donate() throws Exception {
		openURL(new URL(
				"http://domination.sourceforge.net/donate.shtml"));
	}

	public static void donatePayPal() throws Exception {
		openURL(new URL(
				"https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=yura%40yura%2enet&item_name="
						+ GAME_NAME
						+ "%20Donation&no_shipping=0&no_note=1&tax=0&currency_code=GBP&lc=GB&bn=PP%2dDonationsBF&charset=UTF%2d8"));
	}

	public static Properties getPlayerSettings(final Risk risk,
			Class uiclass) {
		Preferences prefs = null;
		try {
			prefs = Preferences.userNodeForPackage(uiclass);
		} catch (Throwable th) {
		} // security
		final Preferences theprefs = prefs;
		return new Properties() {
			public String getProperty(String key) {
				String value = risk.getRiskConfig(key);
				if (theprefs != null) {
					value = theprefs.get(key, value);
				}
				return value;
			}
		};
	}

	public static void loadPlayers(Risk risk, Class uiclass) {
		Properties playerSettings = getPlayerSettings(risk, uiclass);
		for (int cc = 1; cc <= IRiskGame.MAX_PLAYERS; cc++) {
			String name = playerSettings
					.getProperty("default.player" + cc
							+ ".name");
			String color = playerSettings
					.getProperty("default.player" + cc
							+ ".color");
			String type = playerSettings
					.getProperty("default.player" + cc
							+ ".type");
			if (!"".equals(name) && !"".equals(color)
					&& !"".equals(type)) {
				risk.parser("newplayer " + type + " " + color
						+ " " + name);
			}
		}
	}

	public static void savePlayers(Risk risk, Class uiclass) {

		Preferences prefs = null;
		try {
			prefs = Preferences.userNodeForPackage(uiclass);
		} catch (Throwable th) {
		} // security

		if (prefs != null) {

			List players = risk.getGame().getPlayerManager().getPlayers();

			for (int cc = 1; cc <= IRiskGame.MAX_PLAYERS; cc++) {
				String nameKey = "default.player" + cc
						+ ".name";
				String colorKey = "default.player" + cc
						+ ".color";
				String typeKey = "default.player" + cc
						+ ".type";

				String name = "";
				String color = "";
				String type = "";

				Player player = (cc <= players.size()) ? (Player) players
						.get(cc - 1) : null;

				if (player != null) {
					name = player.getName();
					color = ColorUtil
							.getStringForColor(player
									.getColor());
					type = risk.getType(player.getType());
				}
				prefs.put(nameKey, name);
				prefs.put(colorKey, color);
				prefs.put(typeKey, type);

			}

			// on android this does not work, god knows why
			// whats the point of including a class if its
			// most simple and basic operation does not work?
			try {
				prefs.flush();
			} catch (Exception ex) {
				logger.log(Level.INFO, "can not flush prefs",
						ex);
			}

		}
	}

	public static void savePlayers(List players, Class uiclass) {

		Preferences prefs = null;
		try {
			prefs = Preferences.userNodeForPackage(uiclass);
		} catch (Throwable th) {
		} // security

		if (prefs != null) {

			for (int cc = 1; cc <= IRiskGame.MAX_PLAYERS; cc++) {
				String nameKey = "default.player" + cc
						+ ".name";
				String colorKey = "default.player" + cc
						+ ".color";
				String typeKey = "default.player" + cc
						+ ".type";

				String name = "";
				String color = "";
				String type = "";

				String[] player = (cc <= players.size()) ? (String[]) players
						.get(cc - 1) : null;

				if (player != null) {
					name = player[0];
					color = player[1];
					type = player[2];
				}
				prefs.put(nameKey, name);
				prefs.put(colorKey, color);
				prefs.put(typeKey, type);

			}

			// on android this does not work, god knows why
			// whats the point of including a class if its
			// most simple and basic operation does not work?
			try {
				prefs.flush();
			} catch (Exception ex) {
				logger.log(Level.INFO, "can not flush prefs",
						ex);
			}

		}
	}

	public static BufferedReader readMap(InputStream in) throws IOException {

		PushbackInputStream pushback = new PushbackInputStream(in, 3);

		int first = pushback.read();
		if (first == 0xEF) {
			int second = pushback.read();
			if (second == 0xBB) {
				int third = pushback.read();
				if (third == 0xBF) {
					return new BufferedReader(
							new InputStreamReader(
									pushback,
									"UTF-8"));
				}
				pushback.unread(third);
			}
			pushback.unread(second);
		}
		pushback.unread(first);

		return new BufferedReader(new InputStreamReader(pushback,
				"ISO-8859-1"));
	}

	/**
	 * gets the info for a map or cards file in the case of map files it
	 * will get the "name" "crd" "prv" "pic" "map" and any "comment" and
	 * number of "countries" and for cards it will have a "missions" that
	 * will contain the String[] of all the missions
	 */
	public static java.util.Map loadInfo(String fileName, boolean cards) {

		Hashtable info = new Hashtable();

		for (int c = 0; true; c++) {

			BufferedReader bufferin = null;

			try {

				bufferin = RiskUtil.readMap(RiskUtil
						.openMapStream(fileName));
				Vector misss = null;

				if (cards) {
					MapTranslator.setCards(fileName);
					misss = new Vector();
				}

				String input = bufferin.readLine();
				String mode = null;

				while (input != null) {

					if (input.equals("")) {
						// do nothing
						// System.out.print("Nothing\n");
						// // testing
					} else if (input.charAt(0) == ';') {
						String comment = (String) info
								.get("comment");
						String com = input.substring(1)
								.trim();
						if (comment == null) {
							comment = com;
						} else {
							comment = comment
									+ "\n"
									+ com;
						}
						info.put("comment", comment);
					} else {

						if (input.charAt(0) == '['
								&& input.charAt(input
										.length() - 1) == ']') {
							mode = "newsection";
						}

						if ("files".equals(mode)) {

							int space = input
									.indexOf(' ');

							String fm = input
									.substring(0,
											space);
							String val = input
									.substring(space + 1);

							info.put(fm, val);

						} else if ("borders"
								.equals(mode)) {
							// we dont care about
							// anything in or after
							// the borders section
							break;
						} else if ("countries"
								.equals(mode)) {
							info.put("countries",
									Integer.parseInt(input
											.substring(0,
													input.indexOf(' '))));
						} else if ("missions"
								.equals(mode)) {

							StringTokenizer st = new StringTokenizer(
									input);

							String description = MapTranslator
									.getTranslatedMissionName(st
											.nextToken()
											+ "-"
											+ st.nextToken()
											+ "-"
											+ st.nextToken()
											+ "-"
											+ st.nextToken()
											+ "-"
											+ st.nextToken()
											+ "-"
											+ st.nextToken());

							if (description == null) {

								StringBuffer d = new StringBuffer();

								while (st.hasMoreElements()) {

									d.append(st.nextToken());
									d.append(" ");
								}

								description = d.toString();

							}

							misss.add(description);

						} else if ("newsection"
								.equals(mode)) {

							mode = input.substring(
									1,
									input.length() - 1); // set
												// mode
												// to
												// the
												// name
												// of
												// the
												// section

						} else if (mode == null) {
							if (input.indexOf(' ') > 0) {
								info.put(input.substring(
										0,
										input.indexOf(' ')),
										input.substring(input
												.indexOf(' ') + 1));
							}
						}
						// if "continents" or "cards"
						// then just dont do anything in
						// those sections

					}

					input = bufferin.readLine(); // get next
									// line
				}

				if (cards) {
					info.put("missions",
							(String[]) misss.toArray(new String[misss
									.size()]));
					misss = null;
				}

				break;
			} catch (IOException ex) {
				System.err.println("Error trying to load: "
						+ fileName);
				RiskUtil.printStackTrace(ex);
				if (c < 5) { // retry
					try {
						Thread.sleep(1000);
					} catch (Exception ex2) {
					}
				} else { // give up
					break;
				}
			} finally {
				if (bufferin != null) {
					try {
						bufferin.close();
					} catch (Exception ex2) {
					}
				}
			}
		}

		return info;

	}

	public static void saveGameLog(File logFile, IRiskGame game)
			throws IOException {
		FileWriter fileout = new FileWriter(logFile);
		BufferedWriter buffer = new BufferedWriter(fileout);
		PrintWriter printer = new PrintWriter(buffer);
		List commands = game.getCommands();
		int size = commands.size();
		for (int line = 0; line < size; line++) {
			printer.println(commands.get(line));
		}
		printer.close();
	}

	public static OutputStream getOutputStream(File dir, String fileName)
			throws Exception {
		File outFile = new File(dir, fileName);
		// as this could be dir=.../maps fileName=preview/file.jpg
		// we need to make sure the preview dir exists, and if it does
		// not, we must make it
		File parent = outFile.getParentFile();
		if (!parent.isDirectory() && !parent.mkdirs()) { // if it does
									// not
									// exist
									// and i
									// cant
									// make
									// it
			throw new RuntimeException("can not create dir "
					+ parent);
		}
		return new FileOutputStream(outFile);
	}

	public static void rename(File oldFile, File newFile) {
		if (newFile.exists() && !newFile.delete()) {
			throw new RuntimeException("can not del dest file: "
					+ newFile);
		}
		if (!oldFile.renameTo(newFile)) {
			try {
				copy(oldFile, newFile);
				if (!oldFile.delete()) {
					// this is not so bad, but still very
					// strange
					System.err.println("can not del source file: "
							+ oldFile);
				}
			} catch (Exception ex) {
				throw new RuntimeException(
						"rename failed: from: "
								+ oldFile
								+ " to: "
								+ newFile, ex);
			}
		}
	}

	public static Vector asVector(java.util.List list) {
		return list instanceof Vector ? (Vector) list
				: new Vector(list);
	}

	public static Hashtable asHashtable(java.util.Map map) {
		return map instanceof Hashtable ? (Hashtable) map
				: new Hashtable(map);
	}

	public static String replaceAll(String string, String notregex,
			String replacement) {
		return string.replaceAll(quote(notregex),
				quoteReplacement(replacement));
	}

	/**
	 * @see java.util.regex.Pattern#quote(java.lang.String)
	 */
	public static String quote(String s) {
		int slashEIndex = s.indexOf("\\E");
		if (slashEIndex == -1)
			return "\\Q" + s + "\\E";

		StringBuilder sb = new StringBuilder(s.length() * 2);
		sb.append("\\Q");
		slashEIndex = 0;
		int current = 0;
		while ((slashEIndex = s.indexOf("\\E", current)) != -1) {
			sb.append(s.substring(current, slashEIndex));
			current = slashEIndex + 2;
			sb.append("\\E\\\\E\\Q");
		}
		sb.append(s.substring(current, s.length()));
		sb.append("\\E");
		return sb.toString();
	}

	/**
	 * @see java.util.regex.Matcher#quoteReplacement(java.lang.String)
	 */
	public static String quoteReplacement(String s) {
		if ((s.indexOf('\\') == -1) && (s.indexOf('$') == -1))
			return s;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '\\') {
				sb.append('\\');
				sb.append('\\');
			} else if (c == '$') {
				sb.append('\\');
				sb.append('$');
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static void copy(File src, File dest) throws IOException {

		if (src.isDirectory()) {

			// if directory not exists, create it
			if (!dest.exists()) {
				dest.mkdir();
				System.out.println("Directory copied from "
						+ src + "  to " + dest);
			}

			// list all the directory contents
			String files[] = src.list();

			for (int c = 0; c < files.length; c++) {
				// construct the src and dest file structure
				File srcFile = new File(src, files[c]);
				File destFile = new File(dest, files[c]);
				// recursive copy
				copy(srcFile, destFile);
			}

		} else {
			// if file, then copy it
			// Use bytes stream to support all file types
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();
			System.out.println("File copied from " + src + " to "
					+ dest);
		}
	}

	public static String getAtLeastOne(StringTokenizer stringT) {
		StringBuilder text = new StringBuilder(stringT.nextToken());
		while (stringT.hasMoreTokens()) {
			text.append(' ');
			text.append(stringT.nextToken());
		}
		return text.toString();
	}

	/**
	 * Rolls a certain number of dice
	 * 
	 * @param nod
	 *                Number of dice you want to roll
	 * @return int[] Returns an array which was the results of the roll,
	 *         ordered from highest to lowest
	 */
	public static int[] rollDice(int nod) {

		int[] dice = new int[nod];

		for (int j = 0; j < nod; j++) {
			dice[j] = RAND.nextInt(6);
		}

		// NOW SORT THEM, biggest at the beggining
		for (int i = 0; i < nod - 1; i++) {
			int temp, pos = i;

			for (int j = i + 1; j < nod; j++)
				if (dice[j] > dice[pos])
					pos = j;
			temp = dice[i];
			dice[i] = dice[pos];
			dice[pos] = temp;
		}

		/*
		 * System.out.print("After sorting, the dice are:\n");
		 * 
		 * String str="["; if(dice.length>0) { str+=(dice[0]+1); for(int
		 * i=1; i<dice.length; i++) str+="|"+(dice[i]+1); }
		 * System.out.print(str+"]\n");
		 */
		return dice;

	}

	/**
	 * Finds the largest number of connected territories owned by a single
	 * player
	 * 
	 * @param t
	 *                Vector of territories owned by a single player
	 *                (volatile)
	 * @param a
	 *                Vector of adjacent territories
	 * @param n
	 *                Vector of territories owned by a single player
	 *                (non-volatile)
	 * @param p
	 *                The current player
	 */
	public static void getConnectedEmpire(List t, List a, List n, Player p) {

		for (int i = 0; i < n.size(); i++) {

			if (((Country) n.get(i)).getOwner() == p
					&& t.contains(n.get(i))) {

				Country country = (Country) n.get(i);
				t.remove(country);
				a.add(country);

				getConnectedEmpire(t, a,
						country.getNeighbours(), p);

			}
		}

	}

	public static List getConnectedEmpire(Player p) {

		Vector t = (Vector) p.getTerritoriesOwned().clone();

		Vector a = new Vector();
		Vector b = new Vector();

		while (t.isEmpty() == false) {

			Country country = ((Country) t.remove(0));

			a.add(country);

			getConnectedEmpire(t, a, country.getNeighbours(), p);

			if (a.size() > b.size()) {
				b = a;
			}

			a = new Vector();

		}

		return b;

	}

	public static Card getCard(Map<String, List<Card>> cardTypes,
			String name) {
		List<Card> type = cardTypes.get(name);
		if (type != null) {
			return type.get(0);
		}
		return null;
	}

	/**
	 * Find the best (highest) trade Simple greedy search using the various
	 * valid combinations
	 * 
	 * @param cards
	 * @return
	 */
	public static int getBestTrade(List<Card> cards, Card[] bestResult,
			int cardMode, int cardState) {
		Map<String, List<Card>> cardTypes = new HashMap<String, List<Card>>();
		for (Card card : cards) {
			List<Card> cardType = cardTypes.get(card.getName());
			if (cardType == null) {
				cardType = new ArrayList<Card>();
				cardTypes.put(card.getName(), cardType);
			}
			cardType.add(card);
		}
		Card carda = null;
		Card cardb = null;
		Card cardc = null;
		int bestValue = 0;
		if (cardTypes.size() >= 3) {
			carda = getCard(cardTypes, Card.CANNON);
			if (carda == null) {
				carda = getCard(cardTypes, Card.WILDCARD);
			}
			cardb = getCard(cardTypes, Card.CAVALRY);
			if (cardb == null) {
				cardb = getCard(cardTypes, Card.WILDCARD);
			}
			cardc = getCard(cardTypes, Card.INFANTRY);
			if (cardc == null) {
				cardc = getCard(cardTypes, Card.WILDCARD);
			}
			bestValue = getTradeAbsValue(carda.getName(),
					cardb.getName(), cardc.getName(),
					cardMode, cardState);
			if (bestValue > 0) {
				if (bestResult == null) {
					return bestValue;
				}
				bestResult[0] = carda;
				bestResult[1] = cardb;
				bestResult[2] = cardc;
			}
		}
		List<Card> wildCards = cardTypes.get(Card.WILDCARD);
		int wildCardCount = wildCards == null ? 0 : wildCards.size();
		for (Map.Entry<String, List<Card>> entry : cardTypes.entrySet()) {
			carda = null;
			if (entry.getKey().equals(Card.WILDCARD)) {
				if (wildCardCount >= 3) {
					carda = wildCards.get(0);
					cardb = wildCards.get(1);
					cardc = wildCards.get(2);
				}
			} else {
				List<Card> cardList = entry.getValue();
				if (cardList.size() + wildCardCount >= 3) {
					carda = cardList.get(0);
					cardb = cardList.size() > 1 ? cardList
							.get(1) : wildCards
							.get(0);
					cardc = cardList.size() > 2 ? cardList
							.get(2)
							: wildCards.get(2 - cardList
									.size());
				}
			}
			if (carda != null) {
				int val = getTradeAbsValue(carda.getName(),
						cardb.getName(),
						cardc.getName(), cardMode,
						cardState);
				if (val > bestValue) {
					bestValue = val;
					if (bestResult == null) {
						return bestValue;
					}
					bestResult[0] = carda;
					bestResult[1] = cardb;
					bestResult[2] = cardc;
				}
			}
		}
		return bestValue;
	}

	/**
	 * Returns the trading value of the given cards, without taking into
	 * account the territories associated to the cards.
	 * 
	 * @param c1
	 *                The name of the type of the first card.
	 * @param c2
	 *                The name of the type of the second card.
	 * @param c3
	 *                The name of the type of the third card.
	 * @return 0 in case of invalid combination of cards.
	 */
	public static int getTradeAbsValue(String c1, String c2, String c3,
			int cardMode, int cardState) {
		int armies = 0;

		// we shift all wildcards to the front
		if (!c1.equals(Card.WILDCARD)) {
			String n4 = c3;
			c3 = c1;
			c1 = n4;
		}
		if (!c2.equals(Card.WILDCARD)) {
			String n4 = c3;
			c3 = c2;
			c2 = n4;
		}
		if (!c1.equals(Card.WILDCARD)) {
			String n4 = c2;
			c2 = c1;
			c1 = n4;
		}

		if (cardMode == IRiskGame.CARD_INCREASING_SET) {
			if (c1.equals(Card.WILDCARD)
					|| (c1.equals(c2) && c1.equals(c3))
					|| (!c1.equals(c2) && !c1.equals(c3) && !c2
							.equals(c3))) {
				armies = getNewCardState(cardState);
			}
		} else if (cardMode == IRiskGame.CARD_FIXED_SET) {
			// ALL THE SAME or 'have 1 wildcard and 2 the same'
			if ((c1.equals(c2) || c1.equals(Card.WILDCARD))
					&& c2.equals(c3)) {
				if (c3.equals(Card.INFANTRY)) {
					armies = 4;
				} else if (c3.equals(Card.CAVALRY)) {
					armies = 6;
				} else if (c3.equals(Card.CANNON)) {
					armies = 8;
				} else { // (c1.equals( Card.WILDCARD ))
					armies = 12; // Incase someone puts 3
							// wildcards into his
							// set
				}
			}
			// ALL CARDS ARE DIFFERENT (can have 1 wildcard) or 2
			// wildcards and a 3rd card
			else if ((c1.equals(Card.WILDCARD) && c2
					.equals(Card.WILDCARD))
					|| (!c1.equals(c2) && !c2.equals(c3) && !c1
							.equals(c3))) {
				armies = 10;
			}
		} else { // (cardMode==CARD_ITALIANLIKE_SET)
			if (c1.equals(c2) && c1.equals(c3)) {
				// All equal
				if (c1.equals(Card.CAVALRY)) {
					armies = 8;
				} else if (c1.equals(Card.INFANTRY)) {
					armies = 6;
				} else if (c1.equals(Card.CANNON)) {
					armies = 4;
				} else { // (c1.equals( Card.WILDCARD ))
					armies = 0; // Incase someone puts 3
							// wildcards into his
							// set
				}
			} else if (!c1.equals(c2) && !c2.equals(c3)
					&& !c1.equals(c3)
					&& !c1.equals(Card.WILDCARD)) {
				armies = 10;
			}
			// All the same w/1 wildcard
			else if (c1.equals(Card.WILDCARD) && c2.equals(c3)) {
				armies = 12;
			}
			// 2 wildcards, or a wildcard and two different
			else {
				armies = 0;
			}
		}
		return armies;
	}

	public static int getNewCardState(int cardState) {

		if (cardState < 4) {
			return cardState + 4;
		} else if (cardState < 12) {
			return cardState + 2;
		} else if (cardState < 15) {
			return cardState + 3;
		} else {
			return cardState + 5;
		}

	}

	/**
	 * this code is used to check if the borders in the map file are ok
	 * 
	 * @param countries
	 * @throws Exception
	 */
	public static void testMap(Country[] countries) throws Exception {

		// System.out.print("Starting map test...\n");

		for (int c = 0; c < countries.length; c++) {

			Country c1 = countries[c];
			Vector c1neighbours = (Vector) c1.getNeighbours();

			if (c1neighbours.contains(c1)) {
				throw new Exception("Error: " + c1.getName()
						+ " neighbours with itself");
			}

			for (int a = 0; a < c1neighbours.size(); a++) {

				Country c2 = (Country) c1neighbours
						.elementAt(a);
				Vector c2neighbours = (Vector) c2
						.getNeighbours();

				boolean ok = false;

				for (int b = 0; b < c2neighbours.size(); b++) {

					Country c3 = (Country) c2neighbours
							.elementAt(b);

					if (c1 == c3) {
						ok = true;
					}

				}

				if (ok == false) {
					throw new Exception(
							"Borders error with: "
									+ countries[c].getName()
									+ " ("
									+ countries[c].getColor()
									+ ") and "
									+ ((Country) c1neighbours
											.elementAt(a))
											.getName()
									+ " ("
									+ ((Country) c1neighbours
											.elementAt(a))
											.getColor()
									+ ")"); // Display
				}

			}
		}

		// System.out.print("End map test.\n");

	}
}
