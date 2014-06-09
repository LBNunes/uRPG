/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Luísa Bontempo Nunes
//     Created on 2014-05-25 ymd
//
// X11 Licensed Code
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
//
/////////////////////////////////////////////////////////////////////////

package game;

import game.WorldMapScene.WorldArea;

import java.util.ArrayList;

import org.unbiquitous.uImpala.engine.asset.AssetManager;
import org.unbiquitous.uImpala.engine.asset.Sprite;
import org.unbiquitous.uImpala.engine.core.GameComponents;
import org.unbiquitous.uImpala.engine.core.GameObject;
import org.unbiquitous.uImpala.engine.core.GameRenderers;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.util.Corner;

public class Grid extends GameObject {

    private Sprite    bg;
    private Sprite    clearHex;
    private Sprite    redHex;
    private Sprite    greenHex;
    private Sprite    blueHex;
    private Sprite    yellowHex;

    private Screen    screen;

    static final int  hexRadius   = 58;
    static final int  hexHeight   = 50;
    static final int  hexSide     = 29;
    static final int  hexXOffset  = hexHeight * 2;
    static final int  hexYOffset  = hexRadius + hexSide;

    static final int  gridOffsetX = 40;
    static final int  gridOffsetY = 56;

    static final int  topHexX     = gridOffsetX + hexHeight;
    static final int  topHexY     = gridOffsetY + hexRadius;

    static final int  nColumnsMax = 12;
    static final int  nColumnsMin = 11;
    static final int  nRows       = 7;

    private HexColors gridColors[][];

    public enum HexColors {
        CLEAR, RED, GREEN, BLUE, YELLOW
    };

    public enum GridArea {
        SINGLE_HEX, CIRCLE, DIAGONAL_UP_LINE, DIAGONAL_DOWN_LINE, HORIZONTAL_LINE, ALL
    }

    public Grid(AssetManager assets, WorldArea area) {
        bg = LoadBG(assets, area);
        clearHex = assets.newSprite(Config.CLEAR_HEX);
        redHex = assets.newSprite(Config.RED_HEX);
        greenHex = assets.newSprite(Config.GREEN_HEX);
        blueHex = assets.newSprite(Config.BLUE_HEX);
        yellowHex = assets.newSprite(Config.YELLOW_HEX);

        gridColors = new HexColors[nColumnsMax][nRows];
        ClearColors(HexColors.CLEAR);

        screen = GameComponents.get(Screen.class);

        System.out.println("Created Grid.");
    }

    public void ClearColors() {
        ClearColors(HexColors.CLEAR);
    }

    public void ClearColors(HexColors fill) {
        for (int i = 0; i < nColumnsMax; ++i) {
            for (int j = 0; j < nRows; ++j) {
                gridColors[i][j] = fill;
            }
        }
    }

    public void RenderAtHex(Sprite s, int x, int y) {
        RenderAtHex(s, x, y, 1.0f);
    }

    public void RenderAtHex(Sprite s, int x, int y, float opacity) {
        Point p = FindHexPosition(x, y);
        if (p != null)
            s.render(screen, p.x, p.y, Corner.CENTER, opacity);
    }

    public Point FindHexPosition(int x, int y) {

        if (!ValidHexPosition(x, y))
            return null;

        Point pos = new Point((int) (topHexX + x * hexXOffset), (int) (topHexY + y * hexYOffset));

        if (y % 2 == 1)
            pos.x += hexHeight;

        return pos;
    }

    public Point FindHexByPixel(int x, int y) {

        // More info about this method:
        // http://stackoverflow.com/questions/7705228/hexagonal-grids-how-do-you-find-which-hexagon-a-point-is-in
        int brickWidth = (2 * hexHeight);
        int brickHeight = (hexSide * 2 + (hexRadius - hexSide));

        int row, column;

        // First, simplify the grid into a "wall of bricks", and find the row and column that the pixel is in
        row = (y - gridOffsetY) / brickHeight;

        if (row % 2 == 1) {
            column = (x - gridOffsetX - hexHeight) / brickWidth;
        }
        else {
            column = (x - gridOffsetX) / brickWidth;
        }

        // Next, find the position of the pixel relative to the brick's top-left corner
        int relX, relY;

        relY = y - gridOffsetY - (row * brickHeight);

        if (row % 2 == 1) {
            relX = x - gridOffsetX - (column * brickWidth) - hexHeight;
        }
        else {
            relX = x - gridOffsetX - (column * brickWidth);
        }

        // Now, analyze that position to discover whether the pixel "escapes" to the upper hex
        // This is through the line equation y = +-m * x + c
        int c = hexRadius - hexSide;
        double m = c / (double) hexHeight;
        // Left triangle
        if (relX < hexHeight + 1) {
            if (relY < (-m * relX) + c) {
                row -= 1;
                if (row % 2 == 1) {
                    column -= 1;
                }
            }
        }
        // Right triangle
        else {
            if (relY < (m * relX) - c) {
                row -= 1;
                if (row % 2 == 0) {
                    column += 1;
                }
            }
        }

        Point pos = new Point(column, row);

        return pos;
    }

