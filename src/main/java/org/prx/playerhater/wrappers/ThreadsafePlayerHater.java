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
package org.prx.playerhater.wrappers;

import android.app.PendingIntent;
import android.os.Handler;
import android.os.HandlerThread;

import org.prx.playerhater.PlayerHater;
import org.prx.playerhater.Song;

public class ThreadsafePlayerHater extends PlayerHater {

    private static HandlerThread sThread;
    private static Handler sHandler;
    private final PlayerHater mPlayerHater;
    private final Handler mHandler;

    public ThreadsafePlayerHater(PlayerHater playerHater) {
        this(playerHater, null);
    }

    public ThreadsafePlayerHater(PlayerHater playerHater, Handler handler) {
        mPlayerHater = playerHater;
        if (handler == null) {
            handler = getDefaultHandler();
        }
        mHandler = handler;
    }

    private static HandlerThread getHandlerThread() {
        if (sThread == null) {
            sThread = new HandlerThread("ThreadSafePlayerHater");
            sThread.start();
        }
        return sThread;
    }

    private static Handler getDefaultHandler() {
        if (sHandler == null) {
            sHandler = new Handler(getHandlerThread().getLooper());
        }
        return sHandler;
    }

    @Override
    public int enqueue(final Song song) {
        return new PlayerHaterTask<Integer>(mHandler) {

            @Override
            protected Integer run() {
                return mPlayerHater.enqueue(song);
            }

        }.get();
    }

    @Override
    public boolean skipTo(final int position) {
        return new PlayerHaterTask<Boolean>(mHandler) {

            @Override
            protected Boolean run() {
                return mPlayerHater.skipTo(position);
            }

        }.get();
    }

    @Override
    public boolean pause() {
        return new PlayerHaterTask<Boolean>(mHandler) {

            @Override
            protected Boolean run() {
                return mPlayerHater.pause();
            }

        }.get();
    }

    @Override
    public boolean stop() {
        return new PlayerHaterTask<Boolean>(mHandler) {

            @Override
            protected Boolean run() {
                return mPlayerHater.stop();
            }

        }.get();
    }

    @Override
    public boolean play(final int startTime) {
        return new PlayerHaterTask<Boolean>(mHandler) {

            @Override
            protected Boolean run() {
                return mPlayerHater.play(startTime);
            }

        }.get();
    }

    @Override
    public boolean seekTo(final int startTime) {
        return new PlayerHaterTask<Boolean>(mHandler) {

            @Override
            protected Boolean run() {
                return mPlayerHater.seekTo(startTime);
            }

        }.get();
    }

    @Override
    public void setTransportControlFlags(final int transportControlFlags) {
        new PlayerHaterTask<Void>(mHandler) {

            @Override
            protected Void run() {
                mPlayerHater.setTransportControlFlags(transportControlFlags);
                return null;
            }

        }.get();

    }

    @Override
    public int getDuration() {
        return new PlayerHaterTask<Integer>(mHandler) {

            @Override
            protected Integer run() {
                return mPlayerHater.getDuration();
            }

        }.get();
    }

    @Override
    public int getCurrentPosition() {
        return new PlayerHaterTask<Integer>(mHandler) {

            @Override
            protected Integer run() {
                return mPlayerHater.getCurrentPosition();
            }

        }.get();
    }

    @Override
    public boolean isPlaying() {
        return new PlayerHaterTask<Boolean>(mHandler) {

            @Override
            protected Boolean run() {
                return mPlayerHater.isPlaying();
            }

        }.get();
    }

    @Override
    public boolean isLoading() {
        return new PlayerHaterTask<Boolean>(mHandler) {

            @Override
            protected Boolean run() {
                return mPlayerHater.isLoading();
            }

        }.get();
    }

    @Override
    public int getState() {
        return new PlayerHaterTask<Integer>(mHandler) {

            @Override
            protected Integer run() {
                return mPlayerHater.getState();
            }

        }.get();
    }

    @Override
    public int getQueueLength() {
        return new PlayerHaterTask<Integer>(mHandler) {

            @Override
            protected Integer run() {
                return mPlayerHater.getQueueLength();
            }

        }.get();
    }

    @Override
    public void emptyQueue() {
        new PlayerHaterTask<Void>(mHandler) {

            @Override
            protected Void run() {
                mPlayerHater.emptyQueue();
                return null;
            }

        }.get();
    }

    @Override
    public void skip() {
        new PlayerHaterTask<Boolean>(mHandler) {

            @Override
            protected Boolean run() {
                mPlayerHater.skip();
                return true;
            }

        }.get();
    }

    @Override
    public void skipBack() {
        new PlayerHaterTask<Boolean>(mHandler) {

            @Override
            protected Boolean run() {
                mPlayerHater.skipBack();
                return true;
            }

        }.get();
    }

    @Override
    public boolean play() {
        return new PlayerHaterTask<Boolean>(mHandler) {

            @Override
            protected Boolean run() {
                return mPlayerHater.play();
            }

        }.get();
    }

    @Override
    public int getQueuePosition() {
        return new PlayerHaterTask<Integer>(mHandler) {

            @Override
            protected Integer run() {
                return mPlayerHater.getQueuePosition();
            }

        }.get();
    }

    @Override
    public boolean removeFromQueue(final int position) {
        return new PlayerHaterTask<Boolean>(mHandler) {

            @Override
            protected Boolean run() {
                return mPlayerHater.removeFromQueue(position);
            }

        }.get();
    }

    @Override
    public boolean play(final Song song) {
        return new PlayerHaterTask<Boolean>(mHandler) {

            @Override
            protected Boolean run() {
                return mPlayerHater.play(song);
            }

        }.get();
    }

    @Override
    public boolean play(final Song song, final int startTime) {
        return new PlayerHaterTask<Boolean>(mHandler) {

            @Override
            protected Boolean run() {
                return mPlayerHater.play(song, startTime);
            }

        }.get();
    }

    @Override
    public Song nowPlaying() {
        return new PlayerHaterTask<Song>(mHandler) {
            @Override
            protected Song run() {
                return mPlayerHater.nowPlaying();
            }
        }.get();
    }

    @Override
    public void setPendingIntent(final PendingIntent intent) {
        new PlayerHaterTask<Void>(mHandler) {
            @Override
            protected Void run() {
                mPlayerHater.setPendingIntent(intent);
                return null;
            }
        }.get();
    }

    private static abstract class PlayerHaterTask<V> {

        private final Handler mHandler;
        private final Runnable mRunnable;
        private State mState = State.IDLE;
        private V mResult;

        public PlayerHaterTask(Handler handler) {
            mHandler = handler;
            mRunnable = new WorkRunnable();
        }

        public synchronized V get() {
            while (true) {
                if (mState == State.DONE) {
                    return mResult;
                } else if (mState == State.IDLE) {
                    start();
                }
                try {
                    wait(500);
                } catch (InterruptedException e) {
                    // Noop
                }
            }
        }

        protected void start() {
            mHandler.post(mRunnable);
            mState = State.WAITING;
        }

        protected abstract V run();

        private synchronized void set(V result) {
            mResult = result;
            mState = State.DONE;
            notifyAll();
        }

        private static enum State {
            IDLE, WAITING, RUNNING, DONE
        }

        private final class WorkRunnable implements Runnable {

            @Override
            public synchronized void run() {
                mState = State.RUNNING;
                try {
                    set(PlayerHaterTask.this.run());
                } catch (Throwable exception) {
                    set(null);
                    throw new RuntimeException(exception);
                }
            }

        }
    }

}
