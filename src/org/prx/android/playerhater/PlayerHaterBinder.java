package org.prx.android.playerhater;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;

import android.app.Activity;
import android.net.Uri;
import android.os.Binder;
import android.util.Log;

public class PlayerHaterBinder extends Binder implements PlayerHater {

	private final PlayerHaterService mService;

	public PlayerHaterBinder(PlayerHaterService service) {
		mService = service;
	}
	
	public PlayerHaterService getService() {
		Log.w(PlayerHaterService.TAG,
				"#getService() - THIS METHOD HAS BEEN DEPRECATED.");
		Log.w(PlayerHaterService.TAG,
				"You should cast PlayerHaterBinder as PlayerHater and call methods on it directly.");
		return mService;
	}

	@Override
	public boolean play() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean pause() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean stop() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean play(String fileOrUrl) {
		return play(fileOrUrl, fileOrUrl.charAt(0) != '/');
	}

	@Override
	public boolean play(String fileOrUrl, boolean isUrl) {
		if (isUrl) {
			return mService._play(fileOrUrl);
		} else {
			try {
				return play((new FileInputStream(new File(fileOrUrl)))
						.getFD());
			} catch (Exception e) {
				return false;
			}
		}
	}

	@Override
	public String getNowPlaying() {
		return mService._getNowPlaying();
	}

	@Override
	public boolean isPlaying() {
		return mService._isPlaying();
	}

	@Override
	public boolean play(String fileOrUrl, boolean isUrl, Activity activity) {
		setNotificationIntentActivity(activity);
		return play(fileOrUrl, isUrl);
	}

	@Override
	public boolean play(FileDescriptor fd) {
		return mService._play(fd);
	}

	@Override
	public boolean play(FileDescriptor fd, Activity activity) {
		setNotificationIntentActivity(activity);
		return play(fd);
	}

	@Override
	public boolean play(FileDescriptor fd, Activity activity, int view) {
		setNotificationIntentActivity(activity);
		setNotificationView(view);
		return play(fd);
	}

	@Override
	public boolean play(Uri url) {
		return play(url.toString(), true);
	}

	@Override
	public boolean play(Uri url, Activity activity) {
		setNotificationIntentActivity(activity);
		return play(url);
	}

	@Override
	public void setNotificationIntentActivity(Activity activity) {
		mService.setNotificationIntentActivity(activity);
	}

	@Override
	public void setNotificationView(int view) {
		mService.setNotificationView(view);
	}

	@Override
	public boolean play(String fileOrUrl, boolean isUrl, Activity activity,
			int view) {
		setNotificationIntentActivity(activity);
		setNotificationView(view);
		return play(fileOrUrl, isUrl);
	}

	@Override
	public boolean play(Uri url, Activity activity, int view) {
		setNotificationIntentActivity(activity);
		setNotificationView(view);
		return play(url);
	}

	@Override
	public void seekTo(int position) {
		mService.seekTo(position);
	}

	@Override
	public int getPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void set(String key, Object value) {
		mService.set(key, value);
	}

	@Override
	public Object get(String key) {
		return mService.get(key);
	}

	@Override
	public int getState() {
		return mService.getState();
	}
}
