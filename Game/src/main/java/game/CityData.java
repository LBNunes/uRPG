/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Luísa Bontempo Nunes
//     Created on 2014-06-14 ymd
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
import game.Mission.FetchMission;
import game.Mission.KillMission;
import game.Mission.VisitAreaMission;
import game.Mission.VisitCityMission;
import game.PlayerData.KnownCity;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.UUID;

import org.unbiquitous.uImpala.engine.asset.AssetManager;

public class CityData {

    private static CityData  instance            = null;

    UUID                     uuid;
    String                   name;

    ClassID                  academyClass;
    int                      affinityAreaID;
    long                     lastRefresh;
    ArrayList<Mission>       guildMissions;
    LinkedList<Transaction>  marketTransactions;
    ArrayList<Entity>        recruits;
    HashSet<KnownCity>       knownCities;
    ArrayList<PlayerVisit>   playerVisits;

    public static final long ONE_DAY_MILISECONDS = 24 * 60 * 60 * 1000;

    private CityData() {
        uuid = null;
        name = EnvironmentInformation.GetComputerName();
        academyClass = ClassID.NONE;
        guildMissions = new ArrayList<Mission>();
        marketTransactions = new LinkedList<Transaction>();
        recruits = new ArrayList<Entity>();
        knownCities = new HashSet<KnownCity>();
        playerVisits = new ArrayList<PlayerVisit>();
        lastRefresh = 0;
    }

    public static CityData GetData() {
        if (instance == null) {
            instance = Load();
        }
        return instance;
    }

