package com.example.eliam.challengingfruits.fragment;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.eliam.challengingfruits.Challenge;
import com.example.eliam.challengingfruits.activity.ChallengeActivity;
import com.example.eliam.challengingfruits.ChallengeAdapter;
import com.example.eliam.challengingfruits.DBAdapter;
import com.example.eliam.challengingfruits.activity.ChallengeDetailsActivity;
import com.example.eliam.challengingfruits.activity.MainActivity;
import com.example.eliam.challengingfruits.R;
import com.example.eliam.challengingfruits.Utils;

import java.util.ArrayList;

public class ChallengesFragment extends Fragment {

    public static final String TAG = "CHALLENGESFRAGMENT";

    ListView list;
    TextView title_lbl, empty;
    SearchView searchView;
    ImageView action_menu;
    ProgressBar progressBar;

    MainActivity mainActivity;
    ArrayList<Challenge> temp;
    ChallengeAdapter adapter;

    Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            ex.printStackTrace();
            int i=0;
        }
    };

    public ChallengesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(handler);
        mainActivity = (MainActivity) getActivity();
        return inflater.inflate(R.layout.fragment_challenges, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        action_menu = (ImageView) view.findViewById(R.id.action_menu);
        title_lbl = (TextView) view.findViewById(R.id.placeholder_txt);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        initList(view);
        initFab(view);
        initSearchView(view);

        loadChallenges(getActivity());
    }

    public void initFab(View view) {
        View v = view.findViewById(R.id.fab);

        if (v != null)
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ChallengeActivity.class);
                    getActivity().startActivity(intent);
                }
            });
    }

    public void initList(View view) {
        empty = (TextView) view.findViewById(R.id.empty_txt);
        empty.setText(R.string.no_challenge_found);

        temp = Utils.challenges;
        adapter = new ChallengeAdapter(getActivity(), temp);
        list = (ListView) view.findViewById(android.R.id.list);
        list.setAdapter(adapter);
        list.setEmptyView(empty);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showWorkDetails(position);
            }
        });
    }

    public void initSearchView(View view){
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager)
                getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) view.findViewById(R.id.searchView);

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title_lbl.setVisibility(View.GONE);
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                title_lbl.setVisibility(View.VISIBLE);
                return false;
            }
        });
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(true); // Iconify the widget by default
    }

    private void showWorkDetails(int position){
        Challenge challenge = adapter.getData().get(position);

        if (challenge != null) {
            Intent intent = new Intent(getActivity(), ChallengeDetailsActivity.class);
            intent.putExtra(Challenge.CHALLENGE_JSON, challenge.toString());
            getActivity().startActivity(intent);
        }
    }

    private void loadChallenges(final Context context) {
        progressBar.setVisibility(View.VISIBLE);

        (new AsyncTask<Void, Void, ArrayList<Challenge>>() {
            @Override
            protected ArrayList<Challenge> doInBackground(Void... params) {
                ArrayList<Challenge> challenges_tmp;

                DBAdapter dbAdapter = DBAdapter.getInstance(context);
                dbAdapter.open();
                challenges_tmp = dbAdapter.getAllChallenges();
                dbAdapter.close();

                return challenges_tmp;
            }

            @Override
            protected void onPostExecute(ArrayList<Challenge> challenges) {
                super.onPostExecute(challenges);

                progressBar.setVisibility(View.GONE);
                Snackbar.make(list, R.string.challenges_loaded, Snackbar.LENGTH_SHORT).show();

                adapter.clear();
                adapter.addAll(challenges);
                adapter.notifyDataSetChanged();

                if (challenges.size() == 0) {
                    list.setVisibility(View.GONE);
                    empty.setVisibility(View.VISIBLE);
                }
                else {
                    list.setVisibility(View.VISIBLE);
                    empty.setVisibility(View.GONE);
                }

                progressBar.setVisibility(View.GONE);
            }
        }).execute();
    }
}
