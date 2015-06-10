package net.yura.domination.engine.core;

import java.io.BufferedReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import net.yura.domination.engine.ColorUtil;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.translation.MapTranslator;

public class Parser implements Serializable {
	private static final long serialVersionUID = -2051918180934390539L;

	private PropertyManager propertyManager;

	public Parser(PropertyManager propertyManager) {
		this.propertyManager = propertyManager;
	}

	public static class CardsResult {
		private boolean missions = false;
		private boolean cards = false;
		private String cardsFile;

		public boolean isMissions() {
			return missions;
		}

		public void setMissions(boolean missions) {
			this.missions = missions;
		}

		public boolean isCards() {
			return cards;
		}

		public void setCards(boolean cards) {
			this.cards = cards;
		}

		public String getCardsFile() {
			return cardsFile;
		}

		public void setCardsFile(String cardsFile) {
			this.cardsFile = cardsFile;
		}
	}

	public static class MapFileResult {
		private boolean runMapTest = false;
		private boolean missions = false;
		private String previewPicture;
		private String imagePicture;
		private String mapFile;

		public boolean isRunMapTest() {
			return runMapTest;
		}

		public void setRunMapTest(boolean runMapTest) {
			this.runMapTest = runMapTest;
		}

		public String getPreviewPicture() {
			return previewPicture;
		}

		public void setPreviewPicture(String previewPicture) {
			this.previewPicture = previewPicture;
		}

		public String getImagePicture() {
			return imagePicture;
		}

		public void setImagePicture(String imagePicture) {
			this.imagePicture = imagePicture;
		}

		public String getMapFile() {
			return mapFile;
		}

		public void setMapFile(String mapFile) {
			this.mapFile = mapFile;
		}

		public boolean isMissions() {
			return missions;
		}

		public void setMissions(boolean missions) {
			this.missions = missions;
		}
	}

	public static class MapResult {
		private String imagePicture;
		private String imageMap;
		private List<Country> countries;
		private List<Continent> continents;

		public String getImagePicture() {
			return imagePicture;
		}

		public void setImagePicture(String imagePicture) {
			this.imagePicture = imagePicture;
		}

		public String getImageMap() {
			return imageMap;
		}

		public void setImageMap(String imageMap) {
			this.imageMap = imageMap;
		}

		public List<Country> getCountries() {
			return countries;
		}

		public void setCountries(List<Country> countries) {
			this.countries = countries;
		}

		public List<Continent> getContinents() {
			return continents;
		}

		public void setContinents(List<Continent> continents) {
			this.continents = continents;
		}
	}

	public CardsResult parseCards(String f, String defaultCards)
			throws Exception {
		if (f.equals("default")) {
			f = defaultCards;
		}

		BufferedReader bufferin = RiskUtil.readMap(RiskUtil.openMapStream(f));

		String input = bufferin.readLine();
		String mode = "none";

		CardsResult parsed = new CardsResult();

		while (input != null) {

			if (input.equals("") || input.charAt(0) == ';') {

			} else {

				if (input.charAt(0) == '['
						&& input.charAt(input.length() - 1) == ']') {
					mode = "newsection";
				}

				if (mode.equals("newsection")) {

					mode = input.substring(1, input.length() - 1); // set mode
																	// to the
																	// name of
																	// the
																	// section

					if (mode.equals("cards")) {

						parsed.setCards(true);

					} else if (mode.equals("missions")) {

						parsed.setMissions(true);
					}
				}

			}

			input = bufferin.readLine(); // get next line

		}

		if (parsed.isCards() == false) {
			throw new Exception("error with cards file");
		}

		parsed.setCardsFile(f);
		bufferin.close();

		MapTranslator.setCards(f);

		return parsed;
	}

	public MapFileResult parseMapFile(String f, String defaultMap,
			String defaultCards) throws Exception {
		if (f.equals("default")) {
			f = defaultMap;
		}

		BufferedReader bufferin = RiskUtil.readMap(RiskUtil.openMapStream(f));

		MapFileResult result = new MapFileResult();

		result.setRunMapTest(false);
		result.setPreviewPicture(null);

		String input = bufferin.readLine();
		String mode = null;

		boolean yesmap = false;
		boolean yescards = false;

		while (input != null) {

			if (input.equals("") || input.charAt(0) == ';') {

			} else {

				if (input.charAt(0) == '['
						&& input.charAt(input.length() - 1) == ']') {
					mode = "newsection";
				}

				if ("files".equals(mode)) {

					if (input.startsWith("pic ")) {
						result.setImagePicture(input.substring(4));
					}

					else if (input.startsWith("prv ")) {
						result.setPreviewPicture(input.substring(4));
					}

					else if (input.startsWith("crd ")) {
						yescards = true;
						result.setMissions(parseCards(input.substring(4),
								defaultCards).isMissions());
					}

				} else if ("borders".equals(mode)) {

					yesmap = true;

				} else if ("newsection".equals(mode)) {

					mode = input.substring(1, input.length() - 1); // set mode
																	// to the
																	// name of
																	// the
																	// section

				} else if (mode == null) {

					int space = input.indexOf(' ');

					if (input.equals("test")) {

						result.setRunMapTest(true);

					} else if (space >= 0) {
						String key = input.substring(0, space);
						String value = input.substring(space + 1);

						propertyManager.put(key, value);
					}
					// else unknown section
				}
			}

			input = bufferin.readLine(); // get next line
		}

		if (yesmap == false) {
			throw new Exception("error with map file");
		}
		if (yescards == false) {
			throw new Exception("cards file not specified in map file");
		}

		result.setMapFile(f);
		bufferin.close();

		return result;
	}

