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
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.UUID;

import org.unbiquitous.uImpala.engine.asset.AssetManager;

public class CityData {
    UUID                     uuid;
    String                   name;

    ClassID                  academyClass;
    int                      affinityAreaID;
    long                     lastRefresh;
    ArrayList<Mission>       guildMissions;
    ArrayList<Transaction>   marketTransactions;
    ArrayList<Entity>        recruits;
    HashSet<KnownCity>       knownCities;
    HashSet<PlayerVisit>     playerVisits;

    public static final long ONE_DAY_MILISECONDS = 24 * 60 * 60 * 1000;

    public CityData() {
        uuid = null;
        name = EnvironmentInformation.GetComputerName();
        academyClass = ClassID.NONE;
        guildMissions = new ArrayList<Mission>();
        marketTransactions = new ArrayList<Transaction>();
        recruits = new ArrayList<Entity>();
        knownCities = new HashSet<KnownCity>();
        playerVisits = new HashSet<PlayerVisit>();
        lastRefresh = 0;
    }

    // TODO: Decide on how classes are determined

    public static CityData Load(AssetManager assets, String citySave) {

        CityData data = new CityData();

        FileInputStream f;
        try {
            f = new FileInputStream(citySave);
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
                data.marketTransactions.add(new Transaction(line));
            }

            line = s.nextLine();

            int nRecruits = Integer.parseInt(line);

            for (int i = 0; i < nRecruits; ++i) {
                line = s.nextLine();
                tokenizer = new StringTokenizer(line, " ");
                data.recruits.add(new Entity(assets, tokenizer.nextToken(),
                                             data.academyClass, Integer.parseInt(tokenizer.nextToken())));
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
            return CreateNew(assets);
        }
        catch (IOException e) {
            System.out.println("FATAL ERROR: Save file corrupted!");
            System.exit(1);
        }

        return data;
    }

    public static void Save(String save, CityData data) {
        BufferedWriter w = null;

        try {
            w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(save), "utf-8"));
            w.write(data.uuid.toString() + '\n');

            w.write(data.name + " " + data.academyClass.toString() + " " + data.affinityAreaID + '\n');

            w.write("" + data.lastRefresh + '\n');

            w.write("" + data.guildMissions.size() + '\n');

            for (Mission m : data.guildMissions) {
                w.write(m.toString() + '\n');
            }

            w.write("" + data.marketTransactions.size() + '\n');

            for (Transaction t : data.marketTransactions) {
                w.write(t.toString() + '\n');
            }

            w.write("" + data.recruits.size() + '\n');

            for (Entity e : data.recruits) {
                w.write(e.name + " " + e.jobLevel + '\n');
            }

            w.write("" + data.knownCities.size() + '\n');

            for (KnownCity c : data.knownCities) {
                w.write(c.toString() + '\n');
            }

            w.write("" + data.playerVisits.size() + '\n');

            for (PlayerVisit v : data.playerVisits) {
                w.write(v.toString() + '\n');
            }

            w.write("\n");

