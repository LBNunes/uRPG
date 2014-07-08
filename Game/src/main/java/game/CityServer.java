/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Lu�sa Bontempo Nunes
//     Created on 2014-06-14 ymd
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

import java.util.List;
import java.util.UUID;

import org.unbiquitous.uImpala.engine.asset.Text;
import org.unbiquitous.uImpala.engine.core.Game;
import org.unbiquitous.uImpala.engine.core.GameComponents;
import org.unbiquitous.uImpala.engine.core.GameScene;
import org.unbiquitous.uImpala.engine.io.KeyboardEvent;
import org.unbiquitous.uImpala.engine.io.KeyboardSource;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.engine.io.ScreenManager;
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

public class CityServer extends GameScene {

    private CityData       data;
    private Gateway        gateway;
    private Screen         screen;
    private KeyboardSource keyboard;

    private Text           uRPG;
    private Text           cityName;
    private Text           pressEsc;

    private long           lastRefresh;

    public CityServer() {

        screen = GameComponents.get(ScreenManager.class).create();
        screen.open("uRPG", 200, 100, false, Config.WINDOW_ICON);
        GameComponents.put(Screen.class, screen);
        keyboard = screen.getKeyboard();
        keyboard.connect(KeyboardSource.EVENT_KEY_DOWN, new Observation(this, "OnKeyDown"));

        data = CityData.GetData();

        uRPG = assets.newText("font/seguisb.ttf", "uRPG");
        cityName = assets.newText("font/seguisb.ttf", "The City of " + data.name);
        pressEsc = assets.newText("font/seguisb.ttf", "Press ESC to quit");

        gateway = GameComponents.get(Gateway.class);

        lastRefresh = 0;
    }

    @Override
    protected void update() {

        if (screen.isCloseRequested()) {
            CityData.Save();
            GameComponents.get(Game.class).quit();
        }

        long time = System.currentTimeMillis();
        if (time - lastRefresh > 15000) {
            lastRefresh = time;
            try {
                List<DriverData> userDrivers = gateway.listDrivers("uRPG.userDriver");
                if (userDrivers == null || userDrivers.size() == 0) {
                    System.out.println("No users connected.");
                    return;
                }
                UpDevice device = userDrivers.get(0).getDevice();
                Call call = new Call("uRPG.userDriver", "GetUserInfo");
                Response response = gateway.callService(device, call);
                UUID uuid = (UUID) response.getResponseData("uuid");
                String leader = (String) response.getResponseData("leaderName");
                ClassID classID = (ClassID) response.getResponseData("leaderClass");
                System.out.println("Found party led by " + leader + ", the " + Classes.GetClassName(classID));

            }
            catch (ServiceCallException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void render() {
        uRPG.render(screen, 100, 20, Corner.CENTER, 1.0f, 0.0f, 1.0f, 1.0f, Color.white);
        cityName.render(screen, 100, 50, Corner.CENTER, 1.0f, 0.0f, 1.0f, 1.0f, Color.white);
        pressEsc.render(screen, 100, 80, Corner.CENTER, 1.0f, 0.0f, 1.0f, 1.0f, Color.white);
    }

    @Override
    protected void wakeup(Object... args) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void destroy() {
        // TODO Auto-generated method stub

    }

    @SuppressWarnings("unused")
    private void OnKeyDown(Event event, Subject subject) {
        KeyboardEvent e = (KeyboardEvent) event;
        if (e.getKey() == 1) {
            CityData.Save();
            GameComponents.get(Game.class).quit();
        }
    }
}
