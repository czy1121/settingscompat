package ezy.demo.overlaypermission;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import ezy.assist.compat.OverlayPermissionCompat;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    TextView vResult;
    FloatView vFloat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vFloat = new FloatView(this);
        vResult = (TextView) findViewById(R.id.result);

        findViewById(R.id.check).setOnClickListener(this);
        findViewById(R.id.request).setOnClickListener(this);
        findViewById(R.id.hide).setOnClickListener(this);

        checkPermission();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("ezy", "onActivityResult, requestCode = " + requestCode + ", checkPermission = " + OverlayPermissionCompat.check(this));
        if (OverlayPermissionCompat.isGranted(this, requestCode)) {
            vResult.setText("grant ok");
            vFloat.attach();
        }
    }

    void checkPermission() {
        if (OverlayPermissionCompat.check(this)) {
            Log.e("ezy", "checkPermission = granted");
            vResult.setText("granted");
            vFloat.attach();
        } else {
            vResult.setText("denied");
            Log.e("ezy", "checkPermission = denied");
            OverlayPermissionCompat.inquire(this);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
        case R.id.check:
            checkPermission();
            break;
        case R.id.request:
            OverlayPermissionCompat.request(this);
            break;
        case R.id.hide:
            vFloat.detach();
            break;

        }
    }

}
