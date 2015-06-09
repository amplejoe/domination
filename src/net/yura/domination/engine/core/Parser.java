package net.yura.domination.engine.core;

import java.io.BufferedReader;
import java.io.Serializable;

import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.translation.MapTranslator;

public class Parser implements Serializable {
	private static final long serialVersionUID = -2051918180934390539L;

	public class Response {
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

	public Response parseCards(String f, String defaultCards) throws Exception {
		if (f.equals("default")) {
			f = defaultCards;
		}

		BufferedReader bufferin = RiskUtil.readMap(RiskUtil.openMapStream(f));

		String input = bufferin.readLine();
		String mode = "none";

		Response parsed = new Response();

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
}
