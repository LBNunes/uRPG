/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Luísa Bontempo Nunes
//     Created on 2014-05-26 ymd
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

import game.Classes.ClassID;
import game.Item.ItemSlot;

import org.unbiquitous.uImpala.engine.asset.AssetManager;
import org.unbiquitous.uImpala.engine.asset.Sprite;
import org.unbiquitous.uImpala.engine.core.GameObject;
import org.unbiquitous.uImpala.engine.core.GameRenderers;

public class Entity extends GameObject {

    public Point    pos;
    public Sprite   sp;
    public Stats    stats;
    public String   name;
    public String   className;
    public ClassID  classID;
    public EquipSet equipment;
    public int      moveRange;
    public int      jobLevel;
    public int      enemyID;
    public int      currentHP;
    public int      currentMP;
    public int      turnTimer;
    public boolean  playerUnit;

    public Entity(AssetManager assets, String name, ClassID classID, int jobLevel) {
        playerUnit = true;

        pos = new Point();
        sp = assets.newSprite(Classes.GetClassSprite(classID));
        this.name = name;
        this.className = Classes.GetClassName(classID);
        this.classID = classID;
        equipment = new EquipSet();

        this.jobLevel = jobLevel;

        enemyID = 000;

        moveRange = Classes.GetMoveRange(classID);

        RecalculateStats();
        FullHeal();

        turnTimer = 0;
    }

    public Entity(AssetManager assets, int enemyID, int jobLevel) {
        playerUnit = false;

        pos = new Point();
        sp = assets.newSprite(Enemies.GetSprite(enemyID));

        name = Enemies.GetName();
        className = Enemies.GetType(enemyID);

        classID = Enemies.GetClass(enemyID);
        equipment = Enemies.GetEquipment(jobLevel);

        this.enemyID = enemyID;
        this.jobLevel = jobLevel;

        moveRange = Enemies.GetMoveRange(enemyID);

        RecalculateStats();
        FullHeal();

        turnTimer = 0;
    }

    public void FullHeal() {
        currentHP = stats.HP;
        currentMP = stats.MP;
    }

    public void RecalculateStats() {
        Stats base;
        if (playerUnit) {
            base = Classes.GetBaseStats(classID);
        }
        else {
            base = Enemies.GetBaseStats(enemyID);
        }
        int HP = base.HP +
                 equipment.Get(ItemSlot.WEAPON).GetBonusHP() +
                 equipment.Get(ItemSlot.ARMOR).GetBonusHP() +
                 equipment.Get(ItemSlot.EXTRA).GetBonusHP();
        int MP = base.MP +
                 equipment.Get(ItemSlot.WEAPON).GetBonusMP() +
                 equipment.Get(ItemSlot.ARMOR).GetBonusMP() +
                 equipment.Get(ItemSlot.EXTRA).GetBonusMP();
        int atk = base.atk +
                  equipment.Get(ItemSlot.WEAPON).GetBonusAtk() +
                  equipment.Get(ItemSlot.ARMOR).GetBonusAtk() +
                  equipment.Get(ItemSlot.EXTRA).GetBonusAtk();
        int def = base.def +
                  equipment.Get(ItemSlot.WEAPON).GetBonusDef() +
                  equipment.Get(ItemSlot.ARMOR).GetBonusDef() +
                  equipment.Get(ItemSlot.EXTRA).GetBonusDef();
        int mag = base.mag +
                  equipment.Get(ItemSlot.WEAPON).GetBonusMag() +
                  equipment.Get(ItemSlot.ARMOR).GetBonusMag() +
                  equipment.Get(ItemSlot.EXTRA).GetBonusMag();
        int res = base.res +
                  equipment.Get(ItemSlot.WEAPON).GetBonusRes() +
                  equipment.Get(ItemSlot.ARMOR).GetBonusRes() +
                  equipment.Get(ItemSlot.EXTRA).GetBonusRes();
        int spd = base.spd +
                  equipment.Get(ItemSlot.WEAPON).GetBonusSpd() +
                  equipment.Get(ItemSlot.ARMOR).GetBonusSpd() +
                  equipment.Get(ItemSlot.EXTRA).GetBonusSpd();

        this.stats = new Stats(HP, MP, atk, def, mag, res, spd);

    }

    @Override
    protected void update() {
    }

    @Override
    protected void render(GameRenderers renderers) {
    }

    @Override
    protected void wakeup(Object... args) {
    }

    @Override
    protected void destroy() {
    }

    public void Move(int x, int y) {
        pos.x = x;
        pos.y = y;
    }

}
