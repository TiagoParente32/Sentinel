package pt.ipleiria.estg.dei.sentinel.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import pt.ipleiria.estg.dei.sentinel.CompareDates;
import pt.ipleiria.estg.dei.sentinel.Value;
import pt.ipleiria.estg.dei.sentinel.R;

public class StatisticsFragment extends Fragment {

    private Spinner spinnerFilter;
    private EditText dateFrom;
    private EditText dateTo;
    private TextView txtFrom;
    private TextView txtTo;
    private GraphView graph;
    private CheckBox checkBoxTemp;
    private CheckBox checkBoxHum;
    LineGraphSeries<DataPoint> temperatura;
    LineGraphSeries<DataPoint> humidade;
    private Date fromDate;
    private Date toDate;
    private String selected;
    private DatabaseReference mDatabase;
    public static final String TAG = "Statistics";
    private DataSnapshot ds;
    private int i = 0;
    private List<Value> values;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("STATISTICS");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        getActivity().setTitle("DASHBOARD");

        spinnerFilter = view.findViewById(R.id.spinnerFilter);
        dateFrom = view.findViewById(R.id.dateFrom);
        dateTo = view.findViewById(R.id.dateTo);
        txtFrom = view.findViewById(R.id.textViewFrom);
        txtTo = view.findViewById(R.id.textViewTo);
        graph = view.findViewById(R.id.graph);
        checkBoxHum = view.findViewById(R.id.checkBoxHum);
        checkBoxTemp = view.findViewById(R.id.checkBoxTemp);
        values = new ArrayList<>();

        checkBoxHum.setChecked(true);
        checkBoxTemp.setChecked(true);

        //set default date
        Calendar cal = Calendar.getInstance();
//        toDate = new Date(System.currentTimeMillis());
//        cal.setTime(toDate);
//        cal.add(Calendar.HOUR, -24);
//        fromDate = cal.getTime();
        toDate = new Date(System.currentTimeMillis());
        cal.setTime(toDate);
        cal.add(Calendar.WEEK_OF_MONTH, -1);
        fromDate = cal.getTime();

        //set invisible
        toggleFromTo(false);


        //SETUP SPINNER LIST
        String[] arraySpinner = new String[] {
                "Day", "Week", "Month", "Custom"
        };

