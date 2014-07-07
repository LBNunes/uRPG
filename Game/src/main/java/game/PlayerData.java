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

import game.Classes.ClassID;
import game.PlayerData.Inventory.IEntry;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.UUID;

public class PlayerData {

    private static PlayerData instance = null;

    public UUID               uuid;
    public int                gold;
    public int                energy;
    public long               lastRefresh;
    public ArrayList<Entity>  party;
    public Inventory          inventory;
    public ArrayList<Mission> missions;
    public HashSet<KnownCity> knownCities;

    private PlayerData() {
        uuid = null;
        party = new ArrayList<Entity>();
        inventory = new Inventory();
        missions = new ArrayList<Mission>();
        knownCities = new HashSet<KnownCity>();
    }

    public static PlayerData GetData() {
        if (instance == null) {
            instance = Load();
        }
        return instance;
    }

    private static PlayerData Load() {

        PlayerData data = new PlayerData();

        FileInputStream f;
        try {
            f = new FileInputStream(Config.PLAYER_SAVE);
            Scanner s = new Scanner(f);
            String line;
            StringTokenizer tokenizer;

            line = s.nextLine();

            data.uuid = UUID.fromString(line);

            line = s.nextLine();
            tokenizer = new StringTokenizer(line, " ");

            data.gold = Integer.parseInt(tokenizer.nextToken());
            data.energy = Integer.parseInt(tokenizer.nextToken());
            data.lastRefresh = Long.parseLong(tokenizer.nextToken());

            int maxEnergy = (int) (Config.BASE_ENERGY * EnvironmentInformation.GetFreeSpacePercentage());
            if (maxEnergy < 200) {
                maxEnergy = 200;
            }

            if (data.energy > Config.BASE_ENERGY * EnvironmentInformation.GetFreeSpacePercentage()) {
                data.energy = maxEnergy;
            }

            line = s.nextLine();
            tokenizer = new StringTokenizer(line, " ");

            int partySize = Integer.parseInt(tokenizer.nextToken());

            for (int i = 0; i < partySize; ++i) {

                line = s.nextLine();
                tokenizer = new StringTokenizer(line, " ");

                Entity e = new Entity(tokenizer.nextToken(),
                                      ClassID.valueOf(tokenizer.nextToken()),
                                      Integer.parseInt(tokenizer.nextToken()));
                e.GiveJobExp(Integer.parseInt(tokenizer.nextToken()));
                e.equipment.Set(Integer.parseInt(tokenizer.nextToken()),
                                Integer.parseInt(tokenizer.nextToken()),
                                Integer.parseInt(tokenizer.nextToken()));
                e.RecalculateStats();
                e.SpendMP(0);

                int nAbilities = Integer.parseInt(tokenizer.nextToken());

                for (int j = 0; j < nAbilities; ++j) {
                    e.abilities.add(Ability.GetAbility(Integer.parseInt(tokenizer.nextToken())));
                }

                data.party.add(e);
            }

            line = s.nextLine();
            tokenizer = new StringTokenizer(line, " ");

            int inventorySize = Integer.parseInt(tokenizer.nextToken());

            for (int i = 0; i < inventorySize; ++i) {
                data.inventory.AddItem(Integer.parseInt(tokenizer.nextToken()),
                                       Integer.parseInt(tokenizer.nextToken()));
            }

            line = s.nextLine();

            int nMissions = Integer.parseInt(line);

            for (int i = 0; i < nMissions; ++i) {
                line = s.nextLine();
                data.missions.add(Mission.FromString(line));
            }

            line = s.nextLine();

            int nKnownCities = Integer.parseInt(line);

            for (int i = 0; i < nKnownCities; i++) {

                line = s.nextLine();
                tokenizer = new StringTokenizer(line, " ");

                data.knownCities.add(new KnownCity(UUID.fromString(tokenizer.nextToken()),
                                                   tokenizer.nextToken(),
                                                   Long.parseLong(tokenizer.nextToken())));
            }

            s.close();
            f.close();
        }
        catch (FileNotFoundException e) {
            return CreateNew();
        }
        catch (IOException e) {
            System.out.println("FATAL ERROR: Save file corrupted!");
            System.exit(1);
        }

        return data;
    }

