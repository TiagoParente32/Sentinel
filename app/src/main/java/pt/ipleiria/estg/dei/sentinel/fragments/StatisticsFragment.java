package pt.ipleiria.estg.dei.sentinel.fragments;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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

import de.codecrafters.tableview.TableView;
import pt.ipleiria.estg.dei.sentinel.CompareDates;
import pt.ipleiria.estg.dei.sentinel.Value;
import pt.ipleiria.estg.dei.sentinel.R;

public class StatisticsFragment extends Fragment {

    public static final String TAG = "Statistics";
    private Spinner spinnerFilter;
    private EditText editDateFrom;
    private EditText editDateTo;
    private TextView txtFrom;
    private TextView txtTo;
    private GraphView graph;
    private CheckBox checkBoxTemp;
    private CheckBox checkBoxHum;
    private ImageButton btnSearch;
    private DatabaseReference mDatabase;
    private Date fromDate;
    private Date toDate;
    private DataSnapshot ds;
    private List<Value> values;
    private LineGraphSeries<DataPoint> temperatura;
    private LineGraphSeries<DataPoint> humidade;
    private TableLayout tableLayout;
    private Button btnTable;
    private boolean tableBoolean = false;
    private TextView h1;
    private TextView h2;
    private TextView h3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("STATISTICS");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        spinnerFilter = view.findViewById(R.id.spinnerFilter);
        editDateFrom = view.findViewById(R.id.dateFrom);
        editDateTo = view.findViewById(R.id.dateTo);
        txtFrom = view.findViewById(R.id.textViewFrom);
        txtTo = view.findViewById(R.id.textViewTo);
        graph = view.findViewById(R.id.graph);
        checkBoxHum = view.findViewById(R.id.checkBoxHum);
        checkBoxTemp = view.findViewById(R.id.checkBoxTemp);
        values = new ArrayList<>();
        btnSearch = view.findViewById(R.id.btn_search);
        tableLayout = view.findViewById(R.id.tableLayout);
        btnTable = view.findViewById(R.id.btnTable);
        h1 = view.findViewById(R.id.h1);
        h2 = view.findViewById(R.id.h2);
        h3 = view.findViewById(R.id.h3);



        checkBoxHum.setChecked(true);
        checkBoxTemp.setChecked(true);

        //set default date pq o onDataChange é executado primeiro que o evento do spinner
        Calendar cal = Calendar.getInstance();
        toDate = new Date(System.currentTimeMillis());
        cal.setTime(toDate);
        cal.add(Calendar.HOUR, -24);
        fromDate = cal.getTime();

