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
    private Button            itemsButton;
    private Button            partyButton;
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

    private boolean           isDay;
    private Sprite            dayNightIcon;
    private Sprite            goldIcon;
    private Text              playerGold;

    public WorldMapScene() {
        // Initialize the screen manager
        screen = GameComponents.get(ScreenManager.class).create();
        screen.open(Config.WINDOW_TITLE, Config.SCREEN_WIDTH,
                    Config.SCREEN_HEIGHT, Config.FULLSCREEN, Config.WINDOW_ICON);
        GameComponents.put(Screen.class, screen);

        InitializeTables();

        CheckDayNight();

        bg = assets.newSprite(Config.WORLD_BG);

        data = PlayerData.Load(assets, Config.PLAYER_SAVE);

        goldIcon = assets.newSprite(Config.GOLD_ICON);
        playerGold = assets.newText(Config.GOLD_FONT, "");
        playerGold.options(null, Config.GOLD_SIZE, true);

        itemsButton = new Button(
                                 assets,
                                 Config.BUTTON_LOOK,
                                 "Inventory",
                                 Color.white,
                                 (int) (Config.SCREEN_WIDTH / 2 + 0.5 * (35 + Config.BUTTON_X_WIDTH)),
                                 (int) (Config.SCREEN_HEIGHT * 0.95));

        partyButton = new Button(
                                 assets,
                                 Config.BUTTON_LOOK,
                                 "Party",
                                 Color.white,
                                 (int) (Config.SCREEN_WIDTH / 2 - 0.5 * (35 + Config.BUTTON_X_WIDTH)),
                                 (int) (Config.SCREEN_HEIGHT * 0.95));

        UpdateGold();

        CreateRegion(EnvironmentInformation.GetSSID());
    }

    @Override
    protected void update() {

        if (screen.isCloseRequested()) {
            PlayerData.Save(Config.PLAYER_SAVE, data);
            GameComponents.get(Game.class).quit();
        }

        else if (itemsButton.WasPressed()) {
            this.frozen = true;
            this.visible = true;
            GameComponents.get(Game.class).push(new PlayerMenuScene(data, PlayerMenuScene.INVENTORY));
        }

        else if (partyButton.WasPressed()) {
            this.frozen = true;
            this.visible = true;
            GameComponents.get(Game.class).push(new PlayerMenuScene(data, PlayerMenuScene.PARTY));
        }

        else
            for (int i = 0; i < areaButtons.size(); ++i) {
                if (areaButtons.get(i).WasPressed()) {
                    this.frozen = true;
                    this.visible = false;
                    GameComponents.get(Game.class).push(new BattleScene(data, areas[i], isDay));
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

        itemsButton.render(renderers);
        partyButton.render(renderers);

        goldIcon.render(screen, Config.SCREEN_HEIGHT * 0.02f,
                        Config.SCREEN_HEIGHT * 0.98f, Corner.BOTTOM_LEFT);
        playerGold.render(screen, Config.SCREEN_HEIGHT * 0.02f + goldIcon.getWidth() * 1.2f,
                          Config.SCREEN_HEIGHT * 0.99f, Corner.BOTTOM_LEFT);
        dayNightIcon.render(screen, Config.SCREEN_WIDTH - Config.SCREEN_HEIGHT * 0.02f,
                            Config.SCREEN_HEIGHT * 0.98f, Corner.BOTTOM_RIGHT);
    }

    @Override
    protected void wakeup(Object... args) {
        for (Button b : areaButtons) {
            b.Reset();
        }

        for (Button b : cityButtons) {
            b.Reset();
        }

        itemsButton.Reset();
        partyButton.Reset();

        UpdateGold();

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

    private void InitializeTables() {

        Item.InitTable();
        Classes.InitStats();
        Entity.InitNames();
        Entity.InitExp();
        Enemies.InitNames();
        Enemies.InitTable();
        Area.InitAreas();
        Area.InitEnemySets();
    }

    private void CheckDayNight() {

        isDay = EnvironmentInformation.IsDay();
        if (isDay) {
            dayNightIcon = assets.newSprite(Config.DAY_ICON);
        }
        else {
            dayNightIcon = assets.newSprite(Config.NIGHT_ICON);
        }
    }

    private void CreateRegion(String name) {
        regionName = name;
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

    private void UpdateGold() {

        String gold = new String();
        if (data.gold < 100000)
            gold += '0';
        if (data.gold < 10000)
            gold += '0';
        if (data.gold < 1000)
            gold += '0';
        if (data.gold < 100)
            gold += '0';
        if (data.gold < 10)
            gold += '0';
        gold += data.gold;

        playerGold.setText(gold);
    }
}