    public static void Save() {
        BufferedWriter w = null;

        try {
            w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Config.PLAYER_SAVE),
                                                          "utf-8"));
            w.write(instance.uuid.toString() + '\n');

            w.write("" + instance.gold + " " + instance.energy + " " + instance.lastRefresh + "\n");

            w.write("" + instance.party.size());

            for (Entity e : instance.party) {
                w.write("\n");
                WriteEntity(w, e);
            }

            w.write("\n");

            w.write("" + instance.inventory.itemList.size() + " ");

            for (IEntry e : instance.inventory.itemList) {
                w.write("" + e.item + " " + e.amount + " ");
            }

            w.write("\n");

            w.write("" + instance.missions.size() + "\n");

            for (Mission m : instance.missions) {
                w.write(m.toString() + '\n');
            }

            w.write("" + instance.knownCities.size());

            for (KnownCity c : instance.knownCities) {
                w.write(c.toString() + '\n');
            }

            w.write("\n");

            w.close();
        }
        catch (IOException ex) {
        }
    }

    private static void WriteEntity(BufferedWriter w, Entity e) throws IOException {

        w.write("" + e.name + " ");
        w.write("" + e.classID + " " + e.jobLevel + " " + e.jobExp + " ");
        w.write("" + e.equipment.toString() + " ");
        w.write("" + e.abilities.size());

        for (Ability a : e.abilities) {
            w.write(" " + a.id);
        }
    }

    private static PlayerData CreateNew() {

        PlayerData data = new PlayerData();

        data.uuid = UUID.randomUUID();

        data.gold = 0;
        data.energy = (int) (Config.BASE_ENERGY * EnvironmentInformation.GetFreeSpacePercentage());
        data.lastRefresh = System.currentTimeMillis();

        Entity warrior = new Entity(Entity.GetRandomName(), ClassID.WARRIOR, 1);
        Entity rogue = new Entity(Entity.GetRandomName(), ClassID.ROGUE, 1);
        Entity mage = new Entity(Entity.GetRandomName(), ClassID.MAGE, 1);
        mage.abilities.add(Ability.GetAbility(001));

        data.party.add(warrior);
        data.party.add(rogue);
        data.party.add(mage);

        // 3 x Life Potion, 1 x Mana Potion
        data.inventory.AddItem(501, 3);
        data.inventory.AddItem(541, 1);

        return data;
    }

    public static void DiscoverCity(UUID cityUUID, String cityName) {
        instance.knownCities.add(new KnownCity(cityUUID, cityName, System.currentTimeMillis()));
    }

    public static class KnownCity {

        UUID   cityUUID;
        String cityName;
        long   timestamp;

        @Override
        public String toString() {
            return cityUUID.toString() + " " + cityName.toString() + " " + timestamp;
        }

        @Override
        public boolean equals(Object obj) {
            return cityUUID.equals((((KnownCity) obj).cityUUID));
        }

        public KnownCity(UUID _cityUUID, String _cityName, long _timestamp) {
            cityUUID = _cityUUID;
            cityName = _cityName;
            timestamp = _timestamp;
        }

    }

    public static class Inventory {
        public ArrayList<IEntry> itemList;

        public Inventory() {
            itemList = new ArrayList<IEntry>();
        }

        public boolean AddItem(int item, int amount) {
            for (IEntry e : itemList) {
                if (e.item == item) {
                    e.amount += amount;
                    return true;
                }
            }
            itemList.add(new IEntry(item, amount));
            return false;
        }

        public boolean HasItem(int item) {
            return ItemAmount(item) > 0;
        }

        public int ItemAmount(int item) {
            for (IEntry e : itemList) {
                if (e.item == item) {
                    return e.amount;
                }
            }
            return 0;
        }

        public boolean TakeItem(int item, int amount) {
            Iterator<IEntry> i = itemList.iterator();
            while (i.hasNext()) {
                IEntry e = i.next();
                if (e.item == item) {
                    if (amount > e.amount) {
                        return false;
                    }
                    else if (amount == e.amount) {
                        i.remove();
                        return true;
                    }
                    else {
                        e.amount -= amount;
                        return true;
                    }
                }
            }
            return false;
        }

        public int Size() {
            return itemList.size();
        }

        public void Swap(int i1, int i2) {
            IEntry e1 = itemList.get(i1);
            IEntry e2 = itemList.get(i2);

            itemList.set(i1, e2);
            itemList.set(i2, e1);
        }

        public static class IEntry {
            public int item;
            public int amount;

            public IEntry(int _item, int _amount) {
                item = _item;
                amount = _amount;
            }
        }
    }
}
