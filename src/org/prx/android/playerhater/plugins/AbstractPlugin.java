package org.prx.android.playerhater.plugins;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.plugins.PlayerHaterPlugin;
import org.prx.android.playerhater.service.IPlayerHaterBinder;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

/**
 * A simple helper for writing {@linkplain PlayerHaterPlugin}s
 * <p>
 * Subclasses MUST implement a default no-argument constructor.
 * 
 * @see {@link PlayerHaterPlugin}
 * @since 2.0.0
 * @version 2.1.0
 * @author Chris Rhoden
 */
public abstract class AbstractPlugin implements PlayerHaterPlugin {
	private PlayerHater mPlayerHater;
	private IPlayerHaterBinder mBinder;
	private Context mContext;

	public AbstractPlugin() {

	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Overridden implementations should be sure to call
	 * {@code super.onPlayerHaterLoaded} so that future calls to
	 * {@link #getContext()} and {@link #getPlayerHater()} can succeed.
	 */
	@Override
	public void onPlayerHaterLoaded(Context context, PlayerHater playerHater) {
		Log.v(this.getClass().getSimpleName(), "PlayerHater Loaded");
		mContext = context;
		mPlayerHater = playerHater;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Overridden implementations should be sure to call
	 * {@code super.onServiceBound} so that future calls to {@link #getBinder()}
	 * can succeed.
	 */
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

	/**
	 * {@inheritDoc}
	 * <p>
	 * The default implementation forwards thos method call to
	 * {@link #onAudioStarted()}
	 */
	@Override
	public void onAudioResumed() {
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

	/**
	 * {@inheritDoc}
	 * <p>
	 * The default implementation will call {@link #onAlbumArtChangedToUri(Uri)}
	 * , {@link #onTitleChanged(String)}, and {@link #onArtistChanged(String)}
	 */
	@Override
	public void onSongChanged(Song song) {
		if (song != null) {
			onTitleChanged(song.getTitle());
			onArtistChanged(song.getArtist());
			onAlbumArtChangedToUri(song.getAlbumArt());
		}
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

	@Override
	public void onTransportControlFlagsChanged(int transportControlFlags) {
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

	/**
	 * A method providing simple access to the plugin's context without having
	 * to override {@link #onPlayerHaterLoaded(Context, PlayerHater)}
	 * 
	 * @return The {@link Context} in which the plugin is running.
	 */
	protected final Context getContext() {
		return mContext;
	}

	/**
	 * A method providing simple access to a service binder without having to
	 * override {@link onServiceBound(IPlayerHaterBinder)}
	 * 
	 * @return The {@link IPlayerHaterBinder} that the plugin is bound to if the
	 *         service is running. Otherwise, {@code null}.
	 */
	protected final IPlayerHaterBinder getBinder() {
		return mBinder;
	}
}
