/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Luísa Bontempo Nunes
//     Created on 2014-06-07 ymd
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

public class EquipSet {

    private Item item[];

    public EquipSet() {
        item = new Item[3];
        Set(ItemSlot.WEAPON, 000);
        Set(ItemSlot.ARMOR, 000);
        Set(ItemSlot.EXTRA, 000);
    }

    public EquipSet(int weapon, int armor, int extra) {
        item = new Item[3];
        Set(ItemSlot.WEAPON, weapon);
        Set(ItemSlot.ARMOR, armor);
        Set(ItemSlot.EXTRA, extra);
    }

    public Item Get(ItemSlot slot) {
        switch (slot) {
            case WEAPON:
                return item[0];
            case ARMOR:
                return item[1];
            case EXTRA:
                return item[2];
            case NONE:
                return null;
        }
        return null;
    }

    public void Set(ItemSlot slot, int itemID) {
        switch (slot) {
            case WEAPON:
                item[0] = Item.GetItem(itemID);
                break;
            case ARMOR:
                item[1] = Item.GetItem(itemID);
                break;
            case EXTRA:
                item[2] = Item.GetItem(itemID);
                break;
            case NONE:
                break;
        }
    }

    public void Set(int weapon, int armor, int extra) {
        item[0] = Item.GetItem(weapon);
        item[1] = Item.GetItem(armor);
        item[2] = Item.GetItem(extra);
    }

    @Override
    public String toString() {
        return "" + item[0].GetID() + " " + item[1].GetID() + " " + item[2].GetID();
    }
}
