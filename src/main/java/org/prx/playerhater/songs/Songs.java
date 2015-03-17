/*
 * -/*******************************************************************************
 * - * Copyright 2013 Chris Rhoden, Rebecca Nesson, Public Radio Exchange
 * - *
 * - * Licensed under the Apache License, Version 2.0 (the "License");
 * - * you may not use this file except in compliance with the License.
 * - * You may obtain a copy of the License at
 * - *
 * - *   http://www.apache.org/licenses/LICENSE-2.0
 * - *
 * - * Unless required by applicable law or agreed to in writing, software
 * - * distributed under the License is distributed on an "AS IS" BASIS,
 * - * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * - * See the License for the specific language governing permissions and
 * - * limitations under the License.
 * - *****************************************************************************
 */
package org.prx.playerhater.songs;

import android.net.Uri;
import android.os.Bundle;

import org.prx.playerhater.Song;

public class Songs {

	private static final String TITLE = "title",JSON="json";
	private static final String ARTIST = "artist";
	private static final String ALBUM = "album";
	private static final String ALBUMART = "album_art";
	private static final String URI = "uri";
	private static final String EXTRA = "extra";

	public static Bundle toBundle(Song song) {
		Bundle bundle = new Bundle();
		bundle.putString(TITLE, song.getTitle());
        bundle.putString(JSON, song.getSongJson());
		bundle.putString(ARTIST, song.getArtist());
		bundle.putString(ALBUM, song.getAlbumTitle());
		bundle.putParcelable(ALBUMART, song.getAlbumArt());
		bundle.putParcelable(URI, song.getUri());
		bundle.putBundle(EXTRA, song.getExtra());
		return bundle;
	}

	public static Song fromBundle(Bundle bundle) {
		return new UnbundledSong(bundle);
	}

	private static class UnbundledSong implements Song {

		private final String mTitle, mArtist, mAlbum,mJson;
		private final Uri mUri, mAlbumArt;
		private final Bundle mExtra;

		public UnbundledSong(Bundle bundle) {
			mTitle = bundle.getString(TITLE);
            mJson = bundle.getString(JSON);
			mArtist = bundle.getString(ARTIST);
			mAlbum = bundle.getString(ALBUM);
			mUri = bundle.getParcelable(URI);
			mAlbumArt = bundle.getParcelable(ALBUMART);

			mExtra = bundle.getBundle(EXTRA);
		}

		@Override
		public String getTitle() {
			return mTitle;
		}

        @Override
        public String getSongJson() {
            return mJson;
        }

		@Override
		public String getArtist() {
			return mArtist;
		}

		@Override
		public String getAlbumTitle() {
			return mAlbum;
		}

		@Override
		public Uri getAlbumArt() {
			return mAlbumArt;
		}

		@Override
		public Uri getUri() {
			return mUri;
		}

		@Override
		public Bundle getExtra() {
			return mExtra;
		}
	}
}
