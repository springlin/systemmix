package com.myun.utils;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;

public class KeyUtil {

	private int currentKeyCode = 0;

	private static Boolean isDoubleClick = false;
	private static Boolean isLongClick = false;

	CheckForLongPress mPendingCheckForLongPress = null;
	CheckForDoublePress mPendingCheckForDoublePress = null;
	Handler mHandler = new Handler();
	public onKeyClick onkeyclick=null;
	
	Context mContext = null;
	private String TAG = "";

	public KeyUtil(Context context, String tag) {
		mContext = context;
		onkeyclick=null;
		TAG = tag;
	}

	public void dispatchKeyEvent(KeyEvent event) {
		int keycode = event.getKeyCode();

		// 有不同按键按下，取消长按、短按的判断
		if (currentKeyCode != keycode) {
			removeLongPressCallback();
			isDoubleClick = false;
		}

		// 处理长按、单击、双击按键
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			checkForLongClick(event);
		} else if (event.getAction() == KeyEvent.ACTION_UP) {
			checkForDoubleClick(event);
		}


	}

	private void removeLongPressCallback() {
		if (mPendingCheckForLongPress != null) {
			mHandler.removeCallbacks(mPendingCheckForLongPress);
		}
	}

	private void checkForLongClick(KeyEvent event) {
		int count = event.getRepeatCount();
		int keycode = event.getKeyCode();
		if (count == 0) {
			currentKeyCode = keycode;
		} else {
			return;
		}
		if (mPendingCheckForLongPress == null) {
			mPendingCheckForLongPress = new CheckForLongPress();
		}
		mPendingCheckForLongPress.setKeycode(event.getKeyCode());
		mHandler.postDelayed(mPendingCheckForLongPress, 500);
	}

	class CheckForLongPress implements Runnable {

		int currentKeycode = 0;

		public void run() {
			isLongClick = true;
			longPress(currentKeycode);
		}

		public void setKeycode(int keycode) {
			currentKeycode = keycode;
		}
	}

	private void longPress(int keycode) {
		//Log.i(TAG, "--longPress 长按事件--" + keycode);
		if(onkeyclick!=null) onkeyclick.onKeyClick(keycode, 3);
		
	}

	private void singleClick(int keycode) {
		//Log.i(TAG, "--singleClick 单击事件--" + keycode);
		if(onkeyclick!=null) onkeyclick.onKeyClick(keycode, 1);
	}

	private void doublePress(int keycode) {
		//Log.i(TAG, "---doublePress 双击事件--" + keycode);
		if(onkeyclick!=null) onkeyclick.onKeyClick(keycode, 2);
	}

	private void checkForDoubleClick(KeyEvent event) {
		// 有长按时间发生，则不处理单击、双击事件
		removeLongPressCallback();
		if (isLongClick) {
			isLongClick = false;
			return;
		}

		if (!isDoubleClick) {
			isDoubleClick = true;
			if (mPendingCheckForDoublePress == null) {
				mPendingCheckForDoublePress = new CheckForDoublePress();
			}
			mPendingCheckForDoublePress.setKeycode(event.getKeyCode());
			mHandler.postDelayed(mPendingCheckForDoublePress, 400);
		} else {
			// 500ms内两次单击，触发双击
			isDoubleClick = false;
			doublePress(event.getKeyCode());
		}
	}

	class CheckForDoublePress implements Runnable {

		int currentKeycode = 0;

		public void run() {
			if (isDoubleClick) {
				singleClick(currentKeycode);
			}
			isDoubleClick = false;
		}

		public void setKeycode(int keycode) {
			currentKeycode = keycode;
		}
	}

	private void removeDoublePressCallback() {
		if (mPendingCheckForDoublePress != null) {
			mHandler.removeCallbacks(mPendingCheckForDoublePress);
		}
	}
	
	public interface onKeyClick{
		   public void  onKeyClick(int keycode, int action);
	}
}

