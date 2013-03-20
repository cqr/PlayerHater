package org.prx.android.playerhater.util;

import java.util.HashSet;
import java.util.Set;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.R;
import org.prx.android.playerhater.plugins.PlayerHaterPlugin;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.content.res.XmlResourceParser;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class ConfigurationManager implements Parcelable {

	private final Set<String> mPlugins = new HashSet<String>();
	private final Set<String> mPreboundPlugins = new HashSet<String>();

	public ConfigurationManager(Context context) {
		XmlResourceParser parser = context.getResources().getXml(
				R.xml.zzz_ph_config_defaults);
		load(parser);
		try {
			ServiceInfo info = context.getPackageManager().getServiceInfo(
					PlayerHater.buildServiceIntent(context).getComponent(),
					PackageManager.GET_META_DATA);
			if (info != null && info.metaData != null) {
				int id = info.metaData.getInt("org.prx.playerhater.Config", 0);
				if (id != 0) {
					parser = context.getResources().getXml(id);
					load(parser);
				}
			}
		} catch (NameNotFoundException e) {
		}
	}

	public Set<Class<? extends PlayerHaterPlugin>> getPrebindPlugins() {
		return getPlugins(mPreboundPlugins);
	}

	public Set<Class<? extends PlayerHaterPlugin>> getServicePlugins() {
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
				Log.e(PlayerHater.TAG, "Can't load plugin " + pluginName, e);
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
		dest.writeStringArray(getPreboundPluginsArray());
	}

	public static final Parcelable.Creator<ConfigurationManager> CREATOR = new Parcelable.Creator<ConfigurationManager>() {

		@Override
		public ConfigurationManager createFromParcel(Parcel in) {
			return new ConfigurationManager(in);
		}

		@Override
		public ConfigurationManager[] newArray(int size) {
			return new ConfigurationManager[size];
		}
	};

	private static final int PLUGIN = 1;
	private static final int INVALID_TAG = -1;

	private ConfigurationManager(Parcel in) {
		setPluginsArray(in.createStringArray());
		setPreboundPluginsArray(in.createStringArray());
	}

	private String[] getPluginsArray() {
		return mPlugins.toArray(new String[mPlugins.size() - 1]);
	}

	private String[] getPreboundPluginsArray() {
		return mPreboundPlugins.toArray(new String[mPlugins.size() - 1]);
	}

	private void setPluginsArray(String[] plugins) {
		setStringArray(plugins, mPlugins);
	}

	private void setPreboundPluginsArray(String[] plugins) {
		setStringArray(plugins, mPreboundPlugins);
	}

	private void setStringArray(String[] stuff, Set<String> in) {
		in.clear();
		for (String plugin : stuff) {
			in.add(plugin);
		}
	}

	private void load(XmlResourceParser parser) {
		try {
			parser.next();
			int eventType = parser.getEventType();
			boolean pluginEnabled = false;
			boolean prebindPlugin = false;
			boolean pluginDisabled = false;
			String pluginName = null;
			int currentTagType = INVALID_TAG;

			while (eventType != XmlResourceParser.END_DOCUMENT) {
				if (eventType == XmlResourceParser.START_DOCUMENT) {
					//
				} else if (eventType == XmlResourceParser.START_TAG) {
					if (parser.getName().equals("plugin")) {
						currentTagType = PLUGIN;
					}
					pluginEnabled = parser.getAttributeBooleanValue(null,
							"enabled", true);
					prebindPlugin = parser.getAttributeBooleanValue(null,
							"prebind", false);
					pluginDisabled = parser.getAttributeBooleanValue(null,
							"disabled", false);
					pluginName = parser.getAttributeValue(null, "name");
				} else if (eventType == XmlResourceParser.END_TAG) {
					switch (currentTagType) {
					case PLUGIN:
						if (pluginEnabled && pluginName != null) {
							if (prebindPlugin) {
								mPreboundPlugins.add(pluginName);
							} else {
								mPlugins.add(pluginName);
							}
						} else if (pluginDisabled && pluginName != null) {
							mPlugins.remove(pluginName);
							mPreboundPlugins.remove(pluginName);
						}
						break;
					}
				} else if (eventType == XmlResourceParser.TEXT) {
					// NOTHING
				}
				eventType = parser.next();
			}
		} catch (Exception e) {

		}
	}
}