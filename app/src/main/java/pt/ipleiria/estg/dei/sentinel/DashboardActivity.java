package pt.ipleiria.estg.dei.sentinel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;
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
    ProgressBar pb;
    List<String> dateArray;

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


        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    //isto esta muito scuffed
                    int temperaturasSum = 0;
                    int humidadeSum = 0;
                    int numTemperatura = 0;
                    int numHumidade = 0;
                    //--
                    for (DataSnapshot datas:dataSnapshot.child("Edificio A").getChildren()) {
                        dateArray.add(datas.getKey());
                    }
                    Collections.sort(dateArray);
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
                            }
                    }
                    temperatura.setText(""+(temperaturasSum/numTemperatura));
                    humidade.setText(""+(humidadeSum/numHumidade));

                /*String humi = dataSnapshot.child("Edificio A").child("humidade").getValue().toString();
                String temp = dataSnapshot.child("Edificio A").child("temperatura").getValue().toString();

                String qualityOfAir = dataSnapshot.child("Edificio A").child("qualidade").getValue().toString();

                stats.setText("Temperatura: " + temp+ "\r\n" + "Humidade: " +humi);
                humidade.setText(humi);
                temperatura.setText(temp);
                qoa.setText(qualityOfAir);
                ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), Integer.parseInt(qualityOfAir));
                animation.setDuration(1000);
                animation.setInterpolator(new DecelerateInterpolator());
                animation.start();*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });


    }
}
