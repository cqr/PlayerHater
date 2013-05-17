PlayerHater
===========

Android Audio Playback. You hear rumblings of a 2.0 release in the distance.
The earth moves slightly beneath your feet. It is coming.

Usage
-----

Everything is changing. We're getting leaner and faster and much, much easier to deal with.

How about this?

```java
import org.prx.android.playerhater.PlayerHater;

class MyApplicationActivity extends Activity {

	private PlayerHater mPlayerHater;

	@Override
	public void onResume(Bundle savedInstanceState) {
		super.onResume(savedInstanceState);
		mPlayerHater = PlayerHater.get(this);
	}
	
	@Override
	public void onPause() {
		PlayerHater.release(this);
	}
	
	// All your crazy stuff happens here.

}
```

Architecture
------------

Lots of changes to the way this works since last time. Many more files. Plugins. Docs coming soon.

The PlayerHater Object
----------------------

It's a singleton that automatically binds and tears down the service that you're attached to.
It also lets you start working immediately, and will send the stuff you've asked for along to the
service once it's ready.

All public methods conform to this interface:

```java
public interface AudioPlaybackInterface {
	// Controls
	boolean pause();
	boolean stop();

	// Playback
	boolean play();
	boolean play(int startTime);
	boolean play(Uri url);
	boolean play(Uri url, int startTime);
	boolean play(Song song);
	boolean play(Song song, int startTime);

	// Queuing
	void enqueue(Song song);
	boolean skipTo(int position);
	void emptyQueue();

	// For sound effects
	TransientPlayer playEffect(Uri url);
	TransientPlayer playEffect(Uri url, boolean isDuckable);

	// Notification data
	void setAlbumArt(int resourceId);
	void setAlbumArt(Uri url);
	void setTitle(String title);
	void setArtist(String artist);
	void setActivity(Activity activity);

	// Scubber-related data
	int getCurrentPosition();
	int getDuration();

	// Media Player listeners
	void setOnBufferingUpdateListener(OnBufferingUpdateListener listener);
	void setOnCompletionListener(OnCompletionListener listener);
	void setOnInfoListener(OnInfoListener listener);
	void setOnSeekCompleteListener(OnSeekCompleteListener listener);
	void setOnErrorListener(OnErrorListener listener);
	void setOnPreparedListener(OnPreparedListener listener);

	// PlayerHater listener
	void setListener(PlayerHaterListener listener);
	void setListener(PlayerHaterListener listener, boolean withEcho);

	// Other Getters
	Song nowPlaying();
	boolean isPlaying();
	boolean isLoading();
	int getState();
}
```

License
-------

    Copyright 2013 Chris Rhoden, Rebecca Nesson, Public Radio Exchange
     
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
