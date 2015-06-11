package net.yura.domination.engine.ai;

import net.yura.domination.engine.core.IRiskGame;

/**
 * @author Yura Mamyrin
 */
public interface AI {

    int getType();
    String getCommand();

    void setGame(IRiskGame game);

    String getBattleWon();
    String getTacMove();
    String getTrade();
    String getPlaceArmies();
    String getAttack();
    String getRoll();
    String getCapital();
    String getAutoDefendString();

}
