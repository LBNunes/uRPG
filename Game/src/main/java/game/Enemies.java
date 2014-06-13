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
import java.util.StringTokenizer;

public class Enemies {

    private static HashMap<Integer, EnemyData> table      = new HashMap<Integer, EnemyData>();
    private static ArrayList<String>           enemyNames = new ArrayList<String>();

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

    public static EquipSet GetEquipment(int jobLevel) {
        // TODO Auto-generated method stub
        return new EquipSet(0, 0, 0);
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

                table.put(_id, new EnemyData(_type, _sprite, _class, _move, _HP, _MP, _atk, _def, _mag, _res, _spd));
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

    private static class EnemyData {
        public String  type;
        public String  sprite;
        public ClassID classID;
        public int     moveRange;
        public Stats   stats;

        public EnemyData(String _type, String _sprite, ClassID _classID, int _moveRange,
                         int _HP, int _MP, int _atk, int _def, int _mag, int _res, int _spd) {
            type = _type;
            sprite = _sprite;
            classID = _classID;
            moveRange = _moveRange;
            stats = new Stats(_HP, _MP, _atk, _def, _mag, _res, _spd);
        }
    }
}
