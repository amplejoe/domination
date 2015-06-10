package net.yura.domination.engine.core;

import java.io.BufferedReader;
import java.io.Serializable;

import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.Parser.CardsResult;
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

	public static class MapResult {
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

	public MapResult parseMap(String f, String defaultMap, String defaultCards) throws Exception {
		if (f.equals("default")) {
			f = defaultMap;
		}

		BufferedReader bufferin = RiskUtil.readMap(RiskUtil.openMapStream(f));

		MapResult result = new MapResult();

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
						result.setMissions(parseCards(input.substring(4), defaultCards).isMissions());
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
}