    public boolean ValidHexPosition(int x, int y) {
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

    public void ColorArea(int x, int y, GridArea area, HexColors fill, int complement) {

        if (!ValidHexPosition(x, y) && area != GridArea.ALL)
            return;

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
                    ColorArea(x, y + 1, area, fill, complement - 1);
                    ColorArea(x, y - 1, area, fill, complement - 1);
                    ColorArea(x + 1, y, area, fill, complement - 1);
                    ColorArea(x - 1, y, area, fill, complement - 1);

                    if (y % 2 == 1) {
                        ColorArea(x + 1, y + 1, area, fill, complement - 1);
                        ColorArea(x + 1, y - 1, area, fill, complement - 1);
                    }
                    else {
                        ColorArea(x - 1, y + 1, area, fill, complement - 1);
                        ColorArea(x - 1, y - 1, area, fill, complement - 1);
                    }
                }
                break;

            // Line with an inclination of +30°, starting in x, y. Complement specifies length.
            case DIAGONAL_UP_LINE:
                gridColors[x][y] = fill;
                if (complement > 0) {
                    if (y % 2 == 1) {
                        ColorArea(x + 1, y - 1, area, fill, complement - 1);
                    }
                    else {
                        ColorArea(x, y - 1, area, fill, complement - 1);
                    }
                }
                // If it's less than 0, fill the hex (-1, +1)
                else if (complement < 0) {
                    if (y % 2 == 1) {
                        ColorArea(x, y + 1, area, fill, complement + 1);
                    }
                    else {
                        ColorArea(x - 1, y + 1, area, fill, complement + 1);
                    }
                }
                break;
            // Line with an inclination of -30°, starting in x, y. Complement specifies length.
            case DIAGONAL_DOWN_LINE:
                gridColors[x][y] = fill;
                if (complement > 0) {
                    if (y % 2 == 1) {
                        ColorArea(x + 1, y + 1, area, fill, complement - 1);
                    }
                    else {
                        ColorArea(x, y + 1, area, fill, complement - 1);
                    }
                }
                // If it's less than 0, fill the hex (-1, +1)
                else if (complement < 0) {
                    if (y % 2 == 1) {
                        ColorArea(x, y - 1, area, fill, complement + 1);
                    }
                    else {
                        ColorArea(x - 1, y - 1, area, fill, complement + 1);
                    }
                }
                break;
            // Color the line going up or down, starting at x, y. Complement specifies length.
            case HORIZONTAL_LINE:
                gridColors[x][y] = fill;
                // If the length is more than 0, fill the hex (+1, +0)
                if (complement > 0) {
                    ColorArea(x + 1, y, area, fill, complement - 1);
                }
                // If it's less than 0, fill the hex (-1, +0)
                else if (complement < 0) {
                    ColorArea(x - 1, y, area, fill, complement + 1);
                }
                break;
            // Color the whole grid - Complement is ignored.
            case ALL:
                ClearColors(fill);
                break;
        }
    }

    public void ColorHexes(ArrayList<Point> hexes, HexColors fill, boolean clear) {
        if (clear) {
            ClearColors(HexColors.CLEAR);
        }
        for (Point p : hexes) {
            ColorArea(p.x, p.y, GridArea.SINGLE_HEX, fill, 0);
        }
    }

    @Override
    public void render(GameRenderers renderers) {
        bg.render(screen, 0, 0, Corner.TOP_LEFT);

        for (int i = 0; i < nColumnsMax; ++i) {
            for (int j = 0; j < nRows; ++j) {
                switch (gridColors[i][j]) {
                    case RED:
                        RenderAtHex(redHex, i, j, 0.60f);
                        break;
                    case GREEN:
                        RenderAtHex(greenHex, i, j, 0.60f);
                        break;
                    case BLUE:
                        RenderAtHex(blueHex, i, j, 0.60f);
                        break;
                    case YELLOW:
                        RenderAtHex(yellowHex, i, j, 0.60f);
                        break;
                    case CLEAR:
                        RenderAtHex(clearHex, i, j);
                        break;
                }
            }
        }
    }

    private Sprite LoadBG(AssetManager assets, WorldArea area) {
        switch (area) {
            case GRASSLAND:
                return assets.newSprite(Config.GRASS_BG);
            default:
                return null;
        }
    }

    @Override
    public void update() {

    }

    @Override
    protected void wakeup(Object... args) {
    }

    @Override
    protected void destroy() {
    }
}