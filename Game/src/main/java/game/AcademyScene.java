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
import game.WorldMapScene.CityWorldInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
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

public class AcademyScene extends GameScene {

    private Screen         screen;
    private GameRenderers  renderers;
    private KeyboardSource keyboard;
    private Gateway        gateway;

    private PlayerData     data;
    private CityWorldInfo  cityInfo;

    private Sprite         bg;
    private Text           academyText;
    private Button         recruitButton;
    private Button         promoteButton;
    private Button         abilityButton;

    private ClassID        promotion1;
    private ClassID        promotion2;
    private Button         promotion1Button;
    private Button         promotion2Button;

    private EntityWindow   entityWindow;
    private AbilityWindow  abilityWindow;

    boolean                recruiting;

    public AcademyScene(CityWorldInfo info) {

        cityInfo = info;
        data = PlayerData.GetData();

        screen = GameComponents.get(Screen.class);
        keyboard = screen.getKeyboard();
        keyboard.connect(KeyboardSource.EVENT_KEY_DOWN, new Observation(this, "OnKeyDown"));
        gateway = GameComponents.get(Gateway.class);

        bg = assets.newSprite("img/bg/citybg.jpg");

        academyText = assets.newText(Config.WORLD_FONT, Classes.GetClassName(info.academyClass) + " Academy");
        academyText.options(null, Config.WORLD_FONT_SIZE, true);

        recruitButton = new Button(assets, Config.BUTTON_LOOK, "Recruit", Color.white,
                                   Config.SCREEN_WIDTH / 4, Config.SCREEN_HEIGHT / 2);
        promoteButton = new Button(assets, Config.BUTTON_LOOK, "Promote", Color.white,
                                   2 * Config.SCREEN_WIDTH / 4, Config.SCREEN_HEIGHT / 2);
        abilityButton = new Button(assets, Config.BUTTON_LOOK, "Learn Abilities", Color.white,
                                   3 * Config.SCREEN_WIDTH / 4, Config.SCREEN_HEIGHT / 2);

        promotion1 = Classes.GetPromotion1(info.academyClass);
        promotion2 = Classes.GetPromotion2(info.academyClass);

        promotion1Button = new Button(assets, Config.BUTTON_LOOK, Classes.GetClassName(promotion1), Color.white,
                                      3 * Config.SCREEN_WIDTH / 4, Config.SCREEN_HEIGHT / 3);
        promotion2Button = new Button(assets, Config.BUTTON_LOOK, Classes.GetClassName(promotion2), Color.white,
                                      3 * Config.SCREEN_WIDTH / 4, 2 * Config.SCREEN_HEIGHT / 3);
        HideButtons2();

        entityWindow = null;
        abilityWindow = null;
    }

