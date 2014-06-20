/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Luísa Bontempo Nunes
//     Created on 2014-06-05 ymd
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

import org.unbiquitous.uImpala.engine.asset.AssetManager;
import org.unbiquitous.uImpala.engine.asset.Sprite;
import org.unbiquitous.uImpala.engine.asset.Text;
import org.unbiquitous.uImpala.engine.core.GameComponents;
import org.unbiquitous.uImpala.engine.core.GameObject;
import org.unbiquitous.uImpala.engine.core.GameRenderers;
import org.unbiquitous.uImpala.engine.io.MouseEvent;
import org.unbiquitous.uImpala.engine.io.MouseSource;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.util.Color;
import org.unbiquitous.uImpala.util.Corner;
import org.unbiquitous.uImpala.util.observer.Event;
import org.unbiquitous.uImpala.util.observer.Observation;
import org.unbiquitous.uImpala.util.observer.Subject;

public class Button extends GameObject {
    private Screen      screen;
    private MouseSource mouse;
    private Sprite      look;
    private Color       color;
    private Text        text;
    private Point       pos;
    private Rect        box;
    private boolean     pressed;
    private boolean     showTextOnMouseOver;

    public Button(AssetManager assets, String buttonLook, String buttonText, Color textColor, int x, int y) {
        screen = GameComponents.get(Screen.class);
        mouse = screen.getMouse();
        mouse.connect(MouseSource.EVENT_BUTTON_DOWN, new Observation(this, "OnButtonDown"));

        color = textColor;
        look = assets.newSprite(buttonLook);
        text = assets.newText(Config.BUTTON_FONT, buttonText);
        pos = new Point(x, y);

        box = new Rect(x - look.getWidth() / 2, y - look.getHeight() / 2, look.getWidth(), look.getHeight());

        Show();
    }

    @Override
    protected void update() {

    }

    @Override
    protected void render(GameRenderers renderers) {

        if (!visible)
            return;

        look.render(screen, pos.x, pos.y, Corner.CENTER);

        if (!showTextOnMouseOver || box.IsInside(mouse.getX(), mouse.getY())) {
            text.render(screen, pos.x, pos.y, Corner.CENTER, 1.0f, 0.0f, 1.0f, 1.0f, color);
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

    public void Move(int x, int y) {
        pos.x = x;
        pos.y = y;

        box.x = x - box.w / 2;
        box.y = y - box.h / 2;
    }

    public void Hide() {
        visible = false;
        frozen = true;
        Reset();
    }

    public void Show() {
        visible = true;
        frozen = false;
        Reset();
    }

    public boolean WasPressed() {
        return pressed;
    }

    public void Reset() {
        pressed = false;
    }

    public void ShowTextOnMouseOver(boolean flag) {
        showTextOnMouseOver = flag;
    }

    public void OnButtonDown(Event event, Subject subject) {
        MouseEvent e = (MouseEvent) event;

        if (box.IsInside(e.getX(), e.getY()) && !frozen) {
            pressed = true;
        }
    }
}
