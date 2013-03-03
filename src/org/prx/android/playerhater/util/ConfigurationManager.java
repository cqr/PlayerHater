package org.prx.android.playerhater.util;

import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;

public class ConfigurationManager {

	public static boolean getFlag(String applicationName, Resources resources, String name) {
		int id = resources.getIdentifier(name, "bool", applicationName);
		
		if (id == 0) {
			id = resources.getIdentifier("__"+name, "bool", applicationName);
		}
		
		boolean returnValue = false;
		
		try {
			returnValue = resources.getBoolean(id);
		} catch (NotFoundException e) {
			returnValue = false;
		}
		
		return returnValue;
	}
}