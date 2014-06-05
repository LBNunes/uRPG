package game;

import game.Classes.ClassID;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

public class Item {

    public enum ItemSlot {
        NONE, WEAPON, ARMOR, EXTRA
    }

    private static final HashMap<Integer, Item> table = new HashMap<Integer, Item>();

    private int                                 itemID;
    private String                              name;
    private ItemSlot                            slot;
    private ClassID                             classReq;
    private int                                 range;
    private int                                 bonusHP;
    private int                                 bonusMP;
    private int                                 bonusAtk;
    private int                                 bonusDef;
    private int                                 bonusMag;
    private int                                 bonusRes;
    private int                                 bonusSpd;

    public static Item GetItem(int itemID) {
        return table.get(itemID);
    }

    public int GetID() {
        return itemID;
    }

    public String GetName() {
        return name;
    }

    public ItemSlot GetSlot() {
        return slot;
    }

    public ClassID GetClassRequirement() {
        return classReq;
    }

    public int GetRange() {
        return range;
    }

    public int GetBonusHP() {
        return bonusHP;
    }

    public int GetBonusMP() {
        return bonusMP;
    }

    public int GetBonusAtk() {
        return bonusAtk;
    }

    public int GetBonusDef() {
        return bonusDef;
    }

    public int GetBonusMag() {
        return bonusMag;
    }

    public int GetBonusRes() {
        return bonusRes;
    }

    public int GetBonusSpd() {
        return bonusSpd;
    }

    protected Item(int id, String name, ItemSlot slot, ClassID req, int range, int hp, int mp, int atk, int def,
            int mag, int res, int spd) {

        this.itemID = id;
        this.name = name;
        this.slot = slot;
        this.classReq = req;
        this.range = range;
        this.bonusHP = hp;
        this.bonusMP = mp;
        this.bonusAtk = atk;
        this.bonusDef = def;
        this.bonusMag = mag;
        this.bonusRes = res;
        this.bonusSpd = spd;

        table.put(itemID, this);
    }

    public static void InitTable() {
        try {
            FileInputStream f = new FileInputStream(Config.ITEM_LIST);
            Scanner s = new Scanner(f);
            String line;

            int _id;
            String _name;
            ItemSlot _slot;
            ClassID _classReq;
            int _range;
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
                _name = tokenizer.nextToken().replace("_", " ");
                _slot = ItemSlot.valueOf(tokenizer.nextToken());
                _classReq = ClassID.valueOf(tokenizer.nextToken());
                _range = Integer.parseInt(tokenizer.nextToken());
                _HP = Integer.parseInt(tokenizer.nextToken());
                _MP = Integer.parseInt(tokenizer.nextToken());
                _atk = Integer.parseInt(tokenizer.nextToken());
                _def = Integer.parseInt(tokenizer.nextToken());
                _mag = Integer.parseInt(tokenizer.nextToken());
                _res = Integer.parseInt(tokenizer.nextToken());
                _spd = Integer.parseInt(tokenizer.nextToken());

                new Item(_id, _name, _slot, _classReq, _range, _HP, _MP, _atk, _def, _mag, _res, _spd);
            }

            s.close();
            f.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("FATAL ERROR: File '" + Config.ITEM_LIST + "' was not found!");
            System.exit(1);
        }
        catch (IOException e) {
            System.out.println("WARNING: File '" + Config.ITEM_LIST + "' may have been read incorrectly.");
        }
    }

    public static void DumpTable() {
        Set<Integer> keys = table.keySet();
        for (int key : keys) {
            Item item = table.get(key);
            System.out.println(item.name + " (" + item.itemID + ")");
            System.out.println(item.slot + " for " + item.classReq);
            System.out.println(item.bonusHP + "HP " + item.bonusMP + "MP " + item.bonusAtk + "Atk " +
                    item.bonusDef + "Def " + item.bonusMag + "Mag " + item.bonusRes + "Res " + item.bonusSpd + "Spd");
        }
    }
}
