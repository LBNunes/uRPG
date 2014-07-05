/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Luísa Bontempo Nunes
//     Created on 2014-06-11 ymd
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
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Area {

    private static HashMap<Integer, Area> table = new HashMap<Integer, Area>();

    private String                        name;
    private String                        iconPath;
    private String                        bgPath;
    private ClassID                       classBias;
    private ArrayList<EnemySet>           enemySets;

    private Area(String _name, String _icon, String _bg, ClassID _classBias) {
        name = _name;
        iconPath = _icon;
        bgPath = _bg;
        classBias = _classBias;
        enemySets = new ArrayList<EnemySet>();
    }

    public static Area GetArea(int id) {
        return table.get(id);
    }

    public static int GetNumberOfAreas() {
        return table.size();
    }

    public static int[] GetEnemySet(int id, boolean isDay, int rank) {

        final int MAX_SELECTIONS = 3;

        ArrayList<EnemySet> list = table.get(id).enemySets;

        ArrayList<EnemySet> selections = new ArrayList<EnemySet>();
        ArrayList<Integer> set = null;

        for (int i = 0; i < MAX_SELECTIONS; ++i) {
            EnemySet selected = null;
            int deltaRank = 9999999;
            for (EnemySet e : list) {
                int setRank = 0;
                for (int enemyID : e.enemyIDs) {
                    setRank += Enemy.GetEnemy(enemyID).rank;
                }
                if (Math.abs(setRank - rank) < deltaRank && isDay == e.appearsDuringDay && !selections.contains(e)) {
                    deltaRank = Math.abs(setRank - rank);
                    selected = e;
                }
            }
            if (selected != null) {
                selections.add(selected);
            }
            selected = null;
            deltaRank = 99999999;
        }

        set = selections.get(new Random().nextInt(selections.size())).enemyIDs;

        int[] setArray = new int[set.size()];

        for (int i = 0; i < set.size(); ++i) {
            setArray[i] = set.get(i);
        }

        return setArray;
    }

    public static int[] GenerateAreaSet(String regionName) {

        int[] areas = new int[3];
        Random generator = new Random(StringHash(regionName));

        areas[0] = (Integer) table.keySet().toArray()[generator.nextInt(table.size())];
        do {
            areas[1] = (Integer) table.keySet().toArray()[generator.nextInt(table.size())];
        } while (areas[1] == areas[0]);
        do {
            areas[2] = (Integer) table.keySet().toArray()[generator.nextInt(table.size())];
        } while (areas[2] == areas[1] || areas[2] == areas[0]);

        return areas;
    }

    public static int StringHash(String s) {
        int hash = 5381;
        for (int i = 0; i < s.length(); ++i) {
            hash += hash * 5 + s.charAt(i);
        }

        return hash;
    }

    public String GetName() {
        return name;
    }

    public String GetIconPath() {
        return iconPath;
    }

    public String GetBGPath() {
        return bgPath;
    }

    public ClassID GetClassBias() {
        return classBias;
    }

    public static void InitAreas() {
        FileInputStream f;
        try {
            f = new FileInputStream(Config.AREA_DATA);
            Scanner s = new Scanner(f);
            String line;

            int _id;
            String _name;
            String _icon;
            String _bg;
            ClassID _classBias;

            while (s.hasNextLine()) {
                line = s.nextLine();

                if (line.length() == 0 || line.charAt(0) == '#') {
                    continue;
                }

                StringTokenizer tokenizer = new StringTokenizer(line, " ");

                _id = Integer.parseInt(tokenizer.nextToken());
                _name = tokenizer.nextToken().replace("_", " ");
                _icon = tokenizer.nextToken();
                _bg = tokenizer.nextToken();
                _classBias = ClassID.valueOf(tokenizer.nextToken());

                table.put(_id, new Area(_name, _icon, _bg, _classBias));
            }

            s.close();
            f.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("FATAL ERROR: File '" + Config.AREA_DATA + "' was not found!");
            System.exit(1);
        }
        catch (IOException e) {
            System.out.println("WARNING: File '" + Config.AREA_DATA + "' may have been read incorrectly.");
        }
    }

    public static void InitEnemySets() {
        FileInputStream f;
        try {
            f = new FileInputStream(Config.ENEMY_SET_DATA);
            Scanner s = new Scanner(f);
            String line;

            int _id;
            boolean _isDay;
            int _enemyID;
            ArrayList<Integer> _list;

            while (s.hasNextLine()) {
                line = s.nextLine();

                if (line.length() == 0 || line.charAt(0) == '#') {
                    continue;
                }

                StringTokenizer tokenizer = new StringTokenizer(line, " ");

                _id = Integer.parseInt(tokenizer.nextToken());
                _isDay = Integer.parseInt(tokenizer.nextToken()) != 0;
                _list = new ArrayList<Integer>();
                while (tokenizer.hasMoreTokens()) {
                    _enemyID = Integer.parseInt(tokenizer.nextToken());
                    _list.add(_enemyID);
                }

                table.get(_id).enemySets.add(new EnemySet(_list, _isDay));
            }

            s.close();
            f.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("FATAL ERROR: File '" + Config.ENEMY_SET_DATA + "' was not found!");
            System.exit(1);
        }
        catch (IOException e) {
            System.out.println("WARNING: File '" + Config.ENEMY_SET_DATA + "' may have been read incorrectly.");
        }
    }

    private static class EnemySet {
        public ArrayList<Integer> enemyIDs;
        public boolean            appearsDuringDay;

        public EnemySet(ArrayList<Integer> _enemyIDs, boolean _appearsDuringDay) {
            enemyIDs = _enemyIDs;
            appearsDuringDay = _appearsDuringDay;
        }
    }
}
