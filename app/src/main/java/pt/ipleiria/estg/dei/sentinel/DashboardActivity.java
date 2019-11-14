package pt.ipleiria.estg.dei.sentinel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {
    public static final String TAG = "Dashboard";
    private DatabaseReference mDatabase;
    TextView stats;
    TextView qoa;
    TextView humidade;
    TextView temperatura;
    TextView utilmaData;
    ProgressBar pb;
    List<String> dateArray;
    ImageView imageTemp;
    ImageView imageHum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        stats = findViewById(R.id.textViewStats);
        qoa = findViewById(R.id.textViewQOA);
        humidade = findViewById(R.id.textViewHumidade);
        temperatura = findViewById(R.id.textViewTemperatura);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        dateArray = new ArrayList<>();
        ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar2);
        imageTemp = findViewById(R.id.imageViewTemperatura);
        imageHum = findViewById(R.id.imageViewHumidade);


        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //isto esta muito scuffed
                int temperaturasSum = 0;
                int humidadeSum = 0;
                int numTemperatura = 0;
                int numHumidade = 0;
                float mediaHum = 0;
                float mediaTemp = 0;
                int hora = 0;
                String data ;
                //--
                for (DataSnapshot datas:dataSnapshot.child("Edificio A").getChildren()) {
                    dateArray.add(datas.getKey());
                }
                Collections.sort(dateArray);
                data = dateArray.get(dateArray.size()-1);
                for (DataSnapshot latestDate:dataSnapshot.child("Edificio A").child(dateArray.get(dateArray.size()-1)).getChildren()) {
                        for (DataSnapshot key: latestDate.getChildren()) {
                                if(key.getKey().equals("temperatura")){
                                    temperaturasSum+=Integer.parseInt(key.getValue().toString());
                                    numTemperatura++;
                                }
                                if(key.getKey().equals("humidade")){
                                    humidadeSum+=Integer.parseInt(key.getValue().toString());
                                    numHumidade++;
                                }
                                if(key.getKey().equals("hora")){
                                    if(Integer.parseInt(key.getValue().toString())>hora){
                                        hora = Integer.parseInt(key.getValue().toString());
                                    }
                                }
                        }
                }
                mediaTemp = (temperaturasSum/numTemperatura);
                mediaHum = (humidadeSum/numHumidade);


                temperatura.setText(String.valueOf(mediaTemp)+"ºC");
                humidade.setText(String.valueOf(mediaHum));
                stats.setText("Temperatura: " + mediaTemp+ "ºC\r\n" + "Humidade: " +mediaHum);
                //data ta a dar erro idk
                //utilmaData.setText(data + " - " + hora +"H");



                if(mediaTemp>=19 && mediaTemp <=35){
                    //verde
                    imageTemp.setBackgroundColor(Color.parseColor("#90EE90"));
                }else if(mediaTemp<19 || mediaTemp>=35 ){
                    //vermelho
                    imageTemp.setBackgroundColor(Color.parseColor("#8E1600"));

                }

                if(mediaHum>=50 && mediaHum <=75){
                    //verde
                    imageHum.setBackgroundColor(Color.parseColor("#90EE90"));
                }else if(mediaHum<50 || mediaHum>=75 ){
                    //vermelho
                    imageHum.setBackgroundColor(Color.parseColor("#8E1600"));

                }



                if((mediaTemp>=19 && mediaTemp <=35) && (mediaHum>=50 && mediaHum <=75 )){
                    //roda a verde
                    qoa.setText("Good");
                    //vai para 20%
                    ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), 20);
                    animation.setDuration(1000);
                    animation.setInterpolator(new DecelerateInterpolator());
                    animation.start();
                }else if(((mediaTemp<19 || mediaTemp>=35) && (mediaHum>=50 && mediaHum <=75)) ||(
                        (mediaTemp>=19 && mediaTemp <=35) &&(mediaHum<50 || mediaHum>=75))){
                    //roda a amarelo
                    qoa.setText("Average");
                    //vai para 50%
                    ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), 50);
                    animation.setDuration(1000);
                    animation.setInterpolator(new DecelerateInterpolator());
                    animation.start();
                }else{
                    //roda a vermelho
                    qoa.setText("Bad");
                    //vai para 99%
                    ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), 99);
                    animation.setDuration(1000);
                    animation.setInterpolator(new DecelerateInterpolator());
                    animation.start();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });


    }
}
