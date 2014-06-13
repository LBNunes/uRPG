/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Luísa Bontempo Nunes
//     Created on 2014-04-12 ymd
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

                System.out.println("Running on Windows? " + EnvironmentInformation.IsWindows());
                System.out.println("Has Battery? " + EnvironmentInformation.HasBattery());

                if (args.length == 2 && args[0].equals("-rp"))
                    put("root_path", args[1]);
                put("first_scene", WorldMapScene.class);
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
