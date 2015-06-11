package net.yura.domination.engine.core;

public class RiskGameFactory {
	public static IRiskGame create() throws Exception {
		return RiskGame.newInstance();
	}
}