        //set invisible From-To form
        toggleFromTo(false);
        //esconde table e mostra graph só
        toggleTable(false);

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
                //set dos intervalos de datas, no custom é no btnSearch
                switch (spinnerFilter.getSelectedItem().toString()){
                    case "Custom":
                        toggleFromTo(true);
                        break;
                    case "Day":
                        toggleFromTo(false);
                        toDate = new Date(System.currentTimeMillis());
                        cal.setTime(toDate);
                        cal.add(Calendar.HOUR, -24);
                        fromDate = cal.getTime();
                        System.out.println("from" + fromDate.toString() + "to" + toDate.toString());
                        updateGraph(ds);
                        break;
                    case "Week":
                        toggleFromTo(false);
                        toDate = new Date(System.currentTimeMillis());
                        cal.setTime(toDate);
                        cal.add(Calendar.WEEK_OF_MONTH, -1);
                        fromDate = cal.getTime();
                        System.out.println("from"+ fromDate.toString() + "to" + toDate.toString());
                        updateGraph(ds);
                        break;
                    case "Month":
                        toggleFromTo(false);
                        toDate = new Date(System.currentTimeMillis());
                        cal.setTime(toDate);
                        cal.add(Calendar.MONTH, -1);
                        fromDate = cal.getTime();
                        System.out.println("from"+ fromDate.toString() + "to" + toDate.toString());
                        updateGraph(ds);
                        break;
                }
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

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!editDateFrom.getText().toString().matches("([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))") ||
                            !editDateTo.getText().toString().matches("([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))")) {
                        Toast toast = Toast.makeText(getContext(), "Invalid Date Format 'yyyy-MM-dd'", Toast.LENGTH_SHORT);
                        toast.show();
                        return;
                    }
                    toDate = new SimpleDateFormat("yyyy-MM-dd").parse(editDateTo.getText().toString());
                    fromDate = new SimpleDateFormat("yyyy-MM-dd").parse(editDateFrom.getText().toString());
                    updateGraph(ds);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        btnTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTable(!tableBoolean);
                tableBoolean=!tableBoolean;
            }
        });
        return view;
    }

    public void updateGraph(DataSnapshot dataSnapshot) {
        values.clear();
        for (DataSnapshot rooms : dataSnapshot.getChildren()) {
            for (DataSnapshot dates : rooms.getChildren()) {
                for (DataSnapshot array : dates.getChildren()) {
                    StringBuilder stringDate = new StringBuilder(dates.getKey() + " " + array.child("hora").getValue().toString());
                    stringDate.setCharAt(13, ':');
                    stringDate.setCharAt(16, ':');
                    stringDate.setLength(stringDate.length() - 1);
                    try {
                        Date dateToCompare = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(stringDate.toString());
                        if (dateToCompare.compareTo(fromDate) >= 0 && dateToCompare.compareTo(toDate) <= 0) {
                            //dateToCompare > fromDate && dateToCompare<=toDate
                            Value dado = new Value(dateToCompare, Float.parseFloat(array.child("temperatura").getValue().toString()),
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
        Collections.sort(values, new CompareDates());

        DataPoint[] dataPointsTemp = new DataPoint[values.size()];
        DataPoint[] dataPointsHum = new DataPoint[values.size()];

        tableLayout.removeAllViewsInLayout();
        //border
        GradientDrawable border = new GradientDrawable();
        //border.setColor(0xFFFFFFFF); //white background
        border.setStroke(1, 0xFF000000);

        for (int i = 0; i < values.size(); i++) {
            Value value = values.get(i);
            dataPointsTemp[i] = new DataPoint(value.getDate().getTime(), value.getTemperatura());
            dataPointsHum[i] = new DataPoint(value.getDate().getTime(), value.getHumidadade());

            //tableBoolean
            TableRow tr = new TableRow(getContext());
            TextView c1 = new TextView(getContext());
            c1.setText(new SimpleDateFormat("yy-MM-dd hh:mm:ss").format(values.get(i).getDate()));
            c1.setTextColor(Color.WHITE);

            TextView c2 = new TextView(getContext());
            c2.setText(String.format("%.02f",values.get(i).getHumidadade())+"ºC");
            c2.setTextColor(Color.WHITE);

            TextView c3 = new TextView(getContext());
            c3.setText(String.format("%.02f",values.get(i).getTemperatura())+"%");
            c3.setTextColor(Color.WHITE);

            tr.addView(c1);
            tr.addView(c2);
            tr.addView(c3);

            tr.setBackground(border);
            tableLayout.addView(tr);
        }

        temperatura.resetData(dataPointsTemp);
        humidade.resetData(dataPointsHum);

        graph.getViewport().setMinX(fromDate.getTime());
        graph.getViewport().setMaxX(toDate.getTime());

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getGridLabelRenderer().setNumHorizontalLabels(8);
    }


    public void renderGraph(){
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setGridColor(Color.WHITE);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(true);
        graph.getGridLabelRenderer().setHorizontalLabelsAngle(135);
        graph.getGridLabelRenderer().setVerticalLabelsVisible(true);
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getContext(),new SimpleDateFormat("yy/MM/dd HH:mm")));
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graph.getLegendRenderer().setVisible(true);

        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);
        graph.getViewport().scrollToEnd();
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMaxY(100);
        graph.getViewport().setMinY(-100);

        graph.getGridLabelRenderer().reloadStyles();

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
            editDateFrom.setVisibility(View.VISIBLE);
            editDateTo.setVisibility(View.VISIBLE);
            txtFrom.setVisibility(View.VISIBLE);
            txtTo.setVisibility(View.VISIBLE);
            btnSearch.setVisibility(View.VISIBLE);
        }else{
            editDateFrom.setVisibility(View.INVISIBLE);
            editDateTo.setVisibility(View.INVISIBLE);
            txtFrom.setVisibility(View.INVISIBLE);
            txtTo.setVisibility(View.INVISIBLE);
            btnSearch.setVisibility(View.INVISIBLE);
        }
    }

    public void toggleTable(boolean bool){
        if(bool){
            tableLayout.setVisibility(View.VISIBLE);
            h1.setVisibility(View.VISIBLE);
            h2.setVisibility(View.VISIBLE);
            h3.setVisibility(View.VISIBLE);
            graph.setVisibility(View.INVISIBLE);
            checkBoxHum.setVisibility(View.INVISIBLE);
            checkBoxTemp.setVisibility(View.INVISIBLE);
            btnTable.setText("Graph");
        }else {
            tableLayout.setVisibility(View.INVISIBLE);
            h1.setVisibility(View.INVISIBLE);
            h2.setVisibility(View.INVISIBLE);
            h3.setVisibility(View.INVISIBLE);
            graph.setVisibility(View.VISIBLE);
            checkBoxHum.setVisibility(View.VISIBLE);
            checkBoxTemp.setVisibility(View.VISIBLE);
            btnTable.setText("Table");
        }
    }
}
