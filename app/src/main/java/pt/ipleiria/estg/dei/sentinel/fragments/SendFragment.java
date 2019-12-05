package pt.ipleiria.estg.dei.sentinel.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import pt.ipleiria.estg.dei.sentinel.Constants;
import pt.ipleiria.estg.dei.sentinel.R;


public class SendFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor temperatureSensor;
    private Sensor humiditySensor;
    private Spinner spinnerRooms;
    private TextInputLayout editTemperature;
    private TextInputLayout editHumidity;
    private ArrayAdapter adapter;
    private List<String> roomsList ;
    private Button btnSend;
    private Button btnGuessLocation;
    private DatabaseReference ref;
    private String dateString;
    private StringBuilder horaSplit;
    private boolean boolSensorTemp = true;
    private boolean boolSensorHum = true;
    private boolean guessed = true;
    private TextView message ;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("SEND");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_send,container,false);

        //INICIAR VARS
        spinnerRooms = view.findViewById(R.id.spinnerRooms);
        editTemperature = view.findViewById(R.id.editTextTemperature);
        editHumidity = view.findViewById(R.id.editTextHumidity);
        btnSend = view.findViewById(R.id.buttonSend);
        ref = FirebaseDatabase.getInstance().getReference();
        btnGuessLocation = view.findViewById(R.id.buttonGuessLocation);
        message = view.findViewById(R.id.textViewMessageSend);

        //RECEBER DA MAIN ACTIVITY A ROOMSLIST
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            roomsList = bundle.getStringArrayList(Constants.DATA_INTENT_SPINNER_DATA);
        }
        //CRIAR ADAPTER PARA O SPINNER
        //apagar o "EdificioA"
        roomsList.remove(0);
        adapter = new ArrayAdapter<>(this.getActivity(), R.layout.spinner_list, roomsList);
        adapter.setDropDownViewResource(R.layout.spinner_list_dropdown);
        spinnerRooms.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        //para verificar se tem sensores
        PackageManager PM= getActivity().getPackageManager();
        boolean t = PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_AMBIENT_TEMPERATURE);
        boolean h = PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_RELATIVE_HUMIDITY);

        if (t){

            temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
//            editTemperature.set(""+temperatureSensor.getPower());
            editTemperature.getEditText().setText(""+temperatureSensor.getPower());
        } else {
            //mostrar "erro" depois
            editTemperature.getEditText().setText("0");
            editTemperature.setError("Temperature Sensor not found");
        }

        if (h){
            humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
            editHumidity.getEditText().setText(""+humiditySensor.getPower());

        }else {
            //mostrar "erro" depois
            editHumidity.getEditText().setText("0");
            editHumidity.setError("Humidity Sensor not found");

        }

        btnSend.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                //IR BUSCAR DATA
                //send to firebase database
                SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date(System.currentTimeMillis());
                dateString = formatter.format(date);
                SimpleDateFormat formatter2 = new SimpleDateFormat("HH:mm:ss");
                String horaMin = formatter2.format(date);
                //horaSplit;
                horaSplit = new StringBuilder(horaMin);
                System.out.println("*****************************");
                System.out.println("hora lenght" + horaSplit.length());
                horaSplit.setCharAt(2,'h');
                horaSplit.setCharAt(5,'m');
                horaSplit.insert(horaSplit.length(),'s');
                System.out.println("hora : " + horaSplit);
                //horaSplit = horaMin.replace(':','h');
                //horaSplit = horaSplit+"m";
                //horaSplit = horaSplit+"s";

                Map<String,Object> childOfChildUpdates = new HashMap<>();
                childOfChildUpdates.put("hora",horaSplit.toString());
                childOfChildUpdates.put("temperatura",editTemperature.getEditText().getText().toString());
                childOfChildUpdates.put("humidade",editHumidity.getEditText().getText().toString());

                ref.child(spinnerRooms.getSelectedItem().toString()+"/"+ dateString).push().updateChildren(childOfChildUpdates).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            message.setText("Data Sent Sucessfully");
                            message.setVisibility(View.VISIBLE);
                            message.postDelayed(new Runnable() {
                                public void run() {
                                    message.setVisibility(View.INVISIBLE);
                                }
                            }, 3000);
                        }else {
                            message.setText("Error Sending Data");
                            message.setVisibility(View.VISIBLE);
                            message.postDelayed(new Runnable() {
                                public void run() {
                                    message.setVisibility(View.INVISIBLE);
                                }
                            }, 3000);
                        }
                    }
                });


            }
        });

        btnGuessLocation.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //CRIAR RANDOM SELECTED ITEM ON SPINNER
                if(guessed){
                    Random r = new Random();
                    int result = r.nextInt(roomsList.size()-0);
                    spinnerRooms.setSelection(result);

                    spinnerRooms.setEnabled(false);
                    spinnerRooms.setClickable(false);
                    spinnerRooms.setAlpha(0.5f);
                }else{
                    spinnerRooms.setAlpha(1);
                    spinnerRooms.setEnabled(true);
                    spinnerRooms.setClickable(true);
                }
                guessed = !guessed;
            }
        });

        return view;
    }

    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, humiditySensor, SensorManager.SENSOR_DELAY_UI);
    }


    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE && boolSensorTemp){
            editTemperature.getEditText().setText(""+ event.values[0]);
            boolSensorTemp = false;
        }
        if (event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY && boolSensorHum){
            editHumidity.getEditText().setText(""+ event.values[0]);
            boolSensorHum = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
