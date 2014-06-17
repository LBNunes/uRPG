/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Luísa Bontempo Nunes
//     Created on 2014-05-29 ymd
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

import java.util.ArrayList;
import java.util.Random;

import org.unbiquitous.uImpala.engine.asset.Sprite;
import org.unbiquitous.uImpala.engine.asset.Text;
import org.unbiquitous.uImpala.engine.core.Game;
import org.unbiquitous.uImpala.engine.core.GameComponents;
import org.unbiquitous.uImpala.engine.core.GameRenderers;
import org.unbiquitous.uImpala.engine.core.GameScene;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.engine.io.ScreenManager;
import org.unbiquitous.uImpala.util.Color;
import org.unbiquitous.uImpala.util.Corner;

public class WorldMapScene extends GameScene {

    private Screen            screen;
    private GameRenderers     renderers;

    private PlayerData        data;

    private Sprite            bg;
    private Text              regionText;
    private int[]             areas;
    private String            regionName;
    private ArrayList<Button> areaButtons;
    private ArrayList<Button> cityButtons;
    private Point[]           locations = { new Point(314, 214),
                                        new Point(620, 592),
                                        new Point(1020, 378),
                                        new Point(534, 384),
                                        new Point(758, 184),
                                        new Point(370, 550),
                                        new Point(780, 385),
                                        new Point(950, 562),
                                        new Point(1060, 200),
                                        new Point(225, 378),
                                        new Point(130, 555),
                                        new Point(513, 200)
                                        };

    public WorldMapScene() {
        // Initialize the screen manager
        screen = GameComponents.get(ScreenManager.class).create();
        screen.open(Config.WINDOW_TITLE, Config.SCREEN_WIDTH,
                    Config.SCREEN_HEIGHT, Config.FULLSCREEN, Config.WINDOW_ICON);
        GameComponents.put(Screen.class, screen);

        Item.InitTable();
        Classes.InitStats();
        Entity.InitNames();
        Entity.InitExp();
        Enemies.InitNames();
        Enemies.InitTable();
        Area.InitAreas();
        Area.InitEnemySets();

        DayNightDetector.IsDay();

        bg = assets.newSprite(Config.WORLD_BG);

        // TODO: Load Save
        data = PlayerData.Load(assets, Config.PLAYER_SAVE);

        regionName = EnvironmentInformation.GetSSID();
        regionText = assets.newText(Config.WORLD_FONT, "The Lands of " + regionName);
        regionText.options(null, Config.WORLD_FONT_SIZE, true);
        areas = Area.GenerateAreaSet(regionName);

        ShuffleLocations();

        int i;

        areaButtons = new ArrayList<Button>();
        for (i = 0; i < areas.length; ++i) {
            Area a = Area.GetArea(areas[i]);
            Button b = new Button(assets, a.GetIconPath(), a.GetName(), Color.white, locations[i].x, locations[i].y);
            b.ShowTextOnMouseOver(true);
            areaButtons.add(b);
        }

        // TODO: Add city discovery
        cityButtons = new ArrayList<Button>();

    }

    @Override
    protected void update() {

        if (screen.isCloseRequested()) {
            PlayerData.Save(Config.PLAYER_SAVE, data);
            GameComponents.get(Game.class).quit();
        }

        for (int i = 0; i < areaButtons.size(); ++i) {
            if (areaButtons.get(i).WasPressed()) {
                this.frozen = true;
                this.visible = false;
                GameComponents.get(Game.class).push(new BattleScene(data, areas[i]));
            }
        }
    }

    @Override
    protected void render() {
        bg.render(screen, 0, 0, Corner.TOP_LEFT);

        regionText.render(screen, Config.SCREEN_WIDTH / 2, (float) (Config.SCREEN_HEIGHT * 0.066),
                          Corner.CENTER, 1.0f, 0.0f, 1.0f, 1.0f);

        for (Button b : areaButtons) {
            b.render(renderers);
        }

        for (Button b : cityButtons) {
            b.render(renderers);
        }
    }

    @Override
    protected void wakeup(Object... args) {
        for (Button b : areaButtons) {
            b.Reset();
        }

        for (Button b : areaButtons) {
            b.Reset();
        }

        this.frozen = false;
        this.visible = true;
    }

    @Override
    protected void destroy() {
        // TODO Auto-generated method stub

    }

    private void ShuffleLocations() {
        int nLocations = locations.length;
        Random generator = new Random(Area.StringHash(EnvironmentInformation.GetSSID()));

        for (int i = 0; i < 100; ++i) {
            int idx1 = (int) generator.nextInt(nLocations);
            int idx2 = (int) generator.nextInt(nLocations);
            Point p = locations[idx1];
            locations[idx1] = locations[idx2];
            locations[idx2] = p;
        }
    }
}