            w.close();
        }
        catch (IOException ex) {
        }
    }

    private static CityData CreateNew(AssetManager assets) {

        CityData data = new CityData();

        data.uuid = UUID.randomUUID();

        data.name = EnvironmentInformation.GetComputerName();

        data.PickClass();
        data.GenerateMissions();
        data.CreateBasicTransactions();
        data.CreateRecruits(assets);

        data.lastRefresh = System.currentTimeMillis();

        return data;
    }

    public void DiscoverCity(UUID cityUUID, String cityName) {
        if (!cityUUID.equals(uuid)) {
            knownCities.add(new KnownCity(cityUUID, cityName, System.currentTimeMillis()));
        }
    }

    private void PickClass() {
        int areas[] = Area.GenerateAreaSet(EnvironmentInformation.GetSSID());

        int index = new Random().nextInt(areas.length);

        affinityAreaID = areas[index];
        academyClass = Area.GetArea(affinityAreaID).GetClassBias();
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
        int amount = 5 + new Random().nextInt(4);
        int reward = 100 + rank * 30 + amount * 10;

        return new KillMission(uuid, "The City of " + name, reward, rank, enemy, amount);
    }

    private Mission GenerateVisitAreaMission() {

        final int AREA_MISSION_REWARD = 200;
        Random rand = new Random();
        int[] areas = Area.GenerateAreaSet(EnvironmentInformation.GetSSID());
        int area;

        if (areas.length >= Area.GetNumberOfAreas()) {
            area = areas[rand.nextInt(areas.length)];
            return new VisitAreaMission(uuid, "The City of " + name, AREA_MISSION_REWARD / 2, 1, area);
        }
        else {
            while (true) {
                area = rand.nextInt(Area.GetNumberOfAreas() + 1);
                if (area == areas[0] || area == areas[1] || area == areas[2]) {
                    continue;
                }
                if (Area.GetArea(area) == null) {
                    continue;
                }
                break;
            }
        }
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
        int reward = 100 + 20 * rank;

        return new FetchMission(uuid, "The City of " + name, reward, rank, item);

    }

    private void CreateBasicTransactions() {

        Random rand = new Random();

        int nPotions = rand.nextInt(4);
        int nEthers = rand.nextInt(4);
        int nElixirs = rand.nextInt(4) == 0 ? 1 : 0; // 25% of elixir spawning
        int nWarrior = rand.nextInt(academyClass == ClassID.WARRIOR ? 4 : 2);
        int nMage = rand.nextInt(academyClass == ClassID.MAGE ? 4 : 2);
        int nRogue = rand.nextInt(academyClass == ClassID.ROGUE ? 4 : 2);
        int nArmor = rand.nextInt(3);

        float itemFactor = (rand.nextInt(41) + 80) / (float) 100; // price is 80~120%
        float nonNativeFactor = (rand.nextInt(21) + 100) / (float) 100; // price is 100~120%
        float nativeFactor = (-rand.nextInt(21) + 100) / (float) 100; // price is 80~100%
        float armorFactor = itemFactor;

        int potionPrice = (int) (20 * itemFactor);
        int etherPrice = (int) (50 * itemFactor);
        int elixirPrice = (int) (300 * itemFactor);
        int warriorPrice = (int) (150 * (academyClass == ClassID.WARRIOR ? nativeFactor : nonNativeFactor));
        int magePrice = (int) (150 * (academyClass == ClassID.MAGE ? nativeFactor : nonNativeFactor));
        int roguePrice = (int) (150 * (academyClass == ClassID.ROGUE ? nativeFactor : nonNativeFactor));
        int armorPrice = (int) (200 * armorFactor);

        if (nPotions > 0) {
            marketTransactions.add(new Transaction(this.uuid, 501, nPotions, potionPrice));
        }

        if (nEthers > 0) {
            marketTransactions.add(new Transaction(this.uuid, 541, nEthers, etherPrice));
        }

        if (nElixirs > 0) {
            marketTransactions.add(new Transaction(this.uuid, 581, nElixirs, elixirPrice));
        }

        if (nWarrior > 0) {
            marketTransactions.add(new Transaction(this.uuid, 1, nWarrior, warriorPrice));
            marketTransactions.add(new Transaction(this.uuid, 51, nWarrior, warriorPrice));
        }

        if (nMage > 0) {
            marketTransactions.add(new Transaction(this.uuid, 101, nMage, magePrice));
            marketTransactions.add(new Transaction(this.uuid, 151, nMage, magePrice));
        }

        if (nRogue > 0) {
            marketTransactions.add(new Transaction(this.uuid, 201, nRogue, roguePrice));
            marketTransactions.add(new Transaction(this.uuid, 251, nRogue, roguePrice));
        }

        if (nArmor > 0) {
            marketTransactions.add(new Transaction(this.uuid, 301, nArmor, armorPrice));
        }
    }

    private void CreateRecruits(AssetManager assets) {

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
        }

        rank /= 2;

        for (int i = 0; i < N_RECRUITS; ++i) {
            recruits.add(new Entity(assets, Entity.GetRandomName(), academyClass, rand.nextInt(rank) + 1));
        }
    }

    public void Refresh(AssetManager assets) {

        if (System.currentTimeMillis() - lastRefresh < ONE_DAY_MILISECONDS / 4) {
            return;
        }

        ArrayList<Transaction> oldTransactions = marketTransactions;

        marketTransactions = new ArrayList<Transaction>();

        CreateBasicTransactions();

        // Move all player transactions into the new list
        for (Transaction t : oldTransactions) {
            if (!uuid.equals(t.seller)) {
                marketTransactions.add(t);
            }
        }

        ArrayList<Mission> oldMissions = guildMissions;

        guildMissions = new ArrayList<Mission>();

        GenerateMissions();

        // Move player and not expired completed missions
        for (Mission m : oldMissions) {
            if (!uuid.equals(m.questGiver) || m.handedOut) {
                guildMissions.add(m);
            }
        }

        CreateRecruits(assets);

        lastRefresh = System.currentTimeMillis();

    }

    public static class Transaction {
        UUID    seller;
        int     item;
        int     amount;
        int     value;
        boolean completed;

        public Transaction(UUID _seller, int _item, int _amount, int _value) {
            seller = _seller;
            item = _item;
            amount = _amount;
            value = _value;
        }

        public Transaction(String line) {
            StringTokenizer tokenizer = new StringTokenizer(line, " ");
            seller = new UUID(Long.parseLong(tokenizer.nextToken()),
                              Long.parseLong(tokenizer.nextToken()));
            item = Integer.parseInt(tokenizer.nextToken());
            amount = Integer.parseInt(tokenizer.nextToken());
            value = Integer.parseInt(tokenizer.nextToken());
            completed = false;
        }

        @Override
        public String toString() {
            String s = new String();
            s += "" + seller.getMostSignificantBits() + " " + seller.getLeastSignificantBits() + " ";
            s += item + " ";
            s += amount + " ";
            s += value;

            return s;
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

        public PlayerVisit(UUID _playerUUID, int _averageLevel, int _totalLevel,
                           long _firstVisit, long _lastEnergyRestore, long _lastMissionHandout) {
            playerUUID = _playerUUID;
            averageLevel = _averageLevel;
            totalLevel = _totalLevel;
            firstVisit = _firstVisit;
            lastEnergyRestore = _lastEnergyRestore;
            lastMissionHandout = _lastMissionHandout;
        }
    }
}