	public MapResult parseMap(String mapFile, BufferedReader bufferin,
			boolean cleanLoad, Country[] theCountries, Continent[] theContinents)
			throws Exception {
		MapTranslator.setMap(mapFile);

		StringTokenizer st = null;

		List<Country> countries;
		List<Continent> continents;
		if (cleanLoad) {
			countries = new ArrayList<Country>();
			continents = new ArrayList<Continent>();
		} else {
			countries = Arrays.asList(theCountries);
			continents = Arrays.asList(theContinents);
		}

		int mapVer = 1;
		// System.out.print("Starting Load Map...\n");
		int countryCount = 0;
		if (bufferin == null) {
			bufferin = RiskUtil.readMap(RiskUtil.openMapStream(mapFile));
		}

		String input = bufferin.readLine();
		String mode = "none";

		MapResult result = new MapResult();

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

				if (mode.equals("files")) {
					// System.out.print("Adding files\n"); // testing

					if (input.startsWith("pic ")) {
						result.setImagePicture(input.substring(4));
					} // System.out.print("file: ImagePic added!\n"); // testing
					else if (input.startsWith("map ")) {
						result.imageMap = input.substring(4);
					} // System.out.print("file: ImageMap added!\n"); // testing
					else if (input.startsWith("crd ")) {
					} else if (input.startsWith("prv ")) {
					} else {
						throw new Exception(
								"error with files section in map file: "
										+ input);
					}

				} else if (mode.equals("continents")) {
					// System.out.print("Adding continents\n"); // testing

					String id = st.nextToken(); // System.out.print(name+"\n");
												// // testing

					// get translation
					String name = MapTranslator.getTranslatedMapName(id)
							.replaceAll("_", " ");

					int noa = Integer.parseInt(st.nextToken()); // System.out.print(noa+"\n");
																// // testing
					int color = ColorUtil.getColor(st.nextToken()); // System.out.print(color.toString()+"\n");
																	// //
																	// testing

					if (color == 0) {

						// there was no check for null b4 here, but now we need
						// this for the map editor
						color = ColorUtil.getRandomColor();

					}

					if (st.hasMoreTokens()) {
						throw new Exception("unknown item found in map file: "
								+ st.nextToken());
					}

					if (cleanLoad) {
						Continent continent = new Continent(id, name, noa,
								color);
						continents.add(continent);
					}

				} else if (mode.equals("countries")) {
					// System.out.print("Adding countries\n"); // testing

					int color = Integer.parseInt(st.nextToken());
					String id = st.nextToken(); // System.out.print(name+"\n");
												// // testing

					// get translation
					String name = MapTranslator.getTranslatedMapName(id)
							.replaceAll("_", " ");

					int continent = Integer.parseInt(st.nextToken());
					int x = Integer.parseInt(st.nextToken());
					int y = Integer.parseInt(st.nextToken());

					if (st.hasMoreTokens()) {
						throw new Exception("unknown item found in map file: "
								+ st.nextToken());
					}
					if (++countryCount != color) {
						throw new Exception(
								"unexpected number found in map file: " + color);
					}

					Country country;
					if (cleanLoad) {
						country = new Country();
						countries.add(country);
						((Continent) continents.get(continent - 1))
								.addTerritoriesContained(country);
					} else {
						country = (Country) countries.get(color - 1);
					}

					country.setColor(color);
					country.setContinent((Continent) continents
							.get(continent - 1));
					country.setIdString(id);
					country.setName(name);
					country.setX(x);
					country.setY(y);
				} else if (mode.equals("borders")) {
					// System.out.print("Adding borders\n"); // testing

					int country = Integer.parseInt(st.nextToken()); // System.out.print(country+"\n");
																	// //
																	// testing
					while (st.hasMoreElements()) {
						((Country) countries.get(country - 1))
								.addNeighbour(((Country) countries.get(Integer
										.parseInt(st.nextToken()) - 1)));
					}

				} else if (mode.equals("newsection")) {

					mode = input.substring(1, input.length() - 1); // set mode
																	// to the
																	// name of
																	// the
																	// section

					if (mode.equals("files")) {
						// System.out.print("Section: files found\n"); //
						// testing
						result.setImagePicture(null);
						result.imageMap = null;
					} else if (mode.equals("continents")) {
						// System.out.print("Section: continents found\n"); //
						// testing
					} else if (mode.equals("countries")) {
						// System.out.print("Section: countries found\n"); //
						// testing
					} else if (mode.equals("borders")) {
						// System.out.print("Section: borders found\n"); //
						// testing
					} else {
						throw new Exception(
								"unknown section found in map file: " + mode);
					}

				}
				// we are not in any section
				else if (input.startsWith("ver ")) {
					mapVer = Integer
							.parseInt(input.substring(4, input.length()));
				}
			}

			input = bufferin.readLine(); // get next line
		}
		bufferin.close();

		int gameVer = propertyManager.getVersion();
		if (gameVer > mapVer) {
			throw new Exception(mapFile + " too old, ver " + mapVer
					+ ". game saved with ver " + gameVer);
		}

		result.setCountries(countries);
		result.setContinents(continents);
		
		// System.out.print("Map Loaded\n");

		return result;
	}
}
