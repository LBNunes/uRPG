/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Luísa Bontempo Nunes
//     Created on 2014-06-20 ymd
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

import game.Item.ItemSlot;
import game.PlayerData.Inventory;

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

public class ItemWindow extends SelectionWindow {

    static final int  WINDOW_WIDTH    = 20;
    static final int  WINDOW_HEIGHT   = 22;
    static final int  OPTION_OFFSET_X = 32;
    static final int  OPTION_OFFSET_Y = 32;

    private Inventory list;

    public ItemWindow(AssetManager assets, String frame, int x, int y, Inventory list, boolean swappable,
                      Predicate<Item> p) {
        super(assets, frame, x, y, WINDOW_WIDTH, WINDOW_HEIGHT);
        mouse.connect(MouseSource.EVENT_BUTTON_DOWN, new Observation(this, "OnButtonDown"));
        mouse.connect(MouseSource.EVENT_BUTTON_UP, new Observation(this, "OnButtonUp"));
        this.list = list;
        for (int i = 0; i < list.Size(); ++i) {
            Item item = Item.GetItem(list.itemList.get(i).item);
            if (p.Eval(item)) {
                options.add(new ItemOption(assets, options.size(), i, x + OPTION_OFFSET_X,
                                           y + OPTION_OFFSET_Y,
                                           WINDOW_WIDTH * this.frame.getWidth() / 3 - OPTION_OFFSET_X * 2,
                                           (int) (1.2 * this.frame.getHeight()),
                                           swappable, item, list.itemList.get(i).amount));
            }

        }
    }

    @Override
    public void Swap(int index1, int index2) {
        Option o1 = options.get(index1);
        Option o2 = options.get(index2);

        list.Swap(o1.originalIndex, o2.originalIndex);

        o1.index = index2;
        o2.index = index1;

        int oindex1 = o1.originalIndex;
        o1.originalIndex = o2.originalIndex;
        o2.originalIndex = oindex1;

        options.set(index2, o1);
        options.set(index1, o2);

        o1.RecalculateBoxes();
        o2.RecalculateBoxes();
    }

    public Item GetSelectedItem() {
        if (selected == null) {
            return null;
        }
        else {
            return ((ItemOption) selected).item;
        }
    }

    @Override
    protected void wakeup(Object... args) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void destroy() {
    }

    @Override
    public void OnButtonDown(Event event, Subject subject) {
        super.OnButtonDown(event, subject);
    }

    @Override
    public void OnButtonUp(Event event, Subject subject) {
        super.OnButtonUp(event, subject);
    }

    private class ItemOption extends Option {

        Sprite icon;
        Text   name;
        Text   stats1;
        Text   stats2;
        Item   item;

        public ItemOption(AssetManager assets, int _index, int _originalIndex, int _baseX, int _baseY, int _w, int _h,
                          boolean _swappable, Item _item, int amount) {
            super(assets, _index, _originalIndex, _baseX, _baseY, _w, _h, _swappable);

            item = _item;

            if (item.GetSlot() == ItemSlot.NONE) {
                if (item.IsUsable()) {
                    icon = assets.newSprite("img/potion.png");
                }
                else {
                    icon = assets.newSprite("img/craft.png");
                }
            }
            else if (item.GetSlot() == ItemSlot.WEAPON) {
                icon = assets.newSprite("img/weapon.png");
            }
            else if (item.GetSlot() == ItemSlot.ARMOR) {
                icon = assets.newSprite("img/armor.png");
            }
            else {
                icon = assets.newSprite("img/extra.png");
            }
            name = assets.newText("font/seguisb.ttf", item.GetName() + " (" + amount + ")");

            if (item.GetSlot() == ItemSlot.NONE) {
                stats1 = assets.newText("font/seguisb.ttf", GetLine1(item, item.IsUsable()));
                stats2 = null;
            }

            else {
                String line1 = GetLine1(item, false);
                String line2 = GetLine2(item, false);
                if (!line1.contentEquals("")) {
                    stats1 = assets.newText("font/seguisb.ttf", line1);
                    stats2 = assets.newText("font/seguisb.ttf", line2);
                }
                else {
                    stats1 = assets.newText("font/seguisb.ttf", line2);
                    stats2 = null;
                }
            }
        }

