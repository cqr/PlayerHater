package org.prx.android.playerhater;

import android.net.Uri;
import android.os.Bundle;

public class BasicSong implements Song {
	private final Uri mUrl;
	private final String mTitle;
	private final String mArtist;
	private final Uri mAlbumArt;
	private final Bundle mBundle = new Bundle();
	
	public static final String URL = "url";
	public static final String ARTIST = "artist";
	public static final String TITLE = "title";
	public static final String ALBUM_ART = "album art";

	public BasicSong(Bundle bundle) {
		putBundle(bundle);
		mUrl = Uri.parse(mBundle.getString(URL));
		mArtist = mBundle.getString(ARTIST);
		mTitle = mBundle.getString(TITLE);
		mAlbumArt = Uri.parse(mBundle.getString(ALBUM_ART));
	}
	
	public BasicSong(Uri url, String title, String artist, String albumArt, int resourceId) {
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
}
