/**
 * 
 */
package com.racoon.ampdroid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.racoon.ampache.Album;

/**
 * @author Daniel Schruhl
 * 
 */
public class AlbumArrayAdapter extends ArrayAdapter<String> implements SectionIndexer {
	private final Context context;
	private final ArrayList<String> textValues;
	private ArrayList<Album> albums;
	HashMap<String, Integer> albumMap = new HashMap<String, Integer>();
	private String[] sections;
	private String[] sectionsChar;

	public AlbumArrayAdapter(Context context, ArrayList<String> list, ArrayList<Album> albums) {
		super(context, R.layout.album_list_item, list);
		this.context = context;
		this.textValues = list;
		this.albums = albums;

		for (int i = 0; i < albums.size(); ++i) {
			albumMap.put(list.get(i), i);
		}

		Set<String> albumLetters = albumMap.keySet();
		ArrayList<String> albumList = new ArrayList<String>(albumLetters);
		Collections.sort(albumList);
		sections = new String[albumList.size()];
		sectionsChar = new String[albumList.size()];
		for (int i = 0; i < albumList.size(); i++) {
            String albumName = albumList.get(i);
            int endNum = (albumName.length() > 2) ? 2 : albumName.length();
            sections[i] = albumName;
            sectionsChar[i] = albumName.substring(0, endNum);
		}
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.album_list_item, parent, false);
		TextView albumTitle = (TextView) rowView.findViewById(R.id.albumTitle);
		TextView albumArtist = (TextView) rowView.findViewById(R.id.albumArtist);
		TextView albumSongs = (TextView) rowView.findViewById(R.id.albumSongNumber);

		albumTitle.setText(textValues.get(position));
		albumArtist.setText(albums.get(position).getArtist());
		
		String songsText = " Song";
		if (albums.get(position).getTracks() > 1) {
			songsText = " Songs";
		}
		albumSongs.setText(String.valueOf(albums.get(position).getTracks()) + songsText);
		return rowView;
	}

	@Override
	public long getItemId(int position) {
		String item = getItem(position);
		return albumMap.get(item);
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	public int getPositionForSection(int section) {
		if ((sections.length == section) && section == 1) {
			return albumMap.get(sections[section - 1]);
		}
		return albumMap.get(sections[section]);
	}

	public int getSectionForPosition(int position) {
		return 1;
	}

	public Object[] getSections() {
		return sectionsChar;
	}

}
