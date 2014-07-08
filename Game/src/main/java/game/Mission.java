/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Luísa Bontempo Nunes
//     Created on 2014-06-29 ymd
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

import java.util.StringTokenizer;
import java.util.UUID;

public abstract class Mission {
    public enum Objective {
        KILL, VISIT_AREA, VISIT_CITY, FETCH
    }

    public UUID      missionID;
    public UUID      questGiver;
    public String    giverName;
    public int       reward;
    public long      creationTime;
    public int       rank;
    public boolean   completed;
    public boolean   handedOut;
    public Objective objective;

    protected Mission() {

    }

    public Mission(UUID _questGiver, String _giverName, int _reward, int _rank, Objective _objective) {
        missionID = UUID.randomUUID();
        creationTime = System.currentTimeMillis();
        questGiver = _questGiver;
        giverName = _giverName;
        reward = _reward;
        rank = _rank;
        objective = _objective;
        completed = false;
        handedOut = false;
    }

    @Override
    public String toString() {
        return objective.toString() + " " + missionID.toString() + " " + questGiver.toString() + " " +
               giverName.replace(' ', '_') + " " +
               reward + " " +
               creationTime + " " + rank + " " + (completed ? 1 : 0) + " " + (handedOut ? 1 : 0);
    }

    protected void Set(UUID _missionID, UUID _questGiver, String _giverName, int _reward,
                       long _creationTime, int _rank, boolean _completed, boolean _handedOut) {
        missionID = _missionID;
        questGiver = _questGiver;
        giverName = _giverName;
        reward = _reward;
        creationTime = _creationTime;
        rank = _rank;
        completed = _completed;
        handedOut = _handedOut;
    }

