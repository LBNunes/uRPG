package game;

import org.unbiquitous.uImpala.engine.asset.AssetManager;
import org.unbiquitous.uImpala.engine.asset.Sprite;
import org.unbiquitous.uImpala.engine.core.Game;
import org.unbiquitous.uImpala.engine.core.GameComponents;
import org.unbiquitous.uImpala.engine.core.GameObject;
import org.unbiquitous.uImpala.engine.core.GameRenderers;
import org.unbiquitous.uImpala.engine.io.MouseEvent;
import org.unbiquitous.uImpala.engine.io.MouseSource;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.util.Corner;
import org.unbiquitous.uImpala.util.observer.Event;
import org.unbiquitous.uImpala.util.observer.Observation;
import org.unbiquitous.uImpala.util.observer.Subject;

@SuppressWarnings("unused")
public class Grid extends GameObject {

    private Sprite      bg;
    private Sprite      clearHex;
    private Sprite      redHex;
    private Sprite      greenHex;
    private Sprite      blueHex;
    private Sprite      yellowHex;

    private Screen      screen;
    private MouseSource mouse;

    static final int    hexRadius   = 58;
    static final int    hexHeight   = 50;
    static final int    hexSide     = 29;
    static final int    hexXOffset  = hexHeight * 2;
    static final int    hexYOffset  = hexRadius + hexSide;

    static final int    gridOffsetX = 40;
    static final int    gridOffsetY = 56;

    static final int    topHexX     = gridOffsetX + hexHeight;
    static final int    topHexY     = gridOffsetY + hexRadius;

    static final int    nColumnsMax = 12;
    static final int    nColumnsMin = 11;
    static final int    nRows       = 7;

    HexColors           gridColors[][];

    public enum HexColors {
        CLEAR, RED, GREEN, BLUE, YELLOW
    };

    public enum GridArea {
        SINGLE_HEX, CIRCLE, DIAGONAL_UP_LINE, DIAGONAL_DOWN_LINE, HORIZONTAL_LINE, ALL
    }

    public Grid(AssetManager assets) {
        bg = assets.newSprite(Config.GRASS_BG);
        clearHex = assets.newSprite(Config.CLEAR_HEX);
        redHex = assets.newSprite(Config.RED_HEX);
        greenHex = assets.newSprite(Config.GREEN_HEX);
        blueHex = assets.newSprite(Config.BLUE_HEX);
        yellowHex = assets.newSprite(Config.YELLOW_HEX);

        gridColors = new HexColors[nColumnsMax][nRows];
        clearColors(HexColors.CLEAR);

        screen = GameComponents.get(Screen.class);
        mouse = screen.getMouse();
        mouse.connect(MouseSource.EVENT_BUTTON_DOWN, new Observation(this, "buttonDown"));
    }

    private void clearColors(HexColors fill) {
        for (int i = 0; i < nColumnsMax; ++i) {
            for (int j = 0; j < nRows; ++j) {
                gridColors[i][j] = fill;
            }
        }
    }

    private void renderAtHex(Sprite s, int x, int y) {
        Point p = findHexPosition(x, y);
        if (p != null)
            s.render(screen, p.x, p.y, Corner.CENTER);
    }

    private Point findHexPosition(int x, int y) {

        if (!validHexPosition(x, y))
            return null;

        Point pos = new Point((int) (topHexX + x * hexXOffset), (int) (topHexY + y * hexYOffset));

        if (y % 2 == 1)
            pos.x += hexHeight;

        return pos;
    }

    private boolean validHexPosition(int x, int y) {
        if (y % 2 == 1) {
            if (x >= nColumnsMin) {
                return false;
            }
        }
        else if (x >= nColumnsMax) {
            return false;
        }

        if (y >= nRows) {
            return false;
        }

        if (x < 0 || y < 0) {
            return false;
        }
        return true;

    }

