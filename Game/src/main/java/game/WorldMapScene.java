package game;

import org.unbiquitous.uImpala.engine.core.Game;
import org.unbiquitous.uImpala.engine.core.GameComponents;
import org.unbiquitous.uImpala.engine.core.GameScene;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.engine.io.ScreenManager;

public class WorldMapScene extends GameScene {

    private Screen screen;

    // TODO: Figure out which areas will stay...
    public enum WorldArea {
        GRASSLAND, FOREST, DESERT, SWAMP, HAUNTED_MANSION, DUNGEON, CAVE, LAKE, SHORE, MOUNTAINS
    }

    public WorldMapScene() {
        // Initialize the screen manager
        screen = GameComponents.get(ScreenManager.class).create();
        screen.open(Config.WINDOW_TITLE, Config.SCREEN_WIDTH,
                Config.SCREEN_HEIGHT, Config.FULLSCREEN, Config.WINDOW_ICON);
        GameComponents.put(Screen.class, screen);

        Item.InitTable();
        Item.DumpTable();
        Classes.InitStats();
    }

    @Override
    protected void update() {
        this.frozen = true;
        GameComponents.get(Game.class).change(new BattleScene(new PlayerData(), WorldArea.GRASSLAND));
    }

    @Override
    protected void render() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void wakeup(Object... args) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void destroy() {
        // TODO Auto-generated method stub

    }

}
