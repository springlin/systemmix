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

		// �в�ͬ�������£�ȡ���������̰����ж�
		if (currentKeyCode != keycode) {
			removeLongPressCallback();
			isDoubleClick = false;
		}

		// ��������������˫������
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
		//Log.i(TAG, "--longPress �����¼�--" + keycode);
		if(onkeyclick!=null) onkeyclick.onKeyClick(keycode, 3);
		
	}

	private void singleClick(int keycode) {
		//Log.i(TAG, "--singleClick �����¼�--" + keycode);
		if(onkeyclick!=null) onkeyclick.onKeyClick(keycode, 1);
	}

	private void doublePress(int keycode) {
		//Log.i(TAG, "---doublePress ˫���¼�--" + keycode);
		if(onkeyclick!=null) onkeyclick.onKeyClick(keycode, 2);
	}

	private void checkForDoubleClick(KeyEvent event) {
		// �г���ʱ�䷢�����򲻴�������˫���¼�
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
			// 500ms�����ε���������˫��
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

