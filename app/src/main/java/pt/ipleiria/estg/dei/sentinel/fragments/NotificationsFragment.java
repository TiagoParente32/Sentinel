package pt.ipleiria.estg.dei.sentinel.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import pt.ipleiria.estg.dei.sentinel.Constants;
import pt.ipleiria.estg.dei.sentinel.CustomAdapter;
import pt.ipleiria.estg.dei.sentinel.R;

public class NotificationsFragment extends Fragment implements CustomAdapter.EventListener {

    private ListView lView;
    private ArrayList<String> notificationsList;
    private SharedPreferences sharedPref;
    private CustomAdapter adapterList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_notifications,container,false);

        this.sharedPref = getActivity().getSharedPreferences(Constants.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);

        //instantiate custom adapter


        readData();


        adapterList =new CustomAdapter(this.notificationsList,getActivity(),sharedPref,3,this);
        lView = view.findViewById(R.id.lvFavorites);

        TextView emptyText = view.findViewById(R.id.tvEmptyNotifications);


        //handle listview and assign adapter
        lView = view.findViewById(R.id.lvNotifications);
        lView.setAdapter(adapterList);
        lView.setEmptyView(emptyText);


        /*SET SHARED PREFERENCES LISTENER*/
        SharedPreferences.OnSharedPreferenceChangeListener listener = (prefs, key) -> {
            if(key.equals(Constants.PREFERENCES_NOTIFICATIONS_UNREAD)){
                this.notificationsList.clear();
                this.notificationsList.addAll(new ArrayList<>(prefs.getStringSet(Constants.PREFERENCES_NOTIFICATIONS_SET,new HashSet<>())));
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

        if(sharedPref.contains(Constants.PREFERENCES_NOTIFICATIONS_SET)){

            try{
                set = sharedPref.getStringSet(Constants.PREFERENCES_NOTIFICATIONS_SET,new HashSet<>());
            }catch(Exception ex){
                Log.i("ERROR_NOTIFICATIONS_GET","Error getting preference notifications-> " + ex.getMessage());
                set = new HashSet<>();
            }

            this.notificationsList = new ArrayList<>(set);

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