    private void colorArea(int x, int y, GridArea area, HexColors fill, int complement) {

        if (!validHexPosition(x, y) && area != GridArea.ALL)
            return;

        System.out.println("Coloring hex " + x + ", " + y);

        switch (area) {
        // Color the hex at x, y - Complement is unused
            case SINGLE_HEX:
                gridColors[x][y] = fill;
                break;
            // Color the circle centered at x, y - Complement is radius
            case CIRCLE:
                gridColors[x][y] = fill;
                // If the radius is 1 or more, color the hexes/circles centered around it
                if (complement > 0) {
                    colorArea(x, y + 1, area, fill, complement - 1);
                    colorArea(x, y - 1, area, fill, complement - 1);
                    colorArea(x + 1, y, area, fill, complement - 1);
                    colorArea(x - 1, y, area, fill, complement - 1);

                    if (y % 2 == 1) {
                        colorArea(x + 1, y + 1, area, fill, complement - 1);
                        colorArea(x + 1, y - 1, area, fill, complement - 1);
                    }
                    else {
                        colorArea(x - 1, y + 1, area, fill, complement - 1);
                        colorArea(x - 1, y - 1, area, fill, complement - 1);
                    }
                }
                break;

            // Line with an inclination of +30°, starting in x, y. Complement specifies length.
            case DIAGONAL_UP_LINE:
                gridColors[x][y] = fill;
                if (complement > 0) {
                    if (y % 2 == 1) {
                        colorArea(x + 1, y - 1, area, fill, complement - 1);
                    }
                    else {
                        colorArea(x, y - 1, area, fill, complement - 1);
                    }
                }
                // If it's less than 0, fill the hex (-1, +1)
                else if (complement < 0) {
                    if (y % 2 == 1) {
                        colorArea(x, y + 1, area, fill, complement - 1);
                    }
                    else {
                        colorArea(x - 1, y + 1, area, fill, complement - 1);
                    }
                }
                break;
            // Line with an inclination of -30°, starting in x, y. Complement specifies length.
            case DIAGONAL_DOWN_LINE:
                gridColors[x][y] = fill;
                if (complement > 0) {
                    if (y % 2 == 1) {
                        colorArea(x + 1, y + 1, area, fill, complement - 1);
                    }
                    else {
                        colorArea(x, y + 1, area, fill, complement - 1);
                    }
                }
                // If it's less than 0, fill the hex (-1, +1)
                else if (complement < 0) {
                    if (y % 2 == 1) {
                        colorArea(x, y - 1, area, fill, complement - 1);
                    }
                    else {
                        colorArea(x - 1, y - 1, area, fill, complement - 1);
                    }
                }
                break;
            // Color the line going up or down, starting at x, y. Complement specifies length.
            case HORIZONTAL_LINE:
                gridColors[x][y] = fill;
                // If the length is more than 0, fill the hex (+1, +0)
                if (complement > 0) {
                    colorArea(x + 1, y, area, fill, complement - 1);
                }
                // If it's less than 0, fill the hex (-1, +0)
                else if (complement < 0) {
                    colorArea(x - 1, y, area, fill, complement - 1);
                }
                break;
            // Color the whole grid - Complement is ignored.
            case ALL:
                clearColors(fill);
                break;
        }
    }

    @Override
    protected void render(GameRenderers renderers) {
        bg.render(screen, 0, 0, Corner.TOP_LEFT);

        for (int i = 0; i < nColumnsMax; ++i) {
            for (int j = 0; j < nRows; ++j) {
                switch (gridColors[i][j]) {
                    case RED:
                        renderAtHex(redHex, i, j);
                        break;
                    case GREEN:
                        renderAtHex(greenHex, i, j);
                        break;
                    case BLUE:
                        renderAtHex(blueHex, i, j);
                        break;
                    case CLEAR:
                        renderAtHex(clearHex, i, j);
                        break;
                    case YELLOW:
                        renderAtHex(yellowHex, i, j);
                }
            }
        }
    }

    private void buttonDown(Event event, Subject subject) {
        MouseEvent e = (MouseEvent) event;
        colorArea(6, 3, GridArea.DIAGONAL_DOWN_LINE, HexColors.RED, -4);
    }

    @Override
    protected void update() {
        if (screen.isCloseRequested()) {
            GameComponents.get(Game.class).quit();
        }

    }

    @Override
    protected void wakeup(Object... args) {
    }

    @Override
    protected void destroy() {
    }
}