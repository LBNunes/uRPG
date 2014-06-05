package game;

public class Stats {

    public int HP;
    public int MP;
    public int atk;
    public int def;
    public int mag;
    public int res;
    public int spd;

    public Stats() {
        HP = 0;
        MP = 0;
        atk = 0;
        def = 0;
        mag = 0;
        res = 0;
    }

    public Stats(int HP, int MP, int atk, int def, int mag, int res, int spd) {
        SetAll(HP, MP, atk, def, mag, res, spd);
    }

    public void SetAll(int HP, int MP, int atk, int def, int mag, int res, int spd) {
        this.HP = HP;
        this.MP = MP;
        this.atk = atk;
        this.def = def;
        this.mag = mag;
        this.res = res;
        this.spd = spd;
    }
}
