/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Luísa Bontempo Nunes
//     Created on 2014-07-14 ymd
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
import game.Mission.Objective;
import game.Mission.VisitCityMission;
import game.PlayerData.KnownCity;
import game.WorldMapScene.CityWorldInfo;

import java.util.List;
import java.util.UUID;

import org.unbiquitous.uImpala.engine.asset.Sprite;
import org.unbiquitous.uImpala.engine.asset.Text;
import org.unbiquitous.uImpala.engine.core.Game;
import org.unbiquitous.uImpala.engine.core.GameComponents;
import org.unbiquitous.uImpala.engine.core.GameRenderers;
import org.unbiquitous.uImpala.engine.core.GameScene;
import org.unbiquitous.uImpala.engine.io.KeyboardEvent;
import org.unbiquitous.uImpala.engine.io.KeyboardSource;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.util.Color;
import org.unbiquitous.uImpala.util.Corner;
import org.unbiquitous.uImpala.util.observer.Event;
import org.unbiquitous.uImpala.util.observer.Observation;
import org.unbiquitous.uImpala.util.observer.Subject;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.adaptabitilyEngine.ServiceCallException;
import org.unbiquitous.uos.core.driverManager.DriverData;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;

public class CityScene extends GameScene {

    private Screen         screen;
    private KeyboardSource keyboard;
    private GameRenderers  renderers;
    private Gateway        gateway;

    private PlayerData     data;
    private CityWorldInfo  cityInfo;

    private Sprite         bg;
    private Text           cityName;
    private Text           cityAffinity;
    private Button         academyButton;
    private Button         marketButton;
    private Button         guildButton;
    private Button         smithyButton;
    private Button         itemsButton;
    private Button         partyButton;
    private Button         missionsButton;

    private boolean        isDay;
    private Sprite         dayNightIcon;
    private Sprite         goldIcon;
    private Sprite         energyIcon;
    private Text           playerGold;
    private Text           playerEnergy;
    private int            maxEnergy;

    public CityScene(CityWorldInfo info) {

        cityInfo = info;
        data = PlayerData.GetData();

        screen = GameComponents.get(Screen.class);
        keyboard = screen.getKeyboard();
        keyboard.connect(KeyboardSource.EVENT_KEY_DOWN, new Observation(this, "OnKeyDown"));
        gateway = GameComponents.get(Gateway.class);

        CheckDayNight();

        bg = assets.newSprite("img/bg/citybg.jpg");

        energyIcon = assets.newSprite(Config.ENERGY_ICON);
        maxEnergy = (int) (Config.BASE_ENERGY * EnvironmentInformation.GetFreeSpacePercentage());
        if (maxEnergy < 200) {
            maxEnergy = 200;
        }

        energyIcon = assets.newSprite(Config.ENERGY_ICON);
        maxEnergy = PlayerData.GetMaxEnergy();

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

        CreateFacilities();

        cityName = assets.newText(Config.WORLD_FONT, "The City of " + info.name);
        cityName.options(null, Config.WORLD_FONT_SIZE, true);
        cityAffinity = assets.newText(Config.WORLD_FONT, "Aligned with the " + info.areaName);
        cityAffinity.options(null, Config.WORLD_FONT_SIZE / 2, true);

        CheckEnergyRestore();
        PlayerData.DiscoverCity(info.uuid, info.name);
        PlayerData.Save();

        playerEnergy = assets.newText(Config.GOLD_FONT, "" + data.energy + " / " + maxEnergy);
        playerEnergy.options(null, Config.GOLD_SIZE, true);

        for (Mission m : data.missions) {
            if (m.objective == Objective.VISIT_CITY) {
                VisitCityMission mm = (VisitCityMission) m;
                if (mm.completed) {
                    continue;
                }

                if (mm.city.equals(info.uuid)) {
                    mm.completed = true;
                    TextLog.instance.Print("Completed a mission (visit the city of " + mm.cityName + ")", Color.white);
                }

            }
        }
    }

    @Override
    protected void update() {

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
        else if (academyButton.WasPressed()) {
            this.frozen = true;
            this.visible = false;
            GameComponents.get(Game.class).push(new AcademyScene(cityInfo));
        }
        else if (guildButton.WasPressed()) {
            this.frozen = true;
            this.visible = false;
            GameComponents.get(Game.class).push(new GuildScene(cityInfo));
        }
        else if (smithyButton.WasPressed()) {
            this.frozen = true;
            this.visible = false;
            GameComponents.get(Game.class).push(new SmithyScene(cityInfo));
        }
        else if (marketButton.WasPressed()) {
            this.frozen = true;
            this.visible = false;
            GameComponents.get(Game.class).push(new MarketScene(cityInfo));
        }

        TextLog.instance.Update();
        UpdateEnergy();
    }

