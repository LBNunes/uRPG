/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Luísa Bontempo Nunes
//     Created on 2014-07-05 ymd
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

import game.Ability.DamageType;

import java.util.ArrayList;

import org.unbiquitous.uImpala.engine.asset.AssetManager;
import org.unbiquitous.uImpala.engine.asset.Sprite;
import org.unbiquitous.uImpala.engine.asset.Text;
import org.unbiquitous.uImpala.engine.core.GameRenderers;
import org.unbiquitous.uImpala.engine.io.MouseSource;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.util.Corner;
import org.unbiquitous.uImpala.util.observer.Event;
import org.unbiquitous.uImpala.util.observer.Observation;
import org.unbiquitous.uImpala.util.observer.Subject;

public class AbilityWindow extends SelectionWindow {

    static final int           WINDOW_WIDTH    = 20;
    static final int           WINDOW_HEIGHT   = 22;
    static final int           OPTION_OFFSET_X = 32;
    static final int           OPTION_OFFSET_Y = 32;

    private ArrayList<Ability> list;

    public AbilityWindow(AssetManager assets, String frame, int x, int y, ArrayList<Ability> list,
                         Predicate<Ability> p, ArrayList<Integer> goldCost) {
        super(assets, frame, x, y, WINDOW_WIDTH, WINDOW_HEIGHT);
        mouse.connect(MouseSource.EVENT_BUTTON_DOWN, new Observation(this, "OnButtonDown"));
        mouse.connect(MouseSource.EVENT_BUTTON_UP, new Observation(this, "OnButtonUp"));
        this.list = list;
        for (int i = 0; i < list.size(); ++i) {
            Ability a = list.get(i);
            if (p.Eval(a)) {
                options.add(new AbilityOption(assets, options.size(), i, x + OPTION_OFFSET_X, y + OPTION_OFFSET_Y,
                                              WINDOW_WIDTH * this.frame.getWidth() / 3 - OPTION_OFFSET_X * 2,
                                              (int) (this.frame.getHeight() * 1.2), a,
                                              goldCost == null ? 0 : goldCost.get(i)));
            }
        }
    }

    @Override
    public void Swap(int index1, int index2) {
        Option o1 = options.get(index1);
        Option o2 = options.get(index2);

        Ability a1 = list.get(o1.originalIndex);
        Ability a2 = list.get(o2.originalIndex);

        list.set(o1.originalIndex, a2);
        list.set(o2.originalIndex, a1);

        o1.index = index2;
        o2.index = index1;

        options.set(index2, o1);
        options.set(index1, o2);

        int oindex1 = o1.originalIndex;
        o1.originalIndex = o2.originalIndex;
        o2.originalIndex = oindex1;
    }

    public Ability GetSelectedAbility() {
        if (selected == null) {
            return null;
        }
        else {
            return ((AbilityOption) selected).ability;
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

    @Override
    public void OnButtonDown(Event event, Subject subject) {
        super.OnButtonDown(event, subject);
    }

    @Override
    public void OnButtonUp(Event event, Subject subject) {
        super.OnButtonUp(event, subject);
    }

    private class AbilityOption extends Option {

        Sprite  icon;
        Text    name;
        Text    description;
        Text    stats;
        Text    cost;

        Ability ability;

        public AbilityOption(AssetManager assets, int _index, int _originalIndex, int _baseX, int _baseY, int _w,
                             int _h, Ability _ability, int _cost) {
            super(assets, _index, _originalIndex, _baseX, _baseY, _w, _h, false);
            ability = _ability;
            icon = assets.newSprite(PickIcon());
            name = assets.newText("font/seguisb.ttf", ability.name);
            description = assets.newText("font/seguisb.ttf", Describe());
            stats = assets.newText("font/seguisb.ttf", GetStats());
            if (_cost > 0) {
                cost = assets.newText("font/seguisb.ttf", "" + _cost + "G");
            }
        }

        @Override
        public void Render(GameRenderers renderers, Screen screen) {

            icon.render(screen, (float) (box.x + 0.08 * box.w), (float) (box.y + box.h / 2), Corner.CENTER,
                        1.0f, 0.0f, 1.0f, 1.0f);
            name.render(screen, (int) (box.x + 0.10 * box.w + icon.getWidth() / 3),
                        box.y + box.h / 2 - 3 * (name.getHeight() / 2), Corner.TOP_LEFT);
            description.render(screen, (int) (box.x + 0.10 * box.w + icon.getWidth() / 3),
                               box.y + box.h / 2 - name.getHeight() / 2, Corner.TOP_LEFT);
            stats.render(screen, (int) (box.x + 0.10 * box.w + icon.getWidth() / 3),
                         box.y + box.h / 2 + name.getHeight() / 2, Corner.TOP_LEFT);

            int mx = screen.getMouse().getX();
            int my = screen.getMouse().getY();
            if (cost != null) {
                if (box.IsInside(mx, my)) {
                    cost.render(screen, mx, my, Corner.TOP_LEFT);
                }
            }
        }

        @Override
        public void CheckClick(int x, int y) {
            if (box.IsInside(x, y)) {
                selected = true;
            }
        }

        private String PickIcon() {
            if (!ability.damaging) {
                return "img/healicon.png";
            }
            else if (ability.damageType == DamageType.MAGICAL) {
                return "img/magicicon.png";
            }
            else {
                return "img/weapon.png";
            }
        }

        private String Describe() {
            String s = "";
            if (!ability.damaging) {
                s += "Support magic for a ";
            }
            else if (ability.damageType == DamageType.MAGICAL) {
                s += "Offensive magic for a ";
            }
            else {
                s += "Physical Ability for a ";
            }
            s += "Rank " + ability.rank + " " + Classes.GetClassName(ability.classID);
            return s;
        }

        private String GetStats() {
            String s = "" + ability.abilityPower + " Power / Costs " + ability.cost + " MP / ";
            switch (ability.areaType) {
                case SELF:
                    s += "Targets self";
                    break;
                case ALLIES:
                    s += "Targets all allies";
                    break;
                case CIRCLE:
                    if (ability.areaRange > 1) {
                        s += "Targets area";
                    }
                    else {
                        s += "Targets one unit";
                    }
                    break;
                case FOES:
                    s += "Targets all foes";
                    break;
                case LINE:
                    s += "Targets a straight line";
                    break;
            }
            return s;
        }
    }

}
