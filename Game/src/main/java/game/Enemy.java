/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Luísa Bontempo Nunes
//     Created on 2014-06-06 ymd
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

public class Enemy {

    private static HashMap<Integer, Enemy> table      = new HashMap<Integer, Enemy>();
    private static ArrayList<String>       enemyNames = new ArrayList<String>();

    public String                          type;
    public String                          sprite;
    public ClassID                         classID;
    public int                             moveRange;
    public Stats                           stats;
    public int                             rank;
    public boolean                         boss;
    public float                           aggro;
    public ArrayList<Loot>                 possibleLoot;

    private Enemy(String _type, String _sprite, ClassID _classID, int _moveRange,
                  int _HP, int _MP, int _atk, int _def, int _mag, int _res, int _spd,
                  int _rank, boolean _boss, float _aggro) {
        type = _type;
        sprite = _sprite;
        classID = _classID;
        moveRange = _moveRange;
        stats = new Stats(_HP, _MP, _atk, _def, _mag, _res, _spd);
        rank = _rank;
        boss = _boss;
        aggro = _aggro;
        possibleLoot = new ArrayList<Loot>();
    }

    public static Enemy GetEnemy(int enemyID) {
        return table.get(enemyID);
    }

    public static ArrayList<Integer> GetEnemy(Predicate<Enemy> p) {
        ArrayList<Integer> a = new ArrayList<Integer>();

        Set<Integer> keys = table.keySet();
        for (int key : keys) {
            if (p.Eval(table.get(key))) {
                a.add(key);
            }
        }
        return a;
    }

    public static int GetEnemyOfRank(int rank, boolean boss) {

        int selected = 1;
        int deltaRank = 9999999;
        Set<Integer> keys = table.keySet();
        for (int key : keys) {
            int enemyRank = table.get(key).rank;
            boolean isBoss = table.get(key).boss;
            if (Math.abs(enemyRank - rank) < deltaRank && isBoss == boss) {
                deltaRank = Math.abs(enemyRank - rank);
                selected = key;
            }
        }
        return selected;
    }

    public static ArrayList<Item> GetLoot(int enemyID, int enemyRank) {
        ArrayList<Item> loot = new ArrayList<Item>();
        Enemy e = table.get(enemyID);

        for (Loot l : e.possibleLoot) {
            double roll = Math.random();
            if (roll < l.probability) {
                Item item = Item.GetItem(l.itemID);
                if (item.GetRank() >= enemyRank) {
                    loot.add(item);
                }
            }
        }

        return loot;
    }

    public static Stats GetBaseStats(int enemyID) {
        return table.get(enemyID).stats;
    }

    public static String GetSprite(int enemyID) {
        return table.get(enemyID).sprite;
    }

    public static String GetType(int enemyID) {
        return table.get(enemyID).type;
    }

    public static ClassID GetClass(int enemyID) {
        return table.get(enemyID).classID;
    }

    public static String GetName() {
        return enemyNames.get((int) (enemyNames.size() * Math.random()));
    }

    public static int GetMoveRange(int enemyID) {
        return table.get(enemyID).moveRange;
    }

    public static void InitTable() {
        FileInputStream f;
        try {
            f = new FileInputStream(Config.ENEMY_DATA);
            Scanner s = new Scanner(f);
            String line;

            int _id;
            String _type;
            String _sprite;
            ClassID _class;
            int _move;
            int _HP;
            int _MP;
            int _atk;
            int _def;
            int _mag;
            int _res;
            int _spd;
            int _rank;
            boolean _boss;
            float _aggro;

            while (s.hasNextLine()) {
                line = s.nextLine();

                if (line.length() == 0 || line.charAt(0) == '#') {
                    continue;
                }

                StringTokenizer tokenizer = new StringTokenizer(line, " ");

                _id = Integer.parseInt(tokenizer.nextToken());
                _type = tokenizer.nextToken().replace("_", " ");
                _sprite = tokenizer.nextToken();
                _class = ClassID.valueOf(tokenizer.nextToken());
                _move = Integer.parseInt(tokenizer.nextToken());
                _HP = Integer.parseInt(tokenizer.nextToken());
                _MP = Integer.parseInt(tokenizer.nextToken());
                _atk = Integer.parseInt(tokenizer.nextToken());
                _def = Integer.parseInt(tokenizer.nextToken());
                _mag = Integer.parseInt(tokenizer.nextToken());
                _res = Integer.parseInt(tokenizer.nextToken());
                _spd = Integer.parseInt(tokenizer.nextToken());
                _rank = Integer.parseInt(tokenizer.nextToken());
                _boss = Integer.parseInt(tokenizer.nextToken()) != 0;
                _aggro = Float.parseFloat(tokenizer.nextToken());

                table.put(_id, new Enemy(_type, _sprite, _class, _move,
                                         _HP, _MP, _atk, _def, _mag, _res, _spd,
                                         _rank, _boss, _aggro));
            }

            s.close();
            f.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("FATAL ERROR: File '" + Config.ENEMY_DATA + "' was not found!");
            System.exit(1);
        }
        catch (IOException e) {
            System.out.println("WARNING: File '" + Config.ENEMY_DATA + "' may have been read incorrectly.");
        }
    }

    public static void InitNames() {
        FileInputStream f;
        try {
            f = new FileInputStream(Config.ENEMY_NAMES);
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

                enemyNames.add(_name);
            }

            s.close();
            f.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("FATAL ERROR: File '" + Config.ENEMY_NAMES + "' was not found!");
            System.exit(1);
        }
        catch (IOException e) {
            System.out.println("WARNING: File '" + Config.ENEMY_NAMES + "' may have been read incorrectly.");
        }
    }

    public static void InitLoot() {
        FileInputStream f;
        try {
            f = new FileInputStream(Config.LOOT_DATA);
            Scanner s = new Scanner(f);
            String line;

            int _itemID;
            int _enemy;
            float _prob;

            while (s.hasNextLine()) {
                line = s.nextLine();

                if (line.length() == 0 || line.charAt(0) == '#') {
                    continue;
                }

                StringTokenizer tokenizer = new StringTokenizer(line, " ");

                _itemID = Integer.parseInt(tokenizer.nextToken());

                while (tokenizer.hasMoreTokens()) {
                    _enemy = Integer.parseInt(tokenizer.nextToken());
                    _prob = Float.parseFloat(tokenizer.nextToken());
                    GetEnemy(_enemy).possibleLoot.add(new Loot(_itemID, _prob));
                }
            }

            s.close();
            f.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("FATAL ERROR: File '" + Config.LOOT_DATA + "' was not found!");
            System.exit(1);
        }
        catch (IOException e) {
            System.out.println("WARNING: File '" + Config.LOOT_DATA + "' may have been read incorrectly.");
        }
    }

    private static class Loot {
        public int   itemID;
        public float probability;

        public Loot(int _itemID, float _probability) {
            itemID = _itemID;
            probability = _probability;
        }
    }
}
