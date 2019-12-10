package pt.ipleiria.estg.dei.sentinel.fragments;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import pt.ipleiria.estg.dei.sentinel.Constants;
import pt.ipleiria.estg.dei.sentinel.CustomAdapter;
import pt.ipleiria.estg.dei.sentinel.R;

public class MyExposureFragment extends Fragment{
    private ListView lView;
    private ArrayList<String> exposuresList;
    private SharedPreferences sharedPref;
    private CustomAdapter adapterList;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_myexposure,container,false);


        ArrayList<String> list = new ArrayList<>();

        for (String s : exposuresList) {
            list.add(s.split("-")[0]);
        }


        //instantiate custom adapter
        adapterList =new CustomAdapter(list,getActivity(),sharedPref,2);

        TextView emptyText = view.findViewById(R.id.tvEmptyExposure);

        //handle listview and assign adapter
        lView = view.findViewById(R.id.lvMyexposure);
        lView.setAdapter(adapterList);
        lView.setEmptyView(emptyText);


        return view;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle("MY EXPOSURE");

        sharedPref = getActivity().getSharedPreferences(Constants.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);

        readData();

    }

    @Override
    public void onStart() {

        super.onStart();


    }

    private void readData(){
        Set<String> set;

        if(sharedPref.contains(Constants.PREFERENCES_EXPOSURE_SET)){

            try{
                set = sharedPref.getStringSet(Constants.PREFERENCES_EXPOSURE_SET,null);
                Log.i("EXPOSURE_GET","Getting saved exposure");

            }catch(Exception ex){
                Log.i("ERROR_EXPOSURE_GET","Error getting saved exposure-> " + ex.getMessage());
                set = new HashSet<>();
            }

            this.exposuresList = new ArrayList<>(set);

        }else{
            this.exposuresList = new ArrayList<>();
        }


    }


}



