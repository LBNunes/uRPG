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

import game.WorldMapScene.CityWorldInfo;

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

public class SmithyScene extends GameScene {

    private Screen         screen;
    private GameRenderers  renderers;
    private KeyboardSource keyboard;
    private Gateway        gateway;

    private PlayerData     data;
    private CityWorldInfo  cityInfo;

    private Sprite         bg;
    private Text           smithyText;
    private Button         craftButton;
    private Button         viewAllButton;
    private RecipeWindow   window;

    boolean                crafting;

    public SmithyScene(CityWorldInfo info) {

        cityInfo = info;
        data = PlayerData.GetData();

        screen = GameComponents.get(Screen.class);
        keyboard = screen.getKeyboard();
        keyboard.connect(KeyboardSource.EVENT_KEY_DOWN, new Observation(this, "OnKeyDown"));
        gateway = GameComponents.get(Gateway.class);

        bg = assets.newSprite("img/bg/citybg.jpg");

        smithyText = assets.newText(Config.WORLD_FONT, "Smithy");
        smithyText.options(null, Config.WORLD_FONT_SIZE, true);

        craftButton = new Button(assets, Config.BUTTON_LOOK, "Craft", Color.white,
                                 Config.SCREEN_WIDTH / 3, Config.SCREEN_HEIGHT / 2);
        viewAllButton = new Button(assets, Config.BUTTON_LOOK, "All Recipes", Color.white,
                                   2 * Config.SCREEN_WIDTH / 3, Config.SCREEN_HEIGHT / 2);
        window = null;
    }

    @Override
    protected void update() {

        if (screen.isCloseRequested()) {
            PlayerData.Save();
            GameComponents.get(Game.class).quit();
        }

        if (window != null) {
            window.update();
            if (crafting) {
                Recipe r = window.GetSelectedRecipe();
                if (r != null) {
                    for (Integer component : r.components) {
                        data.inventory.TakeItem(component, 1);
                    }
                    data.inventory.AddItem(r.itemID, 1);
                    TextLog.instance.Print("Successfully crafted one " + Item.GetItem(r.itemID).GetName() + ".",
                                           Color.white);
                    PlayerData.Save();
                    window.Freeze();
                    window = null;
                }
            }
        }

        else if (craftButton.WasPressed()) {
            window = new RecipeWindow(assets, "img/window.png", 0, 0, Recipe.GetPossibleRecipes(data.inventory));
            crafting = true;
            craftButton.Hide();
            viewAllButton.Hide();
        }

        else if (viewAllButton.WasPressed()) {
            window = new RecipeWindow(assets, "img/window.png", 0, 0, Recipe.GetAllRecipes());
            crafting = false;
            craftButton.Hide();
            viewAllButton.Hide();
        }

        TextLog.instance.Update();
    }

    @Override
    protected void render() {
        bg.render(screen, 0, 0, Corner.TOP_LEFT);

        smithyText.render(screen, Config.SCREEN_WIDTH / 2, (float) (Config.SCREEN_HEIGHT * 0.066),
                          Corner.CENTER, 1.0f, 0.0f, 1.0f, 1.0f);

        craftButton.render(renderers);
        viewAllButton.render(renderers);

        if (window != null) {
            window.render(renderers);
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

    public void OnKeyDown(Event event, Subject subject) {
        if (!frozen) {
            if (((KeyboardEvent) event).getKey() == 1) {
                if (window != null) {
                    window = null;
                    craftButton.Show();
                    viewAllButton.Show();
                }
                else {
                    frozen = true; // Prevents the pop from being called again if Esc is pressed...
                    GameComponents.get(Game.class).pop();
                }
            }
        }
    }

}
