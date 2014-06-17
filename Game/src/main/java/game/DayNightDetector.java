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

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import com.github.sarxos.webcam.Webcam;

public class DayNightDetector {

    private static BufferedImage image        = null;
    public static final double   LIGHT_FACTOR = 0.50;

    public static void TakePicture() {
        Webcam webcam = Webcam.getDefault();
        webcam.open();

        image = webcam.getImage();

        webcam.close();
    }

    public static boolean IsDay() {

        PrintStream err = System.err;
        try {
            System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream("camLog.txt"))));
        }
        catch (FileNotFoundException e) {
        }

        TakePicture();

        long coef = 0;
        // The sum of the channels of each pixel can go up to 3 * 255.
        // The light factor determines how close the image can be to pure white
        long threshold = (int) (LIGHT_FACTOR * 3 * 255 * image.getWidth() * image.getHeight());

        for (int i = 0; i < image.getWidth(); ++i) {
            for (int j = 0; j < image.getHeight(); ++j) {
                int pixel = image.getRGB(0, 0);
                int r = (pixel & 0x00FF0000) >> 16;
                int g = (pixel & 0x0000FF00) >> 8;
                int b = (pixel & 0x000000FF);

                coef += r + g + b;
            }
        }

        System.setErr(err);

        if (coef >= threshold) {
            System.out.println("Coefficient " + coef + " > " + threshold + " threshold\t\tDay");
            return true;
        }
        else {
            System.out.println("Coefficient " + coef + " < " + threshold + " threshold\t\tNight");
            return false;
        }
    }
}
