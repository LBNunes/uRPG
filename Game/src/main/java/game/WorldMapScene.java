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

import game.Classes.ClassID;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.unbiquitous.uImpala.engine.asset.AssetManager;
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
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.adaptabitilyEngine.ServiceCallException;
import org.unbiquitous.uos.core.driverManager.DriverData;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;

public class WorldMapScene extends GameScene {

    private Screen                   screen;
    private GameRenderers            renderers;
    private Gateway                  gateway;

    private PlayerData               data;

    private Sprite                   bg;
    private Text                     regionText;
    private int[]                    areas;
    private String                   regionName;
    private ArrayList<Button>        areaButtons;
    private ArrayList<Button>        cityButtons;
    private Button                   itemsButton;
    private Button                   partyButton;
    private Button                   missionsButton;
    private Point[]                  locations   = { new Point(314, 214),
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

    private boolean                  isDay;
    private Sprite                   dayNightIcon;
    private Sprite                   goldIcon;
    private Sprite                   energyIcon;
    private Text                     playerGold;
    private Text                     playerEnergy;
    private int                      maxEnergy;
    private long                     lastCityRefresh;
    private ArrayList<CityWorldInfo> cities;

    public static AssetManager       worldAssets = null; // Kludge

    public WorldMapScene() {

        // Initialize the screen manager
        screen = GameComponents.get(ScreenManager.class).create();
        screen.open(Config.WINDOW_TITLE, Config.SCREEN_WIDTH,
                    Config.SCREEN_HEIGHT, Config.FULLSCREEN, Config.WINDOW_ICON);
        GameComponents.put(Screen.class, screen);
        gateway = GameComponents.get(Gateway.class);

        TextLog.instance.SetAssets(assets);

        CheckDayNight();

        bg = assets.newSprite(Config.WORLD_BG);

        data = PlayerData.GetData();
        for (Entity e : data.party) {
            e.LoadSprites(assets);
        }

        energyIcon = assets.newSprite(Config.ENERGY_ICON);
        maxEnergy = PlayerData.GetMaxEnergy();

        playerEnergy = assets.newText(Config.GOLD_FONT, "" + data.energy + " / " + maxEnergy);
        playerEnergy.options(null, Config.GOLD_SIZE, true);

        goldIcon = assets.newSprite(Config.GOLD_ICON);
        playerGold = assets.newText(Config.GOLD_FONT, "");
        playerGold.options(null, Config.GOLD_SIZE, true);

        itemsButton = new Button(assets, Config.BUTTON_LOOK, "Inventory", Color.white,
                                 (int) (Config.SCREEN_WIDTH / 2 - 1 * (35 + Config.BUTTON_X_WIDTH)),
                                 (int) (Config.SCREEN_HEIGHT * 0.95));

        partyButton = new Button(assets, Config.BUTTON_LOOK, "Party", Color.white,
                                 (int) (Config.SCREEN_WIDTH / 2),
                                 (int) (Config.SCREEN_HEIGHT * 0.95));

        missionsButton = new Button(assets, Config.BUTTON_LOOK, "Missions", Color.white,
                                    (int) (Config.SCREEN_WIDTH / 2 + 1 * (35 + Config.BUTTON_X_WIDTH)),
                                    (int) (Config.SCREEN_HEIGHT * 0.95));
        UpdateGold();

        CreateRegion(EnvironmentInformation.GetSSID());

        cities = new ArrayList<CityWorldInfo>();
        cityButtons = new ArrayList<Button>();
        lastCityRefresh = 0;
        RefreshCities(true);

        worldAssets = assets;
    }

    @Override
    protected void update() {

        RefreshCities(false);

        if (screen.isCloseRequested()) {
            PlayerData.Save();
            GameComponents.get(Game.class).quit();
        }

        else if (itemsButton.WasPressed()) {
            this.frozen = true;
            this.visible = true;
            GameComponents.get(Game.class).push(new PlayerMenuScene(PlayerMenuScene.INVENTORY));
        }

        else if (partyButton.WasPressed()) {
            this.frozen = true;
            this.visible = true;
            GameComponents.get(Game.class).push(new PlayerMenuScene(PlayerMenuScene.PARTY));
        }

        else if (missionsButton.WasPressed()) {
            this.frozen = true;
            this.visible = true;
            GameComponents.get(Game.class).push(new PlayerMenuScene(PlayerMenuScene.MISSIONS));
        }

        else {
            for (int i = 0; i < areaButtons.size(); ++i) {
                if (areaButtons.get(i).WasPressed()) {
                    if (data.energy >= Config.ENERGY_PER_BATTLE) {
                        this.frozen = true;
                        this.visible = false;
                        data.energy -= Config.ENERGY_PER_BATTLE;
                        PlayerData.Save();
                        GameComponents.get(Game.class).push(new BattleScene(areas[i], isDay));
                        return; // So it won't scan cities too
                    }
                    else {
                        TextLog.instance.Print("Not enough energy! You need " + Config.ENERGY_PER_BATTLE + ".",
                                               Color.white);
                        areaButtons.get(i).Reset();
                    }
                }
            }
            for (int i = 0; i < cityButtons.size(); ++i) {
                if (cityButtons.get(i).WasPressed()) {
                    // TODO: Check if city is still online
                    this.frozen = true;
                    this.visible = false;
                    GameComponents.get(Game.class).push(new CityScene(cities.get(i)));
                    return;
                }
            }
        }

        TextLog.instance.Update();
        UpdateEnergy();
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
        missionsButton.render(renderers);

        energyIcon.render(screen, Config.SCREEN_HEIGHT * 0.02f,
                          Config.SCREEN_HEIGHT * 0.98f, Corner.BOTTOM_LEFT);
        playerEnergy.render(screen, Config.SCREEN_HEIGHT * 0.02f + goldIcon.getWidth() * 1.2f,
                            Config.SCREEN_HEIGHT * 0.99f, Corner.BOTTOM_LEFT);
        goldIcon.render(screen, Config.SCREEN_HEIGHT * 0.02f,
                        Config.SCREEN_HEIGHT * 0.98f - goldIcon.getHeight() * 1.1f, Corner.BOTTOM_LEFT);
        playerGold.render(screen, Config.SCREEN_HEIGHT * 0.02f + goldIcon.getWidth() * 1.2f,
                          Config.SCREEN_HEIGHT * 0.99f - goldIcon.getHeight() * 1.1f, Corner.BOTTOM_LEFT);
        dayNightIcon.render(screen, Config.SCREEN_WIDTH - Config.SCREEN_HEIGHT * 0.02f,
                            Config.SCREEN_HEIGHT * 0.98f, Corner.BOTTOM_RIGHT);

        TextLog.instance.Render(Config.SCREEN_WIDTH / 2, (int) (0.9 * Config.SCREEN_HEIGHT), Corner.CENTER);

    }

    @Override
    protected void wakeup(Object... args) {
        for (Button b : areaButtons) {
            b.Reset();
        }

        RefreshCities(true);

        itemsButton.Reset();
        partyButton.Reset();
        missionsButton.Reset();

        UpdateGold();

        this.frozen = false;
        this.visible = true;

        TextLog.instance.SetAssets(assets);

        System.gc();
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

    private void UpdateEnergy() {
        if (data.energy < maxEnergy) {
            long currentTime = System.currentTimeMillis();
            int energy = (int) (currentTime - data.lastRefresh) / Config.MS_PER_ENERGY_POINT;
            if (energy > 0) {
                data.energy += energy;
                if (data.energy > maxEnergy) {
                    data.energy = maxEnergy;
                }
                data.lastRefresh = currentTime;
                playerEnergy.setText("" + data.energy + " / " + maxEnergy);
            }
        }
    }

    private void RefreshCities(boolean force) {
        long time = System.currentTimeMillis();
        if (force || time - lastCityRefresh > 10000) {
            lastCityRefresh = time;
            cities.clear();
            cityButtons.clear();
            try {
                List<DriverData> cityDrivers = gateway.listDrivers("uRPG.cityDriver");
                if (cityDrivers == null || cityDrivers.size() == 0) {
                    System.out.println("No cities connected.");
                    TextLog.instance.Print("No cities found yet...", Color.white);
                    return;
                }

                for (DriverData d : cityDrivers) {
                    UpDevice device = d.getDevice();
                    Call call = new Call("uRPG.cityDriver", "GetCityInfo");
                    Response response = gateway.callService(device, call);
                    UUID uuid = UUID.fromString(response.getResponseString("uuid"));
                    String name = response.getResponseString("name");
                    int area = Integer.parseInt(response.getResponseString("area"));
                    cities.add(new CityWorldInfo(uuid, name, area));
                }

                for (int i = areaButtons.size(); i < locations.length && i - areaButtons.size() < cities.size(); ++i) {
                    CityWorldInfo info = cities.get(i - areaButtons.size());
                    Button b = new Button(assets, "img/icon/cityicon.png", "The City of " + info.name,
                                          Color.white, locations[i].x, locations[i].y);
                    b.ShowTextOnMouseOver(true);
                    cityButtons.add(b);
                    TextLog.instance.Print("Found the city of " + info.name + "!", Color.white);

                }
            }
            catch (ServiceCallException e) {
                e.printStackTrace();
            }
        }
    }

    public class CityWorldInfo {
        UUID    uuid;
        String  name;
        int     areaID;
        String  areaName;
        ClassID academyClass;

        public CityWorldInfo(UUID _uuid, String _name, int _areaID) {
            uuid = _uuid;
            name = _name;
            areaID = _areaID;
            areaName = Area.GetArea(areaID).GetName();
            academyClass = Area.GetArea(areaID).GetClassBias();
        }
    }
}
