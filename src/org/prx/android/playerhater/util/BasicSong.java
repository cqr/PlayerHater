package org.prx.android.playerhater.util;

import org.prx.android.playerhater.Song;

import android.net.Uri;
import android.os.Bundle;

public class BasicSong implements Song {
	private final Uri mUrl;
	private final String mTitle;
	private final String mArtist;
	private final Uri mAlbumArt;
	private final Bundle mBundle = new Bundle();
	public int tag;

	public static final String URL = "url";
	public static final String ARTIST = "artist";
	public static final String TITLE = "title";
	public static final String ALBUM_ART = "album art";

	public BasicSong(Bundle bundle) {
		putBundle(bundle);
		mUrl = Uri.parse(bundle.getString(URL));
		mArtist = bundle.getString(ARTIST);
		mTitle = bundle.getString(TITLE);
		String albumArtString = bundle.getString(ALBUM_ART);
		if (albumArtString != null) {
			mAlbumArt = Uri.parse(bundle.getString(ALBUM_ART));
		} else {
			mAlbumArt = null;
		}
	}

	public BasicSong(Uri url, String title, String artist, String albumArt,
			int _) {
		mUrl = url;
		mTitle = title;
		mArtist = artist;
		mAlbumArt = Uri.parse(albumArt);
	}

	public BasicSong(Uri url, String title, String artist, Uri albumArt) {
		mUrl = url;
		mTitle = title;
		mArtist = artist;
		mAlbumArt = albumArt;
	}

	public BasicSong(Uri url, String title, String artist, Uri albumArt, int tag) {
		this(url, title, artist, albumArt);
		this.tag = tag;
	}

	public BasicSong(Song song) {
		this(song.getUri(), song.getTitle(), song.getArtist(), song.getAlbumArt());
	}

	public Bundle putBundle(Bundle bundle) {
		mBundle.clear();
		mBundle.putAll(bundle);

		return mBundle;
	}

	public Bundle getBundle() {
		return mBundle;
	}

	@Override
	public Uri getUri() {
		return mUrl;
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
	public Uri getAlbumArt() {
		return mAlbumArt;
	}
	
	@Override
	public String toString() {
		return "BasicSong{ '" + getTitle() + "' by '" + getArtist() + "' (" + getUri() + ") }"; 
	}
}
