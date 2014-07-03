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

import org.unbiquitous.uImpala.engine.asset.AssetManager;

public class PlayerData {

    UUID               uuid;
    int                gold;
    ArrayList<Entity>  party;
    Inventory          inventory;
    HashSet<KnownCity> knownCities;

    // TODO: Energy data
    // TODO: Mission data
    // TODO: Mission time
    // TODO: Entity Ability data

    public PlayerData() {
        uuid = null;
        party = new ArrayList<Entity>();
        inventory = new Inventory();
        knownCities = new HashSet<KnownCity>();
    }

    public static PlayerData Load(AssetManager assets, String playerSave) {

        PlayerData data = new PlayerData();

        FileInputStream f;
        try {
            f = new FileInputStream(playerSave);
            Scanner s = new Scanner(f);
            String line;
            StringTokenizer tokenizer;

            line = s.nextLine();

            data.uuid = UUID.fromString(line);

            line = s.nextLine();

            data.gold = Integer.parseInt(line);

            line = s.nextLine();
            tokenizer = new StringTokenizer(line, " ");

            int partySize = Integer.parseInt(tokenizer.nextToken());

            for (int i = 0; i < partySize; ++i) {
                Entity e = new Entity(assets, tokenizer.nextToken(),
                                      ClassID.valueOf(tokenizer.nextToken()),
                                      Integer.parseInt(tokenizer.nextToken()));
                e.GiveJobExp(Integer.parseInt(tokenizer.nextToken()));
                e.equipment.Set(Integer.parseInt(tokenizer.nextToken()),
                                Integer.parseInt(tokenizer.nextToken()),
                                Integer.parseInt(tokenizer.nextToken()));
                e.RecalculateStats();
                e.SpendMP(0);
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
            return CreateNew(assets);
        }
        catch (IOException e) {
            System.out.println("FATAL ERROR: Save file corrupted!");
            System.exit(1);
        }

        return data;
    }

    public static void Save(String save, PlayerData data) {
        BufferedWriter w = null;

        try {
            w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(save),
                                                          "utf-8"));
            w.write(data.uuid.toString() + '\n');

            w.write("" + Integer.toString(data.gold) + "\n");

            w.write("" + data.party.size() + " ");

            for (Entity e : data.party) {
                WriteEntity(w, e);
            }

            w.write("\n");

            w.write("" + data.inventory.itemList.size() + " ");

            for (IEntry e : data.inventory.itemList) {
                w.write("" + e.item + " " + e.amount + " ");
            }

            w.write("\n");

            w.write("" + data.knownCities.size());

            for (KnownCity c : data.knownCities) {
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
    }

    private static PlayerData CreateNew(AssetManager assets) {

        PlayerData data = new PlayerData();

        data.uuid = UUID.randomUUID();

        data.gold = 0;

        Entity warrior = new Entity(assets, Entity.GetRandomName(), ClassID.WARRIOR, 1);
        Entity rogue = new Entity(assets, Entity.GetRandomName(), ClassID.ROGUE, 1);
        Entity mage = new Entity(assets, Entity.GetRandomName(), ClassID.MAGE, 1);

        data.party.add(warrior);
        data.party.add(rogue);
        data.party.add(mage);

        // 3 x Life Potion, 1 x Mana Potion
        data.inventory.AddItem(501, 3);
        data.inventory.AddItem(502, 1);

        return data;
    }

    public void DiscoverCity(UUID cityUUID, String cityName) {
        knownCities.add(new KnownCity(cityUUID, cityName, System.currentTimeMillis()));
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