    public static Mission FromString(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line, " ");
        Objective o = Objective.valueOf(tokenizer.nextToken());
        Mission m = null;
        switch (o) {
            case KILL:
                m = KillMission.Restore(UUID.fromString(tokenizer.nextToken()),
                                        UUID.fromString(tokenizer.nextToken()),
                                        tokenizer.nextToken().replace('_', ' '),
                                        Integer.parseInt(tokenizer.nextToken()),
                                        Long.parseLong(tokenizer.nextToken()),
                                        Integer.parseInt(tokenizer.nextToken()),
                                        Integer.parseInt(tokenizer.nextToken()) != 0,
                                        Integer.parseInt(tokenizer.nextToken()) != 0,
                                        Integer.parseInt(tokenizer.nextToken()),
                                        Integer.parseInt(tokenizer.nextToken()));
                break;
            case VISIT_AREA:
                m = VisitAreaMission.Restore(UUID.fromString(tokenizer.nextToken()),
                                             UUID.fromString(tokenizer.nextToken()),
                                             tokenizer.nextToken(),
                                             Integer.parseInt(tokenizer.nextToken()),
                                             Long.parseLong(tokenizer.nextToken()),
                                             Integer.parseInt(tokenizer.nextToken()),
                                             Integer.parseInt(tokenizer.nextToken()) != 0,
                                             Integer.parseInt(tokenizer.nextToken()) != 0,
                                             Integer.parseInt(tokenizer.nextToken()));
                break;
            case VISIT_CITY:
                m = VisitCityMission.Restore(UUID.fromString(tokenizer.nextToken()),
                                             UUID.fromString(tokenizer.nextToken()),
                                             tokenizer.nextToken(),
                                             Integer.parseInt(tokenizer.nextToken()),
                                             Long.parseLong(tokenizer.nextToken()),
                                             Integer.parseInt(tokenizer.nextToken()),
                                             Integer.parseInt(tokenizer.nextToken()) != 0,
                                             Integer.parseInt(tokenizer.nextToken()) != 0,
                                             UUID.fromString(tokenizer.nextToken()),
                                             tokenizer.nextToken());
                break;
            case FETCH:
                m = FetchMission.Restore(UUID.fromString(tokenizer.nextToken()),
                                         UUID.fromString(tokenizer.nextToken()),
                                         tokenizer.nextToken(),
                                         Integer.parseInt(tokenizer.nextToken()),
                                         Long.parseLong(tokenizer.nextToken()),
                                         Integer.parseInt(tokenizer.nextToken()),
                                         Integer.parseInt(tokenizer.nextToken()) != 0,
                                         Integer.parseInt(tokenizer.nextToken()) != 0,
                                         Integer.parseInt(tokenizer.nextToken()));
                break;
        }
        return m;
    }

    public static class KillMission extends Mission {

        int enemyID;
        int amount;

        private KillMission(int _enemyID, int _amount) {
            objective = Objective.KILL;
            enemyID = _enemyID;
            amount = _amount;
        }

        public KillMission(UUID _questGiver, String _giverName, int _reward, int _rank, int _enemyID, int _amount) {
            super(_questGiver, _giverName, _reward, _rank, Objective.KILL);
            enemyID = _enemyID;
            amount = _amount;
        }

        @Override
        public String toString() {
            return super.toString() + " " + enemyID + " " + amount;
        }

        public static KillMission Restore(UUID _missionID, UUID _questGiver, String _giverName, int _reward,
                                          long _creationTime,
                                          int _rank, boolean _completed, boolean _handedOut, int _enemyID,
                                          int _amount) {
            KillMission m = new KillMission(_enemyID, _amount);
            m.Set(_missionID, _questGiver, _giverName, _reward, _creationTime, _rank, _completed, _handedOut);
            return m;
        }
    }

    public static class VisitAreaMission extends Mission {

        public int areaID;

        private VisitAreaMission(int _areaID) {
            objective = Objective.VISIT_AREA;
            areaID = _areaID;
        }

        public VisitAreaMission(UUID _questGiver, String _giverName, int _reward, int _rank, int _areaID) {
            super(_questGiver, _giverName, _reward, _rank, Objective.VISIT_AREA);
            areaID = _areaID;
        }

        @Override
        public String toString() {
            return super.toString() + " " + areaID;
        }

        public static VisitAreaMission Restore(UUID _missionID, UUID _questGiver, String _giverName, int _reward,
                                               long _creationTime, int _rank, boolean _completed, boolean _handedOut,
                                               int _areaID) {
            VisitAreaMission m = new VisitAreaMission(_areaID);
            m.Set(_missionID, _questGiver, _giverName, _reward, _creationTime, _rank, _completed, _handedOut);
            return m;
        }
    }

    public static class VisitCityMission extends Mission {

        public UUID   city;
        public String cityName;

        private VisitCityMission(UUID _city, String _cityName) {
            objective = Objective.VISIT_CITY;
            city = _city;
            cityName = _cityName;
        }

        public VisitCityMission(UUID _questGiver, String _giverName, int _reward, int _rank, UUID _city,
                                String _cityName) {
            super(_questGiver, _giverName, _reward, _rank, Objective.VISIT_CITY);
            city = _city;
            cityName = _cityName;
        }

        @Override
        public String toString() {
            return super.toString() + " " + city + " " + cityName;
        }

        public static VisitCityMission Restore(UUID _missionID, UUID _questGiver, String _giverName, int _reward,
                                               long _creationTime, int _rank, boolean _completed, boolean _handedOut,
                                               UUID _city, String _cityName) {
            VisitCityMission m = new VisitCityMission(_city, _cityName);
            m.Set(_missionID, _questGiver, _giverName, _reward, _creationTime, _rank, _completed, _handedOut);
            return m;
        }
    }

    public static class FetchMission extends Mission {

        int itemID;

        private FetchMission(int _itemID) {
            objective = Objective.FETCH;
            itemID = _itemID;
        }

        public FetchMission(UUID _questGiver, String _giverName, int _reward, int _rank, int _itemID) {
            super(_questGiver, _giverName, _reward, _rank, Objective.FETCH);
            itemID = _itemID;
        }

        @Override
        public String toString() {
            return super.toString() + " " + itemID;
        }

        public static FetchMission Restore(UUID _missionID, UUID _questGiver, String _giverName, int _reward,
                                           long _creationTime, int _rank, boolean _completed, boolean _handedOut,
                                           int _itemID) {
            FetchMission m = new FetchMission(_itemID);
            m.Set(_missionID, _questGiver, _giverName, _reward, _creationTime, _rank, _completed, _handedOut);
            return m;
        }
    }
}
