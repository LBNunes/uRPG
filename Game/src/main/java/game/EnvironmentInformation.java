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

public class EnvironmentInformation {

    public static String GetUserName() {
        return System.getProperty("user.name");
    }

    public static String GetOS() {
        return System.getProperty("os.name");
    }

    public static String GetOSVersion() {
        return System.getProperty("os.version");
    }

    public static boolean IsWindows() {
        return GetOS().startsWith("Windows");
    }

    public static String GetSSID() {
        // TODO: This
        return "Ubiquia";
    }

    public static boolean HasBattery() {
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

    public static void PrintBatteryInformation() {
        if (IsWindows()) {
            Kernel32.SYSTEM_POWER_STATUS batteryStatus = new Kernel32.SYSTEM_POWER_STATUS();
            Kernel32.INSTANCE.GetSystemPowerStatus(batteryStatus);

            System.out.println(batteryStatus);
        }
        else {
            // TODO: Linux support
        }
    }
}
