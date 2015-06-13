package net.yura.domination.engine;

import net.yura.domination.engine.core.IRiskGame;

public interface IRiskOnline extends IRiskChat {
	void setOnlinePlay(OnlineRisk onlineRisk);

	void setAddress(String address);

	void setGame(IRiskGame game);
}
