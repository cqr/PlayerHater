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
package com.chrisrhoden.glisten;

import com.chrisrhoden.glisten.wrappers.BoundPlayerHater;
import com.chrisrhoden.glisten.util.Config;
import com.chrisrhoden.glisten.util.IPlayerHater;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.RemoteControlClient;
import android.util.Log;

public abstract class PlayerHater implements IPlayerHater {

	@SuppressLint("InlinedApi")
	public static final int DEFAULT_TRANSPORT_CONTROL_FLAGS = RemoteControlClient.FLAG_KEY_MEDIA_NEXT
			| RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
			| RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
			| RemoteControlClient.FLAG_KEY_MEDIA_PLAY
			| RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
			| RemoteControlClient.FLAG_KEY_MEDIA_STOP;

	/**
	 * Releases the {@linkplain ServiceConnection} which this instance is using
	 * if this instance is backed by a {@linkplain ServiceConnection}
	 * 
	 * @return {@code true} if this instance was backed by a
	 *         {@linkplain ServiceConnection} and should not be used anymore,
	 *         {@code false} otherwise.
	 */
	public boolean release() {
		return false;
	}

	public boolean setLocalPlugin(PlayerHaterPlugin plugin) {
		return false;
	}

	/**
	 * Constructs an {@linkplain Intent} which will start the appropriate
	 * {@linkplain PlayerHaterService} as configured in the project's
	 * AndroidManifest.xml file.
	 * 
	 * @param context
	 * @return An {@link Intent} which will start the correct service.
	 * @throws IllegalArgumentException
	 *             if there is no appropriate service configured in
	 *             AndroidManifest.xml
	 */
	public static Intent buildServiceIntent(Context context) {
		Intent intent = new Intent("org.prx.playerhater.SERVICE");
		intent.setPackage(context.getPackageName());
		Config.attachToIntent(intent);

		if (context.getPackageManager().queryIntentServices(intent, 0).size() == 0) {
			intent = new Intent(context, PlaybackService.class);
			Config.attachToIntent(intent);
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

	/**
	 * Gets an instance of a {@linkplain BoundPlayerHater} which can be used to
	 * interact with the playback service.
	 * 
	 * Calling this method will also invoke
	 * {@linkplain PlayerHater#configure(Context)} if it has not yet been
	 * called.
	 * 
	 * @since 2.1.0
	 * 
	 * @param context
	 *            The context on which to bind the service.
	 * @return an instance of PlayerHater which one can use to interact with the
	 *         Playback Service.
	 */
	public static PlayerHater bind(Context context) {
		return new BoundPlayerHater(context);
	}
}
