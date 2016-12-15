package ezy.demo.settingscompat;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class FloatView extends ImageView {


    private Rect mRect = new Rect();
    private WindowManager mWm;
    private WindowManager.LayoutParams mLp = new WindowManager.LayoutParams();

    int mTouchSlop;
    float density = getResources().getDisplayMetrics().density;


    public FloatView(Context context) {
        super(context);

        getWindowVisibleDisplayFrame(mRect);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mTouchSlop = mTouchSlop * mTouchSlop;


        mWm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        mLp.gravity = Gravity.LEFT | Gravity.TOP;
        mLp.format = PixelFormat.RGBA_8888;
        mLp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mLp.type = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            mLp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }


        setOnClickListener(mClickListener);
        setOnTouchListener(mTouchListener);
        setImageResource(R.mipmap.ic_launcher);

    }


    public void attach() {
        if (getParent() == null) {
            mWm.addView(this, mLp);
        }
        mWm.updateViewLayout(this, mLp);
        getWindowVisibleDisplayFrame(mRect);
        mRect.top += dp2px(50);
        mLp.y = dp2px(150);
        mLp.x = mRect.width() - dp2px(55);
        reposition();
    }

    public void detach() {
        try {
            mWm.removeViewImmediate(this);
        } catch (Exception e) {

        }
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(v.getContext(), "clicked!", Toast.LENGTH_SHORT).show();
        }
    };

    private boolean isDragging;
    private OnTouchListener mTouchListener = new OnTouchListener() {
        private float touchX;
        private float touchY;
        private float startX;
        private float startY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchX = event.getX() + getLeft();
                touchY = event.getY() + getTop();
                startX = event.getRawX();
                startY = event.getRawY();
                isDragging = false;
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int) (event.getRawX() - startX);
                int dy = (int) (event.getRawY() - startY);
                if ((dx * dx + dy * dy) > mTouchSlop) {
                    isDragging = true;
                    mLp.x = (int) (event.getRawX() - touchX);
                    mLp.y = (int) (event.getRawY() - touchY);
                    mWm.updateViewLayout(FloatView.this, mLp);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                touchX = touchY = 0.0F;
                if (isDragging) {
                    reposition();
                    isDragging = false;
                    return true;
                }
            }
            return false;
        }
    };


    private int dp2px(int dp) {
        return (int) (dp * density);
    }

    private void reposition() {
        if (mLp.x < (mRect.width() - getWidth()) / 2) {
            mLp.x = dp2px(5);
        } else {
            mLp.x = mRect.width() - dp2px(55);
        }
        if (mLp.y < mRect.top) {
            mLp.y = mRect.top;
        }
        mWm.updateViewLayout(this, mLp);
    }
}