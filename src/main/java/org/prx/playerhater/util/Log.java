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
package org.prx.playerhater.util;

import org.prx.playerhater.BuildConfig;
import org.prx.playerhater.service.AbsPlaybackService;

public class Log {

	public static void v(String msg) {
		if (BuildConfig.DEBUG) {
			android.util.Log.v(AbsPlaybackService.TAG, msg);
		}
	}

	public static void d(String msg) {
		if (BuildConfig.DEBUG) {
			android.util.Log.d(AbsPlaybackService.TAG, msg);
		}
	}

	public static void e(String string, Exception e) {
		android.util.Log.e(AbsPlaybackService.TAG, string, e);
	}

}
