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

import game.Item.ItemSlot;

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

public class EntityWindow extends SelectionWindow {

    static final int          WINDOW_WIDTH    = 20;
    static final int          WINDOW_HEIGHT   = 22;
    static final int          OPTION_OFFSET_X = 32;
    static final int          OPTION_OFFSET_Y = 32;

    private ArrayList<Entity> list;

    public EntityWindow(AssetManager assets, String frame, int x, int y, ArrayList<Entity> list, boolean swappable,
                        boolean equippable) {
        super(assets, frame, x, y, WINDOW_WIDTH, WINDOW_HEIGHT);
        mouse.connect(MouseSource.EVENT_BUTTON_DOWN, new Observation(this, "OnButtonDown"));
        mouse.connect(MouseSource.EVENT_BUTTON_UP, new Observation(this, "OnButtonUp"));
        this.list = list;
        for (int i = 0; i < list.size(); ++i) {
            Entity e = list.get(i);
            options.add(new EntityOption(assets, i, i, x + OPTION_OFFSET_X, y + OPTION_OFFSET_Y,
                                         WINDOW_WIDTH * this.frame.getWidth() / 3 - OPTION_OFFSET_X * 2,
                                         (int) (this.frame.getHeight() * 1.2),
                                         swappable, equippable, e));
        }
    }

    @Override
    public void Swap(int index1, int index2) {
        Option o1 = options.get(index1);
        Option o2 = options.get(index2);

        Entity e1 = list.get(o1.originalIndex);
        Entity e2 = list.get(o2.originalIndex);

        list.set(o1.originalIndex, e2);
        list.set(o2.originalIndex, e1);

        o1.index = index2;
        o2.index = index1;

        options.set(index2, o1);
        options.set(index1, o2);

        int oindex1 = o1.originalIndex;
        o1.originalIndex = o2.originalIndex;
        o2.originalIndex = oindex1;
    }

    public Entity GetSelectedEntity() {
        if (selected == null) {
            return null;
        }
        else {
            return ((EntityOption) selected).entity;
        }
    }

    public ItemSlot GetSelectedSlot() {
        if (selected == null) {
            return ItemSlot.NONE;
        }
        else {
            return ((EntityOption) selected).slot;
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

    private class EntityOption extends Option {

        Sprite   icon;
        Sprite   weaponIcon;
        Sprite   armorIcon;
        Sprite   extraIcon;

        Rect     weaponBox;
        Rect     armorBox;
        Rect     extraBox;

        Text     name;
        Text     stats1;
        Text     stats2;

        Entity   entity;
        ItemSlot slot;

        boolean  equippable;

        public EntityOption(AssetManager assets, int _index, int _originalIndex, int _baseX, int _baseY, int _w,
                            int _h, boolean _swappable, boolean _equippable, Entity _entity) {
            super(assets, _index, _originalIndex, _baseX, _baseY, _w, _h, _swappable);
            entity = _entity;
            icon = entity.sp;
            equippable = _equippable;
            if (equippable) {
                weaponIcon = assets.newSprite("img/equipweapon.png");
                armorIcon = assets.newSprite("img/equiparmor.png");
                extraIcon = assets.newSprite("img/equipextra.png");
            }
            name = assets.newText("font/seguisb.ttf", entity.name + ", the " + entity.className + " (JL " +
                                                      entity.jobLevel +
                                                      ", " + entity.jobExp + "/" +
                                                      Entity.GetExpForLevelUp(entity.jobLevel) + ")");
            Item weapon = entity.equipment.Get(ItemSlot.WEAPON);
            stats1 = assets.newText("font/seguisb.ttf", entity.stats.HP + " HP / " + entity.stats.MP + " MP / Range " +
                                                        weapon.GetRange());
            stats2 = assets.newText("font/seguisb.ttf", entity.stats.atk + " Atk / " + entity.stats.def + " Def / " +
                                                        entity.stats.mag + " Mag / " + entity.stats.res + " Res / " +
                                                        entity.stats.spd + " Spd");
            slot = ItemSlot.NONE;

            swapBox.y -= swapBox.w / 2;
            armorBox = swapBox.clone();
            armorBox.y += swapBox.h;
            weaponBox = armorBox.clone();
            weaponBox.x -= weaponBox.w;
            extraBox = armorBox.clone();
            extraBox.x += extraBox.w;
        }

        @Override
        public void Render(GameRenderers renderers, Screen screen) {
            if (swappable) {
                swapIcon.render(screen, swapBox.x, swapBox.y, Corner.TOP_LEFT);
            }
            if (equippable) {
                weaponIcon.render(screen, weaponBox.x, weaponBox.y, Corner.TOP_LEFT);
                armorIcon.render(screen, armorBox.x, armorBox.y, Corner.TOP_LEFT);
                extraIcon.render(screen, extraBox.x, extraBox.y, Corner.TOP_LEFT);
            }
            icon.render(screen, (float) (box.x + 0.08 * box.w), (float) (box.y + box.h / 2), Corner.CENTER,
                        1.0f, 0.0f, 1.0f, 1.0f);
            name.render(screen, (int) (box.x + 0.10 * box.w + icon.getWidth() / 3),
                        box.y + box.h / 2 - 3 * (name.getHeight() / 2), Corner.TOP_LEFT);
            stats1.render(screen, (int) (box.x + 0.10 * box.w + icon.getWidth() / 3),
                          box.y + box.h / 2 - name.getHeight() / 2, Corner.TOP_LEFT);
            stats2.render(screen, (int) (box.x + 0.10 * box.w + icon.getWidth() / 3),
                          box.y + box.h / 2 + name.getHeight() / 2, Corner.TOP_LEFT);
        }

        @Override
        public void CheckClick(int x, int y) {
            if (box.IsInside(x, y)) {
                if (swapBox.IsInside(x, y)) {
                    requestedSwap = true;
                }
                else if (weaponBox.IsInside(x, y)) {
                    selected = true;
                    slot = ItemSlot.WEAPON;
                }
                else if (armorBox.IsInside(x, y)) {
                    selected = true;
                    slot = ItemSlot.ARMOR;
                }
                else if (extraBox.IsInside(x, y)) {
                    selected = true;
                    slot = ItemSlot.EXTRA;
                }
                else {
                    selected = true;
                    slot = ItemSlot.NONE;
                }
            }
        }

        @Override
        public void RecalculateBoxes() {
            super.RecalculateBoxes();
            swapBox.y -= swapBox.w / 2;
            armorBox.SetXY(swapBox.x, swapBox.y + swapBox.h);
            weaponBox.SetXY(armorBox.x - weaponBox.w, armorBox.y);
            extraBox.SetXY(armorBox.x + extraBox.w, armorBox.y);
        }

        @Override
        public void Reset() {
            super.Reset();
            stats1.setText(entity.stats.HP + " HP / " + entity.stats.MP + " MP / Range " +
                           entity.equipment.Get(ItemSlot.WEAPON).GetRange());
            stats2.setText(entity.stats.atk + " Atk / " + entity.stats.def + " Def / " +
                           entity.stats.mag + " Mag / " + entity.stats.res + " Res / " +
                           entity.stats.spd + " Spd");
        }

        @Override
        public String toString() {
            return entity.name;
        }
    }
}
