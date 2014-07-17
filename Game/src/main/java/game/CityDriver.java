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

import game.CityData.PlayerVisit;
import game.CityData.Transaction;
import game.PlayerData.KnownCity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.applicationManager.CallContext;
import org.unbiquitous.uos.core.driverManager.UosDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;

public class CityDriver implements UosDriver {

    private UpDriver driver;

    public CityDriver() {
        driver = new UpDriver("uRPG.cityDriver");
        driver.addService("GetCityInfo");
        driver.addService("IntroducePlayer");
        driver.addService("RequestEnergyRestore");
        driver.addService("RequestRecruitList");
        driver.addService("RequestRecruit");
        driver.addService("RequestTransactionList");
        driver.addService("RequestTransaction");
        driver.addService("RequestCompletedTransactions");
        driver.addService("CreateTransaction");
        driver.addService("RequestMissionList");
        driver.addService("RequestMission");
        driver.addService("RequestReward");
    }

    public void init(Gateway gateway, InitialProperties properties, String instanceId) {
        System.out.println("Starting up City Driver...");
    }

    public UpDriver getDriver() {
        return driver;
    }

    public List<UpDriver> getParent() {
        // TODO Auto-generated method stub
        return null;
    }

    public void destroy() {
        // TODO Auto-generated method stub

    }

    public void Log(String s) {
        System.out.println("CITY DRIVER LOG: " + s);
    }

    public void GetCityInfo(Call call, Response response, CallContext context) {
        CityData data = CityData.GetData();

        response.addParameter("uuid", data.uuid.toString());
        response.addParameter("name", data.name);
        response.addParameter("area", "" + data.affinityAreaID);
        Log("Sent out info.");
    }

    public void IntroducePlayer(Call call, Response response, CallContext context) {
        CityData data = CityData.GetData();

        UUID uuid = UUID.fromString(call.getParameterString("uuid"));
        int averageLevel = Integer.parseInt(call.getParameterString("averageLevel"));
        int totalLevel = Integer.parseInt(call.getParameterString("totalLevel"));
        ArrayList<String> knownCities = (ArrayList<String>) call.getParameter("knownCities");

        int knownCitiesSize = data.knownCities.size();

        for (String s : knownCities) {
            KnownCity k = KnownCity.FromString(s);
            data.DiscoverCity(k);
        }

        data.DiscoverPlayer(uuid, averageLevel, totalLevel);

        Log("Discovered player with UUID " + uuid.toString() +
            ". Discovered " + (data.knownCities.size() - knownCitiesSize) + " new cities.");
        CityData.Save();
    }

    public void RequestEnergyRestore(Call call, Response response, CallContext context) {
        CityData data = CityData.GetData();

        UUID uuid = UUID.fromString(call.getParameterString("uuid"));
        boolean ok = data.ApproveEnergyRestore(uuid);
        response.addParameter("confirmation", "" + ok);
        if (ok) {
            Log("Approved energy restore to player " + uuid.toString() + ".");
        }
        else {
            Log("Denied energy restore to player " + uuid.toString() + ".");
        }
        CityData.Save();
    }

    public void RequestRecruitList(Call call, Response response, CallContext context) {
        CityData data = CityData.GetData();

        String[] recruits = new String[data.recruits.size()];

        for (int i = 0; i < data.recruits.size(); ++i) {
            Entity e = data.recruits.get(i);
            recruits[i] = e.name + " " + e.classID + " " + e.jobLevel;
        }

        response.addParameter("recruits", recruits);
        Log("Sent out recruits list. Size: " + recruits.length);
    }

    public void RequestRecruit(Call call, Response response, CallContext context) {
        CityData data = CityData.GetData();

        String name = call.getParameterString("name");

        for (Entity e : data.recruits) {
            if (e.name.equals(name)) {
                response.addParameter("confirmation", "" + true);
                data.recruits.remove(e);
                CityData.Save();
                Log("Recruit " + e.name + " was recruited.");
                return;
            }
        }
        response.addParameter("confirmation", "" + false);
        Log("Requested recruit " + name + " was not found.");
    }

    public void RequestTransactionList(Call call, Response response, CallContext context) {
        CityData data = CityData.GetData();

        ArrayList<String> transactions = new ArrayList<String>();

        for (Transaction t : data.marketTransactions) {
            if (!t.completed) {
                transactions.add(t.toString());
            }
        }

        response.addParameter("transactions", transactions);
        Log("Sent out transactions list. Size: " + transactions.size());
    }

