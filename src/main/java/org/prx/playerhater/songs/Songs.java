package org.prx.playerhater.songs;

import org.prx.playerhater.Song;

import android.net.Uri;
import android.os.Bundle;

public class Songs {

	private static final String TITLE = "title";
	private static final String ARTIST = "artist";
	private static final String ALBUM = "album";
	private static final String ALBUMART = "album_art";
	private static final String URI = "uri";
	private static final String EXTRA = "extra";

	public static Bundle toBundle(Song song) {
		Bundle bundle = new Bundle();
		bundle.putString(TITLE, song.getTitle());
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

		private final String mTitle, mArtist, mAlbum;
		private final Uri mUri, mAlbumArt;
		private final Bundle mExtra;

		public UnbundledSong(Bundle bundle) {
			mTitle = bundle.getString(TITLE);
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
