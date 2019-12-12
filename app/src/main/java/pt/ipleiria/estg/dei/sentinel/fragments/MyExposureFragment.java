package pt.ipleiria.estg.dei.sentinel.fragments;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import pt.ipleiria.estg.dei.sentinel.Constants;
import pt.ipleiria.estg.dei.sentinel.CustomAdapter;
import pt.ipleiria.estg.dei.sentinel.R;

public class MyExposureFragment extends Fragment implements CustomAdapter.EventListener{
    private ListView lView;
    private ArrayList<String> exposuresList;
    private SharedPreferences sharedPref;
    private CustomAdapter adapterList;
    private ArrayList<Float> temperaturesList;
    private ArrayList<Float> humiditysList;

    private ProgressBar pbTemp;
    private ProgressBar pbHum;
    private TextView humidade;
    private TextView temperatura;
    private TextView qoa;

    private float totalTemp=0;
    private float totalHum=0;
    private float avgTemp=0;
    private float avgHum=0;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_myexposure,container,false);

        humidade = view.findViewById(R.id.txtHum);
        temperatura = view.findViewById(R.id.txtTemp);

        pbTemp = view.findViewById(R.id.pbTemp);
        pbHum = view.findViewById(R.id.pbHum);

        qoa = view.findViewById(R.id.textQoa);

        ArrayList<String> list = new ArrayList<>();
        temperaturesList = new ArrayList<>();
        humiditysList = new ArrayList<>();


        for (String s : exposuresList) {
                //list.add(data[0]);
                list.add(s);
        }


        updateData();


        //instantiate custom adapter
        adapterList =new CustomAdapter(list,getActivity(),sharedPref,2,this);

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

    public void updateData(){
        String[] data;
        totalTemp = 0;
        totalHum = 0;
        temperaturesList.clear();
        humiditysList.clear();

        for (String s : this.exposuresList) {
            data = s.split(":");

            temperaturesList.add(Float.parseFloat(data[1]));
            humiditysList.add(Float.parseFloat(data[2]));

            totalTemp += Float.parseFloat(data[1]);
            totalHum += Float.parseFloat(data[2]);
        }


        /*CALCULATE AVG TEMPERATURE AND HUMIDITY AND SHOW IT*/
        avgTemp = (float) totalTemp/temperaturesList.size();
        avgHum = (float) totalHum/humiditysList.size();

        String tempText = String.format("%.2f",avgTemp) + "ºC";
        String humText = String.format("%.2f",avgHum) + "%";


        temperatura.setText(tempText);
        humidade.setText(humText);

        if (avgTemp >= 19 && avgTemp <= 35) {
            //verde
            pbTemp.getProgressDrawable().setColorFilter(Color.parseColor("#90EE90"), PorterDuff.Mode.SRC_IN);
        } else {
            //vermelho
            pbTemp.getProgressDrawable().setColorFilter(Color.parseColor("#8E1600"), PorterDuff.Mode.SRC_IN);

        }
        if (avgHum >= 50 && avgHum <= 75) {
            //verde
            pbHum.getProgressDrawable().setColorFilter(Color.parseColor("#90EE90"), PorterDuff.Mode.SRC_IN);

        } else {
            //vermelho
            pbHum.getProgressDrawable().setColorFilter(Color.parseColor("#8E1600"), PorterDuff.Mode.SRC_IN);

        }


        if ((avgTemp >= 19 && avgTemp <= 35) && (avgHum >= 50 && avgHum <= 75)) {
            //GREEN TEXT
            qoa.setText("GOOD");
            qoa.setTextColor(Color.GREEN);
        } else if (((avgTemp < 19 || avgTemp >= 35) && (avgHum >= 50 && avgHum <= 75)) || (
            (avgTemp >= 19 && avgTemp <= 35) && (avgHum < 50 || avgHum >= 75))) {
            //YELLOW TEXT
            qoa.setText("AVERAGE");
            qoa.setTextColor(Color.YELLOW);
        } else {
            //RED TEXT
            qoa.setText("BAD");
            qoa.setTextColor(Color.RED);

        }

    }


    @Override
    public void onEvent() {
        readData();
        updateData();
    }
}



