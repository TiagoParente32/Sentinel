package pt.ipleiria.estg.dei.sentinel.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import pt.ipleiria.estg.dei.sentinel.Constants;
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
    private TextView listTitle;
    private Button btnToggle;
    private boolean notifications_on;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        this.sharedPref = getActivity().getSharedPreferences(Constants.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);

        this.notificationsTitle = new ArrayList<>();
        this.notificationsBody = new HashMap<>();
        this.readList = new HashMap<>();
        this.notificationsList = new ArrayList<>();


        adapterList = new CustomAdapterExpandable(this.notificationsTitle, this.notificationsBody, this.readList, getActivity(), sharedPref, this);
        lView = view.findViewById(R.id.lvFavorites);

        TextView emptyText = view.findViewById(R.id.tvEmptyNotifications);


        //handle listview and assign adapter
        btnToggle = view.findViewById(R.id.btnToggleNotifications);
        lView = view.findViewById(R.id.lvNotifications);
        lView.setAdapter((ExpandableListAdapter) adapterList);
        lView.setEmptyView(emptyText);

        getNotificationsState();

        btnToggle.setOnClickListener(v -> {
            toggleNotifications();
        });

        readData();


        lView.setOnGroupClickListener((parent, v, groupPosition, id) -> {
            listTitle = v.findViewById(R.id.listTitle);
            listTitle.setBackgroundColor(Color.TRANSPARENT);
            int read = readList.get(listTitle.getText().toString());
            readList.put(listTitle.getText().toString(),0);
            if(read == 1){
                saveData();
            }
            return false;
        });


        /*SET SHARED PREFERENCES LISTENER*/
        SharedPreferences.OnSharedPreferenceChangeListener listener = (prefs, key) -> {
            if (key.equals(Constants.PREFERENCES_NOTIFICATIONS_UNREAD)) {
                readData();
            }
        };


        sharedPref.registerOnSharedPreferenceChangeListener(listener);


        return view;
    }

    private void getNotificationsState(){
        /*GETS CURRENT STATE*/
        this.notifications_on = sharedPref.getBoolean(Constants.PREFERENCES_NOTIFICATIONS_ON,false);

        if(notifications_on){
            btnToggle.setText("TURN ON");
        }else{
            btnToggle.setText("TURN OFF");
        }
    }

    private void toggleNotifications() {
       if(this.notifications_on){
           sharedPref.edit().putBoolean(Constants.PREFERENCES_NOTIFICATIONS_ON,false).commit();
       }else{
        sharedPref.edit().putBoolean(Constants.PREFERENCES_NOTIFICATIONS_ON,true).commit();
        }
       getNotificationsState();
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
            adapterList.setReadList(readList);
            adapterList.notifyDataSetChanged();

        }else{
            this.notificationsList = new ArrayList<>();
        }

    }


    private void saveData(){
        this.notificationsList.clear();
        int number;
        for (String s : notificationsTitle) {
            this.notificationsList.add(s+":"+notificationsBody.get(s)+":"+readList.get(s));
        }
            try{
                number = sharedPref.getInt(Constants.PREFERENCES_NOTIFICATIONS_UNREAD,0);
                number--;
                sharedPref.edit().putStringSet(Constants.PREFERENCES_NOTIFICATIONS_SET,new HashSet<>(notificationsList)).commit();
                sharedPref.edit().putInt(Constants.PREFERENCES_NOTIFICATIONS_UNREAD,number).commit();

            }catch(Exception ex){
                Log.i("ERROR_NOTIFICATIONS_PUT","Error putting preference notifications-> " + ex.getMessage());
            }

        this.adapterList.notifyDataSetChanged();
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
