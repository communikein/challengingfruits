package com.example.eliam.challengingfruits;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by eliam on 03/06/2016.
 */
public class ChallengeAdapter extends ArrayAdapter<Challenge> {

    private final Context context;
    private ArrayList<Challenge> values;

    public ChallengeAdapter(Context context, ArrayList<Challenge> values) {
        super(context, R.layout.row_challenge, values);
        this.context = context;
        this.values = values;
    }

    public ArrayList<Challenge> getData() {
        return values;
    }

    /**
     * Holder for the list items.
     */
    private class ViewHolder {
        TextView date_txt;
        TextView year_txt;
        TextView challengedBy_txt;
        TextView what_txt;
        TextView points_txt;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        Challenge entry = getItem(position);

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.row_challenge, parent, false);

            holder.date_txt = (TextView) convertView.findViewById(R.id.job_date_txt);
            holder.year_txt = (TextView) convertView.findViewById(R.id.job_year_txt);
            holder.challengedBy_txt = (TextView) convertView.findViewById(R.id.challenge_by_txt);
            holder.what_txt = (TextView) convertView.findViewById(R.id.what_txt);
            holder.points_txt = (TextView) convertView.findViewById(R.id.points_txt);

            convertView.setTag(holder);
        }
        else holder = (ViewHolder) convertView.getTag();

        holder.date_txt.setText(Utils.dayMonth.format(entry.getDeadline()));
        holder.year_txt.setText(Utils.year.format(entry.getDeadline()));
        holder.challengedBy_txt.setText(entry.getChallengedBy());
        holder.what_txt.setText(entry.getWhat());
        holder.points_txt.setText(String.valueOf(entry.getPoints()));

        return convertView;
    }

}

