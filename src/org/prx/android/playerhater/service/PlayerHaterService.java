/*******************************************************************************
 * Copyright 2013 Chris Rhoden, Rebecca Nesson, Public Radio Exchange
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.prx.android.playerhater.service;

import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.plugins.IRemotePlugin;
import org.prx.android.playerhater.plugins.PlayerHaterPlugin;
import org.prx.android.playerhater.plugins.PluginCollection;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.net.Uri;

public interface PlayerHaterService {

	public abstract boolean play(Song song, int position)
			throws IllegalArgumentException;

	public abstract boolean pause();

	public abstract boolean stop();

	public abstract boolean play() throws IllegalStateException;

	public abstract boolean play(int startTime) throws IllegalStateException;

	public abstract void setTitle(String title);

	public abstract void setArtist(String artist);

	public abstract int getCurrentPosition();

	public abstract int getDuration();

	public abstract Song getNowPlaying();

	public abstract boolean isPlaying();

	public abstract boolean isLoading();

	public abstract int getState();

	public abstract void setAlbumArt(int resourceId);

	public abstract void setAlbumArt(Uri url);

	public abstract int enqueue(Song song);
	
	public abstract int getQueueLength();
	
	public abstract int getQueuePosition();

	public abstract boolean skipTo(int position);

	public abstract void emptyQueue();

	public abstract void setIntentClass(Class<? extends Activity> klass);

	public abstract Context getBaseContext();

	public abstract boolean isPaused();

	public abstract void startForeground(int notificationNu,
			Notification notification);

	public abstract void stopForeground(boolean b);

	public abstract void duck();

	public abstract void unduck();

	public abstract boolean seekTo(int max);

	public abstract boolean play(Song song) throws IllegalArgumentException;

	public abstract void onRemoteControlButtonPressed(int keycodeMediaNext);

	void setSongInfo(Song song);

	public abstract void addPluginInstance(PlayerHaterPlugin plugin);

	public abstract boolean skip();

	public abstract boolean skipBack();

	public abstract void setTransportControlFlags(int transportControlFlags);

	void stopService(Song[] songs);

	public abstract boolean removeFromQueue(int position);
	
	public abstract void releaseMediaPlayer();

	public abstract void removeRemotePlugin();

	public abstract void setPluginBinder(IRemotePlugin binder);

	public abstract PluginCollection getPluginCollection();

}
