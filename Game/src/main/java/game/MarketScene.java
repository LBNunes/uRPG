package game;

import game.CityData.Transaction;
import game.WorldMapScene.CityWorldInfo;

import java.util.ArrayList;
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

public class MarketScene extends GameScene {

    private Screen                 screen;
    private GameRenderers          renderers;
    private KeyboardSource         keyboard;
    private Gateway                gateway;

    private PlayerData             data;
    private CityWorldInfo          cityInfo;

    private Sprite                 bg;
    private Text                   marketText;
    private Button                 buyButton;
    private Button                 sellButton;

    private ItemWindow             itemWindow;
    private TextInput              textInput;
    private ArrayList<Transaction> transactions;

    public MarketScene(CityWorldInfo info) {

        cityInfo = info;
        data = PlayerData.GetData();

        screen = GameComponents.get(Screen.class);
        keyboard = screen.getKeyboard();
        keyboard.connect(KeyboardSource.EVENT_KEY_DOWN, new Observation(this, "OnKeyDown"));
        gateway = GameComponents.get(Gateway.class);

        bg = assets.newSprite("img/bg/citybg.jpg");

        marketText = assets.newText(Config.WORLD_FONT, "Market");
        marketText.options(null, Config.WORLD_FONT_SIZE, true);

        buyButton = new Button(assets, Config.BUTTON_LOOK, "Buy", Color.white,
                               (int) (Config.SCREEN_WIDTH * 0.33), Config.SCREEN_HEIGHT / 2);
        sellButton = new Button(assets, Config.BUTTON_LOOK, "Sell", Color.white,
                                (int) (Config.SCREEN_WIDTH * 0.66), Config.SCREEN_HEIGHT / 2);

        itemWindow = null;
        transactions = null;
        textInput = null;

        ArrayList<Transaction> ct = RequestCompletedTransactions();

        for (Transaction t : ct) {
            data.gold += t.value;
            TextLog.instance.Print("Successfully sold your " + Item.GetItem(t.item).GetName() + "!", Color.white);
        }
    }

    @Override
    protected void update() {
        if (screen.isCloseRequested()) {
            PlayerData.Save();
            GameComponents.get(Game.class).quit();
        }

        if (itemWindow != null) {
            itemWindow.update();
            if (transactions != null) {
                Integer idx = itemWindow.GetSelectedIndex();
                if (idx != null) {
                    if (data.gold >= transactions.get(idx).value) {
                        if (RequestTransaction(idx)) {
                            data.gold -= transactions.get(idx).value;
                            data.inventory.AddItem(transactions.get(idx).item, 1);
                            TextLog.instance.Print("Successfully purchased " +
                                                   Item.GetItem(transactions.get(idx).item).GetName() + "!",
                                                   Color.white);
                            PlayerData.Save();
                        }
                        else {
                            TextLog.instance.Print("Please reload the offers window.", Color.red);
                        }

                        itemWindow = null;
                        transactions = null;
                        ShowButtons();
                    }
                    else {
                        TextLog.instance.Print("Not enough gold.", Color.red);
                    }
                }
            }
            else {
                Item it = itemWindow.GetSelectedItem();
                if (it != null && textInput.Finished()) {
                    int price = Integer.parseInt(textInput.GetInput());
                    if (CreateTransaction(it, price)) {
                        data.inventory.TakeItem(it.GetID(), 1);
                        TextLog.instance.Print("Your " + it.GetName() + " was put on sale for " + price + "G.",
                                               Color.white);
                    }
                    else {
                        TextLog.instance.Print("Transaction rejected, please retry.", Color.red);
                    }
                    itemWindow = null;
                    textInput = null;
                    ShowButtons();
                }
            }
        }
        else if (buyButton.WasPressed()) {

            transactions = RequestTransactionList();

            ArrayList<Integer> items = new ArrayList<Integer>();
            ArrayList<Integer> prices = new ArrayList<Integer>();

            for (Transaction t : transactions) {
                items.add(t.item);
                prices.add(t.value);
            }

            itemWindow = new ItemWindow(assets, "img/window.png", 0, 0, items, false, null, prices);
            HideButtons();
        }
        else if (sellButton.WasPressed()) {
            itemWindow = new ItemWindow(assets, "img/window.png", 0, 0, data.inventory, false, null, null);
            textInput = new TextInput(assets, TextInput.NUMERIC, (int) (Config.SCREEN_WIDTH * 0.75),
                                      (int) (Config.SCREEN_HEIGHT * 0.66));
            HideButtons();
        }

        TextLog.instance.Update();
    }

