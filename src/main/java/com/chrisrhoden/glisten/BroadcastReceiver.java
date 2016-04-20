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

import com.chrisrhoden.glisten.broadcast.Receiver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

public class BroadcastReceiver extends Receiver {
	private static Receiver sInstance;
	
	public static void register(Context context) {
		BroadcastReceiver receiver = new BroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		filter.addAction(Intent.ACTION_MEDIA_BUTTON);
		filter.setPriority(10000);
		context.registerReceiver(receiver, filter);
		sInstance = receiver;
	}
	
	public static void release(Context context) {
		if (sInstance != null) {
			context.unregisterReceiver(sInstance);
		}
	}
	
}