        @Override
        public void Render(GameRenderers renderers, Screen screen) {
            if (swappable) {
                swapIcon.render(screen, swapBox.x, swapBox.y, Corner.TOP_LEFT);
            }
            icon.render(screen, (int) (box.x + 0.10 * box.w), box.y + box.h / 2);
            if (stats2 == null) {
                name.render(screen, (int) (box.x + 0.10 * box.w + 40), box.y + box.h / 2 - name.getHeight(),
                            Corner.TOP_LEFT);
                stats1.render(screen, (int) (box.x + 0.10 * box.w + 40), box.y + box.h / 2,
                              Corner.TOP_LEFT);
            }
            else {
                name.render(screen, (int) (box.x + 0.10 * box.w + 40),
                            box.y + box.h / 2 - (3 * name.getHeight()) / 2, Corner.TOP_LEFT);
                stats1.render(screen, (int) (box.x + 0.10 * box.w + 40),
                              box.y + box.h / 2 - name.getHeight() / 2, Corner.TOP_LEFT);
                stats2.render(screen, (int) (box.x + 0.10 * box.w + 40),
                              box.y + box.h / 2 + name.getHeight() / 2, Corner.TOP_LEFT);
            }
        }

        @Override
        public void CheckClick(int x, int y) {
            if (box.IsInside(x, y)) {
                if (swapBox.IsInside(x, y)) {
                    requestedSwap = true;
                }
                else {
                    selected = true;
                }
            }
        }

        private String GetLine1(Item item, boolean usable) {
            String line = "";
            if (usable) {
                if (item.GetBonusHP() != 0 && item.GetBonusMP() != 0) {
                    line = item.GetBonusHP() + " HP / " + item.GetBonusMP() + " MP";
                }
                else if (item.GetBonusHP() != 0) {
                    line = item.GetBonusHP() + " HP";
                }
                else if (item.GetBonusMP() != 0) {
                    line = item.GetBonusMP() + " MP";
                }
                else {
                    line = "Does nothing";
                }
            }
            else if (item.GetSlot() != ItemSlot.NONE) {
                if (item.GetBonusHP() != 0 && item.GetBonusMP() != 0) {
                    line = item.GetBonusHP() + " HP / " + item.GetBonusMP() + " MP";
                    if (item.GetRange() != 0) {
                        line += "/ Range " + item.GetRange();
                    }
                }
                else if (item.GetBonusHP() != 0) {
                    line = item.GetBonusHP() + " HP";
                    if (item.GetRange() != 0) {
                        line += "/ Range " + item.GetRange();
                    }
                }
                else if (item.GetBonusMP() != 0) {
                    line = item.GetBonusMP() + " MP";
                    if (item.GetRange() != 0) {
                        line += "/ Range " + item.GetRange();
                    }
                }
                else if (item.GetRange() != 0) {
                    line = "Range " + item.GetRange();
                }
            }
            else {
                line = "Crafting material";
            }
            return line;
        }

        private String GetLine2(Item item, boolean usable) {
            String line = "";
            boolean slash = false;

            if (item.GetBonusAtk() != 0) {
                line += item.GetBonusAtk() + " Atk";
                slash = true;
            }

            if (item.GetBonusDef() != 0) {

                if (slash) {
                    line += " / ";
                }
                line += item.GetBonusDef() + " Def";
                slash = true;
            }

            if (item.GetBonusMag() != 0) {
                if (slash) {
                    line += " / ";
                }
                line += item.GetBonusMag() + " Mag";
                slash = true;
            }

            if (item.GetBonusRes() != 0) {
                if (slash) {
                    line += " / ";
                }
                line += item.GetBonusRes() + " Res";
                slash = true;
            }

            if (item.GetBonusSpd() != 0) {
                if (slash) {
                    line += " / ";
                }
                line += item.GetBonusSpd() + " Spd";
            }

            return line;
        }

        @Override
        public String toString() {
            return item.GetName();
        }
    }
}
