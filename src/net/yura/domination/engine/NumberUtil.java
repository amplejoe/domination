package net.yura.domination.engine;

public class NumberUtil {
	/**
	 * Gets a cards
	 * 
	 * @param s
	 *                The number you want to parse
	 * @return int The number you wanted
	 * @throws NumberFormatException
	 *                 You cannot parse the string
	 */
	public static int getNumber(String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return -1;
		}
	}
}
