package org.prx.android.playerhater;

import org.prx.android.playerhater.plugins.PlayerHaterPlugin;
import org.prx.android.playerhater.util.AudioPlaybackInterface;
import org.prx.android.playerhater.util.AutoBindPlayerHater;
import org.prx.android.playerhater.util.BroadcastReceiver;
import org.prx.android.playerhater.util.ConfigurationManager;
import org.prx.android.playerhater.util.TransientPlayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.util.Log;

public class PlayerHater implements
		AudioPlaybackInterface {

	private static AutoBindPlayerHater sPlayerHater;
	
	public static boolean EXPANDING_NOTIFICATIONS = false;
	public static boolean TOUCHABLE_NOTIFICATIONS = false;
	public static boolean MODERN_AUDIO_FOCUS = false;
	public static boolean LOCK_SCREEN_CONTROLS = false;
	
	public static PlayerHater get(Context context) {
		return get(context, 0);
	}

	public static PlayerHater get(Context context, int id) {
		if (sPlayerHater == null) {
			sPlayerHater = AutoBindPlayerHater.getInstance();
			sPlayerHater.addContext(context, id);

			if (context instanceof Activity) {
				Activity activity = (Activity) context;
				int button = activity.getIntent().getIntExtra(
						BroadcastReceiver.REMOTE_CONTROL_BUTTON, -1);
				if (button != -1) {
					// XXX FIXME TODO
					// we have resumed the application because the media button
					// was pressed. Not sure what that means we should do here
					// for the sake of the most obvious thing, but it seems like
					// we should somehow ask the activity for the most
					// appropriate song to play.
				}
			}

			Resources resources = context.getResources();
			String applicationName = context.getPackageName();

			LOCK_SCREEN_CONTROLS = ConfigurationManager.getFlag(
					applicationName, resources, "playerhater_lockscreen");
			MODERN_AUDIO_FOCUS = ConfigurationManager.getFlag(applicationName,
					resources, "playerhater_audiofocus");
			TOUCHABLE_NOTIFICATIONS = ConfigurationManager.getFlag(
					applicationName, resources,
					"playerhater_touchable_notification");
			EXPANDING_NOTIFICATIONS = ConfigurationManager.getFlag(
					applicationName, resources,
					"playerhater_expanding_notification");

		} else {
			sPlayerHater.addContext(context, id);
		}

		return new PlayerHater(context, id);
	}
	
	public static void release(Context context) {
		release(context, 0);
	}

	public static void release(Context context, int id) {
		if (sPlayerHater != null) {
			sPlayerHater.requestRelease(context, id, true);
		}
	}
	
	private final Context mContext;
	private final int mId;
	
	private PlayerHater(Context context, int id) {
		mContext = context;
		mId = id;
	}

	@Override
	public boolean pause() {
		return sPlayerHater.pause();
	}

	@Override
	public boolean stop() {
		return sPlayerHater.stop();
	}

	@Override
	public boolean play() {
		return sPlayerHater.play();
	}

	@Override
	public boolean play(int startTime) {
		return sPlayerHater.play(startTime);
	}

	@Override
	public boolean play(Uri url) {
		return sPlayerHater.play(url);
	}

	@Override
	public boolean play(Uri url, int startTime) {
		return sPlayerHater.play(url, startTime);
	}

	@Override
	public boolean play(Song song) {
		return sPlayerHater.play(song);
	}

	@Override
	public boolean play(Song song, int startTime) {
		return sPlayerHater.play(song, startTime);
	}

	@Override
	public boolean seekTo(int startTime) {
		return sPlayerHater.seekTo(startTime);
	}

	@Override
	public void enqueue(Song song) {
		sPlayerHater.enqueue(song);
	}

	@Override
	public boolean skipTo(int position) {
		return sPlayerHater.skipTo(position);
	}

	@Override
	public void emptyQueue() {
		sPlayerHater.emptyQueue();
	}

	@Override
	public TransientPlayer playEffect(Uri url) {
		return sPlayerHater.playEffect(url);
	}

	@Override
	public TransientPlayer playEffect(Uri url, boolean isDuckable) {
		return sPlayerHater.playEffect(url, isDuckable);
	}

	@Override
	public void setAlbumArt(int resourceId) {
		sPlayerHater.setAlbumArt(resourceId);
	}

	@Override
	public void setAlbumArt(Uri url) {
		sPlayerHater.setAlbumArt(url);
	}

	@Override
	public void setTitle(String title) {
		sPlayerHater.setTitle(title);
	}

	@Override
	public void setArtist(String artist) {
		sPlayerHater.setArtist(artist);
	}

	@Override
	public void setActivity(Activity activity) {
		sPlayerHater.setActivity(activity);
	}

	@Override
	public int getCurrentPosition() {
		return sPlayerHater.getCurrentPosition();
	}

	@Override
	public int getDuration() {
		return sPlayerHater.getDuration();
	}

	@Override
	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
		sPlayerHater.setOnBufferingUpdateListener(listener);
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		sPlayerHater.setOnCompletionListener(listener);
	}

	@Override
	public void setOnInfoListener(OnInfoListener listener) {
		sPlayerHater.setOnInfoListener(listener);
	}

	@Override
	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		sPlayerHater.setOnSeekCompleteListener(listener);
	}

	@Override
	public void setOnErrorListener(OnErrorListener listener) {
		sPlayerHater.setOnErrorListener(listener);
	}

	@Override
	public void setOnPreparedListener(OnPreparedListener listener) {
		sPlayerHater.setOnPreparedListener(listener);
	}

	@Override
	public void setListener(PlayerHaterListener listener) {
		sPlayerHater.setListener(listener);
	}

	@Override
	public void setListener(PlayerHaterListener listener, boolean withEcho) {
		sPlayerHater.setListener(listener, withEcho);
	}

	@Override
	public Song nowPlaying() {
		return sPlayerHater.nowPlaying();
	}

	@Override
	public boolean isPlaying() {
		return sPlayerHater.isPlaying();
	}

	@Override
	public boolean isLoading() {
		return sPlayerHater.isLoading();
	}

	@Override
	public int getState() {
		return sPlayerHater.getState();
	}

	@Override
	public void registerPlugin(PlayerHaterPlugin plugin) {
		sPlayerHater.registerPlugin(plugin);
	}

	@Override
	public void unregisterPlugin(PlayerHaterPlugin plugin) {
		sPlayerHater.unregisterPlugin(plugin);
	}
	
	public void release() {
		release(mContext, mId);
	}

	public static Intent buildServiceIntent(Context context) {
		Intent intent = new Intent("org.prx.android.playerhater.SERVICE");
		intent.setPackage(context.getPackageName());
		if (context.getPackageManager().queryIntentServices(intent, 0).size() == 0) {
			intent = new Intent(context, PlaybackService.class);
	
			if (context.getPackageManager().queryIntentServices(intent, 0)
					.size() == 0) {
				IllegalArgumentException e = new IllegalArgumentException(
						"No usable service found.");
				String tag = context.getPackageName() + "/PlayerHater";
				String message = "Please define your Playback Service. For help, refer to: https://github.com/PRX/PlayerHater/wiki/Setting-Up-Your-Manifest";
				Log.e(tag, message, e);
				throw e;
			}
		}
	
		return intent;
	}

}
