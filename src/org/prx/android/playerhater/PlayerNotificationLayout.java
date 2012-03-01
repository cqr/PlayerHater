package org.prx.android.playerhater;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class PlayerNotificationLayout extends RelativeLayout {
	
	public ImageView icon;
	
	public PlayerNotificationLayout(Context context) {
		super(context);
		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		addView(icon = makeIcon(context));
	}
	
	private static ImageView makeIcon(Context context) {
		ImageView icon = new ImageView(context);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
		params.alignWithParent = true;
		params.rightMargin = 10;
		icon.setBackgroundColor(Color.BLUE);
		icon.setLayoutParams(params);
		return icon;	
	}

}
