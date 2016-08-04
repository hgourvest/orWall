package org.ethack.orwall.lib;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.command.SimpleCommand;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class Util {
    public static boolean isOrbotInstalled(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo(Constants.ORBOT_APP_NAME, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static int getOrbotUID(Context context){
        try {
            PackageManager pm = context.getPackageManager();
            return pm.getApplicationInfo(Constants.ORBOT_APP_NAME, 0).uid;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    /**
     * Apply or remove rules for captive portal detection.
     * Captive portal detection works with DNS and redirection detection.
     * Once the device is connected, Android will probe the network in order to get a page, located on Google servers.
     * If it can connect to it, this means we're not in a captive network; otherwise, it will prompt for network login.
     * @param status boolean, true if we want to enable this probe.
     * @param context application context
     */
    public static void enableCaptiveDetection(boolean status, Context context) {
        // TODO: find a way to disable it on android <4.4
        // TODO: we may want to get some setting writer directly through the API.
        // This seems to be done with a System app only. orWall may become a system app.
        if (Build.VERSION.SDK_INT > 18) {

            String CMD;
            if (status) {
                CMD = new File(context.getDir("bin", 0), "activate_portal.sh").getAbsolutePath();
            } else {
                CMD = new File(context.getDir("bin", 0), "deactivate_portal.sh").getAbsolutePath();
            }
            Shell shell = null;
            try {
                shell = Shell.startRootShell();
            } catch (IOException e) {
                Log.e("Shell", "Unable to get shell");
            }

            if (shell != null) {
                SimpleCommand command = new SimpleCommand(CMD);
                try {
                    shell.add(command).waitForFinish();
                } catch (IOException e) {
                    Log.e("Shell", "IO Error");
                } catch (TimeoutException e) {
                    Log.e("Shell", "Timeout");
                } finally {
                    try {
                        shell.close();
                    } catch (IOException e) {

                    }
                }
            }
        }
    }

    public static String StringJoin(char sep, ArrayList<String> arr){
        if (arr == null) return null;
        switch (arr.size()){
            case 0: return null;
            case 1: return arr.get(0);
            default:
                StringBuilder ret = new StringBuilder();
                ret.append(arr.get(0));
                for (int i = 1; i < arr.size(); i++) {
                    ret.append(sep).append(arr.get(i));
                }
                return ret.toString();
        }
    }

    public static ArrayList<String> StringSplit(String str) {
        if (str == null) return null;
        str = str.trim();
        if (str.isEmpty()) return null;
        ArrayList<String> ret = new ArrayList<>();
        for(String s: str.split("\\s+")) ret.add(s);
        return ret;
    }

    public static ArrayList<String> StringFilterTCP(String str) {
        ArrayList<String> items = StringSplit(str);
        if (items != null){
            ArrayList<String> ret = new ArrayList<>();
            String port;
            String[] list;
            for (int i = 0; i < items.size(); i++) {
                port = items.get(i);
                list = port.split(":");
                switch (list.length) {
                    case 1:
                        ret.add(port);
                        break;
                    case 2:
                        if (list[0].equals("tcp")) {
                            ret.add(list[1]);
                        }
                        break;
                    default:
                        // error
                }
            }
            if (ret.size() > 0)
                return ret;
        }
        return null;
    }

    public static ArrayList<String> StringFilterUDP(String str) {
        ArrayList<String> items = StringSplit(str);
        if (items != null){
            ArrayList<String> ret = new ArrayList<>();
            String port;
            String[] list;
            for (int i = 0; i < items.size(); i++) {
                port = items.get(i);
                list = port.split(":");
                if (list.length == 2 && list[0].equals("udp")) {
                    ret.add(list[1]);
                }
            }
            if (ret.size() > 0)
                return ret;
        }
        return null;
    }
}
