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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.unbiquitous.uImpala.engine.asset.AssetManager;
import org.unbiquitous.uImpala.engine.asset.Sprite;
import org.unbiquitous.uImpala.engine.asset.Text;
import org.unbiquitous.uImpala.engine.io.MouseSource;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.util.Color;
import org.unbiquitous.uImpala.util.Corner;

public class Entity {

    public Point                      pos;
    public Sprite                     sp;
    public Stats                      stats;
    public String                     name;
    public String                     className;
    public ClassID                    classID;
    public EquipSet                   equipment;
    public int                        moveRange;
    public int                        jobLevel;
    public int                        jobExp;
    public int                        enemyID;
    public int                        currentHP;
    public int                        currentMP;
    public int                        turnTimer;
    public boolean                    playerUnit;

    public Text                       description1;
    public Text                       description2;
    public Text                       description3;
    public Gauge                      hpGauge;
    public Gauge                      mpGauge;

    private static ArrayList<String>  names = new ArrayList<String>();
    private static ArrayList<Integer> exp   = new ArrayList<Integer>();

    public Entity(AssetManager assets, String name, ClassID classID, int jobLevel) {
        playerUnit = true;

        pos = new Point();
        sp = assets.newSprite(Classes.GetClassSprite(classID));
        this.name = name;
        this.className = Classes.GetClassName(classID);
        this.classID = classID;
        equipment = new EquipSet();

        this.jobLevel = jobLevel;
        jobExp = 0;

        enemyID = 000;

        moveRange = Classes.GetMoveRange(classID);

        RecalculateStats();

        turnTimer = 0;

        hpGauge = new Gauge(assets, stats.HP, Color.green);
        mpGauge = new Gauge(assets, stats.MP, Color.blue);
        description1 = assets.newText(Config.DESCRIPTION_FONT, name);
        description2 = assets.newText(Config.DESCRIPTION_FONT, className + " (" + jobLevel + ")");
        description3 = assets.newText(Config.DESCRIPTION_FONT,
                                      "HP " + currentHP + "/" + stats.HP + " " + currentMP + "/" + stats.MP);
        description1.options(null, Config.DESCRIPTION_FONT_SIZE, null);
        description2.options(null, Config.DESCRIPTION_FONT_SIZE, null);
        description3.options(null, Config.DESCRIPTION_FONT_SIZE, null);
        FullHeal();
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
        jobExp = 0;

        moveRange = Enemies.GetMoveRange(enemyID);

        RecalculateStats();

        turnTimer = 0;

        hpGauge = new Gauge(assets, stats.HP, Color.red);
        mpGauge = new Gauge(assets, stats.MP, Color.blue);
        description1 = assets.newText(Config.DESCRIPTION_FONT, name);
        description2 = assets.newText(Config.DESCRIPTION_FONT, className + " (" + jobLevel + ")");
        description3 = assets.newText(Config.DESCRIPTION_FONT,
                                      "HP " + currentHP + "/" + stats.HP + " " + currentMP + "/" + stats.MP);
        description1.options(null, Config.DESCRIPTION_FONT_SIZE, null);
        description2.options(null, Config.DESCRIPTION_FONT_SIZE, null);
        description3.options(null, Config.DESCRIPTION_FONT_SIZE, null);

        FullHeal();
    }

    public void FullHeal() {
        currentHP = stats.HP;
        currentMP = stats.MP;
        hpGauge.Update(currentHP);
        mpGauge.Update(currentMP);
        description3.setText(currentHP + "/" + stats.HP + " " + currentMP + "/" + stats.MP);
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

    public void Update() {

    }

    public void Render(Screen screen, int x, int y) {

        MouseSource mouse = screen.getMouse();
        Rect box = new Rect(x - sp.getWidth() / 2, y - sp.getHeight() / 2, sp.getWidth(), sp.getHeight());

        if (!IsDead()) {
            sp.render(screen, x, y, Corner.CENTER);
        }
        else {
            sp.render(screen, x, y, Corner.CENTER, 1.0f, (playerUnit ? -90 : 90));
        }

        if (box.IsInside(mouse.getX(), mouse.getY())) {

            hpGauge.Render(screen, x, (int) (y - (sp.getHeight() / 2 + mpGauge.GetHeight() * 1.5)));
            mpGauge.Render(screen, x, (int) (y - (sp.getHeight() / 2 + mpGauge.GetHeight() * 0.5)));

            description1.render(screen, x, y, Corner.TOP_LEFT);
            description2.render(screen, x, y + description1.getHeight(), Corner.TOP_LEFT);
            description3.render(screen, x, y + 2 * description1.getHeight(), Corner.TOP_LEFT);
        }

    }

    public void Move(int x, int y) {
        pos.x = x;
        pos.y = y;
    }

    public boolean IsDead() {
        return currentHP <= 0;
    }

    public boolean GiveJobExp(int exp) {
        jobExp += exp;
        int levelUp = GetExpForLevelUp(jobLevel);
        boolean leveled = false;
        while (jobExp > levelUp) {
            jobLevel++;
            jobExp -= levelUp;
            levelUp = GetExpForLevelUp(jobLevel);
            leveled = true;
        }

        return leveled;
    }

    public void Damage(int damage) {
        currentHP -= damage;
        if (currentHP < 0) {
            currentHP = 0;
        }

        hpGauge.Update(currentHP);
        description3.setText(currentHP + "/" + stats.HP + " " + currentMP + "/" + stats.MP);
    }

    public void SpendMP(int cost) {
        currentMP -= cost;
        if (currentMP < 0) {
            currentMP = 0;
        }

        mpGauge.Update(currentMP);
        description3.setText(currentHP + "/" + stats.HP + " " + currentMP + "/" + stats.MP);
    }

    public static String GetRandomName() {
        return names.get((int) (names.size() * Math.random()));
    }

    public static int GetExpForLevelUp(int jobLevel) {
        return exp.get(jobLevel);
    }

    public static void InitNames() {
        FileInputStream f;
        try {
            f = new FileInputStream(Config.PLAYER_NAMES);
            Scanner s = new Scanner(f);
            String line;

            String _name;

            while (s.hasNextLine()) {
                line = s.nextLine();

                if (line.length() == 0 || line.charAt(0) == '#') {
                    continue;
                }

                StringTokenizer tokenizer = new StringTokenizer(line, " ");

                _name = tokenizer.nextToken();

                names.add(_name);
            }

            s.close();
            f.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("FATAL ERROR: File '" + Config.PLAYER_NAMES + "' was not found!");
            System.exit(1);
        }
        catch (IOException e) {
            System.out.println("WARNING: File '" + Config.PLAYER_NAMES + "' may have been read incorrectly.");
        }
    }

    public static void InitExp() {
        FileInputStream f;
        try {
            f = new FileInputStream(Config.EXP_TABLE);
            Scanner s = new Scanner(f);
            String line;

            int _exp;

            while (s.hasNextLine()) {
                line = s.nextLine();

                if (line.length() == 0 || line.charAt(0) == '#') {
                    continue;
                }

                StringTokenizer tokenizer = new StringTokenizer(line, " ");

                _exp = Integer.parseInt(tokenizer.nextToken());

                exp.add(_exp);
            }

            s.close();
            f.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("FATAL ERROR: File '" + Config.EXP_TABLE + "' was not found!");
            System.exit(1);
        }
        catch (IOException e) {
            System.out.println("WARNING: File '" + Config.EXP_TABLE + "' may have been read incorrectly.");
        }
    }
}
