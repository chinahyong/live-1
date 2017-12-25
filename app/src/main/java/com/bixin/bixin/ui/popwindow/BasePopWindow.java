package com.bixin.bixin.ui.popwindow;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.PopupWindow;

import java.lang.ref.WeakReference;

/**
 * Created by Live on 8/22/17.
 */

public class BasePopWindow extends PopupWindow {
    public Context mContext;
    protected MyHandler mHandler = new MyHandler(this);

    public BasePopWindow(Context context){
        mContext = context;
    }

    protected void handleMessage(Message msg){

    }

    public void sendMsg(Message msg) {
        if (mHandler != null) {
            mHandler.sendMessage(msg);
        } else {
            handleMessage(msg);
        }
    }

    protected void sendEmptyMsg(int msg) {
        if (mHandler != null) {
            mHandler.sendEmptyMessage(msg);
        }
    }

    private static class MyHandler extends Handler{
        private final WeakReference<BasePopWindow> moPopWindow;

        public MyHandler(BasePopWindow context) {
            moPopWindow = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            BasePopWindow popWindow = moPopWindow.get();
            if (popWindow != null) {
                popWindow.handleMessage(msg);
            }
        }
    }
}
