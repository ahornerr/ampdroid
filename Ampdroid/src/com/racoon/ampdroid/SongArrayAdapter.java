/**
 *
 */
package com.racoon.ampdroid;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.racoon.ampache.Song;

/**
 * @author Daniel Schruhl
 */
public class SongArrayAdapter extends ArrayAdapter<String> implements SectionIndexer {
    private final Context context;
    private ArrayList<Song> songs;
//    HashMap<String, Integer> songMap = new HashMap<String, Integer>();

    private HashMap<String, Integer> alphaIndexer;
    private String[] sections;
//    private String sectionString;
    private ArrayList<String> songTitles = new ArrayList<String>();


    public SongArrayAdapter(Context context, ArrayList<Song> songs) {
        super(context, R.layout.song_list_item);
        this.context = context;
        this.songs = songs;

        this.clear();

        alphaIndexer = new HashMap<String, Integer>();

        for (int i = 0; i < songs.size(); ++i) {
            String songTitle = songs.get(i).getTitle();
            songTitles.add(songTitle);
            String firstChar = songTitle.substring(0, 1).toUpperCase();
            alphaIndexer.put(firstChar, i);
        }

        this.addAll(songTitles);

        Set<String> sectionLetters = alphaIndexer.keySet();
        ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);
        Collections.sort(sectionList);
        sections = new String[sectionList.size()];
        sectionList.toArray(sections);
        Log.d("SongArrayAdapter", this.sections.length + " sections");
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.song_list_item, parent, false);
        TextView songTitle = (TextView) rowView.findViewById(R.id.songTitle);
        TextView songArtist = (TextView) rowView.findViewById(R.id.songArtist);

        songTitle.setText(getItem(position));
        songArtist.setText(songs.get(position).getArtist());
        return rowView;
    }

//    @Override
//    public long getItemId(int position) {
////        String item = getItem(position);
////        return songMap.get(item);
//        return position;
//    }


    @Override
    public int getPositionForSection(int section) {
        Log.v("getPositionForSection", ""+section);
        return alphaIndexer.get(sections[section]);
    }

    @Override
    public int getSectionForPosition(int position) {
//        String songTitle = songs.get(position).getTitle();
//        String letter = songTitle.substring(0, 1).toUpperCase();
//        for (int i = 0; i < sections.length; i++) {
//            if (letter.equals(sections[i])) {
//                Log.d("position", "" + i);
//                return i;
//            }
//        }
        Log.v("getSectionForPosition", "" + position);
        return 0;
    }

    @Override
    public Object[] getSections() {
        return sections;
    }

}
