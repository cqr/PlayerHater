package org.prx.android.playerhater.lifecycle;

import org.prx.android.playerhater.R;
import org.prx.android.playerhater.service.PlayerHaterService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ExpandableNotificationPlugin extends TouchableNotificationPlugin implements
		LifecycleListener.RemoteControl {
	
	private RemoteViews mExpandedView;
	private Notification mNotification;
	

	public ExpandableNotificationPlugin(PlayerHaterService service) {
		super(service);
	}
	
	@Override
	protected Notification getNotification() {
		if (mNotification == null) {
			mNotification = super.getNotification();
			mNotification.bigContentView = getExpandedView();
		}
		
		return mNotification;
	}
	
	private RemoteViews getExpandedView() {
		if (mExpandedView == null) {
			mExpandedView = new RemoteViews(mService.getBaseContext()
					.getPackageName(),
					R.layout.__player_hater_notification_expanded);
			setListeners(mExpandedView);
		}

		mExpandedView.setTextViewText(R.id.title, mNotificationTitle);
		mExpandedView.setTextViewText(R.id.text, mNotificationText);
		mExpandedView.setImageViewResource(R.id.image, R.drawable.__player_hater_icon);

		return mExpandedView;
	}
	
	@Override
	protected void setTextViewText(int viewId, String text) {
		super.setTextViewText(viewId, text);
		if (mExpandedView != null) {
			mExpandedView.setTextViewText(viewId, text);
		}
	}
	
	@Override
	protected void setViewEnabled(int viewId, boolean enabled) {
		if (mExpandedView != null) {
			mExpandedView.setBoolean(viewId, "setEnabled", enabled);
		}
		super.setViewEnabled(viewId, enabled);
	}
	
	@Override
	protected void setImageViewResource(int viewId, int resourceId) {
		super.setImageViewResource(viewId, resourceId);
		if (mExpandedView != null) {
			mExpandedView.setImageViewResource(viewId, resourceId);
		}
	}
	
	@Override
	protected void setImageViewUri(int viewId, Uri contentUri) {
		super.setImageViewUri(viewId, contentUri);
		if (mExpandedView != null) {
			mExpandedView.setImageViewUri(viewId, contentUri);
		}
	}
	
	@Override
	protected Notification buildNotification() {
		return getNotificationBuilder().build();
	}
}
