/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Luísa Bontempo Nunes
//     Created on 2014-06-20 ymd
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

import game.Item.ItemSlot;

import org.unbiquitous.uImpala.engine.core.Game;
import org.unbiquitous.uImpala.engine.core.GameComponents;
import org.unbiquitous.uImpala.engine.core.GameRenderers;
import org.unbiquitous.uImpala.engine.core.GameScene;
import org.unbiquitous.uImpala.engine.io.KeyboardEvent;
import org.unbiquitous.uImpala.engine.io.KeyboardSource;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.util.observer.Event;
import org.unbiquitous.uImpala.util.observer.Observation;
import org.unbiquitous.uImpala.util.observer.Subject;

public class PlayerMenuScene extends GameScene {

    public static final int PARTY     = 1;
    public static final int INVENTORY = 2;
    public static final int MISSIONS  = 3;

    private Screen          screen;
    private GameRenderers   renderers;
    private KeyboardSource  keyboard;
    private EntityWindow    party;
    private ItemWindow      items;
    private PlayerData      data;

    public PlayerMenuScene(int menuType) {

        screen = GameComponents.get(Screen.class);
        keyboard = screen.getKeyboard();
        keyboard.connect(KeyboardSource.EVENT_KEY_DOWN, new Observation(this, "OnKeyDown"));

        this.data = PlayerData.GetData();

        if (menuType == INVENTORY) {
            party = null;
            items = new ItemWindow(assets, "img/window.png", 0, 0, data.inventory, true,
                                   new Predicate<Item>() {
                                       public boolean Eval(Item a) {
                                           return true;
                                       }
                                   });
        }
        else if (menuType == PARTY) {
            party = new EntityWindow(assets, "img/window.png", 0, 0, data.party, true, true);
            items = null;
        }
    }

    @Override
    protected void update() {

        if (screen.isCloseRequested()) {
            PlayerData.Save();
            GameComponents.get(Game.class).quit();
        }

        if (items != null) {
            items.update();
        }

        if (party != null) {
            if (!party.Frozen()) {
                party.update();
                if (party.GetSelectedEntity() != null && party.GetSelectedSlot() != ItemSlot.NONE) {
                    items = new ItemWindow(assets, "img/window.png", 0, 0, data.inventory, false,
                                           new Predicate<Item>() {
                                               public boolean Eval(Item a) {
                                                   return (a.GetSlot() == party.GetSelectedSlot() &&
                                                   Classes.CanEquip(party.GetSelectedEntity().classID, a));
                                               }
                                           });
                    party.Freeze();
                }
            }
            else {
                if (items.GetSelectedItem() != null) {
                    if (party.GetSelectedEntity().equipment.Get(party.GetSelectedSlot()).GetID() != 0) {
                        data.inventory.AddItem(party.GetSelectedEntity().equipment.Get(party.GetSelectedSlot()).GetID(),
                                               1);
                    }
                    data.inventory.TakeItem(items.GetSelectedItem().GetID(), 1);
                    party.GetSelectedEntity().equipment.Set(party.GetSelectedSlot(), items.GetSelectedItem().GetID());
                    party.GetSelectedEntity().RecalculateStats();
                    items = null;
                    party.Reset();
                }
            }
        }
    }

    @Override
    protected void render() {
        if (party != null) {
            party.render(renderers);
        }

        if (items != null) {
            items.render(renderers);
        }
    }

    @Override
    protected void wakeup(Object... args) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void destroy() {

    }

    public void OnKeyDown(Event event, Subject subject) {
        if (!frozen) {
            if (((KeyboardEvent) event).getKey() == 1) {
                if (party != null && items != null) {
                    items = null;
                    party.Reset();
                }
                else {
                    PlayerData.Save();
                    frozen = true; // Prevents the pop from being called again if Esc is pressed...
                    GameComponents.get(Game.class).pop();
                }
            }
        }
    }
}
