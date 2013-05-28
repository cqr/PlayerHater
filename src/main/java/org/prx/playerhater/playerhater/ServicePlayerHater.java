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
package org.prx.playerhater.playerhater;

import org.prx.playerhater.PlayerHater;
import org.prx.playerhater.PlayerHaterListener;
import org.prx.playerhater.Song;
import org.prx.playerhater.plugins.PlayerHaterListenerPlugin;
import org.prx.playerhater.service.PlayerHaterService;

import android.app.Activity;
import android.net.Uri;

public class ServicePlayerHater extends PlayerHater {
	private final PlayerHaterService mService;
	
	public ServicePlayerHater(PlayerHaterService service) {
		mService = service;
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
	public boolean play() {
		return mService.play();
	}

	@Override
	public boolean play(int startTime) {
		return mService.play(startTime);
	}

	@Override
	public boolean play(Song song) {
		return mService.play(song);
	}

	@Override
	public boolean play(Song song, int startTime) {
		return mService.play(song, startTime);
	}

	@Override
	public boolean seekTo(int startTime) {
		return mService.seekTo(startTime);
	}

	@Override
	public int enqueue(Song song) {
		return mService.enqueue(song);
	}

	@Override
	public boolean skipTo(int position) {
		return mService.skipTo(position);
	}

	@Override
	public void skip() {
		mService.skip();
	}

	@Override
	public void skipBack() {
		mService.skipBack();
	}

	@Override
	public void emptyQueue() {
		mService.emptyQueue();
	}

	@Override
	public void setAlbumArt(int resourceId) {
		mService.setAlbumArt(resourceId);
	}

	@Override
	public void setAlbumArt(Uri url) {
		mService.setAlbumArt(url);
	}

	@Override
	public void setTitle(String title) {
		mService.setTitle(title);
	}

	@Override
	public void setArtist(String artist) {
		mService.setArtist(artist);
	}

	@Override
	public void setActivity(Activity activity) {
		mService.setIntentClass(activity.getClass());
	}

	@Override
	public int getCurrentPosition() {
		return mService.getCurrentPosition();
	}

	@Override
	public int getDuration() {
		return mService.getDuration();
	}

	@Override
	@Deprecated
	public void setListener(PlayerHaterListener listener) {
		mService.addPluginInstance(new PlayerHaterListenerPlugin(listener));
	}

	@Override
	public Song nowPlaying() {
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
	public int getState() {
		return mService.getState();
	}

	@Override
	public void setTransportControlFlags(int transportControlFlags) {
		mService.setTransportControlFlags(transportControlFlags);
	}

	@Override
	public int getQueueLength() {
		return mService.getQueueLength();
	}

	@Override
	public int getQueuePosition() {
		return mService.getQueuePosition();
	}

	@Override
	public boolean removeFromQueue(int position) {
		return mService.removeFromQueue(position);
	}

}
