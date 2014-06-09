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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;

import org.unbiquitous.uImpala.engine.asset.Animation;
import org.unbiquitous.uImpala.engine.asset.AssetManager;
import org.unbiquitous.uImpala.engine.core.GameComponents;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.engine.time.Stopwatch;
import org.unbiquitous.uImpala.util.Corner;

public class AnimationQueue {

    private Queue<ArrayList<AnimationData>> queue;
    private AssetManager                    assets;
    private Screen                          screen;

    public AnimationQueue(AssetManager assets) {
        this.assets = assets;
        queue = new ArrayDeque<ArrayList<AnimationData>>();
        screen = GameComponents.get(Screen.class);
    }

    private class AnimationData {
        public Animation animation;
        public Stopwatch stopwatch;
        public float     timeLimit;
        public Point     position;

        public AnimationData(Animation a, Point p, float timeLimit) {
            animation = a;
            stopwatch = new Stopwatch();
            stopwatch.start();
            position = p;
            this.timeLimit = (float) (timeLimit * 0.95);
        }
    }

    public boolean IsEmpty() {
        return queue.isEmpty();
    }

    public void NewGroup() {

        queue.add(new ArrayList<AnimationData>());
    }

    public void Push(String path, int frames, float fps, Point point) {
        ArrayList<AnimationData> queueTop = queue.peek();
        if (queueTop == null) {
            queueTop = new ArrayList<AnimationData>();
            queue.add(queueTop);
        }
        queueTop.add(new AnimationData(assets.newAnimation(path, frames, fps),
                                       point, 1000 * frames / fps));
    }

    public void Update() {
        ArrayList<AnimationData> queueTop = queue.peek();
        if (queueTop == null) {
            return;
        }
        Iterator<AnimationData> i = queueTop.iterator();
        while (i.hasNext()) {
            AnimationData a = i.next();
            if (a.stopwatch.time() > a.timeLimit) {
                i.remove();
            }
        }
        if (queueTop.isEmpty()) {
            queue.remove();
        }
    }

    public void Render() {
        ArrayList<AnimationData> queueTop = queue.peek();
        if (queueTop == null) {
            return;
        }
        for (AnimationData a : queueTop) {
            a.animation.render(screen, a.position.x, a.position.y, Corner.CENTER);
        }
    }
}
