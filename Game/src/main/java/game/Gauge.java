/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Luísa Bontempo Nunes
//     Created on 2014-06-12 ymd
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
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.util.Color;
import org.unbiquitous.uImpala.util.Corner;

public class Gauge {

    private Sprite body;
    private Sprite fill;
    private int    max;
    private int    current;
    private Color  color;

    public Gauge(AssetManager assets, int max, Color color) {
        body = assets.newSprite(Config.GAUGE_BODY);
        fill = assets.newSprite(Config.GAUGE_FILL);
        this.max = max;
        this.current = max;
        this.color = color;
    }

    public void Render(Screen screen, int x, int y) {
        body.render(screen, x, y, Corner.CENTER);
        fill.render(screen, x + (((current / (float) max) - 1) * fill.getWidth() / 2), y, Corner.CENTER,
                    1.0f, 0.0f, current / (float) max, 1.0f, color);
    }

    public void Update(int current) {
        this.current = current;
    }

    public void NewMax(int _max) {
        max = _max;
    }

    public int GetWidth() {
        return body.getWidth();
    }

    public int GetHeight() {
        return body.getHeight();
    }
}
