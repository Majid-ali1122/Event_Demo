package com.example.event_demo;
import static com.example.event_demo.Utils.Util.getMonth;
import static com.example.event_demo.Utils.Util.toastMsg;
import static java.util.Calendar.MINUTE;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ItemAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<DataModel> arrayList;
    private Handler handler;

    private Runnable runnable;
    private MyInterface listener;

    public ItemAdapter(MyInterface listener){
        this.listener = listener;
    }
    public ItemAdapter(Context context, ArrayList<DataModel> arrayList) {
        super();
        this.context = context;
        this.arrayList = arrayList;

    }

    @Override
    public int getCount() {
        return this.arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(R.layout.row_item, null);
        TextView titleTextView = convertView.findViewById(R.id.title);
        TextView dateTextView = convertView.findViewById(R.id.dateTitle);
        TextView timeTextView = convertView.findViewById(R.id.timeTitle);
        TextView count = convertView.findViewById(R.id.count);
        final ImageView delImageView = convertView.findViewById(R.id.delete);
        delImageView.setTag(position);
        final ImageView editimg = convertView.findViewById(R.id.edit);

        final View finalConvertView = convertView;


        delImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = (int) v.getTag();
                Animation animSlideRight = AnimationUtils.loadAnimation(context, R.anim.slide_out_right);
                animSlideRight.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        // Fires when animation starts
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // ...
                        deleteItem(pos);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // ...
                    }
                });
                finalConvertView.startAnimation(animSlideRight);
            }
        });

        DataModel dataModel = arrayList.get(position);
        titleTextView.setText(dataModel.getEventtitle());
        dateTextView.setText(dataModel.getDate());
        timeTextView.setText(dataModel.getTime());
        long dekay= Long.parseLong(dataModel.getStatus());
        editimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).showAddDialog(true,dataModel.getEventtitle(),dataModel.getDate(),dataModel.getTime(),dataModel.getStatus());
//                listener.dialog(dataModel.getEventtitle(),dataModel.getDate(),dataModel.getTime(),dataModel.getStatus());
            }
        });
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 1000);
                try {

                    Date currentDate = new Date();
                    if (dekay > currentDate.getTime()) {
                        long diff = dekay - currentDate.getTime();
                        long days = diff / (24 * 60 * 60 * 1000);
                        diff -= days * (24 * 60 * 60 * 1000);
                        long hours = diff / (60 * 60 * 1000);
                        diff -= hours * (60 * 60 * 1000);
                        long minutes = diff / (60 * 1000);
                        diff -= minutes * (60 * 1000);
                        long seconds = diff / 1000;
                        count.setText("" + String.format("%02d", days) + "d : " + String.format("%02d", hours) + "h : "
                                + String.format("%02d", minutes) + "m : "
                                + String.format("%02d", seconds)+ "s");
                    }
                    else
                    {
                        count.setText("Event Started");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 1000);

        return convertView;

    }

    public void deleteItem(int position) {
        deleteItemFromDb(arrayList.get(position).getEventtitle(), arrayList.get(position).getDate(), arrayList.get(position).getTime(), arrayList.get(position).getStatus());
        arrayList.remove(position);
        notifyDataSetChanged();
    }

    public void deleteItemFromDb(String name, String date,String time, String status) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        try {
            databaseHelper.deleteData(name, date, time, status);
            toastMsg("Deleted Successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            toastMsg("Something went wrong");
        }
    }

    //Create and call toast messages when necessary
    public void toastMsg(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }


}
