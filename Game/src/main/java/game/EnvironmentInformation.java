/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Luísa Bontempo Nunes
//     Created on 2014-06-07 ymd
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
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileStore;
import java.nio.file.FileSystemException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.sarxos.webcam.Webcam;

public class EnvironmentInformation {

    private static EnvironmentInformation instance;

    public String                         userName;
    public String                         os;
    public String                         osVersion;
    public String                         ssid;
    public String                         computerName;
    public double                         freeSpace;
    public boolean                        hasBattery;
    public boolean                        isDay;

    public static void Initialize() {

        instance = new EnvironmentInformation();

        instance.userName = System.getProperty("user.name");
        instance.os = System.getProperty("os.name");
        instance.osVersion = System.getProperty("os.version");
        instance.freeSpace = FindFreeSpacePercentage();
        instance.ssid = FindSSID();
        instance.hasBattery = DiscoverBattery();
        instance.computerName = DiscoverComputerName();
        instance.isDay = DiscoverDay();
    }

    public static String GetUserName() {
        return instance.userName;
    }

    public static String GetOS() {
        return instance.os;
    }

    public static String GetOSVersion() {
        return instance.osVersion;
    }

    public static double GetFreeSpacePercentage() {
        return instance.freeSpace;
    }

    private static double FindFreeSpacePercentage() {
        int nRoots = 0;
        double freeSpace = 0;
        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            try {
                FileStore store = Files.getFileStore(root);
                double fs = store.getUsableSpace() / (double) store.getTotalSpace();
                freeSpace += fs;
                nRoots += 1;
            }
            catch (FileSystemException e) {
            }
            catch (IOException e) {
            }
        }

        if (nRoots == 0) {
            return 0.5;
        }

        freeSpace /= nRoots;

        return freeSpace;
    }

    public static boolean IsWindows() {
        return GetOS().startsWith("Windows");
    }

    public static String GetSSID() {
        return instance.ssid;
    }

    private static String FindSSID() {
        String ssid = null;

        if (IsWindows()) {
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "netsh wlan show interfaces");
                Process p = pb.start();

                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;

                while (true) {
                    line = r.readLine();
                    if (line == null) {
                        break;
                    }
                    if (line.contains("SSID")) {
                        ssid = line.substring(line.indexOf(':') + 2);
                        break;
                    }
                }
            }
            catch (IOException e) {
            }

            if (ssid == null) {
                ssid = "Not found";
            }

        }
        else {
            // TODO: Linux
            ssid = "Not found";
        }

        return ssid;
    }

    public static boolean HasBattery() {
        return instance.hasBattery;
    }

    private static boolean DiscoverBattery() {
        if (IsWindows()) {
            Kernel32.SYSTEM_POWER_STATUS batteryStatus = new Kernel32.SYSTEM_POWER_STATUS();
            Kernel32.INSTANCE.GetSystemPowerStatus(batteryStatus);

            return batteryStatus.BatteryFlag != (byte) 128;
        }
        else {
            // TODO: Linux support
            return false;
        }
    }

    public static String GetComputerName() {
        return instance.computerName;
    }

    private static String DiscoverComputerName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e) {
            return "Unknown Name";
        }
    }

    public static boolean IsDay() {
        return instance.isDay;
    }

    private static boolean DiscoverDay() {

        final double LIGHT_FACTOR = 0.50;

        PrintStream err = System.err;
        FileOutputStream f = null;
        try {
            f = new FileOutputStream("log.txt");
            System.setErr(new PrintStream(f));
        }
        catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Webcam webcam = Webcam.getDefault();
        webcam.open();

        BufferedImage image = webcam.getImage();

        webcam.close();

        // The luminosity of each pixel can go up to 255.
        // The light factor determines a luminosity threshold: above the threshold, it's day, below, night.
        long threshold = (int) (LIGHT_FACTOR * 255 * image.getWidth() * image.getHeight());
        long coef = 0;

        for (int i = 0; i < image.getWidth(); ++i) {
            for (int j = 0; j < image.getHeight(); ++j) {
                int pixel = image.getRGB(0, 0);
                int r = (pixel & 0x00FF0000) >> 16;
                int g = (pixel & 0x0000FF00) >> 8;
                int b = (pixel & 0x000000FF);

                // Luminosity formula taken from
                // http://gotfu.wordpress.com/2011/12/18/computeluminosity-how-to-calculate-the-luminosity-of-a-pixel-using-ntsc-formula/
                coef += Math.round((0.299 * r) + (0.587 * g) + (0.114 * b));
            }
        }

        System.setErr(err);
        try {
            if (f != null)
                f.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.gc();

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
