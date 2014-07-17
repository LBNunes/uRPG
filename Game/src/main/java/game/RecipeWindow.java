package game;

import game.ItemWindow.ItemOption;

import java.util.ArrayList;

import org.unbiquitous.uImpala.engine.asset.AssetManager;
import org.unbiquitous.uImpala.engine.asset.Text;
import org.unbiquitous.uImpala.engine.core.GameRenderers;
import org.unbiquitous.uImpala.engine.io.MouseSource;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.util.Corner;
import org.unbiquitous.uImpala.util.observer.Event;
import org.unbiquitous.uImpala.util.observer.Observation;
import org.unbiquitous.uImpala.util.observer.Subject;

public class RecipeWindow extends SelectionWindow {

    static final int          WINDOW_WIDTH    = 13;
    static final int          WINDOW_HEIGHT   = 22;
    static final int          OPTION_OFFSET_X = 32;
    static final int          OPTION_OFFSET_Y = 32;

    private ArrayList<Recipe> list;

    public RecipeWindow(AssetManager assets, String frame, int x, int y, ArrayList<Recipe> list) {
        super(assets, frame, x, y, WINDOW_WIDTH, WINDOW_HEIGHT);
        mouse.connect(MouseSource.EVENT_BUTTON_DOWN, new Observation(this, "OnButtonDown"));
        mouse.connect(MouseSource.EVENT_BUTTON_UP, new Observation(this, "OnButtonUp"));
        this.list = list;
        for (int i = 0; i < list.size(); ++i) {
            Recipe r = list.get(i);
            options.add(new RecipeOption(assets, i, i, x + OPTION_OFFSET_X, y + OPTION_OFFSET_Y,
                                         WINDOW_WIDTH * this.frame.getWidth() / 3 - OPTION_OFFSET_X * 2,
                                         (int) (this.frame.getHeight() * 1.0), r));
        }
    }

    @Override
    public void Swap(int index1, int index2) {
        Option o1 = options.get(index1);
        Option o2 = options.get(index2);

        Recipe r1 = list.get(o1.originalIndex);
        Recipe r2 = list.get(o2.originalIndex);

        list.set(o1.originalIndex, r2);
        list.set(o2.originalIndex, r1);

        o1.index = index2;
        o2.index = index1;

        options.set(index2, o1);
        options.set(index1, o2);

        int oindex1 = o1.originalIndex;
        o1.originalIndex = o2.originalIndex;
        o2.originalIndex = oindex1;

    }

    public Recipe GetSelectedRecipe() {
        if (selected == null) {
            return null;
        }
        else {
            return ((RecipeOption) selected).recipe;
        }
    }

    @Override
    protected void wakeup(Object... args) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void destroy() {
        // TODO Auto-generated method stub

    }

    @Override
    public void OnButtonDown(Event event, Subject subject) {
        super.OnButtonDown(event, subject);
    }

    @Override
    public void OnButtonUp(Event event, Subject subject) {
        super.OnButtonUp(event, subject);
    }

    private class RecipeOption extends ItemOption {

        ArrayList<Text> components;
        Recipe          recipe;

        public RecipeOption(AssetManager assets, int _index, int _originalIndex, int _baseX, int _baseY, int _w,
                            int _h, Recipe _recipe) {
            super(assets, _index, _originalIndex, _baseX, _baseY, _w, _h, false, Item.GetItem(_recipe.itemID), 1, 0);
            recipe = _recipe;

            System.out.println("Recipe for item " + recipe.itemID + "(" + recipe.components.size() + " components)");

            components = new ArrayList<Text>();
            for (Integer component : recipe.components) {
                Item i = Item.GetItem(component);
                components.add(assets.newText("font/seguisb.ttf", i.GetName()));
            }
        }

        @Override
        public void Render(GameRenderers renderers, Screen screen) {
            super.Render(renderers, screen);
            int mx = screen.getMouse().getX();
            int my = screen.getMouse().getY();

            if (box.IsInside(mx, my)) {
                int h = components.get(0).getHeight();
                for (int i = 0; i < components.size(); ++i) {
                    components.get(i).render(screen, mx, my + i * h, Corner.TOP_LEFT);
                }
            }
        }

        @Override
        public void CheckClick(int x, int y) {
            if (box.IsInside(x, y)) {
                selected = true;
            }
        }
    }
}
