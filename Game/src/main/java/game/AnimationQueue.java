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
import org.unbiquitous.uImpala.util.Color;
import org.unbiquitous.uImpala.util.Corner;

public class AnimationQueue {

    private Queue<ArrayList<AnimQueueData>> queue;
    private AssetManager                    assets;
    private Screen                          screen;

    public AnimationQueue(AssetManager assets) {
        this.assets = assets;
        queue = new ArrayDeque<ArrayList<AnimQueueData>>();
        screen = GameComponents.get(Screen.class);
    }

    public boolean IsEmpty() {
        return queue.isEmpty();
    }

    public void NewGroup() {

        queue.add(new ArrayList<AnimQueueData>());
    }

    public void Push(String path, int frames, float fps, Point point) {
        ArrayList<AnimQueueData> queueTop = queue.peek();
        if (queueTop == null) {
            queueTop = new ArrayList<AnimQueueData>();
            queue.add(queueTop);
        }
        queueTop.add(new QueuedAnimation(assets.newAnimation(path, frames, fps),
                                         point, 1000 * frames / fps));
    }

    public void Push(int damage, boolean critical, Color color, Point pos) {
        ArrayList<AnimQueueData> queueTop = queue.peek();
        if (queueTop == null) {
            queueTop = new ArrayList<AnimQueueData>();
            queue.add(queueTop);
        }
        queueTop.add(new QueuedDamage(new Damage(assets, damage, critical, color), pos));
    }

    public void Update() {
        ArrayList<AnimQueueData> queueTop = queue.peek();
        if (queueTop == null) {
            return;
        }
        Iterator<AnimQueueData> i = queueTop.iterator();
        while (i.hasNext()) {
            AnimQueueData a = i.next();
            if (a.stopwatch.time() > a.timeLimit) {
                i.remove();
            }
        }
        if (queueTop.isEmpty()) {
            queue.remove();
        }
    }

    public void Render() {
        ArrayList<AnimQueueData> queueTop = queue.peek();
        if (queueTop == null) {
            return;
        }
        for (AnimQueueData a : queueTop) {
            a.Render();
        }
    }

    private abstract class AnimQueueData {
        public Stopwatch stopwatch;
        public float     timeLimit;
        public Point     position;

        public AnimQueueData(Point p, float timeLimit) {
            position = p;
            this.timeLimit = (float) (timeLimit * 0.95);
            stopwatch = new Stopwatch();
            stopwatch.start();
        }

        public abstract void Render();
    }

    private class QueuedAnimation extends AnimQueueData {
        public Animation animation;

        public QueuedAnimation(Animation a, Point p, float timeLimit) {
            super(p, timeLimit);
            animation = a;
        }

        @Override
        public void Render() {
            animation.render(screen, position.x, position.y, Corner.CENTER);
        }
    }

    private class QueuedDamage extends AnimQueueData {
        public Damage damage;
        public float  distance;

        public QueuedDamage(Damage d, Point p) {
            super(p, Config.DAMAGE_ANIMATION_TIME);
            damage = d;
            this.distance = Config.DAMAGE_ANIMATION_HOVER;
        }

        @Override
        public void Render() {
            damage.Render(screen, position.x, (int) (position.y - ((stopwatch.time() / timeLimit) * distance)));
        }
    }
}
