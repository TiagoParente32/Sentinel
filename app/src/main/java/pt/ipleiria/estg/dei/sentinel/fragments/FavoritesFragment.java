package pt.ipleiria.estg.dei.sentinel.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import pt.ipleiria.estg.dei.sentinel.Constants;
import pt.ipleiria.estg.dei.sentinel.CustomAdapter;
import pt.ipleiria.estg.dei.sentinel.R;

public class FavoritesFragment extends Fragment implements View.OnClickListener {
    private ListView lView;
    private ArrayList<String> favoritesList;
    private SharedPreferences sharedPref;
    private ImageButton btnAdd;
    private Spinner spinnerRooms;
    private ArrayAdapter adapterSpinner;
    private ArrayList<String> roomsList ;
    private CustomAdapter adapterList;




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_favorites, container, false);


        //instantiate custom adapter

        adapterList = new CustomAdapter(favoritesList, this.getContext(),sharedPref);
        TextView emptyText = view.findViewById(R.id.tvEmpty);
        btnAdd = view.findViewById(R.id.btnAdd);
        spinnerRooms = view.findViewById(R.id.spinnerRoomsFavorites);


        btnAdd.setOnClickListener(this);


        //RECEBER DA MAIN ACTIVITY A ROOMSLIST
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            roomsList = bundle.getStringArrayList(Constants.DATA_INTENT_SPINNER_DATA);
        }else{
            roomsList = new ArrayList<>();
        }


        //handle listview and assign adapter
        lView = view.findViewById(R.id.lvFavorites);
        lView.setAdapter(adapterList);
        lView.setEmptyView(emptyText);


        /*SPINNER ROOM*/
        adapterSpinner = new ArrayAdapter<>(this.getActivity(), R.layout.spinner_list, roomsList);
        adapterSpinner.setDropDownViewResource(R.layout.spinner_list_dropdown);
        spinnerRooms.setAdapter(adapterSpinner);

        readData();

        adapterSpinner.notifyDataSetChanged();


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

        this.adapterList.list = favorites;
        this.adapterList.notifyDataSetChanged();

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

            this.favoritesList = new ArrayList<>(set);

        }else{
            this.favoritesList = new ArrayList<>();
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
        String room =(String)  spinnerRooms.getSelectedItem();

        if(favoritesList.contains(room)){
            return;
        }

       this.favoritesList.add(room);

       persistData(this.favoritesList);



    }
}
