/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Luísa Bontempo Nunes
//     Created on 2014-05-29 ymd
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Classes {

    public enum ClassID {
        WARRIOR, PALADIN, BERSERKER, MAGE, ARCHMAGE, CLERIC, ROGUE, HUNTER, NINJA, NONE
    }

    private static HashMap<ClassID, ClassData> table = new HashMap<ClassID, ClassData>();

    public static Stats GetBaseStats(ClassID id) {
        return table.get(id).stats;
    }

    public static String GetClassName(ClassID id) {
        switch (id) {
            case WARRIOR:
                return "Warrior";
            case PALADIN:
                return "Paladin";
            case BERSERKER:
                return "Berserker";
            case MAGE:
                return "Mage";
            case ARCHMAGE:
                return "Archmage";
            case CLERIC:
                return "Cleric";
            case ROGUE:
                return "Rogue";
            case HUNTER:
                return "Hunter";
            case NINJA:
                return "Ninja";
            case NONE:
                return "";
        }
        return null;
    }

    public static String GetClassSprite(ClassID id) {
        return table.get(id).sprite;
    }

    public static int GetMoveRange(ClassID id) {
        return table.get(id).moveRange;
    }

    public static boolean CanEquip(ClassID charClass, Item item) {

        if (item.GetSlot() == ItemSlot.NONE)
            return false;

        switch (item.GetClassRequirement()) {
            case WARRIOR:
                return charClass == ClassID.WARRIOR ||
                       charClass == ClassID.PALADIN ||
                       charClass == ClassID.BERSERKER;
            case PALADIN:
                return charClass == ClassID.PALADIN;
            case BERSERKER:
                return charClass == ClassID.BERSERKER;
            case MAGE:
                return charClass == ClassID.MAGE ||
                       charClass == ClassID.ARCHMAGE ||
                       charClass == ClassID.CLERIC;
            case ARCHMAGE:
                return charClass == ClassID.ARCHMAGE;
            case CLERIC:
                return charClass == ClassID.CLERIC;
            case ROGUE:
                return charClass == ClassID.ROGUE ||
                       charClass == ClassID.HUNTER ||
                       charClass == ClassID.NINJA;
            case HUNTER:
                return charClass == ClassID.HUNTER;
            case NINJA:
                return charClass == ClassID.NINJA;
            case NONE:
                return true;
        }
        return false;
    }

    public static boolean IsCritical(Entity attacker) {

        double critFactor = 1.0f - table.get(attacker.classID).critRate;
        return critFactor < Math.random();
    }

    public static int GetPhysicalFactor(Entity attacker, Entity defender) {
        // Possible adjustments: divide levels, square one of the factors, multiply by constant
        float jobMultiplier = 3 * attacker.jobLevel;
        float statsFactor = (attacker.stats.atk) / (float) (defender.stats.def);
        float randomFactor = 80 + (int) (Math.random() * ((120 - 80) + 1));
        return (int) (jobMultiplier * statsFactor * randomFactor / 100);
    }

    public static int GetMagicalFactor(Entity caster, Entity defender) {
        // Possible adjustment: Include Ability Power here. Magic will probably end up too powerful
        return (int) (caster.jobLevel * caster.stats.mag / (float) (defender.stats.res));
    }

    public static void InitStats() {

        FileInputStream f;
        try {
            f = new FileInputStream(Config.CLASS_DATA);
            Scanner s = new Scanner(f);
            String line;

            ClassID _class;
            String _sprite;
            int _move;
            int _HP;
            int _MP;
            int _atk;
            int _def;
            int _mag;
            int _res;
            int _spd;
            double _critRate;

            while (s.hasNextLine()) {
                line = s.nextLine();

                if (line.length() == 0 || line.charAt(0) == '#') {
                    continue;
                }

                StringTokenizer tokenizer = new StringTokenizer(line, " ");

                _class = ClassID.valueOf(tokenizer.nextToken());
                _sprite = tokenizer.nextToken();
                _move = Integer.parseInt(tokenizer.nextToken());
                _HP = Integer.parseInt(tokenizer.nextToken());
                _MP = Integer.parseInt(tokenizer.nextToken());
                _atk = Integer.parseInt(tokenizer.nextToken());
                _def = Integer.parseInt(tokenizer.nextToken());
                _mag = Integer.parseInt(tokenizer.nextToken());
                _res = Integer.parseInt(tokenizer.nextToken());
                _spd = Integer.parseInt(tokenizer.nextToken());
                _critRate = Double.parseDouble(tokenizer.nextToken());

                table.put(_class, new ClassData(_sprite, _HP, _MP, _atk, _def, _mag, _res, _spd, _move, _critRate));
            }

            s.close();
            f.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("FATAL ERROR: File '" + Config.CLASS_DATA + "' was not found!");
            System.exit(1);
        }
        catch (IOException e) {
            System.out.println("WARNING: File '" + Config.CLASS_DATA + "' may have been read incorrectly.");
        }
    }

    private static class ClassData {
        public String sprite;
        public Stats  stats;
        public int    moveRange;
        public double critRate;

        public ClassData(String _sprite, int _HP, int _MP, int _atk, int _def, int _mag, int _res, int _spd,
                         int _moveRange, double _critRate) {

            sprite = _sprite;
            stats = new Stats(_HP, _MP, _atk, _def, _mag, _res, _spd);
            moveRange = _moveRange;
            critRate = _critRate;
        }
    }
}
