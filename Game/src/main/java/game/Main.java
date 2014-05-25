package game;

import java.util.ArrayList;

import org.unbiquitous.uImpala.engine.core.GameSettings;
import org.unbiquitous.uImpala.engine.io.KeyboardManager;
import org.unbiquitous.uImpala.engine.io.MouseManager;
import org.unbiquitous.uImpala.engine.io.ScreenManager;
import org.unbiquitous.uImpala.engine.io.SpeakerManager;
import org.unbiquitous.uImpala.jse.impl.core.Game;

public class Main {

    @SuppressWarnings({ "serial" })
    public static void main(final String[] args) {
        Game.run(new GameSettings() {
            {
                String current = System.getProperty("user.dir");
                System.out.println("Current working directory in Java : "
                        + current);

                if (args.length == 2 && args[0].equals("-rp"))
                    put("root_path", args[1]);
                put("first_scene", BattleScene.class);
                put("input_managers", new ArrayList<Class<?>>() {
                    {
                        add(KeyboardManager.class);
                        add(MouseManager.class);
                    }
                });
                put("output_managers", new ArrayList<Class<?>>() {
                    {
                        add(ScreenManager.class);
                        add(SpeakerManager.class);
                    }
                });
            }
        });
    }
}
