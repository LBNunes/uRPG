package game;

import game.Classes.ClassID;
import game.Item.ItemSlot;

import org.unbiquitous.uImpala.engine.asset.AssetManager;
import org.unbiquitous.uImpala.engine.asset.Sprite;
import org.unbiquitous.uImpala.engine.core.GameObject;
import org.unbiquitous.uImpala.engine.core.GameRenderers;

public class Entity extends GameObject {

    public Point   pos;
    public Sprite  sp;
    public Stats   stats;
    public String  name;
    public String  className;
    public ClassID charClass;
    public Item    equipment[];
    public int     jobLevel;
    public int     currentHP;
    public int     currentMP;

    public Entity(AssetManager assets, String sprite, String name, ClassID charClass, String className, int jobLevel) {
        pos = new Point();
        sp = assets.newSprite(sprite);
        this.name = name;
        this.className = className;
        this.charClass = charClass;
        equipment = new Item[3];
        SetEquipment(ItemSlot.WEAPON, 000);
        SetEquipment(ItemSlot.ARMOR, 000);
        SetEquipment(ItemSlot.EXTRA, 000);

        this.jobLevel = jobLevel;

        RecalculateStats();
        FullHeal();
    }

    public void SetEquipment(ItemSlot slot, int itemID) {
        switch (slot) {
            case WEAPON:
                equipment[0] = Item.GetItem(itemID);
                break;
            case ARMOR:
                equipment[1] = Item.GetItem(itemID);
                break;
            case EXTRA:
                equipment[2] = Item.GetItem(itemID);
                break;
            case NONE:
                break;
        }
    }

    public void FullHeal() {
        currentHP = stats.HP;
        currentMP = stats.MP;
    }

    public void RecalculateStats() {
        if (charClass != ClassID.NONE) {
            Stats base = Classes.GetBaseStats(charClass);
            int HP = base.HP + equipment[0].GetBonusHP() + equipment[1].GetBonusHP() + equipment[2].GetBonusHP();
            int MP = base.MP + equipment[0].GetBonusMP() + equipment[1].GetBonusMP() + equipment[2].GetBonusMP();
            int atk = base.atk + equipment[0].GetBonusAtk() + equipment[1].GetBonusAtk() + equipment[2].GetBonusAtk();
            int def = base.def + equipment[0].GetBonusDef() + equipment[1].GetBonusDef() + equipment[2].GetBonusDef();
            int mag = base.mag + equipment[0].GetBonusMag() + equipment[1].GetBonusMag() + equipment[2].GetBonusMag();
            int res = base.res + equipment[0].GetBonusRes() + equipment[1].GetBonusRes() + equipment[2].GetBonusRes();
            int spd = base.spd + equipment[0].GetBonusSpd() + equipment[1].GetBonusSpd() + equipment[2].GetBonusSpd();
            this.stats = new Stats(HP, MP, atk, def, mag, res, spd);
        }
        else {
            // TODO: Enemies!
        }
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

}
