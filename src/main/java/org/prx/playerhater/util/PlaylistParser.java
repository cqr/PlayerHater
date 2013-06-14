package org.prx.playerhater.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;

import android.net.Uri;

public final class PlaylistParser {

	private static String[] PLS_MIME_TYPES = new String[] { "audio/scpls",
			"audio/x-scpls" };
	private static String[] M3U_MIME_TYPES = new String[] { "audio/x-mpegurl" };

	public static Uri[] parsePlaylist(Uri uri) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = httpclient.execute(new HttpHead(uri
					.toString()));
			Header contentType = response.getEntity().getContentType();
			if (contentType != null) {
				String mimeType = contentType.getValue().split(";")[0].trim();

				for (String plsMimeType : PLS_MIME_TYPES) {
					if (plsMimeType.equalsIgnoreCase(mimeType)) {
						return parsePls(uri);
					}
				}

				for (String m3uMimeType : M3U_MIME_TYPES) {
					if (m3uMimeType.equalsIgnoreCase(mimeType)) {
						return parseM3u(uri);
					}
				}
			}
		} catch (Exception e) {}
		return new Uri[] { uri };
	}

	private static Uri[] parsePls(Uri uri) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = httpclient.execute(new HttpGet(uri.toString()));
			HttpEntity entity = response.getEntity();
			InputStream inputStream = entity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			String header = reader.readLine();
			if (header.trim().equalsIgnoreCase("[playlist]")) {
				String line;
				ArrayList<Uri> uriList = new ArrayList<Uri>();
				do {
					line = reader.readLine();
					if (line != null) {
						if (line.startsWith("File")) {
							String fileName = line.substring(
									line.indexOf("=") + 1).trim();
							uriList.add(Uri.parse(fileName));
						}
					}
				} while (line != null);
				if (uriList.size() > 0) {
					Uri[] res = new Uri[uriList.size()];
					return uriList.toArray(res);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Uri[] { uri };
	}

	private static Uri[] parseM3u(Uri uri) {

		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = httpclient.execute(new HttpGet(uri
					.toString()));
			HttpEntity entity = response.getEntity();
			InputStream inputStream = entity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			String line;
			ArrayList<Uri> uriList = new ArrayList<Uri>();
			do {
				line = reader.readLine();
				if (line != null) {
					if (!line.startsWith("#")) {
						uriList.add(Uri.parse(line.trim()));
					}
				}
			} while (line != null);
			if (uriList.size() > 0) {
				Uri[] res = new Uri[uriList.size()];
				return uriList.toArray(res);
			}
		} catch (Exception e) {

		}
		return new Uri[] { uri };
	}

}
