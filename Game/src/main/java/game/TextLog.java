/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Luísa Bontempo Nunes
//     Created on 2014-06-08 ymd
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

import java.util.ArrayDeque;
import java.util.Iterator;

import org.unbiquitous.uImpala.engine.asset.AssetManager;
import org.unbiquitous.uImpala.engine.asset.Text;
import org.unbiquitous.uImpala.engine.core.GameComponents;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.engine.time.DeltaTime;
import org.unbiquitous.uImpala.util.Color;
import org.unbiquitous.uImpala.util.Corner;

public class TextLog {

    public static final TextLog instance = new TextLog(Config.LOG_CAP, Config.LOG_FONT, Config.LOG_FONT_SIZE,
                                                       Config.LOG_EXPIRE_TIME);

    private Screen              screen;
    private int                 cap;
    private String              font;
    private float               expireTime;
    private ArrayDeque<LogData> queue;
    private AssetManager        assets;

    public TextLog(int logCap, String logFont, int logFontSize, float logExpireTime) {

        screen = GameComponents.get(Screen.class);

        cap = logCap;
        font = logFont;
        expireTime = logExpireTime;

        queue = new ArrayDeque<LogData>();
    }

    public void SetAssets(AssetManager assets) {
        this.assets = assets;
    }

    public void Print(String text, Color color) {
        Text t = assets.newText(font, text);
        Color c = color;
        queue.add(new LogData(t, c));
        if (queue.size() > cap) {
            queue.remove();
        }
    }

    public void Update() {
        DeltaTime dt = GameComponents.get(DeltaTime.class);
        Iterator<LogData> i = queue.iterator();
        while (i.hasNext()) {
            LogData l = i.next();
            if (l.currentTime > expireTime) {
                i.remove();
            }
            else
                l.currentTime += dt.getDT();
        }
    }

    public void Render(int x, int y, Corner corner) {

        if (queue.isEmpty()) {
            return;
        }

        int textHeight = queue.peek().text.getHeight();
        int i = 0;

        for (LogData l : queue) {
            l.text.render(screen, x, y - (queue.size() - i) * textHeight, Corner.CENTER,
                          1.0f, 0.0f, 1.0f, 1.0f, l.color);
            i++;
        }
    }

    public void Clear() {
        queue.clear();
    }

    private static class LogData {
        Text  text;
        Color color;
        float currentTime;

        public LogData(Text t, Color c) {
            currentTime = 0;
            color = c;
            text = t;
        }
    }
}
