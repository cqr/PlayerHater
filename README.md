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

Architecture
------------

Ok, so the way it works is this

```
PlayerHater
├── MediaPlayerWrapper.java        // Implements the state machine for Android's MediaPlayer class.
├── OnPlayerLoadingListener.java   // An interface you should implement
├── PlayerHater.java               // An interface that PlayerHaterBinder implements
├── PlayerHaterBinder.java         // Your main interaction point with PlayerHater
├── PlayerHaterService.java        // The workhorse
├── PlayerListenerManager.java     // Handles maintaining MediaPlayer event listeners between sessions
└── UpdateProgressRunnable.java    // Handles updating the scrubber/duration indicator.
```

If you want to build your own extensions to the library, here's how you do it for now:

 * Build a new interface which extends PlayerHater and adds any methods you want.
 * Extend PlayerHaterBinder and implement the interface you just created.
 * Extend PlayerHaterService to add the actual functionality/persistence (Binders are disposable)
 * Cast the binder you got back with the interface you created.
 * ???
 * Profit!
