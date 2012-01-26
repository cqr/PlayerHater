PlayerHater
===========

Let's call it a library? For working with audio playback in Android. 2.2+ only, kthx.

Usage
-----

Basically, you should do the following:

```java
import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.PlayerHaterService;

class MyApplicationActivity extends Activity {

	private PlayerHater mPlayerHater;

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName name, IBinder service) {
			mPlayerHater = (PlayerHater)service;
		}

		public void onServiceDisconnected(ComponentName name) {
			mPlayerHater = null;
		}
	};

	public void onStart() {
		if (mPlayerHater == null) {
			Intent playerHaterIntent = new Intent(this, PlayerHaterService.class);
			Context context = getApplicationContext();

			context.bindService(playerHaterIntent, mConnection, Context.BIND_AUTO_CREATE);
		}
	}

}
```