    @Override
    protected void update() {

        if (screen.isCloseRequested()) {
            PlayerData.Save();
            GameComponents.get(Game.class).quit();
        }

        if (abilityWindow != null) {
            abilityWindow.update();
            if (entityWindow != null) {
                entityWindow.update();
                Entity e = entityWindow.GetSelectedEntity();
                if (e != null) {
                    Ability a = abilityWindow.GetSelectedAbility();
                    data.gold -= a.rank * 100;
                    e.abilities.add(a);
                    TextLog.instance.Print(e.name + " learned " + a.name, Color.white);
                    abilityWindow = null;
                    entityWindow = null;
                    ShowButtons();
                }
            }
            else {
                Ability a = abilityWindow.GetSelectedAbility();
                if (a != null) {
                    if (data.gold > a.rank * 100) {
                        ArrayList<Entity> list = new ArrayList<Entity>();
                        for (Entity e : data.party) {
                            if (Classes.IsClassCompatible(e.classID, a.classID) &&
                                a.rank <= e.jobLevel &&
                                !e.abilities.contains(a)) {
                                list.add(e);
                            }
                        }
                        entityWindow = new EntityWindow(assets, "img/window.png", 0, 0, list, false, false, null);
                        abilityWindow.Freeze();
                    }
                    else {
                        TextLog.instance.Print("Not enough gold.", Color.red);
                        abilityWindow.Reset();
                    }
                }
            }
        }

        else if (entityWindow != null) {
            entityWindow.update();
            Entity e = entityWindow.GetSelectedEntity();
            if (e != null) {
                if (recruiting) {
                    if (data.gold >= 500 + 150 * e.jobLevel) {
                        if (RequestRecruit(e)) {
                            data.party.add(e);
                            TextLog.instance.Print("Successfully recruited " + e.name + "!", Color.white);
                            data.gold -= 500 + 150 * e.jobLevel;
                        }
                        else {
                            TextLog.instance.Print("Please reload the recruits screen.", Color.red);
                        }
                        entityWindow = null;
                        ShowButtons();
                    }
                    else {
                        TextLog.instance.Print("Not enough gold.", Color.red);
                        entityWindow.Reset();
                    }
                }
                else {
                    if (promotion1Button.WasPressed()) {
                        e.Promote(WorldMapScene.worldAssets, promotion1);
                        data.gold -= Config.PROMOTION_COST;
                        TextLog.instance.Print(e.name + " was successfully promoted to the " +
                                               Classes.GetClassName(promotion1) + " class!", Color.white);
                        entityWindow = null;
                        ShowButtons();
                        HideButtons2();
                    }
                    else if (promotion1Button.WasPressed()) {
                        e.Promote(WorldMapScene.worldAssets, promotion2);
                        data.gold -= Config.PROMOTION_COST;
                        TextLog.instance.Print(e.name + " was successfully promoted to the " +
                                               Classes.GetClassName(promotion2) + " class!", Color.white);
                        entityWindow = null;
                        ShowButtons();
                        HideButtons2();
                    }
                }
            }
        }

        else if (recruitButton.WasPressed()) {
            ArrayList<Entity> recruits = GetRecruits();
            ArrayList<Integer> costs = new ArrayList<Integer>();
            if (recruits == null) {
                TextLog.instance.Print("Error contacting city.", Color.white);
                recruitButton.Reset();
            }
            else {
                for (Entity e : recruits) {
                    costs.add(500 + 150 * e.jobLevel);
                }
                entityWindow = new EntityWindow(assets, "img/window.png", 0, 0, recruits, false, false, costs);
                recruiting = true;
                HideButtons();
            }
        }
        else if (promoteButton.WasPressed()) {
            if (data.gold < Config.PROMOTION_COST) {
                TextLog.instance.Print("Not enough gold for promoting", Color.red);
                promoteButton.Reset();
            }
            else {
                ArrayList<Entity> list = new ArrayList<Entity>();
                ArrayList<Integer> costs = new ArrayList<Integer>();

                for (Entity e : data.party) {
                    if (e.classID == cityInfo.academyClass && e.jobLevel > Config.LEVEL_FOR_PROMOTION) {
                        list.add(e);
                    }
                }
                for (Entity e : list) {
                    costs.add(Config.PROMOTION_COST);
                }
                entityWindow = new EntityWindow(assets, "img/window.png", 0, 0, list, false, false, costs);
                recruiting = false;
                HideButtons();
                ShowButtons2();
            }
        }
        else if (abilityButton.WasPressed()) {
            ArrayList<Ability> abilities = Ability.GetAbility(new Predicate<Ability>() {
                public boolean Eval(Ability a) {
                    return Classes.IsClassCompatible(a.classID, cityInfo.academyClass);
                }
            });
            ArrayList<Integer> costs = new ArrayList<Integer>();
            for (Ability a : abilities) {
                costs.add(a.rank * 100);
            }
            abilityWindow = new AbilityWindow(assets, "img/window.png", 0, 0, abilities,
                                              new Predicate<Ability>() {
                                                  public boolean Eval(Ability a) {
                                                      return true;
                                                  }
                                              }, costs);
            HideButtons();
        }

        TextLog.instance.Update();
    }

