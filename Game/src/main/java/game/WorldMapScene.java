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

import org.unbiquitous.uImpala.engine.core.Game;
import org.unbiquitous.uImpala.engine.core.GameComponents;
import org.unbiquitous.uImpala.engine.core.GameScene;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.engine.io.ScreenManager;

public class WorldMapScene extends GameScene {

    private Screen screen;

    // TODO: Figure out which areas will stay...
    public enum WorldArea {
        GRASSLAND, FOREST, DESERT, SWAMP, HAUNTED_MANSION, DUNGEON, CAVE, LAKE, SHORE, MOUNTAINS
    }

    public WorldMapScene() {
        // Initialize the screen manager
        screen = GameComponents.get(ScreenManager.class).create();
        screen.open(Config.WINDOW_TITLE, Config.SCREEN_WIDTH,
                    Config.SCREEN_HEIGHT, Config.FULLSCREEN, Config.WINDOW_ICON);
        GameComponents.put(Screen.class, screen);

        Item.InitTable();
        Classes.InitStats();
        Enemies.InitNames();
        Enemies.InitTable();
    }

    @Override
    protected void update() {
        this.frozen = true;
        GameComponents.get(Game.class).change(new BattleScene(new PlayerData(), WorldArea.GRASSLAND));
    }

    @Override
    protected void render() {
        // TODO Auto-generated method stub

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
