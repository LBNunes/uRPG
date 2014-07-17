/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Luísa Bontempo Nunes
//     Created on 2014-07-17 ymd
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

import org.lwjgl.input.Keyboard;
import org.unbiquitous.uImpala.engine.asset.AssetManager;
import org.unbiquitous.uImpala.engine.asset.Text;
import org.unbiquitous.uImpala.engine.core.GameComponents;
import org.unbiquitous.uImpala.engine.io.KeyboardEvent;
import org.unbiquitous.uImpala.engine.io.KeyboardSource;
import org.unbiquitous.uImpala.engine.io.MouseEvent;
import org.unbiquitous.uImpala.engine.io.MouseSource;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.util.Color;
import org.unbiquitous.uImpala.util.Corner;
import org.unbiquitous.uImpala.util.observer.Event;
import org.unbiquitous.uImpala.util.observer.Observation;
import org.unbiquitous.uImpala.util.observer.Subject;

public class TextInput {

    public static final int NUMERIC      = 1;
    public static final int ALPHABETIC   = 2;
    public static final int ALPHANUMERIC = 3;

    private Screen          screen;
    private KeyboardSource  keyboard;
    private MouseSource     mouse;

    private String          input;
    private Text            text;
    private Button          button;
    private int             type;
    private int             x;
    private int             y;
    private boolean         focus;
    private Rect            box;

    public TextInput(AssetManager assets, int _type, int _x, int _y) {
        screen = GameComponents.get(Screen.class);
        mouse = screen.getMouse();
        mouse.connect(MouseSource.EVENT_BUTTON_DOWN, new Observation(this, "OnButtonDown"));
        keyboard = screen.getKeyboard();
        keyboard.connect(KeyboardSource.EVENT_KEY_DOWN, new Observation(this, "OnKeyDown"));

        input = "";
        text = assets.newText(Config.BUTTON_FONT, "Type...");
        x = _x;
        y = _y;
        button = new Button(assets, Config.BUTTON_LOOK, "OK", Color.white, _x, _y);
        button.Move(x, y + button.GetHeight() / 2);

        focus = false;
        box = new Rect(x - text.getWidth() / 2, y - text.getHeight(), text.getWidth(), text.getHeight());

        type = _type;
    }

    public void Render(Screen screen) {
        text.render(screen, box.x, box.y, Corner.TOP_LEFT, 1.0f, 0.0f, 1.0f, 1.0f,
                    input.length() == 0 ? Color.gray : Color.white);
        button.render(null);
    }

    public boolean Finished() {
        return button.WasPressed();
    }

    public String GetInput() {
        return input;
    }

    private void Refresh() {
        if (input.length() == 0) {
            text.setText("Type...");
        }
        else {
            text.setText(input);
        }
        box.Set(x - text.getWidth() / 2, y - text.getHeight(), text.getWidth(), text.getHeight());
    }

    @SuppressWarnings("unused")
    private void OnKeyDown(Event event, Subject subject) {
        if (button.WasPressed() || !focus)
            return;

        KeyboardEvent e = (KeyboardEvent) event;
        if (type == ALPHABETIC || type == ALPHANUMERIC) {
            if (Character.isAlphabetic(e.getCharacter())) {
                input += e.getCharacter();
                Refresh();
            }
        }
        if (type == NUMERIC || type == ALPHANUMERIC) {
            if (Character.isDigit(e.getCharacter())) {
                input += e.getCharacter();
                Refresh();
            }
        }
        if (e.getKey() == Keyboard.KEY_RETURN) {
            button.Press();
        }
        if (e.getKey() == Keyboard.KEY_BACK) {
            if (input.length() > 0) {
                input = input.substring(0, input.length() - 2);
            }
            Refresh();
        }
    }

    @SuppressWarnings("unused")
    private void OnButtonDown(Event event, Subject subject) {
        MouseEvent e = (MouseEvent) event;

        if (box.IsInside(e.getX(), e.getY())) {
            focus = true;
            button.Reset();
        }
        else {
            focus = false;
        }
    }
}