        ArrayAdapter adapter = new ArrayAdapter<>(this.getActivity(), R.layout.spinner_list, arraySpinner);
        adapter.setDropDownViewResource(R.layout.spinner_list_dropdown);
        spinnerFilter.setAdapter(adapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Calendar cal = Calendar.getInstance();
                switch (spinnerFilter.getSelectedItem().toString()){
                    case "Custom":
                        toggleFromTo(true);
                        break;
                    case "Day":
                        toggleFromTo(false);
                        //reset vars
    //                        temperatura = new LineGraphSeries<>();
    //                        humidade = new LineGraphSeries<>();
                        toDate = new Date(System.currentTimeMillis());
                        cal.setTime(toDate);
                        cal.add(Calendar.HOUR, -24);
                        fromDate = cal.getTime();
                        System.out.println("from" + fromDate.toString() + "to" + toDate.toString());
                        updateGraph(ds);
                        break;
                    case "Week":
//                        temperatura = new LineGraphSeries<>();
//                        humidade = new LineGraphSeries<>();
                        toggleFromTo(false);
                        toDate = new Date(System.currentTimeMillis());
                        cal.setTime(toDate);
                        cal.add(Calendar.WEEK_OF_MONTH, -1);
                        fromDate = cal.getTime();
                        System.out.println("from"+ fromDate.toString() + "to" + toDate.toString());
                        updateGraph(ds);
                        break;
                    case "Month":
//                        temperatura = new LineGraphSeries<>();
//                        humidade = new LineGraphSeries<>();
                        toggleFromTo(false);
                        toDate = new Date(System.currentTimeMillis());
                        cal.setTime(toDate);
                        cal.add(Calendar.MONTH, -1);
                        fromDate = cal.getTime();
                        System.out.println("from"+ fromDate.toString() + "to" + toDate.toString());
                        updateGraph(ds);
                        break;
                }
                selected = spinnerFilter.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //graph
        renderGraph();

        //eventos para chekcboxes
        checkBoxHum.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    graph.addSeries(humidade);
                }else {
                    graph.removeSeries(humidade);
                }
            }
        });
        checkBoxTemp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    graph.addSeries(temperatura);
                }else {
                    graph.removeSeries(temperatura);
                }
            }
        });

        //firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ds = dataSnapshot;
                updateGraph(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });

        return view;
    }

    public void updateGraph(DataSnapshot dataSnapshot){
        values.clear();
        for (DataSnapshot rooms: dataSnapshot.getChildren()) {
            for (DataSnapshot dates: rooms.getChildren()) {
                for(DataSnapshot array: dates.getChildren()){
                    StringBuilder stringDate = new StringBuilder(dates.getKey() + " " + array.child("hora").getValue().toString());
                    stringDate.setCharAt(13,':');
                    stringDate.setCharAt(16,':');
                    stringDate.setLength(stringDate.length() - 1);
                    try {
                        Date dateToCompare = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(stringDate.toString());
                        if(dateToCompare.compareTo(fromDate)>=0 && dateToCompare.compareTo(toDate)<=0){
                            //dateToCompare > fromDate && dateToCompare<=toDate
                            Value dado = new Value(dateToCompare,Float.parseFloat(array.child("temperatura").getValue().toString()),
                                    Float.parseFloat(array.child("humidade").getValue().toString()));
                            values.add(dado);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        //ordenar por data
        //AED carrega
        Collections.sort(values, new CompareDates());

        DataPoint[] dataPointsTemp = new DataPoint[values.size()];
        DataPoint[] dataPointsHum = new DataPoint[values.size()];

        //porque? porque o foreach nao estava a dar dont ask me why
        for(int i = 0; i< values.size(); i++){
            Value value = values.get(i);
            dataPointsTemp[i] = new DataPoint(value.getDate().getTime(),value.getTemperatura());
            dataPointsHum[i] = new DataPoint(value.getDate().getTime(),value.getHumidadade());
        }
        temperatura.resetData(dataPointsTemp);
        humidade.resetData(dataPointsHum);

        graph.getViewport().scrollToEnd();

    }

    public void renderGraph(){
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setGridColor(Color.WHITE);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(true);
        graph.getGridLabelRenderer().setHorizontalLabelsAngle(90);
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity(),new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")));
        graph.getGridLabelRenderer().reloadStyles();
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graph.getLegendRenderer().setVisible(true);


        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);
        graph.getViewport().scrollToEnd();
//        graph.onDataChanged(false, false);
//        graph.getViewport().calcCompleteRange();
//        graph.getViewport().computeScroll();
//        graph.getViewport().isXAxisBoundsManual();
//        graph.getViewport().isYAxisBoundsManual();
//        graph.getViewport().setMaxXAxisSize(100);


        //exemplo
        temperatura = new LineGraphSeries<DataPoint>();
        humidade = new LineGraphSeries<DataPoint>();
        temperatura.setColor(Color.RED);
        temperatura.setTitle("temperatura");
        humidade.setColor(Color.GREEN);
        humidade.setTitle("humidade");

        graph.addSeries(temperatura);
        graph.addSeries(humidade);
    }

    public void toggleFromTo(boolean bool){
        if(bool){
            dateFrom.setVisibility(View.VISIBLE);
            dateTo.setVisibility(View.VISIBLE);
            txtFrom.setVisibility(View.VISIBLE);
            txtTo.setVisibility(View.VISIBLE);
        }else{
            dateFrom.setVisibility(View.INVISIBLE);
            dateTo.setVisibility(View.INVISIBLE);
            txtFrom.setVisibility(View.INVISIBLE);
            txtTo.setVisibility(View.INVISIBLE);
        }
    }
}
