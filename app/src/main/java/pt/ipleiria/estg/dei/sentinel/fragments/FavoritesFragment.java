package pt.ipleiria.estg.dei.sentinel.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import pt.ipleiria.estg.dei.sentinel.Constants;
import pt.ipleiria.estg.dei.sentinel.CustomAdapter;
import pt.ipleiria.estg.dei.sentinel.R;

public class FavoritesFragment extends Fragment implements View.OnClickListener {
    private ListView lView;
    private ArrayList<String> list;
    private SharedPreferences sharedPref;
    private Button btnAdd;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_favorites, container, false);


        //instantiate custom adapter

        CustomAdapter adapter = new CustomAdapter(list, this.getContext(),sharedPref);
        TextView emptyText = view.findViewById(R.id.tvEmpty);
        btnAdd = view.findViewById(R.id.btnAdd);


        btnAdd.setOnClickListener(this);


        //handle listview and assign adapter
        lView = view.findViewById(R.id.lvFavorites);
        lView.setAdapter(adapter);
        lView.setEmptyView(emptyText);




        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle("FAVORITES");

        sharedPref = getActivity().getSharedPreferences(Constants.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);


        /*CHECKS IF FAVORITES ARE ALREADY SAVED*/
        readData();

    }


    public  void persistData(ArrayList<String> favorites){
        Set<String> set = new HashSet<>(favorites);
        try{
            sharedPref.edit().putStringSet(Constants.PREFERENCES_FAVORITES_SET,set).commit();
        }catch(Exception ex){
            Log.i("ERROR_FAVORITES_SAVE","Error saving preference favorites-> " + ex.getMessage());
        }


    }

    private void readData(){
        Set<String> set;

        if(sharedPref.contains(Constants.PREFERENCES_FAVORITES_SET)){

            try{
                set = sharedPref.getStringSet(Constants.PREFERENCES_FAVORITES_SET,null);
            }catch(Exception ex){
                Log.i("ERROR_FAVORITES_SAVE","Error saving preference favorites-> " + ex.getMessage());
                set = new HashSet<>();
            }

            this.list = new ArrayList<>(set);

        }else{
            this.list = new ArrayList<>();
        }

    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnAdd:
                create();
        }
    }

    private void create() {
    }
}
