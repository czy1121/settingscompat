package ezy.assist.compat;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RomUtil {
    private static final String TAG = "RomUtil";



    private static final String KEY_EMUI_VERSION_NAME = "ro.build.version.emui";
    private static final String KEY_EMUI_VERSION_CODE = "ro.build.hw_emui_api_level";

    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_HANDY_MODE_SF = "ro.miui.has_handy_mode_sf";
    private static final String KEY_MIUI_REAL_BLUR = "ro.miui.has_real_blur";

    private static final String KEY_FLYME_PUBLISHED = "ro.flyme.published";
    private static final String KEY_FLYME_FLYME = "ro.meizu.setupwizard.flyme";


    public static boolean isEmui() {
        return !TextUtils.isEmpty(getProp(KEY_EMUI_VERSION_NAME));
    }

    public static boolean isMiui() {
        return !TextUtils.isEmpty(getProp(KEY_MIUI_VERSION_NAME));
    }

    public static boolean isFlyme() {
        return Build.DISPLAY.toLowerCase().contains("flyme");
    }

    public static boolean isQihu() {
        return Build.MANUFACTURER.toLowerCase().contains("qihu");
    }


    // 获取 华为(emui) 版本号, 获取失败返回 4.0
    public static double getEmuiVersion() {
        String version = getProp(KEY_EMUI_VERSION_NAME);
        try {
            return Double.parseDouble(version.substring(version.indexOf("_") + 1));
        } catch (Exception e) {
            Log.e(TAG, "get emui version code error, version : " + version);
        }
        return 4.0;
    }


    // 获取小米(miui)版本号，获取失败返回 -1
    public static int getMiuiVersion() {
        String version = getProp(KEY_MIUI_VERSION_NAME);
        try {
            return Integer.parseInt(version.substring(1));
        } catch (Exception e) {
            Log.e(TAG, "get miui version code error, version : " + version);
        }
        return -1;
    }

    public static String getProp(String name) {
        String line = null;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + name);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            Log.e(TAG, "Unable to read prop " + name, ex);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(TAG, "Exception while closing InputStream", e);
                }
            }
        }
        return line;
    }
}