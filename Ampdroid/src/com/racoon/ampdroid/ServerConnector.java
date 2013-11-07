/**
 * 
 */
package com.racoon.ampdroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import ampache.ServerConnection;
import android.annotation.SuppressLint;
import android.net.ParseException;
import android.util.Log;

/**
 * @author Daniel Schruhl
 * 
 */
public class ServerConnector implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String user;
	private String password;
	private String server;
	private String authKey;
	private ServerConnection ampacheConnection;

	public ServerConnector() {
		super();
	}

	public ServerConnector(String user, String password, String server) {
		super();
		this.user = user;
		this.password = password;
		this.server = server;
	}

	@SuppressLint("SimpleDateFormat")
	public boolean isConnected(boolean network) {
		if (!network) {
			return false;
		}
		String time = Long.toString(System.currentTimeMillis() / 1000);
		String key = password;
		String passphrase = generateShaHash(time + key);

		String urlString = new String(server + "/server/xml.server.php?action=handshake&auth=" + passphrase
				+ "&timestamp=" + time + "&version=350001&user=" + user);
		Log.d("passwort:", key);
		Log.d("passphrase:", passphrase);
		URL url;
		try {
			url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.connect();

			XmlPullParserFactory pullParserFactory;
			try {
				pullParserFactory = XmlPullParserFactory.newInstance();
				XmlPullParser parser = pullParserFactory.newPullParser();

				InputStream in_s = con.getInputStream();
				parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
				parser.setInput(in_s, null);

				parseXML(parser);
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (this.ampacheConnection != null) {
				Log.d("ampache connection:", this.ampacheConnection.getAuth());
				Date currentDate = new Date();
				Date date = new Date();
				try {
					String rawDate;
					SimpleDateFormat sdfToDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss+01:00");
					rawDate = ampacheConnection.getSession_expire().replace("T", " ");
					date = sdfToDate.parse(rawDate);
				} catch (ParseException ex2) {
					ex2.printStackTrace();
				}
				Log.d("expire:", this.ampacheConnection.getSession_expire());
				Log.d("token:", this.authKey);
				Log.d("dates:", date.toString() + ", " + currentDate.toString() + " - " + String.valueOf(date.compareTo(currentDate)));
				if (date.compareTo(currentDate) <= 0) {
					extendSession();
				}
				return true;
			}
			return false;
		} catch (Exception e) {
			Log.d("error", "keine Verbindung möglich");
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the server
	 */
	public String getServer() {
		return server;
	}

	/**
	 * @param server the server to set
	 */
	public void setServer(String server) {
		this.server = server;
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	public String generateShaHash(String s) {
		String hash = "";
		hash = bin2hex(getHash(s)).toLowerCase();
		return hash;
	}

	public void extendSession() {
		String urlString = new String(server + "/server/xml.server.php?action=ping&auth=" + authKey);
		URL url;
		try {
			url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.connect();
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line;

			System.out.println("Response: " + con.getResponseCode());
			System.out.println("Content-type: " + con.getContentType());
			System.out.println("Content-length: " + con.getContentLength());

			while ((line = br.readLine()) != null)
				Log.d("ping:", line);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param password
	 * @return
	 */
	public byte[] getHash(String password) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		digest.reset();
		return digest.digest(password.getBytes());
	}

	/**
	 * 
	 * @param data
	 * @return
	 */
	static String bin2hex(byte[] data) {
		return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data));
	}

	/**
	 * @return the authKey
	 */
	public String getAuthKey() {
		return authKey;
	}

	/**
	 * @param authKey the authKey to set
	 */
	public void setAuthKey(String authKey) {
		this.authKey = authKey;
	}

	private void parseXML(XmlPullParser parser) throws XmlPullParserException, IOException {
		int eventType = parser.getEventType();
		ServerConnection serverConnection = null;

		while (eventType != XmlPullParser.END_DOCUMENT) {
			String name = null;
			switch (eventType) {
			case XmlPullParser.START_TAG:
				name = parser.getName();
				if (name.equals("root")) {
					serverConnection = new ServerConnection();
				} else if (serverConnection != null) {
					if (name.equals("auth")) {
						serverConnection.setAuth(parser.nextText());
						this.authKey = serverConnection.getAuth();
					} else if (name.equals("api")) {
						serverConnection.setApi(parser.nextText());
					} else if (name.equals("session_expire")) {
						serverConnection.setSession_expire(parser.nextText());
					} else if (name.equals("update")) {
						serverConnection.setUpdate(parser.nextText());
					} else if (name.equals("add")) {
						serverConnection.setAdd(parser.nextText());
					} else if (name.equals("clean")) {
						serverConnection.setClean(parser.nextText());
					} else if (name.equals("songs")) {
						serverConnection.setSongs(Integer.parseInt(parser.nextText()));
					} else if (name.equals("albums")) {
						serverConnection.setAlbums(Integer.parseInt(parser.nextText()));
					} else if (name.equals("artists")) {
						serverConnection.setArtists(Integer.parseInt(parser.nextText()));
					} else if (name.equals("playlists")) {
						serverConnection.setPlaylists(Integer.parseInt(parser.nextText()));
					} else if (name.equals("videos")) {
						serverConnection.setVideos(Integer.parseInt(parser.nextText()));
					} else if (name.equals("catalogs")) {
						serverConnection.setCatalogs(Integer.parseInt(parser.nextText()));
					}
				}
				break;
			case XmlPullParser.END_TAG:
				name = parser.getName();
				Log.d("bug", "ampache connection nicht gesetzt");
				if (name.equalsIgnoreCase("root") && serverConnection != null) {
					setAmpacheConnection(serverConnection);
					Log.d("bug", "ampache connection gesetzt");
				}
			}
			eventType = parser.next();
		}
	}

	/**
	 * @return the ampacheConnection
	 */
	public ServerConnection getAmpacheConnection() {
		return ampacheConnection;
	}

	/**
	 * @param ampacheConnection the ampacheConnection to set
	 */
	public void setAmpacheConnection(ServerConnection ampacheConnection) {
		this.ampacheConnection = ampacheConnection;
	}
}