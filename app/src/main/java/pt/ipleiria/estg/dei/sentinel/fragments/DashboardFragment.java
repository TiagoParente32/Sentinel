package pt.ipleiria.estg.dei.sentinel.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import pt.ipleiria.estg.dei.sentinel.Constants;
import pt.ipleiria.estg.dei.sentinel.activities.MainActivity;
import pt.ipleiria.estg.dei.sentinel.R;


public class DashboardFragment extends Fragment {

    //-----------------UI----------------
    private TextView qoa;
    private TextView humidade;
    private TextView temperatura;
    private TextView ultimaData;
    private ProgressBar pb;
    private ProgressBar pbTemp;
    private ProgressBar pbHum;
    private Spinner spinnerRooms;
    private ImageButton btnShare;
    private ImageButton btnAddFavorites;
    private ImageButton btnExposure;
    private SharedPreferences sharedPref;
    private TextView textEmptyData;
    private ProgressBar pbBackground;
    private ImageView imgHum;
    private ImageView imgTemp;

    //------------variables---------------
    private DatabaseReference mDatabase;
    public static final String TAG = "Dashboard";
    private DataSnapshot ds;
    //para depois usar como query para encontrar
    private String selected = "Edificio A";
    private int selectedIndex = 0;
    private float temperaturasSum = 0;
    private float humidadeSum = 0;
    private int numTemperatura = 0;
    private int numHumidade = 0;
    private float mediaHum = 0;
    private float mediaTemp = 0;
    private int checkListener = 0;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> favoritesList;
    private ArrayList<String> exposureList;
    private SwipeRefreshLayout swipeRefresh;
    private Date lastDate =  new Date(Long.MIN_VALUE);


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        getActivity().setTitle("DASHBOARD");


        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    ds = dataSnapshot;
                    updateUIOnDataChange(dataSnapshot);
                } catch (Exception e) {
                    Log.w(TAG, "Failed to read value.", e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_dashboard,container,false);

        sharedPref = getActivity().getSharedPreferences(Constants.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);

        qoa = view.findViewById(R.id.textViewQOA);
        humidade = view.findViewById(R.id.txtHum);
        temperatura = view.findViewById(R.id.txtTemp);
        ultimaData = view.findViewById(R.id.textViewData);
        pb = (ProgressBar) view.findViewById(R.id.progressBar2);
        spinnerRooms = view.findViewById(R.id.spinnerRooms);
        pbTemp = view.findViewById(R.id.pbTemp);
        pbHum = view.findViewById(R.id.pbHum);
        btnShare = view.findViewById(R.id.btnShare);
        btnAddFavorites = view.findViewById(R.id.btnAddFavorite);
        btnExposure = view.findViewById(R.id.btnExposure);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        textEmptyData = view.findViewById(R.id.textEmptyData);
        pbBackground = view.findViewById(R.id.progressBar3);
        imgHum = view.findViewById(R.id.imageViewHumidade);
        imgTemp = view.findViewById(R.id.imageViewTemperatura);
        // ON REFRESH LISTENER
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                System.out.println("refreshing...");
                updateUIOnItemSelected();
                swipeRefresh.setRefreshing(false);
                Snackbar.make(getView(), R.string.refresh_success, Snackbar.LENGTH_SHORT).show();
            }
        });

        /*GET FAVORITES LIST*/
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


        /*GET EXPOSURE LIST*/

        if(sharedPref.contains(Constants.PREFERENCES_EXPOSURE_SET)){

            try{
                set = sharedPref.getStringSet(Constants.PREFERENCES_EXPOSURE_SET,null);
            }catch(Exception ex){
                Log.i("ERROR_EXPOSURE_GETTING","Error getting saved exposures-> " + ex.getMessage());
                set = new HashSet<>();
            }

            this.exposureList = new ArrayList<>(set);

        }else{
            this.exposureList = new ArrayList<>();
        }


        /*ON CLICK ACTIONS*/
        btnShare.setOnClickListener(v -> ((MainActivity)getActivity()).loginToTwitter());

        btnAddFavorites.setOnClickListener(v -> persistFavorite());

        btnExposure.setOnClickListener(v -> persistExposure());



        //check if user is authenticated
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user == null) {
//            // User is  not signed in
//            //nao consegue alterar o sitio
//            spinnerRooms.setEnabled(false);
//            spinnerRooms.setClickable(false);
//            spinnerRooms.setAlpha(0.5f);
//            btnAddFavorites.setVisibility(View.INVISIBLE);
//            btnShare.setVisibility(View.INVISIBLE);
//            btnExposure.setVisibility(View.INVISIBLE);
//
//        }else{
//            spinnerRooms.setAlpha(1);
//        }
        checkUserAuth();

        //user is signed in
        spinnerRooms.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    checkIsFavorite();
                    //este if é para o listener nao dar trigger quando o spinner é criado
                    if (++checkListener > 1) {
                        selectedIndex = position;
                        updateUIOnItemSelected();
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Failed to read value.", e);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }

    private boolean checkIsFavorite(){
        /*CHECKS IF SELECTED ROOM IS IN FAVORITES LIST*/
        if(favoritesList.contains(spinnerRooms.getSelectedItem())) {
            btnAddFavorites.setImageResource(R.drawable.ic_star_white);
            return true;
        }
        btnAddFavorites.setImageResource(R.drawable.ic_star_white_border);
        return false;

    }

    private void persistFavorite(){
        if(checkIsFavorite()){

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.DialogTheme);

                builder.setMessage(R.string.dialog_message)
                        .setTitle(R.string.favoriteDialogTitle);

                builder.setPositiveButton(R.string.ok, (dialog, id) -> {
                    this.favoritesList.remove((String)spinnerRooms.getSelectedItem());
                    adapter.notifyDataSetChanged();
                    try{
                        sharedPref.edit().putStringSet(Constants.PREFERENCES_FAVORITES_SET,new HashSet<>(this.favoritesList)).commit();
                        btnAddFavorites.setImageResource(R.drawable.ic_star_white_border);

                    }catch(Exception ex){
                        Log.i("ERROR_FAVORITES_SAVE","Error saving preference favorites-> " + ex.getMessage());
                    }            });
                builder.setNegativeButton(R.string.cancel, (dialog, id) -> {

                });
                AlertDialog dialog = builder.create();
                dialog.show();

        }else{

            this.favoritesList.add((String)spinnerRooms.getSelectedItem());
            btnAddFavorites.setImageResource(R.drawable.ic_star_white);

        }
        try{
            HashSet<String>  set = new HashSet<>(this.favoritesList);
            sharedPref.edit().putStringSet(Constants.PREFERENCES_FAVORITES_SET,set).commit();
        }catch(Exception ex){
            Log.i("ERROR_FAVORITES_SAVE","Error saving preference favorites-> " + ex.getMessage());
        }


    }

    private void persistExposure(){
        Toast.makeText(getActivity(),"Room added to Exposure Data!",Toast.LENGTH_SHORT).show();
        try{
            String data = (String)spinnerRooms.getSelectedItem() + ':' + mediaTemp + ':' + mediaHum + ':' +  new SimpleDateFormat("yyyy-mm-dd-HH:mm:ss", Locale.getDefault()).format(new Date());


            this.exposureList.add(data);
            HashSet<String>  set = new HashSet<>(this.exposureList);


            sharedPref.edit().putStringSet(Constants.PREFERENCES_EXPOSURE_SET,set).commit();

            Log.i("EXPOSURE_SAVED","EXPOSURE SAVED SUCCESSFULLY");


        }catch(Exception ex){
            Log.i("ERROR_EXPOSURE_SAVE","Error saving preference exposure-> " + ex.getMessage());
        }
    }

    public void updateUIOnDataChange(DataSnapshot dataSnapshot) {
        //resetar todas as variaveis
        temperaturasSum = 0;
        humidadeSum = 0;
        numTemperatura = 0;
        numHumidade = 0;
        mediaHum = 0;
        mediaTemp = 0;
        lastDate = new Date(Long.MIN_VALUE);
        //lista de rooms
        List<String> roomsList = new ArrayList<>();
        //adicionar "Edificio A" hardcoded ja que nao está na bd
        roomsList.add("Edificio A");

        //iterar pelos nodes de "rooms" todos e addicionar a lista
        for (DataSnapshot rooms : dataSnapshot.getChildren()) {
            roomsList.add(rooms.getKey());
        }
        setSpinnerData(roomsList);

        //dar setup ao adapter e atribuilo ao spinner ( para meter os rooms todos sempre na dropdown)
        adapter = new ArrayAdapter<String>(this.getActivity(), R.layout.spinner_list, roomsList);
        adapter.setDropDownViewResource(R.layout.spinner_list_dropdown);
        spinnerRooms.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        //aqui como ao dar setup ao adapter atribui o indice 0 como default fazer esta verificaçao para
        //voltar ao selected value
        if (spinnerRooms.getSelectedItemPosition() != selectedIndex) {
            spinnerRooms.setSelection(selectedIndex);
        }

        //default vais buscar "EdificioA"
        //se o index estiver 0, ou seja na primeira call, ou quando "edificio A" selected, nos outros updates skippa isto pq vai fazer o listener do spinner

        iterateDatasnapshot();

        updateText();

        //setup das cores segundo os nossos limites
        updateUIColors(mediaTemp, mediaHum);

        setData(temperatura.getText().toString(),humidade.getText().toString(),selected,qoa.getText().toString());


    }

    public void updateUIColors(float mediaTemp, float mediaHum) {
        if (mediaTemp >= 19 && mediaTemp <= 35) {
            //verde
            pbTemp.getProgressDrawable().setColorFilter(Color.parseColor("#90EE90"), PorterDuff.Mode.SRC_IN);
        } else {
            //vermelho
            pbTemp.getProgressDrawable().setColorFilter(Color.parseColor("#8E1600"), PorterDuff.Mode.SRC_IN);

        }

        if (mediaHum >= 50 && mediaHum <= 75) {
            //verde
            pbHum.getProgressDrawable().setColorFilter(Color.parseColor("#90EE90"), PorterDuff.Mode.SRC_IN);

        } else {
            //vermelho
            pbHum.getProgressDrawable().setColorFilter(Color.parseColor("#8E1600"), PorterDuff.Mode.SRC_IN);

        }


        if ((mediaTemp >= 19 && mediaTemp <= 35) && (mediaHum >= 50 && mediaHum <= 75)) {
            //roda a verde
            qoa.setText("Good");
            //vai para 20%
            ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), 20);
            animation.setDuration(500);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        } else if (((mediaTemp < 19 || mediaTemp >= 35) && (mediaHum >= 50 && mediaHum <= 75)) || (
                (mediaTemp >= 19 && mediaTemp <= 35) && (mediaHum < 50 || mediaHum >= 75))) {
            //roda a amarelo
            qoa.setText("Average");
            //vai para 50%
            ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), 50);
            animation.setDuration(500);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        } else {
            //roda a vermelho
            qoa.setText("Bad");
            //vai para 99%
            ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), 99);
            animation.setDuration(500);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        }
    }

    public void updateUIOnItemSelected() {
        //resetar todas as variaveis
        temperaturasSum = 0;
        humidadeSum = 0;
        numTemperatura = 0;
        numHumidade = 0;
        mediaHum = 0;
        mediaTemp = 0;
        lastDate =  new Date(Long.MIN_VALUE);
        //ir buscar o selected item para string
        selected = spinnerRooms.getSelectedItem().toString();

        iterateDatasnapshot();

        updateText();
        //update das cores com os nossos limites
        updateUIColors(mediaTemp, mediaHum);

        setData(temperatura.getText().toString(),humidade.getText().toString(),selected,qoa.getText().toString());
    }

    public void updateText(){
        toggleUI(true);

        if(numHumidade != 0 && numTemperatura != 0){
            //calculo de medias
            mediaTemp = (temperaturasSum / numTemperatura);
            mediaHum = (humidadeSum / numHumidade);
        }else{
            toggleUI(false);
            textEmptyData.setText("No Data Found");
        }

        //limitar humidade de 0%-100%
        if (mediaHum < 0) {
            mediaHum = 0;
        }
        if (mediaHum > 100) {
            mediaHum = 100;
        }

        //atribuir a UI
        temperatura.setText( String.format("%.02f", mediaTemp) + "ºC");
        humidade.setText( String.format("%.02f", mediaHum) + "%");
        ultimaData.setText("Last Update: " + new SimpleDateFormat("yyyy-mm-dd hh:mm:ss").format(lastDate));
    }

    public void toggleUI(boolean bool){
        if(bool){
            textEmptyData.setVisibility(View.INVISIBLE);
            pb.setVisibility(View.VISIBLE);
            pbHum.setVisibility(View.VISIBLE);
            pbTemp.setVisibility(View.VISIBLE);
            qoa.setVisibility(View.VISIBLE);
            humidade.setVisibility(View.VISIBLE);
            temperatura.setVisibility(View.VISIBLE);
            ultimaData.setVisibility(View.VISIBLE);
            pbBackground.setVisibility(View.VISIBLE);
            imgHum.setVisibility(View.VISIBLE);
            imgTemp.setVisibility(View.VISIBLE);
            btnShare.setVisibility(View.VISIBLE);

            checkUserAuth();
        }else {
            textEmptyData.setVisibility(View.VISIBLE);
            pb.setVisibility(View.INVISIBLE);
            pbHum.setVisibility(View.INVISIBLE);
            pbTemp.setVisibility(View.INVISIBLE);
            qoa.setVisibility(View.INVISIBLE);
            humidade.setVisibility(View.INVISIBLE);
            temperatura.setVisibility(View.INVISIBLE);
            ultimaData.setVisibility(View.INVISIBLE);
            pbBackground.setVisibility(View.INVISIBLE);
            imgHum.setVisibility(View.INVISIBLE);
            imgTemp.setVisibility(View.INVISIBLE);
            btnShare.setVisibility(View.INVISIBLE);
        }
    }

    public void iterateDatasnapshot(){
        Calendar cal = Calendar.getInstance();
        Date toDate = new Date(System.currentTimeMillis());
        cal.setTime(toDate);
        cal.add(Calendar.HOUR, -24);
        Date fromDate = cal.getTime();

        //se for "Edificio A" fazer a media geral de todos os rooms naquele dia
        if (selected.equals("Edificio A") || selectedIndex == 0) {
            //iterar por todos os rooms
            for (DataSnapshot rooms : ds.getChildren()) {
                for (DataSnapshot dates : rooms.getChildren()) {
                    for (DataSnapshot key : dates.getChildren()) {
                        StringBuilder stringDate = new StringBuilder(dates.getKey() + " " + key.child("hora").getValue().toString());
                        stringDate.setCharAt(13, ':');
                        stringDate.setCharAt(16, ':');
                        stringDate.setLength(stringDate.length() - 1);
                        try{
                            Date dateToCompare = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(stringDate.toString());
                            if (dateToCompare.compareTo(fromDate) >= 0 && dateToCompare.compareTo(toDate) <= 0) {
                                if(dateToCompare.compareTo(lastDate)>0){
                                    lastDate = dateToCompare;
                                }
                                temperaturasSum += Float.parseFloat(key.child("temperatura").getValue().toString());
                                humidadeSum += Float.parseFloat(key.child("humidade").getValue().toString());
                                numTemperatura++;
                                numHumidade++;
                            }
                        }catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            //se nao for "Edificio A" fazer media so do room "selected" na data
            //ou seja iterar apenas pelo filho "selected"
            for (DataSnapshot dates : ds.child(selected).getChildren()) {
                for (DataSnapshot key : dates.getChildren()) {
                        StringBuilder stringDate = new StringBuilder(dates.getKey() + " " + key.child("hora").getValue().toString());
                        stringDate.setCharAt(13, ':');
                        stringDate.setCharAt(16, ':');
                        stringDate.setLength(stringDate.length() - 1);
                        try{
                            Date dateToCompare = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(stringDate.toString());
                            if (dateToCompare.compareTo(fromDate) >= 0 && dateToCompare.compareTo(toDate) <= 0) {
                                if(dateToCompare.compareTo(lastDate)>0){
                                    lastDate = dateToCompare;
                                }
                                temperaturasSum += Float.parseFloat(key.child("temperatura").getValue().toString());
                                humidadeSum += Float.parseFloat(key.child("humidade").getValue().toString());
                                numTemperatura++;
                                numHumidade++;
                            }
                        }catch (ParseException e) {
                            e.printStackTrace();
                        }
                }
            }
        }
    }

    public void checkUserAuth(){
        //check if user is authenticated
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // User is  not signed in
            //nao consegue alterar o sitio
            spinnerRooms.setEnabled(false);
            spinnerRooms.setClickable(false);
            spinnerRooms.setAlpha(0.5f);
            btnAddFavorites.setVisibility(View.INVISIBLE);
            btnShare.setVisibility(View.INVISIBLE);
            btnExposure.setVisibility(View.INVISIBLE);
        }else{
            spinnerRooms.setAlpha(1);
        }
    }

    public void setData(String temperature,String humidity,String location,String airQuality) {
        Activity activity = getActivity();
        if(activity instanceof MainActivity) {
            ((MainActivity) activity).setData(temperature,humidity,location,airQuality);
        }
    }

    public void setSpinnerData(List<String> roomsList ) {
        Activity activity = getActivity();
        if(activity instanceof MainActivity) {
            ((MainActivity) activity).setSpinnerData(roomsList);
        }
    }

}