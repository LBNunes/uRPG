/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Luísa Bontempo Nunes
//     Created on 2014-07-03 ymd
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

import game.Mission.FetchMission;
import game.Mission.KillMission;
import game.Mission.Objective;
import game.Mission.VisitAreaMission;
import game.Mission.VisitCityMission;

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

public class MissionWindow extends SelectionWindow {

    static final int           WINDOW_WIDTH    = 20;
    static final int           WINDOW_HEIGHT   = 22;
    static final int           OPTION_OFFSET_X = 32;
    static final int           OPTION_OFFSET_Y = 32;

    private ArrayList<Mission> list;

    public MissionWindow(AssetManager assets, String frame, int x, int y, ArrayList<Mission> list) {
        super(assets, frame, x, y, WINDOW_WIDTH, WINDOW_HEIGHT);
        mouse.connect(MouseSource.EVENT_BUTTON_DOWN, new Observation(this, "OnButtonDown"));
        mouse.connect(MouseSource.EVENT_BUTTON_UP, new Observation(this, "OnButtonUp"));
        this.list = list;
        for (int i = 0; i < list.size(); ++i) {
            Mission m = list.get(i);
            options.add(new MissionOption(assets, i, i, x + OPTION_OFFSET_X, y + OPTION_OFFSET_Y,
                                          WINDOW_WIDTH * this.frame.getWidth() / 3 - OPTION_OFFSET_X * 2,
                                          (int) (this.frame.getHeight() * 1.2), m));
        }
    }

    @Override
    public void Swap(int index1, int index2) {

        Option o1 = options.get(index1);
        Option o2 = options.get(index2);

        Mission m1 = list.get(o1.originalIndex);
        Mission m2 = list.get(o2.originalIndex);

        list.set(o1.originalIndex, m2);
        list.set(o2.originalIndex, m1);

        o1.index = index2;
        o2.index = index1;

        options.set(index2, o1);
        options.set(index1, o2);

        int oindex1 = o1.originalIndex;
        o1.originalIndex = o2.originalIndex;
        o2.originalIndex = oindex1;
    }

    public Mission GetSelectedMission() {
        if (selected == null) {
            return null;
        }
        else {
            return ((MissionOption) selected).mission;
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

    private class MissionOption extends Option {

        Sprite  icon;
        Sprite  goldIcon;

        Text    rank;
        Text    objective;
        Text    reward;

        Mission mission;

        public MissionOption(AssetManager assets, int _index, int _originalIndex, int _baseX, int _baseY, int _w,
                             int _h, Mission _mission) {
            super(assets, _index, _originalIndex, _baseX, _baseY, _w, _h, false);
            mission = _mission;
            icon = assets.newSprite(GetIconPath(mission));
            goldIcon = assets.newSprite(Config.GOLD_ICON);
            rank = assets.newText("font/seguisb.ttf", "Rank " + mission.rank);
            objective = assets.newText("font/seguisb.ttf", GetObjective(mission));
            reward = assets.newText("font/seguisb.ttf", "" + mission.reward);
        }

        @Override
        public void Render(GameRenderers renderers, Screen screen) {
            icon.render(screen, (int) (box.x + 0.10 * box.w), box.y + box.h / 2);
            rank.render(screen, (int) (box.x + 0.10 * box.w + 40), box.y + box.h / 2 - rank.getHeight() / 2,
                        Corner.TOP_LEFT);
            objective.render(screen, (int) (box.x + 0.10 * box.w + 40), box.y + box.h / 2 + rank.getHeight() / 2,
                             Corner.TOP_LEFT);
            goldIcon.render(screen, swapBox.x - goldIcon.getWidth(), swapBox.y, Corner.TOP_LEFT);
            reward.render(screen, swapBox.x, swapBox.y, Corner.TOP_LEFT);
        }

        @Override
        public void CheckClick(int x, int y) {
            if (box.IsInside(x, y)) {
                selected = true;
            }
        }

        private String GetObjective(Mission m) {
            String message = "";
            if (m.objective == Objective.KILL) {
                KillMission km = (KillMission) m;
                message += "Kill " + km.amount + " " + Enemy.GetType(km.enemyID);
            }
            else if (m.objective == Objective.FETCH) {
                FetchMission fm = (FetchMission) m;
                message += "Find one " + Item.GetItem(fm.itemID).GetName();
            }
            else if (m.objective == Objective.VISIT_AREA) {
                VisitAreaMission vam = (VisitAreaMission) m;
                message += "Visit the " + Area.GetArea(vam.areaID).GetName();
            }
            else if (m.objective == Objective.VISIT_CITY) {
                VisitCityMission vcm = (VisitCityMission) m;
                message += "Visit " + vcm.cityName;
            }
            return message;
        }

        private String GetIconPath(Mission m) {
            switch (m.objective) {
                case FETCH:
                    return "img/craft.png";
                case VISIT_AREA:
                    return "img/visitarea.png";
                case VISIT_CITY:
                    return "img/visitcity.png";
                case KILL:
                default:
                    return "img/weapon.png";
            }
        }
    }
}
