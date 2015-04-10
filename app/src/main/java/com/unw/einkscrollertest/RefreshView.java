package com.unw.einkscrollertest;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.widget.TextView;

/**
 * Created by unw on 15. 4. 7..
 */
public class RefreshView extends TextView
{
    private String TAG;

    private static final int PAGE_PREVIOUS = 92;
    private static final int PAGE_NEXT = 93;
    private static final int VOLUME_UP = 24;
    private static final int VOLUME_DOWN = 25;


    private OnPageKeyListener mOnPageKeyListener;

    public RefreshView(Context context) {
        super(context);
        init();
    }

    public RefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RefreshView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init()
    {
        TAG = getClass().getSimpleName();
    }

    public void setOnPageKeyListener(OnPageKeyListener onPageKeyListener)
    {
        this.mOnPageKeyListener = onPageKeyListener;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown(), keyCode : " + keyCode);

        // 사용자가 PAGE_PREVIOUS, PAGE_NEXT 테스트 하게 해서 상수값으로 설정해두고
        // 그 값을 비교한다.
        // 그 전에 설정에 볼륨키 설정 켜있는지도 확인
        // 일단은 그냥
        if (mOnPageKeyListener != null) {
            switch (keyCode) {
                case VOLUME_UP :
                case PAGE_PREVIOUS :
                    mOnPageKeyListener.onPagePrevious();
                    break;
                case VOLUME_DOWN :
                case PAGE_NEXT :
                    mOnPageKeyListener.onPageNext();
                    break;
                case 4 :
                    return false;
            }
        }

        return super.onKeyDown(keyCode, event);
    }


    public interface OnPageKeyListener
    {
        boolean onPageNext();

        boolean onPagePrevious();
    }
}
