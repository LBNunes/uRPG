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
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.util.Corner;

public class ItemWindow extends SelectionWindow {

    static final int  WINDOW_WIDTH    = 13;
    static final int  WINDOW_HEIGHT   = 22;
    static final int  OPTION_OFFSET_X = 32;
    static final int  OPTION_OFFSET_Y = 32;

    private Inventory list;

    public ItemWindow(AssetManager assets, String frame, int x, int y, Inventory list, boolean swappable,
                      Predicate<Item> p) {
        super(assets, frame, x, y, WINDOW_WIDTH, WINDOW_HEIGHT);

        this.list = list;
        for (int i = 0; i < list.Size(); ++i) {
            Item item = Item.GetItem(list.itemList.get(i).item);
            if (p.Eval(item)) {
                options.add(new ItemOption(assets, i, options.size(), x + OPTION_OFFSET_X, y + OPTION_OFFSET_Y,
                                           WINDOW_WIDTH * this.frame.getWidth() / 3 - OPTION_OFFSET_X * 2,
                                           2 * this.frame.getHeight() / 3,
                                           swappable, item));
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

        options.set(index1, o2);
        options.set(index2, o1);
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

    private class ItemOption extends Option {

        Sprite icon;
        Text   name;
        Text   stats1;
        Text   stats2;
        Item   item;

        public ItemOption(AssetManager assets, int _index, int _originalIndex, int _baseX, int _baseY, int _w, int _h,
                          boolean _swappable, Item _item) {
            super(assets, _index, _originalIndex, _baseX, _baseY, _w, _h, _swappable);

            item = _item;

            if (item.GetSlot() == ItemSlot.NONE) {
                if (item.IsUseable()) {
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
            name = assets.newText("font/seguisb.ttf", item.GetName());

            if (item.GetSlot() == ItemSlot.NONE) {
                stats1 = assets.newText("font/seguisb.ttf", GetLine1(item, true));
                stats2 = null;
            }

            else {
                stats1 = assets.newText("font/seguisb.ttf", GetLine1(item, false));
                stats2 = assets.newText("font/seguisb.ttf", GetLine2(item, false));
            }
        }

        @Override
        public void Render(GameRenderers renderers, Screen screen) {
            if (swappable) {
                swapIcon.render(screen, swapBox.x, swapBox.y, Corner.TOP_LEFT);
            }
            if (stats2 == null) {
                icon.render(screen, (int) (box.x + 0.10 * box.w), box.y + box.h / 2 - name.getHeight() / 2);
                name.render(screen, (int) (box.x + 0.10 * box.w + icon.getWidth()),
                            box.y + box.h / 2 - name.getHeight() / 2);
                stats1.render(screen, (int) (box.x + 0.10 * box.w + icon.getWidth()),
                              box.y + box.h / 2 + name.getHeight() / 2);
            }
            else {
                icon.render(screen, (int) (box.x + 0.10 * box.w), box.y + box.h / 2 - name.getHeight());
                name.render(screen, (int) (box.x + 0.10 * box.w + icon.getWidth()),
                            box.y + box.h / 2 - name.getHeight());
                stats1.render(screen, (int) (box.x + 0.10 * box.w + icon.getWidth()),
                              box.y + box.h / 2);
                stats2.render(screen, (int) (box.x + 0.10 * box.w + icon.getWidth()),
                              box.y + box.h / 2 + name.getHeight());
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
            String line;
            if (usable) {
                if (item.GetBonusHP() > 0 && item.GetBonusMP() > 0) {
                    line = item.GetBonusHP() + " HP / " + item.GetBonusMP() + " MP";
                }
                else if (item.GetBonusHP() > 0) {
                    line = item.GetBonusHP() + " HP";
                }
                else if (item.GetBonusMP() > 0) {
                    line = item.GetBonusMP() + " MP";
                }
                else {
                    line = "Does nothing";
                }
            }
            else if (item.GetSlot() != ItemSlot.NONE) {
                if (item.GetBonusHP() > 0 && item.GetBonusMP() > 0) {
                    line = item.GetBonusHP() + " HP / " + item.GetBonusMP() + " MP / " + "Range " + item.GetRange();
                }
                else if (item.GetBonusHP() > 0) {
                    line = item.GetBonusHP() + " HP / " + "Range " + item.GetRange();
                }
                else if (item.GetBonusMP() > 0) {
                    line = item.GetBonusMP() + " MP /" + "Range " + item.GetRange();
                }
                else {
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
            boolean started = false;

            if (item.GetBonusAtk() > 0) {
                line += item.GetBonusAtk() + " Atk";
                started = true;
            }

            if (started) {
                line += " / ";
            }

            if (item.GetBonusDef() > 0) {
                line += item.GetBonusDef() + " Def";
                started = true;
            }

            if (started) {
                line += " / ";
            }

            if (item.GetBonusMag() > 0) {
                line += item.GetBonusMag() + " Mag";
                started = true;
            }

            if (started) {
                line += " / ";
            }

            if (item.GetBonusRes() > 0) {
                line += item.GetBonusRes() + " Res";
                started = true;
            }

            if (started) {
                line += " / ";
            }

            if (item.GetBonusSpd() > 0) {
                line += item.GetBonusSpd() + " Spd";
            }

            return line;
        }
    }
}