    public void RequestTransaction(Call call, Response response, CallContext context) {
        CityData data = CityData.GetData();

        Log("Transaction request...");

        UUID seller = UUID.fromString(call.getParameterString("sellerUUID"));
        int item = (Integer) call.getParameter("item");
        int price = (Integer) call.getParameter("price");

        Transaction sought = null;

        Log("Searching for transaction " + seller.toString() + " " + item + " " + price);

        for (Transaction t : data.marketTransactions) {
            Log("Compare to " + t.seller.toString() + " " + t.item + " " + t.value + " " + t.completed);
            if (t.seller.equals(seller) && t.item == item && t.value == price && !t.completed) {
                Log("Found!");
                sought = t;
                break;
            }
        }
        if (sought == null) {
            response.addParameter("confirmation", "" + false);
            Log("Denied sale of one " + Item.GetItem(item).GetName() + " belonging to " + seller.toString() + " for " +
                price + "G.");
        }
        else {
            response.addParameter("confirmation", "" + true);
            sought.completed = true;
            Log("Approved sale of one " + Item.GetItem(item).GetName() + " belonging to " + seller.toString() +
                " for " + price + "G.");
            CityData.Save();
        }
    }

    public void RequestCompletedTransactions(Call call, Response response, CallContext context) {
        CityData data = CityData.GetData();

        UUID playerUUID = UUID.fromString(call.getParameterString("playerUUID"));
        ArrayList<String> transactions = new ArrayList<String>();

        Iterator<Transaction> i = data.marketTransactions.iterator();

        while (i.hasNext()) {
            Transaction t = i.next();
            if (playerUUID.equals(t.seller) && t.completed) {
                transactions.add(t.toString());
                i.remove();
                CityData.Save();
            }
        }

        response.addParameter("transactions", transactions);
        Log("Sent out completed transactions from player " + playerUUID.toString() + ". Size: " + transactions.size());
    }

    public void CreateTransaction(Call call, Response response, CallContext context) {

        CityData data = CityData.GetData();

        UUID playerUUID = UUID.fromString(call.getParameterString("playerUUID"));
        int item = (Integer) call.getParameter("item");
        int price = (Integer) call.getParameter("price");

        data.marketTransactions.addFirst(new Transaction(playerUUID, item, price));
        CityData.Save();
        Log("Inserted new transaction by player " + playerUUID.toString());

        response.addParameter("confirmation", true);
    }

    public void RequestMissionList(Call call, Response response, CallContext context) {
        CityData data = CityData.GetData();

        ArrayList<String> missions = new ArrayList<String>();

        for (Mission m : data.guildMissions) {
            if (!m.completed) {
                missions.add(m.toString());
            }
        }

        response.addParameter("missions", missions);
        Log("Sent out mission list. Size: " + missions.size());
    }

    public void RequestMission(Call call, Response response, CallContext context) {
        CityData data = CityData.GetData();

        UUID missionID = UUID.fromString(call.getParameterString("missionID"));
        UUID playerUUID = UUID.fromString(call.getParameterString("playerUUID"));

        PlayerVisit player = null;

        for (PlayerVisit v : data.playerVisits) {
            if (v.playerUUID.equals(playerUUID)) {
                player = v;
            }
        }

        if (player == null) {
            response.addParameter("confirmation", "" + false);
            Log("Player " + playerUUID.toString() + " not found.");
            return;
        }

        long time = System.currentTimeMillis();

        for (Mission m : data.guildMissions) {
            if (m.missionID.equals(missionID)) {
                if (!m.completed) {
                    if (time - player.lastMissionHandout > Config.MS_PER_MISSION_HANDOUT) {
                        response.addParameter("confirmation", "" + true);
                        m.handedOut = true;
                        player.lastMissionHandout = time;
                        Log("Mission " + missionID.toString() + " handed out to player ");
                        CityData.Save();
                        return;
                    }
                    else {
                        Log("Denied mission " + missionID.toString() + " to player " + playerUUID.toString() +
                            "(too soon)");
                    }
                }
                else {
                    Log("Denied mission " + missionID.toString() + " to player " + playerUUID.toString() +
                        "(completed)");
                }

            }
        }
        response.addParameter("confirmation", "" + false);
        Log("Denied mission " + missionID.toString() + " to player " + playerUUID.toString() + "(not found)");
    }

    public void RequestReward(Call call, Response response, CallContext context) {
        CityData data = CityData.GetData();

        UUID missionID = UUID.fromString(call.getParameterString("missionID"));
        for (Mission m : data.guildMissions) {
            if (m.missionID.equals(missionID)) {
                if (!m.completed) {
                    response.addParameter("confirmation", "" + true);
                    m.completed = true;
                    CityData.Save();
                    return;
                }
                else {
                    response.addParameter("confirmation", "" + false);
                    Log("Denied mission " + missionID.toString() + " 's reward to player (already completed)");
                    return;
                }
            }
        }
        response.addParameter("confirmation", "" + false);
        Log("Denied mission " + missionID.toString() + "'s reward to player (not found)");
    }
}
