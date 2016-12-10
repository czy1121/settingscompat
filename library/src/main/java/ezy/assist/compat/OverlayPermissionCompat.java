/*
 * Copyright 2016 czy1121
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ezy.assist.compat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;


public class OverlayPermissionCompat {
    private static final String TAG = "ezy.overlay";

    private static int sRequestCode = 36724;

    public static void setRequestCode(int code) {
        sRequestCode = code;
    }

    public static boolean check(Context context) {
        return impl.check(context);
    }

    public static void request(Activity activity) {
        impl.request(activity);
    }

    public static void inquire(final Activity activity) {
        new AlertDialog.Builder(activity).setMessage("需要悬浮窗权限，是否去授权？").setNegativeButton("否", null).setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                request(activity);
            }
        }).show();
    }

    public static boolean isGranted(Context context, int requestCode) {
        return requestCode == sRequestCode && impl.check(context);
    }

    private interface IMPL {
        boolean check(Context context);

        void request(Activity activity);
    }

    private static class IMPLBase implements IMPL {

        public boolean check(Context context) {
            return true;
        }

        public void request(Activity activity) {
        }

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static class IMPL19 extends IMPLBase {

        private static final int OP_SYSTEM_ALERT_WINDOW = 24;

        @Override
        public boolean check(Context context) {
            return checkOp(context, OP_SYSTEM_ALERT_WINDOW);
        }

        public void request(Activity activity) {
            if (RomUtil.isMiui()) {
                editForMiui(activity);
            } else if (RomUtil.isEmui()) {
                editForEmui(activity);
            } else if (RomUtil.isFlyme()) {
                editForMeizu(activity);
            } else if (RomUtil.isQihu()) {
                editForQihu(activity);
            } else {
                Log.e(TAG, "ROM no support~~");
            }
        }

        boolean checkOp(Context context, int op) {
            AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {
                Method method = AppOpsManager.class.getDeclaredMethod("checkOp", int.class, int.class, String.class);
                return AppOpsManager.MODE_ALLOWED == (int) method.invoke(manager, op, Binder.getCallingUid(), context.getPackageName());
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
            return false;
        }

        boolean startSafely(Context context, Intent intent) {
            if (context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
                start(context, intent);
                return true;
            } else {
                Log.e(TAG, "Intent is not available!");
                return false;
            }
        }

        void start(Context context, Intent intent) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, sRequestCode);
            } else {
                context.startActivity(intent);
            }
        }

        // 小米
        void editForMiui(Context context) {
            int version = RomUtil.getMiuiVersion();
            Log.e(TAG, "miui version " + version);
            if (version == 5) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                startSafely(context, intent);
            } else if (version > 5) {
                Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                intent.putExtra("extra_pkgname", context.getPackageName());
                intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                if (!startSafely(context, intent)){
                    intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                    startSafely(context, intent);
                }
            }
        }

        final static String HUAWEI_PACKAGE = "com.huawei.systemmanager";
        final static String HUAWEI_SETTING = "com.huawei.permissionmanager.ui.MainActivity";
        final static String HUAWEI_SETTING_3_1 = "com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity";
        final static String HUAWEI_SETTING_3_0 = "com.huawei.notificationmanager.ui.NotificationManagmentActivity";

        // 华为 http://blog.csdn.net/pkandroid/article/details/52014653
        void editForEmui(Context context) {
            try {
                Intent intent = new Intent();
                // 悬浮窗管理页面
                double version = RomUtil.getEmuiVersion();
                Log.e(TAG, "emui version: " + version);
                if (version == 3.1) {
                    intent.setComponent(new ComponentName(HUAWEI_PACKAGE, HUAWEI_SETTING_3_1));
                } else {
                    intent.setComponent(new ComponentName(HUAWEI_PACKAGE, HUAWEI_SETTING_3_0));
                }
                start(context, intent);
            } catch (SecurityException e) {
                Log.e(TAG, Log.getStackTraceString(e));
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(HUAWEI_PACKAGE, HUAWEI_SETTING));
                startSafely(context, intent);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, Log.getStackTraceString(e));
                // 手机管家版本较低 HUAWEI SC-UL10
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.permission.TabItem"));
                startSafely(context, intent);
            } catch (Exception e) {
                // 抛出异常时提示信息
                Toast.makeText(context, "进入设置页面失败，请手动设置悬浮窗权限", Toast.LENGTH_LONG).show();
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        // 魅族
        void editForMeizu(Context context) {
            Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
            intent.setClassName("com.meizu.safe", "com.meizu.safe.security.AppSecActivity");
            intent.putExtra("packageName", context.getPackageName());
            start(context, intent);
        }

        // 360
        void editForQihu(Context context) {
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", "com.android.settings.Settings$OverlaySettingsActivity");
            start(context, intent);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static class IMPL23 extends IMPLBase {

        @Override
        public boolean check(Context context) {
            return Settings.canDrawOverlays(context);
        }

        @Override
        public void request(Activity activity) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, sRequestCode);
        }
    }


    static final IMPL impl;

    static {
        final int version = android.os.Build.VERSION.SDK_INT;
        if (version >= 23) {
            impl = new IMPL23();
        } else if (version >= 19) {
            impl = new IMPL19();
        } else {
            impl = new IMPLBase();
        }
    }
}
