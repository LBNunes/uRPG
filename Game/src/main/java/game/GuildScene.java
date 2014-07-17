package game;

import game.Mission.FetchMission;
import game.Mission.Objective;
import game.WorldMapScene.CityWorldInfo;

import java.util.ArrayList;
import java.util.Iterator;
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

public class GuildScene extends GameScene {

    private Screen         screen;
    private GameRenderers  renderers;
    private KeyboardSource keyboard;
    private Gateway        gateway;

    private PlayerData     data;
    private CityWorldInfo  cityInfo;

    private Sprite         bg;
    private Text           guildText;
    private Button         acceptButton;
    private Button         proposeButton;

    private MissionWindow  missionWindow;

    public GuildScene(CityWorldInfo info) {

        cityInfo = info;
        data = PlayerData.GetData();

        screen = GameComponents.get(Screen.class);
        keyboard = screen.getKeyboard();
        keyboard.connect(KeyboardSource.EVENT_KEY_DOWN, new Observation(this, "OnKeyDown"));
        gateway = GameComponents.get(Gateway.class);

        bg = assets.newSprite("img/bg/citybg.jpg");

        guildText = assets.newText(Config.WORLD_FONT, "Guild");
        guildText.options(null, Config.WORLD_FONT_SIZE, true);

        acceptButton = new Button(assets, Config.BUTTON_LOOK, "Accept", Color.white,
                                  (int) (Config.SCREEN_WIDTH * 0.33), Config.SCREEN_HEIGHT / 2);
        proposeButton = new Button(assets, Config.BUTTON_LOOK, "Propose", Color.white,
                                   (int) (Config.SCREEN_WIDTH * 0.66), Config.SCREEN_HEIGHT / 2);

        missionWindow = null;

        Iterator<Mission> i = data.missions.iterator();
        while (i.hasNext()) {
            Mission m = i.next();
            if (m.guildCityUUID.equals(info.uuid)) {
                if (m.completed) {
                    boolean ok;

                    try {
                        ok = RequestReward(m);
                        if (ok) {
                            TextLog.instance.Print("Obtained " + m.reward + " from completed mission!", Color.white);
                            data.gold += m.reward;
                        }
                        else {
                            TextLog.instance.Print("One of your missions has already been completed by another player...",
                                                   Color.white);
                        }
                        i.remove();
                        PlayerData.Save();
                    }
                    catch (Exception e) {
                        TextLog.instance.Print("Failed to contact city.", Color.red);
                        break;
                    }
                }
                else if (m.objective == Objective.FETCH) {
                    FetchMission mm = (FetchMission) m;

                    if (data.inventory.HasItem(mm.itemID)) {
                        boolean ok;
                        try {
                            ok = RequestReward(mm);
                            if (ok) {
                                TextLog.instance.Print("Obtained " + m.reward + " from completed mission!", Color.white);
                                data.gold += m.reward;
                                data.inventory.TakeItem(mm.itemID, 1);
                            }

                            else {
                                TextLog.instance.Print("One of your missions has already been completed by another player...",
                                                       Color.white);
                            }
                            i.remove();
                            PlayerData.Save();
                        }
                        catch (Exception e) {
                            TextLog.instance.Print("Failed to contact city.", Color.red);
                            break;
                        }
                    }
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

        if (missionWindow != null) {
            missionWindow.update();
            Mission m = missionWindow.GetSelectedMission();
            if (m != null) {
                if (RequestMission(m)) {
                    data.missions.add(m);
                    TextLog.instance.Print("Successfully took on a mission!", Color.white);
                    PlayerData.Save();
                }
                else {
                    TextLog.instance.Print("You cannot take a new mission yet.", Color.red);
                }
                missionWindow = null;
                ShowButtons();
            }
        }
        else if (acceptButton.WasPressed()) {
            ArrayList<Mission> missions = RequestMissionList();
            missionWindow = new MissionWindow(assets, "img/window.png", 0, 0, missions);
            HideButtons();
        }
        else if (proposeButton.WasPressed()) {
            // TODO: LOL
        }

        TextLog.instance.Update();
    }

    @Override
    protected void render() {
        bg.render(screen, 0, 0, Corner.TOP_LEFT);

        guildText.render(screen, Config.SCREEN_WIDTH / 2, (float) (Config.SCREEN_HEIGHT * 0.066),
                         Corner.CENTER, 1.0f, 0.0f, 1.0f, 1.0f);

        acceptButton.render(renderers);
        proposeButton.render(renderers);

        if (missionWindow != null) {
            missionWindow.render(renderers);
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

    private ArrayList<Mission> RequestMissionList() {

        UpDevice device = FindCity();
        if (device == null) {
            System.out.println("Error contacting city.");
            return null;
        }

        try {
            ArrayList<Mission> missions = new ArrayList<Mission>();
            Call call = new Call("uRPG.cityDriver", "RequestMissionList");
            Response response = gateway.callService(device, call);
            ArrayList<String> missionstxt = (ArrayList<String>) response.getResponseData("missions");
            for (String s : missionstxt) {
                missions.add(Mission.FromString(s));
            }
            return missions;

        }
        catch (ServiceCallException e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean RequestMission(Mission m) {
        UpDevice device = FindCity();
        if (device == null) {
            System.out.println("Error contacting city");
            return false;
        }
        try {
            Call call = new Call("uRPG.cityDriver", "RequestMission");
            call.addParameter("missionID", m.missionID);
            call.addParameter("playerUUID", data.uuid);
            Response response = gateway.callService(device, call);
            return Boolean.valueOf(response.getResponseString("confirmation"));
        }
        catch (ServiceCallException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    private boolean RequestReward(Mission m) throws Exception {

        UpDevice device = FindCity();
        if (device == null) {
            System.out.println("Error contacting city");
            throw new Exception("Error contacting city");
        }
        try {
            Call call = new Call("uRPG.cityDriver", "RequestReward");
            call.addParameter("missionID", m.missionID);
            Response response = gateway.callService(device, call);
            return Boolean.valueOf(response.getResponseString("confirmation"));
        }
        catch (ServiceCallException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    private void ShowButtons() {
        acceptButton.Show();
        proposeButton.Show();
    }

    private void HideButtons() {
        acceptButton.Hide();
        proposeButton.Hide();
    }

    public void OnKeyDown(Event event, Subject subject) {
        if (!frozen) {
            if (((KeyboardEvent) event).getKey() == 1) {
                if (missionWindow != null) {
                    missionWindow = null;
                    ShowButtons();
                }
                else {
                    frozen = true; // Prevents the pop from being called again if Esc is pressed...
                    GameComponents.get(Game.class).pop();
                }
            }
        }
    }
}
