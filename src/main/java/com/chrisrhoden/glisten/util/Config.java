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
package com.chrisrhoden.glisten.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.chrisrhoden.glisten.PlayerHater;
import com.chrisrhoden.glisten.PlayerHaterPlugin;
import com.chrisrhoden.glisten.R;
import com.chrisrhoden.glisten.plugins.PluginCollection;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.content.res.Resources.NotFoundException;
import android.os.Parcel;
import android.os.Parcelable;

public class Config implements Parcelable {
	private static final String EXTRA_CONFIG = "config";
	private static Config sInstance;

	public static void attachToIntent(Intent intent) {
		if (sInstance != null) {
			intent.putExtra(EXTRA_CONFIG, sInstance);
		}
	}

	public static Config fromIntent(Intent intent) {
		return intent.getExtras().getParcelable(Config.EXTRA_CONFIG);
	}

	public static Config getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new Config(context);
		}
		return sInstance;
	}

	public PluginCollection run(Context context, PlayerHater playerHater) {
		PluginCollection collection = new PluginCollection();
		return run(context, playerHater, collection);
	}

	public PluginCollection run(Context context, PlayerHater playerHater,
			PluginCollection collection) {
		collection.writeLock();
		for (Class<? extends PlayerHaterPlugin> pluginKlass : getPlugins()) {
			try {
				PlayerHaterPlugin plugin = pluginKlass.newInstance();
				collection.add(plugin);
			} catch (Exception e) {
				Log.e("Could not instantiate plugin "
						+ pluginKlass.getCanonicalName(), e);
			}
		}
		collection.onPlayerHaterLoaded(context, playerHater);
		collection.unWriteLock();
		return collection;
	}

	private final Set<String> mPlugins = new HashSet<String>();

	private Config(Context context) {
		XmlResourceParser parser = context.getResources().getXml(
				R.xml.zzz_ph_config_defaults);
		load(parser, context);
		try {
			ServiceInfo info = context.getPackageManager().getServiceInfo(
					PlayerHater.buildServiceIntent(context).getComponent(),
					PackageManager.GET_META_DATA);
			if (info != null && info.metaData != null) {
				int id = info.metaData.getInt("org.prx.playerhater.Config", 0);
				if (id != 0) {
					parser = context.getResources().getXml(id);
					load(parser, context);
				}
			}
		} catch (NameNotFoundException e) {
			// If this happens, we can just use the default configuration.
		}
	}

	private Set<Class<? extends PlayerHaterPlugin>> getPlugins() {
		return getPlugins(mPlugins);
	}

	@SuppressWarnings("unchecked")
	private Set<Class<? extends PlayerHaterPlugin>> getPlugins(
			Set<String> strings) {
		Set<Class<? extends PlayerHaterPlugin>> plugins = new HashSet<Class<? extends PlayerHaterPlugin>>();
		for (String pluginName : strings) {
			try {
				plugins.add((Class<? extends PlayerHaterPlugin>) Class
						.forName(pluginName));
			} catch (Exception e) {
				Log.e("Can't load plugin " + pluginName, e);
			}
		}
		return plugins;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeStringArray(getPluginsArray());
	}

	public static final Parcelable.Creator<Config> CREATOR = new Parcelable.Creator<Config>() {

		@Override
		public Config createFromParcel(Parcel in) {
			return new Config(in);
		}

		@Override
		public Config[] newArray(int size) {
			return new Config[size];
		}
	};

	private static final int PLUGIN = 1;
	private static final int INVALID_TAG = -1;

	private Config(Parcel in) {
		setPluginsArray(in.createStringArray());
	}

	private String[] getPluginsArray() {
		return mPlugins.toArray(new String[mPlugins.size() - 1]);
	}

	private void setPluginsArray(String[] plugins) {
		setStringArray(plugins, mPlugins);
	}

	private void setStringArray(String[] stuff, Set<String> in) {
		in.clear();
		Collections.addAll(in, stuff);
	}

	private void load(XmlResourceParser parser, Context context) {
		Resources res = context.getResources();
		try {
			parser.next();
			int eventType = parser.getEventType();
			boolean pluginEnabled = false;
			boolean pluginDisabled = false;
			String pluginName = null;
			int currentTagType = INVALID_TAG;

			while (eventType != XmlResourceParser.END_DOCUMENT) {
				if (eventType == XmlResourceParser.START_TAG) {
					if (parser.getName().equals("plugin")) {
						currentTagType = PLUGIN;
					}
					pluginEnabled = loadBooleanOrResourceBoolean(res, parser,
							"enabled", true);
					pluginDisabled = loadBooleanOrResourceBoolean(res, parser,
							"disabled", false);

					pluginName = parser.getAttributeValue(null, "name");
				} else if (eventType == XmlResourceParser.END_TAG) {
					switch (currentTagType) {
					case PLUGIN:
						if (pluginEnabled && pluginName != null) {
							mPlugins.add(pluginName);
						} else if (pluginDisabled && pluginName != null) {
							mPlugins.remove(pluginName);
						}
						break;
					}
				}
				eventType = parser.next();
			}
		} catch (Exception e) {

		}
	}

	private boolean loadBooleanOrResourceBoolean(Resources res,
			XmlResourceParser parser, String attrName, boolean def) {
		int id;
		boolean result = def;
		try {
			id = parser.getAttributeResourceValue(null, attrName, 0);
			if (id != 0) {
				try {
					result = res.getBoolean(id);
				} catch (NotFoundException e) {
					result = parser.getAttributeBooleanValue(null, attrName,
							def);
				}
			} else {
				result = parser.getAttributeBooleanValue(null, attrName, def);
			}
		} catch (Exception e) {
			return result;
		}
		return result;
	}
}
