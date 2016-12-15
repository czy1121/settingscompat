package ezy.demo.settingscompat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import ezy.assist.compat.RomUtil;
import ezy.assist.compat.SettingsCompat;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    TextView vResult;
    FloatView vFloat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vFloat = new FloatView(this);
        vResult = (TextView) findViewById(R.id.result);

        findViewById(R.id.result).setOnClickListener(this);
        findViewById(R.id.check).setOnClickListener(this);
        findViewById(R.id.manage).setOnClickListener(this);
        findViewById(R.id.toggle).setOnClickListener(this);
        findViewById(R.id.detail).setOnClickListener(this);

        TextView tv = (TextView) findViewById(R.id.info);
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());

        tv.setText(readString("/system/build.prop"));
        if (SettingsCompat.canDrawOverlays(this)) {
            vFloat.attach();
        } else {
            vFloat.detach();
        }
    }

    void checkPermission() {
        if (SettingsCompat.canDrawOverlays(this)) {
            vResult.setText(RomUtil.getVersion() + "\n" + RomUtil.getName() + "\ngranted");
//            vFloat.attach();
        } else {
            vResult.setText(RomUtil.getVersion() + "\n" +RomUtil.getName() + "\ndenied");
//            vFloat.detach();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
        case R.id.detail:
            Intent intent1 = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent1.setData(Uri.fromParts("package", this.getPackageName(), null));
            startActivity(intent1);
            break;
        case R.id.result:
            ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            manager.setPrimaryClip(ClipData.newPlainText("build.prop", readString("/system/build.prop")));
            break;
        case R.id.check:
            checkPermission();
            if (SettingsCompat.canDrawOverlays(this)) {
                vFloat.attach();
            } else {
                vFloat.detach();
            }
            break;
        case R.id.manage:
            SettingsCompat.manageDrawOverlays(this);
            break;
        case R.id.toggle:
            boolean granted1 = SettingsCompat.canDrawOverlays(this);
            SettingsCompat.setDrawOverlays(this, !granted1);
            boolean granted2 = SettingsCompat.canDrawOverlays(this);
            vResult.setText(RomUtil.getVersion() + "\n" +RomUtil.getName() + "\ngranted: " + granted1 + ", " + granted2);
            if (granted2) {
                vFloat.attach();
            } else {
                vFloat.detach();
            }
            break;

        }
    }

    public static String readString(String file) {
        InputStream input = null;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            input = new FileInputStream(new File(file));
            byte[] buffer = new byte[1024 * 4];
            int n;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
            output.flush();
            return output.toString("UTF-8");
        } catch (IOException e) {
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }
}
