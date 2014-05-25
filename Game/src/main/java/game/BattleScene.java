package game;

import org.unbiquitous.uImpala.engine.core.GameComponents;
import org.unbiquitous.uImpala.engine.core.GameObjectTreeScene;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.engine.io.ScreenManager;

public class BattleScene extends GameObjectTreeScene {

    private Screen screen;

    public BattleScene() {

        // Initialize the screen manager
        screen = GameComponents.get(ScreenManager.class).create();
        screen.open(Config.WINDOW_TITLE, Config.SCREEN_WIDTH,
                Config.SCREEN_HEIGHT, Config.FULLSCREEN, Config.WINDOW_ICON);
        GameComponents.put(Screen.class, screen);

        add(new Grid(assets));
    }
}