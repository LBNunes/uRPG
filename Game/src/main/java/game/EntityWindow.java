/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Luísa Bontempo Nunes
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
import org.unbiquitous.uImpala.engine.core.GameRenderers;
import org.unbiquitous.uImpala.engine.io.Screen;

public class EntityWindow extends SelectionWindow {

    static final int          WINDOW_WIDTH  = 13;
    static final int          WINDOW_HEIGHT = 22;

    private ArrayList<Entity> list;

    public EntityWindow(AssetManager assets, String frame, int x, int y, ArrayList<Entity> list) {
        super(assets, frame, x, y, WINDOW_WIDTH, WINDOW_HEIGHT);
        this.list = list;
    }

    private class EntityOption extends Option {

        public EntityOption(AssetManager assets, int _index, int _originalIndex, int _baseX, int _baseY, int _w,
                            int _h, boolean _swappable) {
            super(assets, _index, _originalIndex, _baseX, _baseY, _w, _h, _swappable);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void Render(GameRenderers renderers, Screen screen) {
            // TODO Auto-generated method stub

        }

        @Override
        public void CheckClick(int x, int y) {
            // TODO Auto-generated method stub

        }

    }

    @Override
    public void Swap(int index1, int index2) {
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
