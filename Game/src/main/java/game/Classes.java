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

    private static HashMap<ClassID, Stats> baseStatTable = new HashMap<ClassID, Stats>();

    public static Stats GetBaseStats(ClassID id) {
        return baseStatTable.get(id);
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

    public boolean IsCritical(Entity attacker) {
        double critFactor;
        switch (attacker.charClass) {
            case NINJA:
                critFactor = 0.70;
                break;
            case ROGUE:
            case HUNTER:
            case BERSERKER:
                critFactor = 0.80;
                break;
            default:
                critFactor = 0.90;
                break;
        }
        return critFactor < Math.random();
    }

    public static int GetPhysicalFactor(Entity attacker, Entity defender) {
        // Possible adjustments: divide levels, square one of the factors, multiply by constant
        return (int) (attacker.jobLevel * (attacker.stats.atk) / (float) (attacker.stats.def));
    }

    public static int GetMagicalFactor(Entity caster, Entity defender) {
        // Possible adjustment: Include Ability Power here. Magic will probably end up too powerful
        return (int) (caster.jobLevel * caster.stats.mag / (float) (defender.stats.res));
    }

    public static void InitStats() {

        FileInputStream f;
        try {
            f = new FileInputStream(Config.BASE_STAT_LIST);
            Scanner s = new Scanner(f);
            String line;

            ClassID _class;
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

                _class = ClassID.valueOf(tokenizer.nextToken());
                _HP = Integer.parseInt(tokenizer.nextToken());
                _MP = Integer.parseInt(tokenizer.nextToken());
                _atk = Integer.parseInt(tokenizer.nextToken());
                _def = Integer.parseInt(tokenizer.nextToken());
                _mag = Integer.parseInt(tokenizer.nextToken());
                _res = Integer.parseInt(tokenizer.nextToken());
                _spd = Integer.parseInt(tokenizer.nextToken());

                baseStatTable.put(_class, new Stats(_HP, _MP, _atk, _def, _mag, _res, _spd));
            }

            s.close();
            f.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("FATAL ERROR: File '" + Config.BASE_STAT_LIST + "' was not found!");
            System.exit(1);
        }
        catch (IOException e) {
            System.out.println("WARNING: File '" + Config.BASE_STAT_LIST + "' may have been read incorrectly.");
        }
    }
}
