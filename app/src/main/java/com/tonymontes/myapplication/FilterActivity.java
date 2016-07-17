package com.tonymontes.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashMap;

/**
 * @author tony
 * @since 7/17/16
 */

public class FilterActivity extends Activity {

    String[] filterChoices;
    TypedArray filterBitmaps;

    ListView listview;
    CustomListAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        filterChoices = getResources().getStringArray(R.array.filter_choices);
        filterBitmaps = getResources().obtainTypedArray(R.array.filter_images);

        // 1. get passed intent
        Intent intent = getIntent();

        // 2. get statusFilterIndex from intent
        int statusFilterIndex = intent.getIntExtra("statusFilterIndex", 0);

        myAdapter = new CustomListAdapter(this, filterChoices, filterBitmaps, statusFilterIndex);

        listview = (ListView) findViewById(R.id.listView);
        listview.setAdapter(myAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                myAdapter.toggleChecked(getApplicationContext(), position);
                //Toast.makeText(getApplicationContext(), filterChoices.get(position), Toast.LENGTH_SHORT).show();

                // comment the next line if you don't want to go back after a selection
                finishWithResult();
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {

        super.onPostCreate(savedInstanceState);

        LinearLayout root = (LinearLayout) findViewById(R.id.listView).getParent().getParent().getParent().getParent();
        AppBarLayout bar = (AppBarLayout) LayoutInflater.from(this).inflate(R.layout.toolbar_filter, root, false);
        root.addView(bar, 0);

        Toolbar tbar = (Toolbar) bar.getChildAt(0);
        tbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finishWithResult();
            }
        });
    }

    private void finishWithResult()
    {
        int selectedRow = myAdapter.getCurPos();

        Intent intent = new Intent();
        intent.putExtra("filterChoice", selectedRow);
        setResult(RESULT_OK, intent);

        finish();
    }

    private static class CustomListAdapter extends BaseAdapter {

        private HashMap<Integer, Boolean> myChecked = new HashMap<Integer, Boolean>();
        private int prevPos = 0;

        private LayoutInflater lInflater;

        private String[] choices;
        private TypedArray bitmaps;

        ViewHolder listViewHolder;
        int iconColor;

        public CustomListAdapter(Context context,
                                 String[] choices,
                                 TypedArray bitmaps,
                                 int pos) {

            iconColor = ContextCompat.getColor(context, R.color.colorPrimary);
            lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            this.choices = choices;
            this.bitmaps = bitmaps;
            this.prevPos = pos;

            myChecked.put(pos, true); // check the first item
            for (int i = 0; i < choices.length; i++) {

                if (i != pos)
                    myChecked.put(i, false);
            }
        }

        public int getCurPos() {
            return prevPos;
        }

        @Override
        public int getCount() {
            return choices.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = convertView;

            if (row == null) {

                listViewHolder = new ViewHolder();
                row = lInflater.inflate(R.layout.row, parent, false);

                listViewHolder.imageView = (ImageView) row.findViewById(R.id.view1);
                listViewHolder.textView = (TextView) row.findViewById(R.id.text1);
                listViewHolder.checkBox = (CheckBox) row.findViewById(R.id.checkBox1);

                row.setTag(listViewHolder);
            }
            else
                listViewHolder = (ViewHolder)row.getTag();

            // tint the image
            listViewHolder.imageView.setColorFilter(iconColor);

            // set the image
            int filterBitmap = bitmaps.getResourceId(position, 0);
            listViewHolder.imageView.setImageResource(position > 0? filterBitmap : android.R.color.transparent);

            // set the text
            listViewHolder.textView.setText(choices[position]);

            // check the box
            Boolean checked = myChecked.get(position);
            listViewHolder.checkBox.setChecked(checked);

            return row;
        }

        public void toggleChecked(Context context, int position) {

            if (!myChecked.get(position)) {

                myChecked.put(position, true);
                myChecked.put(prevPos, false);
                prevPos = position;

                // uncomment the next line if you don't want to go back after a selection
                //notifyDataSetChanged();

                // store value in shared preferences
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("statusFilterKey", position);
                editor.apply();
            }
        }

        static class ViewHolder {

            ImageView imageView;
            TextView textView;
            CheckBox checkBox;
        }
    }
}