    private static CityData Load() {

        CityData data = new CityData();

        FileInputStream f;
        try {
            f = new FileInputStream(Config.CITY_SAVE);
            Scanner s = new Scanner(f);
            String line;
            StringTokenizer tokenizer;

            line = s.nextLine();

            data.uuid = UUID.fromString(line);

            line = s.nextLine();
            tokenizer = new StringTokenizer(line, " ");

            data.name = tokenizer.nextToken();
            data.academyClass = ClassID.valueOf(tokenizer.nextToken());
            data.affinityAreaID = Integer.valueOf(tokenizer.nextToken());

            line = s.nextLine();

            data.lastRefresh = Long.parseLong(line);

            line = s.nextLine();

            int nMissions = Integer.parseInt(line);

            for (int i = 0; i < nMissions; ++i) {
                line = s.nextLine();
                data.guildMissions.add(Mission.FromString(line));
            }

            line = s.nextLine();

            int nTransactions = Integer.parseInt(line);

            for (int i = 0; i < nTransactions; ++i) {
                line = s.nextLine();
                tokenizer = new StringTokenizer(line, " ");
                data.marketTransactions.add(Transaction.FromString(line));
            }

            line = s.nextLine();

            int nRecruits = Integer.parseInt(line);

            for (int i = 0; i < nRecruits; ++i) {
                line = s.nextLine();
                tokenizer = new StringTokenizer(line, " ");
                data.recruits.add(new Entity(tokenizer.nextToken(),
                                             data.academyClass,
                                             Integer.parseInt(tokenizer.nextToken())));
            }

            line = s.nextLine();

            int nKnownCities = Integer.parseInt(line);
            for (int i = 0; i < nKnownCities; ++i) {
                line = s.nextLine();
                tokenizer = new StringTokenizer(line, " ");
                data.knownCities.add(new KnownCity(UUID.fromString(tokenizer.nextToken()),
                                                   tokenizer.nextToken(),
                                                   Long.parseLong(tokenizer.nextToken())));
            }

            line = s.nextLine();

            int nPlayerVisits = Integer.parseInt(line);
            for (int i = 0; i < nPlayerVisits; ++i) {
                line = s.nextLine();
                tokenizer = new StringTokenizer(line, " ");
                data.playerVisits.add(new PlayerVisit(UUID.fromString(tokenizer.nextToken()),
                                                      Integer.parseInt(tokenizer.nextToken()),
                                                      Integer.parseInt(tokenizer.nextToken()),
                                                      Long.parseLong(tokenizer.nextToken()),
                                                      Long.parseLong(tokenizer.nextToken()),
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

        if (instance == null) {
            return;
        }
        BufferedWriter w = null;

        try {
            w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Config.CITY_SAVE), "utf-8"));
            w.write(instance.uuid.toString() + '\n');

            w.write(instance.name + " " + instance.academyClass.toString() + " " + instance.affinityAreaID + '\n');

            w.write("" + instance.lastRefresh + '\n');

            w.write("" + instance.guildMissions.size() + '\n');

            for (Mission m : instance.guildMissions) {
                w.write(m.toString() + '\n');
            }

            w.write("" + instance.marketTransactions.size() + '\n');

            for (Transaction t : instance.marketTransactions) {
                w.write(t.toString() + '\n');
            }

            w.write("" + instance.recruits.size() + '\n');

            for (Entity e : instance.recruits) {
                w.write(e.name + " " + e.jobLevel + '\n');
            }

            w.write("" + instance.knownCities.size() + '\n');

            for (KnownCity c : instance.knownCities) {
                w.write(c.toString() + '\n');
            }

            w.write("" + instance.playerVisits.size() + '\n');

            for (PlayerVisit v : instance.playerVisits) {
                w.write(v.toString() + '\n');
            }

            w.write("\n");

            w.close();
        }
        catch (IOException ex) {
        }
    }

    private static CityData CreateNew() {

        CityData data = new CityData();

        data.uuid = UUID.randomUUID();

        data.name = EnvironmentInformation.GetComputerName();

        data.PickClass();
        data.GenerateMissions();
        data.CreateBasicTransactions();
        data.CreateRecruits();

        data.lastRefresh = System.currentTimeMillis();

        return data;
    }

    public void DiscoverCity(KnownCity k) {
        final int ONE_WEEK = 7 * 24 * 60 * 60 * 1000;
        if (!k.cityUUID.equals(uuid) && System.currentTimeMillis() - k.timestamp < ONE_WEEK) {
            knownCities.add(k);
        }
    }

    public void DiscoverPlayer(UUID playerUUID, int averageLevel, int totalLevel) {
        for (PlayerVisit p : playerVisits) {
            if (p.playerUUID.equals(playerUUID)) {
                p.averageLevel = averageLevel;
                p.totalLevel = totalLevel;
                return;
            }
        }
        PlayerVisit p = new PlayerVisit(playerUUID, averageLevel, totalLevel);
        playerVisits.add(p);
    }

    public boolean ApproveEnergyRestore(UUID playerUUID) {
        for (PlayerVisit p : playerVisits) {
            if (p.playerUUID.equals(playerUUID)) {
                long time = System.currentTimeMillis();
                if (time - p.lastEnergyRestore >= Config.MS_PER_CITY_RESTORE) {
                    p.lastEnergyRestore = time;
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        return false;
    }

    public boolean ApproveMissionHandout(UUID playerUUID, UUID missionUUID) {
        for (PlayerVisit p : playerVisits) {
            if (p.playerUUID.equals(playerUUID)) {
                long time = System.currentTimeMillis();
                if (time - p.lastMissionHandout >= Config.MS_PER_CITY_RESTORE) {
                    for (Mission m : guildMissions) {
                        if (m.missionID.equals(missionUUID) && !m.completed) {
                            p.lastEnergyRestore = time;
                            m.handedOut = true;
                            return true;
                        }
                        else {
                            return false;
                        }
                    }
                }
                else {
                    return false;
                }
            }
        }
        return false;
    }

    private void PickClass() {
        int areas[] = Area.GenerateAreaSet(EnvironmentInformation.GetSSID());

        int index = new Random().nextInt(areas.length);

        affinityAreaID = areas[index];
        academyClass = Area.GetArea(affinityAreaID).GetClassBias();
        System.out.println("Rolled " + index + ": " + Area.GetArea(affinityAreaID).GetName());
    }

    private void GenerateMissions() {

        Random rand = new Random();

        int rank = 0;

        if (playerVisits.size() == 0) {
            rank = 1;
        }

        else {
            for (PlayerVisit v : playerVisits) {
                if (v.averageLevel > rank) {
                    rank = v.averageLevel;
                }
            }
        }

        if (knownCities.size() > 0) {
            guildMissions.add(GenerateVisitCityMission());
        }
        else {
            guildMissions.add(GenerateFetchMission(rand.nextInt(rank) + 1));
        }
        guildMissions.add(GenerateVisitAreaMission());
        guildMissions.add(GenerateFetchMission(rand.nextInt(rank) + 1));
        guildMissions.add(GenerateKillMission(rand.nextInt(rank) + 1));
        guildMissions.add(GenerateKillMission(rand.nextInt(rank) + 1));

    }

    private Mission GenerateKillMission(int rank) {

        boolean boss;
        if (Math.random() > 0.9) {
            boss = true;
        }
        else {
            boss = false;
        }

        int enemy = Enemy.GetEnemyOfRank(rank * 2, boss);
        int eRank = Enemy.GetEnemy(enemy).rank;
        int amount = boss ? 1 : 2 + new Random().nextInt(4);
        int reward = 100 + eRank * 30 + amount * 10;

        return new KillMission(uuid, "The City of " + name, reward, eRank, enemy, amount);
    }

    private Mission GenerateVisitAreaMission() {

        final int AREA_MISSION_REWARD = 200;
        int[] areas = Area.GenerateAreaSet(EnvironmentInformation.GetSSID());
        int area = Area.GetRandomArea();
        if (area == areas[0] || area == areas[1] || area == areas[2]) {
            return new VisitAreaMission(uuid, "The City of " + name, AREA_MISSION_REWARD / 2, 1, area);
        }
        else
            return new VisitAreaMission(uuid, "The City of " + name, AREA_MISSION_REWARD, 1, area);
    }

    private Mission GenerateVisitCityMission() {

        final int VISIT_CITY_REWARD = 300;

        KnownCity c = null;

        int rand = new Random().nextInt(knownCities.size());
        int i = 0;
        for (KnownCity k : knownCities) {
            if (i == rand) {
                c = k;
                break;
            }
            i += 1;
        }

        return new VisitCityMission(uuid, "The City of " + name, VISIT_CITY_REWARD, 1, c.cityUUID, c.cityName);
    }

    private Mission GenerateFetchMission(int rank) {

        int item = Item.GetLootOfRank(rank);
        int iRank = Item.GetItem(item).GetRank();
        int reward = 100 + 20 * iRank;

        return new FetchMission(uuid, "The City of " + name, reward, iRank, item);

    }

    private void CreateBasicTransactions() {

        Random rand = new Random();

        int nPotions = rand.nextInt(3) + 1;
        int nEthers = rand.nextInt(3) + 1;
        int nElixirs = rand.nextInt(10) == 0 ? 1 : 0; // 10% chance of elixir spawning
        int nWarrior = rand.nextInt(academyClass == ClassID.WARRIOR ? 3 : 2);
        int nMage = rand.nextInt(academyClass == ClassID.MAGE ? 3 : 2);
        int nRogue = rand.nextInt(academyClass == ClassID.ROGUE ? 3 : 2);
        int nArmor = rand.nextInt(3);

        float itemFactor = (rand.nextInt(41) + 80) / (float) 100; // price is 80~120%
        float nonNativeFactor = (rand.nextInt(21) + 100) / (float) 100; // price is 100~120%
        float nativeFactor = (-rand.nextInt(21) + 100) / (float) 100; // price is 80~100%
        float armorFactor = itemFactor;

        int potionPrice = (int) (20 * itemFactor);
        int etherPrice = (int) (40 * itemFactor);
        int elixirPrice = (int) (500 * itemFactor);
        int warriorPrice = (int) (150 * (academyClass == ClassID.WARRIOR ? nativeFactor : nonNativeFactor));
        int magePrice = (int) (150 * (academyClass == ClassID.MAGE ? nativeFactor : nonNativeFactor));
        int roguePrice = (int) (150 * (academyClass == ClassID.ROGUE ? nativeFactor : nonNativeFactor));
        int armorPrice = (int) (200 * armorFactor);

        for (int i = 0; i < nPotions; ++i) {
            marketTransactions.add(new Transaction(this.uuid, 501, potionPrice));
        }

        for (int i = 0; i < nEthers; ++i) {
            marketTransactions.add(new Transaction(this.uuid, 541, etherPrice));
        }

        for (int i = 0; i < nElixirs; ++i) {
            marketTransactions.add(new Transaction(this.uuid, 581, elixirPrice));
        }

        for (int i = 0; i < nWarrior; ++i) {
            marketTransactions.add(new Transaction(this.uuid, 1, warriorPrice));
            marketTransactions.add(new Transaction(this.uuid, 51, warriorPrice));
        }

        for (int i = 0; i < nMage; ++i) {
            marketTransactions.add(new Transaction(this.uuid, 101, magePrice));
            marketTransactions.add(new Transaction(this.uuid, 151, magePrice));
        }

        for (int i = 0; i < nRogue; ++i) {
            marketTransactions.add(new Transaction(this.uuid, 201, roguePrice));
            marketTransactions.add(new Transaction(this.uuid, 251, roguePrice));
        }

        for (int i = 0; i < nArmor; ++i) {
            marketTransactions.add(new Transaction(this.uuid, 301, armorPrice));
        }
    }

    private void CreateRecruits() {

        final int N_RECRUITS = 3;

        recruits.clear();

        Random rand = new Random();

        int rank = 0;

        if (playerVisits.size() == 0) {
            rank = 1;
        }

        else {
            for (PlayerVisit v : playerVisits) {
                rank += v.averageLevel;
            }
            rank /= playerVisits.size();
            rank /= 2;
        }

        for (int i = 0; i < N_RECRUITS; ++i) {
            recruits.add(new Entity(Entity.GetRandomName(), academyClass, rand.nextInt(rank) + 1));
        }
    }

    public void Refresh(AssetManager assets) {

        if (System.currentTimeMillis() - lastRefresh < ONE_DAY_MILISECONDS / 4) {
            return;
        }

        System.out.println("Refreshing city data...");

        LinkedList<Transaction> oldTransactions = marketTransactions;

        marketTransactions = new LinkedList<Transaction>();

        CreateBasicTransactions();

        // Move all player transactions into the new list
        for (Transaction t : oldTransactions) {
            if (!uuid.equals(t.seller)) {
                marketTransactions.addFirst(t);
            }
        }

        ArrayList<Mission> oldMissions = guildMissions;

        guildMissions = new ArrayList<Mission>();

        GenerateMissions();

        // Move player and not expired completed missions
        for (Mission m : oldMissions) {
            if (!uuid.equals(m.guildCityUUID) || m.handedOut) {
                guildMissions.add(m);
            }
        }

        CreateRecruits();
        for (Entity e : recruits) {
            e.LoadSprites(assets);
        }

        lastRefresh = System.currentTimeMillis();

    }

    public static class Transaction {
        UUID    seller;
        int     item;
        int     value;
        boolean completed;

        public Transaction(UUID _seller, int _item, int _value) {
            seller = _seller;
            item = _item;
            value = _value;
            completed = false;
        }

        public Transaction(UUID _seller, int _item, int _value, boolean _completed) {
            seller = _seller;
            item = _item;
            value = _value;
            completed = _completed;
        }

        @Override
        public String toString() {
            String s = new String();
            s += seller.toString() + " ";
            s += item + " ";
            s += value + " ";
            s += completed;

            return s;
        }

        public static Transaction FromString(String s) {
            StringTokenizer tokenizer = new StringTokenizer(s, " ");
            return new Transaction(UUID.fromString(tokenizer.nextToken()),
                                   Integer.parseInt(tokenizer.nextToken()),
                                   Integer.parseInt(tokenizer.nextToken()),
                                   Boolean.parseBoolean(tokenizer.nextToken()));
        }
    }

    public static class PlayerVisit {

        UUID playerUUID;
        int  averageLevel;
        int  totalLevel;
        long firstVisit;
        long lastEnergyRestore;
        long lastMissionHandout;

        @Override
        public boolean equals(Object obj) {

            return playerUUID.equals(((PlayerVisit) obj).playerUUID);
        }

        @Override
        public String toString() {
            return playerUUID.toString() + " " + averageLevel + " " + totalLevel + " " +
                   firstVisit + " " + lastEnergyRestore + " " + lastMissionHandout;
        }

        public PlayerVisit(UUID _playerUUID, int _averageLevel, int _totalLevel) {
            playerUUID = _playerUUID;
            averageLevel = _averageLevel;
            totalLevel = _totalLevel;
            firstVisit = System.currentTimeMillis();
            lastEnergyRestore = 0;
            lastMissionHandout = 0;
        }

        public PlayerVisit(UUID _playerUUID, int _averageLevel, int _totalLevel, long _firstVisit,
                           long _lastEnergyRestore, long _lastMissionHandout) {
            playerUUID = _playerUUID;
            averageLevel = _averageLevel;
            totalLevel = _totalLevel;
            firstVisit = _firstVisit;
            lastEnergyRestore = _lastEnergyRestore;
            lastMissionHandout = _lastMissionHandout;
        }
    }
}
