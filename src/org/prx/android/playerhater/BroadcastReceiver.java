package org.prx.android.playerhater;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;

public class BroadcastReceiver extends android.content.BroadcastReceiver {

	private static final String TAG = "BroadcastReceiver";
	private static PlaybackService mService;

	public BroadcastReceiver() { 
		super(); 
	}
	
	public BroadcastReceiver(PlaybackService service) {
		super();
		mService = service;
	}

	@Override
	public void onReceive(Context context, Intent intent) { 
		BroadcastReceiver.doReceive(context, intent); 
	}
	
	@SuppressLint("NewApi")
	public static void doReceive(Context context, Intent intent) {
		Log.d(TAG, "Received"); 
		if (mService.isPlaying() && intent.getAction() != null && 
				(intent.getAction().equals(Intent.ACTION_HEADSET_PLUG) ||
			     intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
				&& intent.getIntExtra("state", 0) == 0) {
			mService.pause();
		}
		if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) { 
            KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            Log.d(TAG, "Key event is " + event); 
            if (event.getAction() == KeyEvent.ACTION_DOWN) { return; }
            if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode() ||
                KeyEvent.KEYCODE_MEDIA_PAUSE == event.getKeyCode() || 
                KeyEvent.KEYCODE_HEADSETHOOK == event.getKeyCode() ||
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == event.getKeyCode()) {
            	if (mService.isPlaying()) { 
            		mService.pause(); 
            	} else if (mService.isPaused()) { 
            		try { 
            			mService.play();
            		} catch (IOException e) { 
            			e.printStackTrace(); 
            		} catch (java.lang.NullPointerException e) { 
            			e.printStackTrace(); 
            		} catch (Exception e) { 
            			e.printStackTrace(); 
            		}
            	} 
            }
		}
		if (intent.getAction() != null && intent.getAction().equals(NotificationHandler.PLAY_PAUSE_ACTION)) { 
			try { 
				if (mService.isPlaying()) { 
					mService.pause(); 
				} else { 
					mService.play();
				}
			} catch (IOException e) { 
				e.printStackTrace(); 
			}
		}
		if (intent.getAction() != null && intent.getAction().equals(NotificationHandler.STOP_ACTION)) { 
			try { 
				mService.stop(); 
			} catch (Exception e) { 
				e.printStackTrace(); 
			}
		}
	}

}
