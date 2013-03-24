package org.prx.android.playerhater.plugins;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.plugins.PlayerHaterPlugin;
import org.prx.android.playerhater.service.IPlayerHaterBinder;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

public abstract class AbstractPlugin implements PlayerHaterPlugin {

	protected static final String _TAG = "AbstractPlugin";
	private PlayerHater mPlayerHater;
	private IPlayerHaterBinder mBinder;
	private Context mContext;

	public AbstractPlugin() {

	}

	@Override
	public void onPlayerHaterLoaded(Context context, PlayerHater playerHater) {
		mContext = context;
		mPlayerHater = playerHater;
	}

	@Override
	public void onServiceBound(IPlayerHaterBinder playerHaterBinder) {
		mBinder = playerHaterBinder;
	}

	@Override
	public void onServiceStopping() {
		mContext = null;
		mPlayerHater = null;
	}

	@Override
	public void onAudioStarted() {
	}

	@Override
	public void onAudioResumed() {
		Log.w(_TAG, "Forwarding a call to onAudioResumed => onAudioStarted "
				+ getClass().getSimpleName());
		onAudioStarted();
	}

	@Override
	public void onAudioStopped() {
	}

	@Override
	public void onTitleChanged(String title) {
	}

	@Override
	public void onArtistChanged(String artist) {
	}

	@Override
	public void onAlbumArtChanged(int resourceId) {
	}

	@Override
	public void onAlbumArtChangedToUri(Uri url) {
	}

	@Override
	public void onSongChanged(Song song) {
	}

	@Override
	public void onDurationChanged(int duration) {
	}

	@Override
	public void onAudioLoading() {
	}

	@Override
	public void onAudioPaused() {
	}

	@Override
	public void onNextSongAvailable(Song nextSong) {
	}

	@Override
	public void onNextSongUnavailable() {
	}

	@Override
	public void onIntentActivityChanged(PendingIntent pending) {
	}

	@Override
	public void onSongFinished(Song song, int reason) {
	}

	@Override
	public void onChangesComplete() {
	}

	/**
	 * Grants the plugin easy access to the instance of {@link PlayerHater} that
	 * it is permitted to use.
	 * 
	 * @return An instance of PlayerHater
	 */
	protected final PlayerHater getPlayerHater() {
		return mPlayerHater;
	}

	protected final Context getContext() {
		return mContext;
	}

	protected final IPlayerHaterBinder getBinder() {
		return mBinder;
	}
	
}