    @Override
    protected void render() {
        bg.render(screen, 0, 0, Corner.TOP_LEFT);

        cityName.render(screen, Config.SCREEN_WIDTH / 2, (float) (Config.SCREEN_HEIGHT * 0.066),
                        Corner.CENTER, 1.0f, 0.0f, 1.0f, 1.0f);
        cityAffinity.render(screen, Config.SCREEN_WIDTH / 2, (float) (Config.SCREEN_HEIGHT * 0.15),
                            Corner.CENTER, 1.0f, 0.0f, 1.0f, 1.0f);

        guildButton.render(renderers);
        smithyButton.render(renderers);
        marketButton.render(renderers);
        academyButton.render(renderers);

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

        guildButton.Reset();
        smithyButton.Reset();
        academyButton.Reset();
        marketButton.Reset();

        itemsButton.Reset();
        partyButton.Reset();
        missionsButton.Reset();

        UpdateGold();

        this.frozen = false;
        this.visible = true;

        System.gc();
    }

    @Override
    protected void destroy() {
        // TODO Auto-generated method stub

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

            PlayerData.Save();
        }
    }

    private void CreateFacilities() {
        smithyButton = new Button(assets, "img/icon/smithy.png", "Smithy", Color.red, 420, 280);
        guildButton = new Button(assets, "img/icon/guild.png", "Guild", Color.red, 420, 540);
        marketButton = new Button(assets, "img/icon/market.png", "Market", Color.red, 860, 280);

        if (cityInfo.academyClass == ClassID.WARRIOR) {
            academyButton = new Button(assets, "img/icon/warrioracademy.png", "Warrior Academy", Color.red, 860, 540);
        }
        else if (cityInfo.academyClass == ClassID.ROGUE) {
            academyButton = new Button(assets, "img/icon/rogueacademy.png", "Rogue Academy", Color.red, 860, 540);
        }
        else {
            academyButton = new Button(assets, "img/icon/mageacademy.png", "Mage Academy", Color.red, 860, 540);
        }

        smithyButton.ShowTextOnMouseOver(true);
        guildButton.ShowTextOnMouseOver(true);
        marketButton.ShowTextOnMouseOver(true);
        academyButton.ShowTextOnMouseOver(true);
    }

    private void CheckEnergyRestore() {

        try {
            List<DriverData> cityDrivers = gateway.listDrivers("uRPG.cityDriver");
            if (cityDrivers == null || cityDrivers.size() == 0) {
                System.out.println("Error contacting city");
                return;
            }

            for (DriverData d : cityDrivers) {
                UpDevice device = d.getDevice();
                Call call = new Call("uRPG.cityDriver", "GetCityInfo");
                Response response = gateway.callService(device, call);

                UUID uuid = UUID.fromString(response.getResponseString("uuid"));

                if (uuid.equals(cityInfo.uuid)) {
                    int averageLevel = 0;
                    int totalLevel = 0;
                    String[] ss = new String[data.knownCities.size()];
                    int i;
                    for (i = 0; i < data.party.size() && i < 5; ++i) {
                        totalLevel += data.party.get(i).jobLevel;
                    }
                    averageLevel = totalLevel / i;

                    Object[] kcs = data.knownCities.toArray();

                    for (i = 0; i < kcs.length; ++i) {
                        ss[i] = ((KnownCity) kcs[i]).toString();
                    }

                    call = new Call("uRPG.cityDriver", "IntroducePlayer");
                    call.addParameter("uuid", data.uuid);
                    call.addParameter("averageLevel", "" + averageLevel);
                    call.addParameter("totalLevel", "" + totalLevel);
                    call.addParameter("knownCities", ss);

                    // There should be nothing in this response
                    response = gateway.callService(device, call);

                    call = new Call("uRPG.cityDriver", "RequestEnergyRestore");
                    call.addParameter("uuid", data.uuid);

                    response = gateway.callService(device, call);

                    boolean ok = Boolean.valueOf(response.getResponseString("confirmation"));

                    if (ok) {
                        data.energy += Config.ENERGY_PER_CITY_VISIT;
                        if (data.energy > maxEnergy) {
                            data.energy = maxEnergy;
                        }
                        TextLog.instance.Print("Energy restored!", Color.white);
                    }
                }
            }
        }
        catch (ServiceCallException e) {
            e.printStackTrace();
        }
    }

    public void OnKeyDown(Event event, Subject subject) {
        if (!frozen) {
            if (((KeyboardEvent) event).getKey() == 1) {
                frozen = true; // Prevents the pop from being called again if Esc is pressed...
                GameComponents.get(Game.class).pop();
            }
        }
    }
}
