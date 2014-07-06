package game;

import game.PlayerData.Inventory;
import game.PlayerData.Inventory.IEntry;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Recipe {

    private static ArrayList<Recipe> recipes;

    public int                       itemID;
    public ArrayList<Integer>        components;

    public static ArrayList<Recipe> GetAllRecipes() {
        return recipes;
    }

    public static ArrayList<Recipe> GetPossibleRecipes(Inventory inv) {
        ArrayList<Recipe> list = new ArrayList<Recipe>();
        boolean[] present = new boolean[1000];

        for (boolean b : present) {
            b = false;
        }

        for (IEntry e : inv.itemList) {
            present[e.item] = true;
        }

        for (Recipe r : recipes) {
            for (Integer component : r.components) {
                if (present[component]) {
                    list.add(r);
                }
            }
        }

        return list;
    }

    private Recipe(int _itemID, ArrayList<Integer> _components) {
        itemID = _itemID;
        components = _components;
    }

    public static void InitTable() {
        FileInputStream f;
        try {
            f = new FileInputStream(Config.RECIPE_DATA);
            Scanner s = new Scanner(f);
            String line;

            int itemID;
            ArrayList<Integer> components = new ArrayList<Integer>();

            while (s.hasNextLine()) {
                line = s.nextLine();

                if (line.length() == 0 || line.charAt(0) == '#') {
                    continue;
                }

                StringTokenizer tokenizer = new StringTokenizer(line, " ");

                itemID = Integer.parseInt(tokenizer.nextToken());

                while (tokenizer.hasMoreTokens()) {
                    components.add(Integer.parseInt(tokenizer.nextToken()));
                }

                recipes.add(new Recipe(itemID, components));
            }

            s.close();
            f.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("FATAL ERROR: File '" + Config.RECIPE_DATA + "' was not found!");
            System.exit(1);
        }
        catch (IOException e) {
            System.out.println("WARNING: File '" + Config.RECIPE_DATA + "' may have been read incorrectly.");
        }
    }

}