    @Override
    protected void render() {
        bg.render(screen, 0, 0, Corner.TOP_LEFT);

        academyText.render(screen, Config.SCREEN_WIDTH / 2, (float) (Config.SCREEN_HEIGHT * 0.066),
                           Corner.CENTER, 1.0f, 0.0f, 1.0f, 1.0f);

        recruitButton.render(renderers);
        promoteButton.render(renderers);
        abilityButton.render(renderers);
        promotion1Button.render(renderers);
        promotion2Button.render(renderers);

        if (abilityWindow != null) {
            abilityWindow.render(renderers);
        }

        if (entityWindow != null) {
            entityWindow.render(renderers);
        }

        TextLog.instance.Render(Config.SCREEN_WIDTH / 2, (int) (0.9 * Config.SCREEN_HEIGHT), Corner.CENTER);
    }

    @Override
    protected void wakeup(Object... args) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void destroy() {
        // TODO Auto-generated method stub

    }

    private UpDevice FindCity() {
        try {
            List<DriverData> cityDrivers = gateway.listDrivers("uRPG.cityDriver");
            if (cityDrivers == null || cityDrivers.size() == 0) {
                System.out.println("Error contacting city");
                return null;
            }

            for (DriverData d : cityDrivers) {
                UpDevice device = d.getDevice();
                Call call = new Call("uRPG.cityDriver", "GetCityInfo");
                Response response = gateway.callService(device, call);

                UUID uuid = UUID.fromString(response.getResponseString("uuid"));

                if (uuid.equals(cityInfo.uuid)) {
                    return device;
                }
            }
        }
        catch (ServiceCallException e) {
            e.printStackTrace();
        }

        return null;
    }

    private ArrayList<Entity> GetRecruits() {

        UpDevice device = FindCity();
        if (device == null) {
            System.out.println("Error contacting city.");
            return null;
        }

        try {
            ArrayList<Entity> recruits = new ArrayList<Entity>();
            Call call = new Call("uRPG.cityDriver", "RequestRecruitList");
            Response response = gateway.callService(device, call);
            ArrayList<String> recruitstxt = (ArrayList<String>) response.getResponseData("recruits");
            for (String s : recruitstxt) {
                StringTokenizer tokenizer = new StringTokenizer(s, " ");
                Entity e = new Entity(tokenizer.nextToken(),
                                      ClassID.valueOf(tokenizer.nextToken()),
                                      Integer.parseInt(tokenizer.nextToken()));
                recruits.add(e);
                e.LoadSprites(WorldMapScene.worldAssets);
            }
            return recruits;

        }
        catch (ServiceCallException e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean RequestRecruit(Entity e) {
        UpDevice device = FindCity();
        if (device == null) {
            System.out.println("Error contacting city");
            return false;
        }
        try {
            Call call = new Call("uRPG.cityDriver", "RequestRecruit");
            call.addParameter("name", e.name);
            Response response = gateway.callService(device, call);
            return Boolean.valueOf(response.getResponseString("confirmation"));
        }
        catch (ServiceCallException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    private void ShowButtons() {
        recruitButton.Show();
        promoteButton.Show();
        abilityButton.Show();
    }

    private void HideButtons() {
        recruitButton.Hide();
        promoteButton.Hide();
        abilityButton.Hide();
    }

    private void ShowButtons2() {
        promotion1Button.Show();
        promotion2Button.Show();
    }

    private void HideButtons2() {
        promotion1Button.Hide();
        promotion2Button.Hide();
    }

    public void OnKeyDown(Event event, Subject subject) {
        if (!frozen) {
            if (((KeyboardEvent) event).getKey() == 1) {
                if (abilityWindow != null || entityWindow != null) {
                    abilityWindow = null;
                    entityWindow = null;
                    ShowButtons();
                    HideButtons2();
                }
                else {
                    frozen = true; // Prevents the pop from being called again if Esc is pressed...
                    GameComponents.get(Game.class).pop();
                }
            }
        }
    }

}
