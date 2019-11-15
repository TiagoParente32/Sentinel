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

public class DashboardActivity extends AppCompatActivity {
    public static final String TAG = "Dashboard";
    private DatabaseReference mDatabase;
    TextView qoa;
    TextView humidade;
    TextView temperatura;
    TextView ultimaData;
    ProgressBar pb;
    ImageView imageTemp;
    ImageView imageHum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        qoa = findViewById(R.id.textViewQOA);
        humidade = findViewById(R.id.textViewHumidade);
        temperatura = findViewById(R.id.textViewTemperatura);
        ultimaData = findViewById(R.id.textViewData);
        pb = (ProgressBar) findViewById(R.id.progressBar2);
        imageTemp = findViewById(R.id.imageViewTemperatura);
        imageHum = findViewById(R.id.imageViewHumidade);


        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                updateUI(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });


    }

    public void updateUI(DataSnapshot dataSnapshot){
        int temperaturasSum = 0;
        int humidadeSum = 0;
        int numTemperatura = 0;
        int numHumidade = 0;
        float mediaHum = 0;
        float mediaTemp = 0;
        String hora = "";
        String data = getlatestDate(dataSnapshot,"Edificio A");

        for (DataSnapshot latestDate:dataSnapshot.child("Edificio A").child(data).getChildren()) {
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
                    if(key.getValue().toString().compareTo(hora)>0){
                        hora = key.getValue().toString();
                    }
                }
            }
        }
        mediaTemp = (temperaturasSum/numTemperatura);
        mediaHum = (humidadeSum/numHumidade);


        temperatura.setText(String.valueOf(mediaTemp)+"ºC");
        humidade.setText(String.valueOf(mediaHum));
        ultimaData.setText(data + " " + hora);

        updateUIColors(mediaTemp,mediaHum);
    }

    public String getlatestDate(DataSnapshot dataSnapshot, String edificio){
        String data = "";
        for (DataSnapshot datas:dataSnapshot.child(edificio).getChildren()) {
            if(datas.getKey().compareTo(data)>0){
                data = datas.getKey();
            }
        }
        return data;
    }

    public float getAverage(DataSnapshot dataSnapshot,String edificio,String date,String key){
        int soma = 0;
        int n = 0;
        for (DataSnapshot latestDate:dataSnapshot.child("Edificio A").child(date).getChildren()) {
            for (DataSnapshot value: latestDate.getChildren()) {
                if(value.getKey().equals(key)){
                    soma+=Integer.parseInt(value.getValue().toString());
                    n++;
                }
            }
        }

        return(soma/n);
    }

    public void updateUIColors(float mediaTemp, float mediaHum){
        if(mediaTemp>=19 && mediaTemp <=35){
            //verde
            imageTemp.setBackgroundColor(Color.parseColor("#008000"));
        }else{
            //vermelho
            imageTemp.setBackgroundColor(Color.parseColor("#FF0000"));

        }

        if(mediaHum>=50 && mediaHum <=75){
            //verde
            imageHum.setBackgroundColor(Color.parseColor("#008000"));
        }else {
            //vermelho
            imageHum.setBackgroundColor(Color.parseColor("#FF0000"));

        }



        if((mediaTemp>=19 && mediaTemp <=35) && (mediaHum>=50 && mediaHum <=75 )){
            //roda a verde
            qoa.setText("Good");
            //vai para 20%
            ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), 20);
            animation.setDuration(500);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        }else if(((mediaTemp<19 || mediaTemp>=35) && (mediaHum>=50 && mediaHum <=75)) ||(
                (mediaTemp>=19 && mediaTemp <=35) &&(mediaHum<50 || mediaHum>=75))){
            //roda a amarelo
            qoa.setText("Average");
            //vai para 50%
            ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), 50);
            animation.setDuration(500);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        }else{
            //roda a vermelho
            qoa.setText("Bad");
            //vai para 99%
            ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), 99);
            animation.setDuration(500);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        }
    }
}
