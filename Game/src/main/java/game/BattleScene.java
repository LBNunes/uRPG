package game;

import game.Grid.GridArea;
import game.Grid.HexColors;
import game.WorldMapScene.WorldArea;

import java.util.ArrayList;

import org.unbiquitous.uImpala.engine.core.Game;
import org.unbiquitous.uImpala.engine.core.GameComponents;
import org.unbiquitous.uImpala.engine.core.GameObjectTreeScene;
import org.unbiquitous.uImpala.engine.core.GameRenderers;
import org.unbiquitous.uImpala.engine.io.MouseEvent;
import org.unbiquitous.uImpala.engine.io.MouseSource;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.util.observer.Event;
import org.unbiquitous.uImpala.util.observer.Observation;
import org.unbiquitous.uImpala.util.observer.Subject;

public class BattleScene extends GameObjectTreeScene {

    private enum TurnStage {
        BATTLE_BEGIN, FIND_NEXT_PLAYER
    }

    private Screen      screen;
    private MouseSource mouse;

    ArrayList<Entity>   playerUnits;
    ArrayList<Entity>   enemyUnits;
    Grid                grid;
    TurnStage           currentStage;
    TurnStage           nextStage;

    public BattleScene(PlayerData playerData, WorldArea area) {

        // Initialize the screen manager
        screen = GameComponents.get(Screen.class);

        mouse = screen.getMouse();
        mouse.connect(MouseSource.EVENT_BUTTON_DOWN, new Observation(this, "OnButtonDown"));

        // TODO: Unpack player data
        playerUnits = new ArrayList<Entity>();

        // TODO: Generate enemies
        enemyUnits = new ArrayList<Entity>();

        grid = new Grid(assets, area);
    }

    @SuppressWarnings("unused")
    private void OnButtonDown(Event event, Subject subject) {
        MouseEvent e = (MouseEvent) event;
        grid.ClearColors(HexColors.CLEAR);
        Point p = grid.FindHexByPixel(e.getX(), e.getY());
        grid.ColorArea(p.x, p.y, GridArea.CIRCLE, HexColors.BLUE, 2);
    }

    protected void update() {
        if (screen.isCloseRequested()) {
            GameComponents.get(Game.class).quit();
        }
        grid.update();
        for (Entity e : playerUnits) {
            e.update();
        }

        for (Entity e : enemyUnits) {
            e.update();
        }

        TurnLogic();
    }

    private void TurnLogic() {
        // TODO Auto-generated method stub

    }

    protected void render() {
        GameRenderers renderers = new GameRenderers();
        grid.render(renderers);
        for (Entity e : playerUnits) {
            e.render(renderers);
        }

        for (Entity e : enemyUnits) {
            e.render(renderers);
        }
    }
}