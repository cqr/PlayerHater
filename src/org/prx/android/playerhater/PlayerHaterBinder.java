package org.prx.android.playerhater;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import org.prx.android.playerhater.PlayerHater;

import android.app.Activity;
import android.content.res.Resources;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;

public class PlayerHaterBinder extends Binder implements PlayerHater {

	private final PlaybackService mService;
	private PlayerListenerManager mPlayerListenerManager;

	public PlayerHaterBinder(PlaybackService service,
			PlayerListenerManager playerListenerManager) {
		mService = service;
		mPlayerListenerManager = playerListenerManager;
	}

	@Override
	public boolean play() throws IllegalStateException, IOException {
		return mService.play();
	}

	@Override
	public boolean pause() {
		return mService.pause();
	}

	@Override
	public boolean stop() {
		return mService.stop();
	}

	@Override
	public boolean play(String fileOrUrl) throws IllegalStateException,
			IllegalArgumentException, SecurityException, IOException {
		return play(fileOrUrl, fileOrUrl.charAt(0) != '/');
	}

	@Override
	public boolean play(String fileOrUrl, boolean isUrl)
			throws IllegalStateException, IllegalArgumentException,
			SecurityException, IOException {
		if (isUrl) {
			return mService.play(fileOrUrl);
		} else {
			try {
				return play((new FileInputStream(new File(fileOrUrl))).getFD());
			} catch (Exception e) {
				return false;
			}
		}
	}

	@Override
	public String getNowPlaying() {
		return mService.getNowPlaying();
	}

	@Override
	public boolean isPlaying() {
		return mService.isPlaying();
	}
	
	@Override 
	public boolean isLoading() { 
		return mService.isLoading(); 
	}

	@Override
	public boolean play(String fileOrUrl, boolean isUrl, Activity activity)
			throws IllegalStateException, IllegalArgumentException,
			SecurityException, IOException {
		setNotificationIntentActivity(activity);
		return play(fileOrUrl, isUrl);
	}

	@Override
	public boolean play(FileDescriptor fd) throws IllegalStateException,
			IllegalArgumentException, SecurityException, IOException {
		return mService.play(fd);
	}

	@Override
	public boolean play(FileDescriptor fd, Activity activity)
			throws IllegalStateException, IllegalArgumentException,
			SecurityException, IOException {
		setNotificationIntentActivity(activity);
		return play(fd);
	}

	@Override
	public boolean play(FileDescriptor fd, Activity activity, int view)
			throws IllegalStateException, IllegalArgumentException,
			SecurityException, IOException {
		setNotificationIntentActivity(activity);
		setNotificationView(view);
		return play(fd);
	}

	@Override
	public boolean play(Uri url) throws IllegalStateException,
			IllegalArgumentException, SecurityException, IOException {
		return play(url.toString(), true);
	}

	@Override
	public boolean play(Uri url, Activity activity)
			throws IllegalStateException, IllegalArgumentException,
			SecurityException, IOException {
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
			int view) throws IllegalStateException, IllegalArgumentException,
			SecurityException, IOException {
		setNotificationIntentActivity(activity);
		setNotificationView(view);
		return play(fileOrUrl, isUrl);
	}

	@Override
	public boolean play(Uri url, Activity activity, int view)
			throws IllegalStateException, IllegalArgumentException,
			SecurityException, IOException {
		setNotificationIntentActivity(activity);
		setNotificationView(view);
		return play(url);
	}

	@Override
	public void seekTo(int position) {
		mService.seekTo(position);
	}

	@Override
	public int getCurrentPosition() {
		return mService.getCurrentPosition();
	}

	@Override
	public int getState() {
		return mService.getState();
	}

	/*
	 * We use the delegation pattern here, rather than doing things
	 * automatically
	 */
	@Override
	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
		mPlayerListenerManager.setOnBufferingUpdateListener(listener);
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		mPlayerListenerManager.setOnCompletionListener(listener);
	}

	@Override
	public void setOnInfoListener(OnInfoListener listener) {
		mPlayerListenerManager.setOnInfoListener(listener);
	}

	@Override
	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		mService.setOnSeekCompleteListener(listener);
	}

	@Override
	public void setOnErrorListener(OnErrorListener listener) {
		mService.setOnErrorListener(listener);
	}

	@Override
	public void setOnPreparedListener(OnPreparedListener listener) {
		mService.setOnPreparedListener(listener);
	}

	@Override
	public void setListener(PlayerHaterListener listener) {
		mService.setListener(listener);
	}

	/*
	 * End delegated listener methods
	 */

	@Override
	public Bundle getBundle() {
		return mService.getBundle();
	}

	@Override
	public void commitBundle(Bundle icicle) {
		mService.commitBundle(icicle);
	}

	@Override
	public TransientPlayer transientPlay(String fileOrUrl) {
		return transientPlay(fileOrUrl, false);
	}

	@Override
	public TransientPlayer transientPlay(String fileOrUrl, boolean isDuckable) {
		if (fileOrUrl.charAt(0) == '/') {
			try {
				return transientPlay(
						(new FileInputStream(new File(fileOrUrl))).getFD(),
						isDuckable);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} 
		return mService.transientPlay(fileOrUrl, isDuckable);
	}

	@Override
	public TransientPlayer transientPlay(FileDescriptor file) {
		return transientPlay(file, false);
	}

	@Override
	public TransientPlayer transientPlay(FileDescriptor file, boolean isDuckable) {
		return mService.transientPlay(file, isDuckable);
	}

	@Override
	public int getDuration() {
		return mService.getDuration();
	}

	@Override
	public void setNotificationIcon(int notificationIcon) {
		mService.setNotificationIcon(notificationIcon);
	}
	
	@Override
	public void setAutoNotify(boolean autoNotify) {
		mService.setAutoNotify(autoNotify);
	}
	
	@Override
	public void startForeground() {
		mService.doStartForeground();
	}
	
	@Override
	public void stopForeground() {
		mService.doStopForeground();
	}

	@Override
	public void setNotificationTitle(String notificationTitle) {
		mService.setNotificationTitle(notificationTitle);
	}

	@Override
	public void setNotificationText(String notificationText) {
		mService.setNotificationText(notificationText);
	}

	@Override
	public void setLockScreenImage(FileDescriptor file) {
		mService.setLockScreenImage(file); 
	}
	
	@Override 
	public void setLockScreenImage(Resources res, int id) { 
		mService.setLockScreenImage(res, id); 
	}

	@Override
	public void setLockScreenTitle(String title) {
		mService.setLockScreenTitle(title); 
	}
}