    @Override
    protected void render() {
        bg.render(screen, 0, 0, Corner.TOP_LEFT);

        marketText.render(screen, Config.SCREEN_WIDTH / 2, (float) (Config.SCREEN_HEIGHT * 0.066),
                          Corner.CENTER, 1.0f, 0.0f, 1.0f, 1.0f);

        buyButton.render(renderers);
        sellButton.render(renderers);

        if (itemWindow != null) {
            itemWindow.render(renderers);
        }

        if (textInput != null) {
            textInput.Render(screen);
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

    private ArrayList<Transaction> RequestTransactionList() {

        UpDevice device = FindCity();
        if (device == null) {
            System.out.println("Error contacting city.");
            return null;
        }

        try {
            ArrayList<Transaction> ts = new ArrayList<Transaction>();
            Call call = new Call("uRPG.cityDriver", "RequestTransactionList");
            Response response = gateway.callService(device, call);
            ArrayList<String> transactionstxt = (ArrayList<String>) response.getResponseData("transactions");
            for (String s : transactionstxt) {
                ts.add(Transaction.FromString(s));
            }
            return ts;

        }
        catch (ServiceCallException e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean RequestTransaction(int idx) {
        UpDevice device = FindCity();
        if (device == null) {
            System.out.println("Error contacting city");
            return false;
        }
        try {
            Transaction t = transactions.get(idx);
            Call call = new Call("uRPG.cityDriver", "RequestTransaction");
            call.addParameter("sellerUUID", t.seller.toString());
            call.addParameter("item", t.item);
            call.addParameter("price", t.value);
            Response response = gateway.callService(device, call);
            return Boolean.valueOf(response.getResponseString("confirmation"));
        }
        catch (ServiceCallException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    private ArrayList<Transaction> RequestCompletedTransactions() {

        ArrayList<Transaction> ts = new ArrayList<Transaction>();

        UpDevice device = FindCity();
        if (device == null) {
            System.out.println("Error contacting city");
            return ts;
        }
        try {
            Call call = new Call("uRPG.cityDriver", "RequestCompletedTransactions");
            call.addParameter("playerUUID", data.uuid.toString());
            Response response = gateway.callService(device, call);
            ArrayList<String> transactionstxt = (ArrayList<String>) response.getResponseData("transactions");
            for (String s : transactionstxt) {
                ts.add(Transaction.FromString(s));
            }
            return ts;
        }
        catch (ServiceCallException ex) {
            ex.printStackTrace();
        }
        return ts;
    }

    private boolean CreateTransaction(Item item, int price) {

        UpDevice device = FindCity();
        if (device == null) {
            System.out.println("Error contacting city");
            return false;
        }
        try {
            Call call = new Call("uRPG.cityDriver", "CreateTransaction");
            call.addParameter("playerUUID", data.uuid.toString());
            call.addParameter("item", item.GetID());
            call.addParameter("price", price);
            Response response = gateway.callService(device, call);
            return (Boolean) response.getResponseData("confirmation");
        }
        catch (ServiceCallException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void ShowButtons() {
        buyButton.Show();
        sellButton.Show();
    }

    private void HideButtons() {
        buyButton.Hide();
        sellButton.Hide();
    }

    public void OnKeyDown(Event event, Subject subject) {
        if (!frozen) {
            if (((KeyboardEvent) event).getKey() == 1) {
                if (itemWindow != null) {
                    itemWindow = null;
                    transactions = null;
                    textInput = null;
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
