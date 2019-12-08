package pt.ipleiria.estg.dei.sentinel.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

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
    private String selected;



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
        dateFrom = view.findViewById(R.id.dateFrom);
        dateTo = view.findViewById(R.id.dateTo);
        txtFrom = view.findViewById(R.id.textViewFrom);
        txtTo = view.findViewById(R.id.textViewTo);
        graph = view.findViewById(R.id.graph);
        checkBoxHum = view.findViewById(R.id.checkBoxHum);
        checkBoxTemp = view.findViewById(R.id.checkBoxTemp);

        checkBoxHum.setChecked(true);
        checkBoxTemp.setChecked(true);

        //set invisible
        dateFrom.setVisibility(View.INVISIBLE);
        dateTo.setVisibility(View.INVISIBLE);
        txtFrom.setVisibility(View.INVISIBLE);
        txtTo.setVisibility(View.INVISIBLE);


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
                if(spinnerFilter.getSelectedItem().toString() == "Custom"){
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
                selected = spinnerFilter.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
        //graph
        renderGraph();
        return view;
    }

    public void renderGraph(){
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setGridColor(Color.WHITE);
        graph.getGridLabelRenderer().reloadStyles();
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        //exemplo
        temperatura = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        humidade = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 3),
                new DataPoint(1, 5),
                new DataPoint(2, 6),
                new DataPoint(3, 2),
                new DataPoint(4, 7)
        });
        temperatura.setColor(Color.RED);
        temperatura.setTitle("temperatura");
        humidade.setColor(Color.GREEN);
        humidade.setTitle("humidade");

        graph.addSeries(temperatura);
        graph.addSeries(humidade);
    }

}
