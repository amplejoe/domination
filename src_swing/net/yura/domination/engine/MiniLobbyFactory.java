package net.yura.domination.engine;

import java.awt.Window;

import net.yura.domination.lobby.mini.MiniLobbyRisk;
import net.yura.domination.mapstore.MapChooser;
import net.yura.lobby.mini.MiniLobbyClient;
import net.yura.lobby.model.Game;
import net.yura.lobby.model.GameType;

public class MiniLobbyFactory
{
	public static MiniLobbyClient makeMiniLobbyClient(Risk risk,final Window window) {
        MapChooser.loadThemeExtension();
        return new MiniLobbyClient( new MiniLobbyRisk(risk) {
            private net.yura.domination.lobby.client.GameSetupPanel gsp;
            public void openGameSetup(GameType gameType) {
                if (gsp==null) {
                    gsp = new net.yura.domination.lobby.client.GameSetupPanel();
                }
                Game result = gsp.showDialog( window , gameType.getOptions(), lobby.whoAmI() );
                if (result!=null) {
                    lobby.createNewGame(result);
                }
            }
            public String getAppName() {
                return "SwingDomination";
            }
            public String getAppVersion() {
                return RiskUtil.RISK_VERSION;
            }
        } );
    }
}
