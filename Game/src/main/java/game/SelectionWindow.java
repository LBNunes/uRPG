/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Lu�sa Bontempo Nunes
//     Created on 2014-06-19 ymd
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

import java.util.ArrayList;

import org.unbiquitous.uImpala.engine.asset.AssetManager;
import org.unbiquitous.uImpala.engine.asset.Sprite;
import org.unbiquitous.uImpala.engine.asset.TileSet;
import org.unbiquitous.uImpala.engine.core.GameComponents;
import org.unbiquitous.uImpala.engine.core.GameObject;
import org.unbiquitous.uImpala.engine.core.GameRenderers;
import org.unbiquitous.uImpala.engine.io.MouseEvent;
import org.unbiquitous.uImpala.engine.io.MouseSource;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.engine.time.Stopwatch;
import org.unbiquitous.uImpala.util.observer.Event;
import org.unbiquitous.uImpala.util.observer.Observation;
import org.unbiquitous.uImpala.util.observer.Subject;

public abstract class SelectionWindow extends GameObject {

    protected ArrayList<Option> options;
    protected Screen            screen;
    protected MouseSource       mouse;
    protected Stopwatch         stopwatch;
    protected TileSet           window;
    protected Sprite            frame;
    protected int               x;
    protected int               y;
    protected int               width;
    protected int               height;
    protected int               scroll;
    protected boolean           drag;

    protected Option            selected;

    static final int            MAX_CLICK_TIME = 250;

    public abstract void Swap(int index1, int index2);

    public SelectionWindow(AssetManager assets, String frame, int x, int y, int width, int height) {
        this.screen = GameComponents.get(Screen.class);
        this.mouse = screen.getMouse();
        mouse.connect(MouseSource.EVENT_BUTTON_DOWN, new Observation(this, "OnButtonDown"));
        mouse.connect(MouseSource.EVENT_BUTTON_UP, new Observation(this, "OnButtonUp"));
        this.frame = assets.newSprite(frame);
        this.window = assets.newTileSet(this.frame, 3, 3);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.stopwatch = new Stopwatch();
        this.stopwatch.reset();
        scroll = 0;
        drag = false;
        selected = null;
    }

    @Override
    public void update() {
        if (drag) {
            scroll += mouse.getDeltaY();
            for (Option o : options) {
                o.Update(scroll);
            }
        }
    }

    @Override
    public void render(GameRenderers renderers) {

        // Fill
        for (int i = 1; i < width - 1; ++i) {
            for (int j = 1; j < height - 1; ++j) {
                window.render(4, screen, x + i * (frame.getWidth() / 3),
                              y + j * (frame.getHeight() / 3));
            }
        }

        // Options
        for (Option o : options) {
            o.Render(renderers, screen);
        }

        // Corners
        window.render(0, screen, x, y);
        window.render(2, screen, x + ((width - 1) * (frame.getWidth() / 3)), y);
        window.render(6, screen, x, y + ((height - 1) * (frame.getHeight() / 3)));
        window.render(8, screen, x + ((width - 1) * (frame.getWidth() / 3)),
                      y + ((height - 1) * (frame.getHeight() / 3)));

        // Sides
        for (int i = 1; i < width - 1; ++i) {
            window.render(1, screen, x + i * (frame.getWidth() / 3), y);
            window.render(7, screen, x + i * (frame.getWidth() / 3),
                          y + ((height - 1) * (frame.getHeight() / 3)));
        }
        for (int j = 1; j < height - 1; ++j) {
            window.render(3, screen, x, y + j * (frame.getHeight() / 3));
            window.render(5, screen, x + ((width - 1) * (frame.getWidth() / 3)),
                          y + j * (frame.getHeight() / 3));
        }
    }

    public void OnButtonDown(Event event, Subject subject) {
        stopwatch.start();
        drag = true;
    }

    public void OnButtonUp(Event event, Subject subject) {
        if (stopwatch.time() < MAX_CLICK_TIME) {
            MouseEvent e = (MouseEvent) event;
            int swapRequester = -1;
            for (int i = 0; i < options.size(); ++i) {
                Option o = options.get(i);
                o.CheckClick(e.getX(), e.getY());
                if (o.requestedSwap) {
                    if (swapRequester != -1) {
                        options.get(swapRequester).Reset();
                        o.Reset();
                        Swap(swapRequester, i);
                    }
                    else
                        swapRequester = i;
                }
                if (o.selected) {
                    selected = o;
                    o.Reset();
                }
            }
        }
        drag = false;
    }

    protected static abstract class Option {

        public Rect    box;

        public Point   base;

        public int     index;
        public int     originalIndex;

        public boolean swappable;
        public boolean requestedSwap;
        public boolean selected;

        public Sprite  swapIcon;
        public Rect    swapBox;

        public abstract void Render(GameRenderers renderers, Screen screen);

        public abstract void CheckClick(int x, int y);

        public void Update(int scroll) {
            box.y -= scroll;
            swapBox.y -= scroll;
        }

        public Option(AssetManager assets, int _index, int _originalIndex, int _baseX, int _baseY, int _w, int _h,
                      boolean _swappable) {
            index = _index;
            originalIndex = _originalIndex;
            swappable = _swappable;
            box = new Rect(_baseX, _baseY + _index * _h, _w, _h);
            base = new Point(_baseX, _baseY);

            swapIcon = assets.newSprite("img/swap.png");
            swapBox = new Rect((int) (_baseX + _w * 0.80), _baseY + _h / 2,
                               swapIcon.getWidth(), swapIcon.getHeight());
        }

        public boolean RequestedSwap() {
            return requestedSwap;
        }

        public boolean Selected() {
            return selected;
        }

        public void Reset() {
            selected = false;
            requestedSwap = false;
        }
    }
}
