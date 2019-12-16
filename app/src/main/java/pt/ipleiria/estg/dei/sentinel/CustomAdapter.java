package pt.ipleiria.estg.dei.sentinel;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import pt.ipleiria.estg.dei.sentinel.activities.MainActivity;
import pt.ipleiria.estg.dei.sentinel.fragments.FavoritesFragment;
import pt.ipleiria.estg.dei.sentinel.fragments.MyExposureFragment;

public class CustomAdapter extends BaseAdapter implements ListAdapter {
    public ArrayList<String> list;
    private Context context;
    private SharedPreferences sharedPref;
    private EventListener listener;
    private int view; // 1-favorites 2-exposure


    public CustomAdapter(ArrayList<String> list, Context context, SharedPreferences sharePref,int view,EventListener listener){
        this.list = list;
        this.context = context;
        this.sharedPref = sharePref;
        this.view = view;
        this.listener = listener;

    }

    public interface EventListener {
        void onEvent();
    }


    @Override
    public int getCount() {
        return  list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_view_custom, null);
        }

        //Handle TextView and display string from your list
        TextView listItemText = view.findViewById(R.id.list_item_string);
        listItemText.setText(list.get(position).split(":")[0]);
        ImageButton deleteBtn = view.findViewById(R.id.btnDelete);


        listItemText.setTextColor(Color.WHITE);


        deleteBtn.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.DialogTheme);

            builder.setMessage(R.string.favoritDialogMessage)
                    .setTitle(R.string.favoriteDialogTitle);

            builder.setPositiveButton(R.string.ok, (dialog, id) -> {
                list.remove(position);
                notifyDataSetChanged();
                try{
                    if(this.view == 1){
                        sharedPref.edit().putStringSet(Constants.PREFERENCES_FAVORITES_SET,new HashSet<>(this.list)).commit();

                    }else if(this.view == 2){
                        sharedPref.edit().putStringSet(Constants.PREFERENCES_EXPOSURE_SET,new HashSet<>(this.list)).commit();
                        this.listener.onEvent();
                    }else if(this.view == 3){

                        this.listener.onEvent();
                    }
                }catch(Exception ex){
                    Log.i("ERROR_FAVORITES_SAVE","Error saving preference favorites-> " + ex.getMessage());
                }
            });
            builder.setNegativeButton(R.string.cancel, (dialog, id) -> {

            });
            AlertDialog dialog = builder.create();
            dialog.show();

        });

        return view;
    }


}