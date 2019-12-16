package pt.ipleiria.estg.dei.sentinel.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import pt.ipleiria.estg.dei.sentinel.Constants;
import pt.ipleiria.estg.dei.sentinel.CustomAdapter;
import pt.ipleiria.estg.dei.sentinel.CustomAdapterExpandable;
import pt.ipleiria.estg.dei.sentinel.R;

public class NotificationsFragment extends Fragment implements CustomAdapterExpandable.EventListener {

    private ExpandableListView lView;
    private ArrayList<String> notificationsList;
    private ArrayList<String> notificationsTitle;
    private HashMap<String,String> notificationsBody;
    private HashMap<String,Integer> readList;
    private SharedPreferences sharedPref;
    private CustomAdapterExpandable adapterList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_notifications,container,false);

        this.sharedPref = getActivity().getSharedPreferences(Constants.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);

        this.notificationsTitle = new ArrayList<>();
        this.notificationsBody = new HashMap<>();
        this.readList = new HashMap<>();
        this.notificationsList = new ArrayList<>();


        readData();


        adapterList =new CustomAdapterExpandable(this.notificationsTitle,this.notificationsBody,this.readList,getActivity(),sharedPref, this);
        lView = view.findViewById(R.id.lvFavorites);

        TextView emptyText = view.findViewById(R.id.tvEmptyNotifications);


        //handle listview and assign adapter
        lView = view.findViewById(R.id.lvNotifications);
        lView.setAdapter((ExpandableListAdapter)adapterList);
        lView.setEmptyView(emptyText);

        lView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {




                return false;
            }
        });


        /*SET SHARED PREFERENCES LISTENER*/
        SharedPreferences.OnSharedPreferenceChangeListener listener = (prefs, key) -> {
            if(key.equals(Constants.PREFERENCES_NOTIFICATIONS_UNREAD)){
                readData();
                adapterList.notifyDataSetChanged();
            }
        };

        sharedPref.registerOnSharedPreferenceChangeListener(listener);


        return view;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle("NOTIFICATIONS");


    }

    private void readData(){
        Set<String> set;
        this.notificationsList.clear();
        this.notificationsBody.clear();
        this.notificationsTitle.clear();
        this.readList.clear();

        if(sharedPref.contains(Constants.PREFERENCES_NOTIFICATIONS_SET)){

            try{
                set = sharedPref.getStringSet(Constants.PREFERENCES_NOTIFICATIONS_SET,new HashSet<>());
            }catch(Exception ex){
                Log.i("ERROR_NOTIFICATIONS_GET","Error getting preference notifications-> " + ex.getMessage());
                set = new HashSet<>();
            }

            this.notificationsList = new ArrayList<>(set);

            String[] data;
            /*SPLITS STRINGS*/
            for (String s: this.notificationsList) {
                data = s.split(":");
                this.notificationsTitle.add(data[0]);
                this.notificationsBody.put(data[0],data[1]);
                this.readList.put(data[0],Integer.parseInt(data[2]));
            }

        }else{
            this.notificationsList = new ArrayList<>();
        }


    }

    @Override
    public void onStart() {

        super.onStart();



    }

    @Override
    public void onEvent() {
        readData();
        //updateData();
    }
}
