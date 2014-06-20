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

import org.unbiquitous.uImpala.engine.core.Game;
import org.unbiquitous.uImpala.engine.core.GameComponents;
import org.unbiquitous.uImpala.engine.core.GameRenderers;
import org.unbiquitous.uImpala.engine.core.GameScene;

public class PlayerMenuScene extends GameScene {

    public static final int PARTY     = 1;
    public static final int INVENTORY = 2;

    private GameRenderers   renderers;
    private EntityWindow    party;
    private ItemWindow      items;

    public PlayerMenuScene(PlayerData data, int menuType) {

        if (menuType == INVENTORY) {
            party = null;
            items = new ItemWindow(assets, "img/window.png", 0, 0, data.inventory, true,
                                   new Predicate<Item>() {
                                       public boolean Eval(Item a) {
                                           return true;
                                       }
                                   });
        }
    }

    @Override
    protected void update() {

        if (screen.isCloseRequested()) {
            PlayerData.Save(Config.PLAYER_SAVE, data);
            GameComponents.get(Game.class).quit();
        }

        if (items != null) {
            items.update();
        }
    }

    @Override
    protected void render() {
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
        // TODO Auto-generated method stub

    }

}
