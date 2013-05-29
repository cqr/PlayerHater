package org.prx.playerhater.songs;

import java.util.HashMap;
import java.util.Map;

import org.prx.playerhater.Song;

import android.util.SparseArray;

public class SongHost {
	
	private static final SparseArray<Song> sSongs = new SparseArray<Song>();
	private static final Map<Song, Integer> sTags = new HashMap<Song, Integer>();
	
	
	public static int getTag(Song song) {
		if (sTags.containsKey(song)) {
			return sTags.get(song);
		} else {
			int tag = song.hashCode();
			sTags.put(song, tag);
			sSongs.put(tag, song);
			return tag;
		}
	}
	
	public static Song getSong(int tag) {
		Song song = sSongs.get(tag);
		if (song != null) {
			return song;
		} else {
			song = new RemoteSong(tag);
			sTags.put(song, tag);
			sSongs.put(tag, song);
			return song;
		}
	}

}
