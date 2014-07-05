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

import game.Classes.ClassID;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

public class Ability {

    private static HashMap<Integer, Ability> table = new HashMap<Integer, Ability>();

    public enum AreaType {
        SELF, CIRCLE, LINE, ALLIES, FOES
    }

    public enum DamageType {
        PHYSICAL, MAGICAL
    }

    public int        id;
    public String     name;
    public String     path;
    public int        frames;
    public float      fps;
    public ClassID    classID;
    public int        rank;
    public int        abilityPower;
    public int        cost;
    public int        castRange;
    public int        areaRange;
    public AreaType   areaType;
    public DamageType damageType;
    public boolean    damaging;
    public boolean    targetsDead;

    public static Ability GetAbility(int abilityID) {
        return table.get(abilityID);
    }

    public static ArrayList<Ability> GetEnemy(Predicate<Ability> p) {
        ArrayList<Ability> list = new ArrayList<Ability>();

        Set<Integer> keys = table.keySet();
        for (int key : keys) {
            Ability a = table.get(key);
            if (p.Eval(a)) {
                list.add(a);
            }
        }
        return list;
    }

    private Ability(int id, String name, String path, int frames, float fps, ClassID classID, int rank,
                    int abilityPower, int cost, int castRange, int areaRange, AreaType areaType, DamageType damageType,
                    boolean damaging, boolean targetsDead) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.frames = frames;
        this.fps = fps;
        this.classID = classID;
        this.rank = rank;
        this.abilityPower = abilityPower;
        this.cost = cost;
        this.castRange = castRange;
        this.areaRange = areaRange;
        this.areaType = areaType;
        this.damageType = damageType;
        this.damaging = damaging;
        this.targetsDead = targetsDead;
    }

    public static void InitTable() {
        FileInputStream f;
        try {
            f = new FileInputStream(Config.ABILITY_DATA);
            Scanner s = new Scanner(f);
            String line;

            int id;
            String name;
            String path;
            int frames;
            float fps;
            ClassID classID;
            int rank;
            int abilityPower;
            int cost;
            int castRange;
            int areaRange;
            AreaType areaType;
            DamageType damageType;
            boolean damaging;
            boolean targetsDead;

            while (s.hasNextLine()) {
                line = s.nextLine();

                if (line.length() == 0 || line.charAt(0) == '#') {
                    continue;
                }

                StringTokenizer tokenizer = new StringTokenizer(line, " ");

                id = Integer.parseInt(tokenizer.nextToken());
                name = tokenizer.nextToken().replace("_", " ");
                classID = ClassID.valueOf(tokenizer.nextToken());
                path = tokenizer.nextToken();
                frames = Integer.parseInt(tokenizer.nextToken());
                fps = Float.parseFloat(tokenizer.nextToken());
                rank = Integer.parseInt(tokenizer.nextToken());
                abilityPower = Integer.parseInt(tokenizer.nextToken());
                cost = Integer.parseInt(tokenizer.nextToken());
                castRange = Integer.parseInt(tokenizer.nextToken());
                areaRange = Integer.parseInt(tokenizer.nextToken());
                areaType = AreaType.valueOf(tokenizer.nextToken());
                damageType = DamageType.valueOf(tokenizer.nextToken());
                damaging = Integer.parseInt(tokenizer.nextToken()) != 0;
                targetsDead = Integer.parseInt(tokenizer.nextToken()) != 0;

                table.put(id, new Ability(id, name, path, frames, fps, classID, rank, abilityPower, cost, castRange,
                                          areaRange, areaType, damageType, damaging, targetsDead));
            }

            s.close();
            f.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("FATAL ERROR: File '" + Config.ABILITY_DATA + "' was not found!");
            System.exit(1);
        }
        catch (IOException e) {
            System.out.println("WARNING: File '" + Config.ABILITY_DATA + "' may have been read incorrectly.");
        }
    }
}
