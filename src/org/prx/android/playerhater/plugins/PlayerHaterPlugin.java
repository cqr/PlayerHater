package org.prx.android.playerhater.plugins;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.service.IPlayerHaterBinder;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;

/**
 * An interface that plugins for PlayerHater must implement.
 * <p>
 * Plugins must implement a default constructor that takes no arguments.
 * <p>
 * Most implementations should consider extending {@linkplain AbstractPlugin}
 * instead of implementing this interface.
 * 
 * @since 2.0.0
 * @version 2.1.0
 * 
 * @author Chris Rhoden
 */
public interface PlayerHaterPlugin {

	/**
	 * Called when the plugin can start doing work against PlayerHater.
	 * 
	 * @param context
	 *            A {@link Context} that the plugin can use to interact with the
	 *            application. This will either be the
	 *            {@link ApplicationContext} associated with the
	 *            {@link Application} running the plugin, or it will be the
	 *            context that is bound when this plugin was attached using
	 *            {@link BoundPlayerHater#setBoundPlugin(PlayerHaterPlugin)}
	 * @param playerHater
	 *            A instance of one of the subclasses of {@link PlayerHater}.
	 *            This is the plugin's handle to PlayerHater, and it should not
	 *            attempt to access it in any other way.
	 *            <p>
	 *            If this plugin is loaded in the standard way, this will be an
	 *            instance of {@link ServicePlayerHater}. Otherwise, it will be
	 *            a {@link BoundPlayerHater} or a {@link BinderPlayerHater},
	 *            depending on the current status of the Service.
	 */
	void onPlayerHaterLoaded(Context context, PlayerHater playerHater);

	/**
	 * Called when the service starts. In standard plugins, this will be called
	 * immediately after {@link #onPlayerHaterLoaded(Context, PlayerHater)}. In
	 * prebound or bound plugins, it will be called once the Service has been
	 * started.
	 * 
	 * @param binder
	 *            The {@link IPlayerHaterBinder} that this plugin can access.
	 */
	void onServiceBound(IPlayerHaterBinder binder);

	/**
	 * Called when the binder is no longer going to be available. For standard
	 * plugins, this also indicates that the plugin will be shutting down. For
	 * Bound or Prebound plugins, it is possible for the plugin to continue
	 * running even when the service is not bound.
	 */
	void onServiceStopping();

	void onSongChanged(Song song);

	void onSongFinished(Song song, int reason);

	void onDurationChanged(int duration);

	void onAudioLoading();

	void onAudioPaused();

	void onAudioResumed();

	void onAudioStarted();

	void onAudioStopped();

	void onTitleChanged(String title);

	void onArtistChanged(String artist);

	void onAlbumArtChanged(int resourceId);

	void onAlbumArtChangedToUri(Uri url);

	void onNextSongAvailable(Song nextTrack);

	void onNextSongUnavailable();

	void onIntentActivityChanged(PendingIntent pending);

	void onChangesComplete();

	void onTransportControlFlagsChanged(int transportControlFlags);
}
