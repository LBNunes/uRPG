package game;

import org.unbiquitous.uImpala.engine.asset.AssetManager;
import org.unbiquitous.uImpala.engine.asset.Sprite;
import org.unbiquitous.uImpala.engine.core.Game;
import org.unbiquitous.uImpala.engine.core.GameComponents;
import org.unbiquitous.uImpala.engine.core.GameObject;
import org.unbiquitous.uImpala.engine.core.GameRenderers;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.util.Corner;

public class Grid extends GameObject {

    private Sprite   bg;
    private Sprite   clearHex;
    private Sprite   redHex;
    private Sprite   greenHex;
    private Sprite   blueHex;
    private Sprite   yellowHex;
    private Screen   screen;

    static final int hexRadius   = 62;
    static final int hexHeight   = 53;
    static final int hexSide     = 32;
    static final int hexXOffset  = hexRadius + hexSide;

    static final int gridOffsetX = 14;
    static final int gridOffsetY = 64;

    static final int topHexX     = gridOffsetX + hexRadius;
    static final int topHexY     = gridOffsetY + hexHeight;

    static final int nColumns    = 13;
    static final int nRowsMin    = 5;
    static final int nRowsMax    = 6;

    HexColors        gridColors[][];

    private enum HexColors {
        CLEAR, RED, GREEN, BLUE, YELLOW
    };

    public Grid(AssetManager assets) {
        bg = assets.newSprite(Config.GRASS_BG);
        clearHex = assets.newSprite(Config.CLEAR_HEX);
        redHex = assets.newSprite(Config.RED_HEX);
        greenHex = assets.newSprite(Config.GREEN_HEX);
        blueHex = assets.newSprite(Config.BLUE_HEX);
        yellowHex = assets.newSprite(Config.YELLOW_HEX);

        gridColors = new HexColors[nColumns][nRowsMax];
        clearColors(HexColors.CLEAR);

        screen = GameComponents.get(Screen.class);
    }

    private void clearColors(HexColors fill) {
        for (int i = 0; i < nColumns; ++i) {
            for (int j = 0; j < nRowsMax; ++j) {
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

        Point pos = new Point((int) (topHexX + x * hexXOffset),
                (int) (topHexY + 2 * y * hexHeight));

        if (x % 2 == 1)
            pos.y += hexHeight;

        return pos;
    }

    private boolean validHexPosition(int x, int y) {
        if (x % 2 == 1) {
            if (y >= nRowsMin) {
                return false;
            }
        }
        else if (y >= nRowsMax) {
            return false;
        }

        else if (x >= nColumns) {
            return false;
        }

        else if (x < 0 || y < 0) {
            return false;
        }

        return true;

    }

    @Override
    protected void render(GameRenderers renderers) {
        bg.render(screen, 0, 0, Corner.TOP_LEFT);

        for (int i = 0; i < nColumns; ++i) {
            for (int j = 0; j < nRowsMax; ++j) {
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