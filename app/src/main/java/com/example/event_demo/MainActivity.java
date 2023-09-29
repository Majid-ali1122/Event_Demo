package com.example.event_demo;


import static com.example.event_demo.Utils.Util.getMonth;
import static com.example.event_demo.Utils.Util.toastMsg;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.event_demo.Receivers.NotificationReceiver;
import com.example.event_demo.Utils.Util;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static java.util.Calendar.MINUTE;



public class MainActivity extends AppCompatActivity implements MyInterface {

    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    private final static String default_notification_channel_id = "default";
    private static final String TAG = "MainActivity";
    private DatabaseHelper databaseHelper;
    private ArrayList<DataModel> items;
    private ItemAdapter itemsAdopter;
    private ListView itemsListView;
    private FloatingActionButton fab;
    TextView datecount;
    ItemAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);
        fab = findViewById(R.id.fab);
        itemsListView = findViewById(R.id.itemsList);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            ) {
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
         adapter = new ItemAdapter(MainActivity.this);
        TextView empty = findViewById(R.id.emptyTextView);
         datecount = findViewById(R.id.abc);
        empty.setText(Html.fromHtml(getString(R.string.listEmptyText),0));
        FrameLayout emptyView = findViewById(R.id.emptyView);
        itemsListView.setEmptyView(emptyView);

        populateListView();
        onFabClick();
        hideFab();
    }
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {

                } else {

                }
            });

    private void scheduleNotification(Notification notification, long delay) {
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.set(AlarmManager.RTC_WAKEUP, delay, pendingIntent);
        Log.d(TAG, "scheduleNotification: Notification set successfully!");
    }
    private Notification getNotification(String content) {
        //on notification click open MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, default_notification_channel_id);
        builder.setContentTitle("Event Reminder");
        builder.setContentText(content);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.calen);
        builder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);
        builder.setChannelId(NOTIFICATION_CHANNEL_ID);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        return builder.build();
    }

    private void insertDataToDb(String title, String date, String time,String status) {
        boolean insertData = databaseHelper.insertData(title, date, time, status);
        if (insertData) {
            try {
                populateListView();
                toastMsg("Added successfully!",MainActivity.this);
                Log.d(TAG, "Inserted into database");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            toastMsg("Something went wrong",MainActivity.this);
    }
    private void update(String actualname,String title, String date, String time,String status) {
        boolean insertData = databaseHelper.update(actualname,title, date, time, status);
        if (insertData) {
            try {
                populateListView();
                toastMsg("Update successfully!", MainActivity.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else
            toastMsg("Something went wrong",MainActivity.this);

    }
    public void populateListView() {
        try {
            items = databaseHelper.getAllData();
            itemsAdopter = new ItemAdapter(this, items);
            itemsListView.setAdapter(itemsAdopter);
            itemsAdopter.notifyDataSetChanged();
            Log.d(TAG, "populateListView: Displaying data in list view");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideFab() {
        itemsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    fab.show();
                }else{
                    fab.hide();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
    }



    private void onFabClick() {
        try {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddDialog(false,"","","","");
                    Log.d(TAG, "onFabClick: Opened edit dialog");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void showAddDialog(boolean type , String titlee, String date2, String time2,String status) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.AlertDialog);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        dialogBuilder.setView(dialogView);


        final EditText editTitle = dialogView.findViewById(R.id.edit_title);
        final TextView dateText = dialogView.findViewById(R.id.date);
        final TextView timeText = dialogView.findViewById(R.id.time);
        final long date = System.currentTimeMillis();
        //Set current date as default date
        if(type)
        {
            editTitle.setText(titlee);
            dateText.setText(date2);
            timeText.setText(time2);
        }
        else
        {

            SimpleDateFormat dateSdf = new SimpleDateFormat("d MMMM");
            String dateString = dateSdf.format(date);
            dateText.setText(dateString);

            //Set current time as default time
            SimpleDateFormat timeSdf = new SimpleDateFormat("hh : mm a");
            String timeString = timeSdf.format(date);
            timeText.setText(timeString);
        }


        final Calendar cal = Calendar.getInstance();
        if(type)
        {
            cal.setTimeInMillis(Long.parseLong(status));

        }
        else
        {
            cal.setTimeInMillis(System.currentTimeMillis());
        }



        dateText.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                final DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                String newMonth = getMonth(monthOfYear + 1);
                                dateText.setText(dayOfMonth + " " + newMonth);
                                cal.set(Calendar.YEAR, year);
                                cal.set(Calendar.MONTH, monthOfYear);
                                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                Log.d(TAG, "onDateSet: Date has been set successfully");
                            }
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                datePickerDialog.getDatePicker().setMinDate(date);
            }
        });


        timeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                String time;
                                String minTime = String.format("%02d", minute);

                                if (hourOfDay >= 0 && hourOfDay < 12) {
                                    time = hourOfDay + " : " + minTime + " AM";
                                } else {
                                    if (hourOfDay != 12) {
                                        hourOfDay = hourOfDay - 12;
                                    }
                                    time = hourOfDay + " : " + minTime + " PM";
                                }


                                cal.set(Calendar.HOUR, hourOfDay);
                                cal.set(Calendar.MINUTE, minute);
                                cal.set(Calendar.SECOND, 0);
                                if(cal.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
                                {
                                    toastMsg("Past time is not allowed",MainActivity.this);
                                }
                                else
                                {
                                    timeText.setText(time);
                                }


                                Log.d(TAG, "onTimeSet: Time has been set successfully");
                            }
                        }, cal.get(Calendar.HOUR), cal.get(MINUTE), false);
                timePickerDialog.show();
            }
        });


        dialogBuilder.setTitle("Add new Event!");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String title = editTitle.getText().toString();
                String date = dateText.getText().toString();
                String time = timeText.getText().toString();
                if (title.length() != 0) {
                    try {
                        if(type)
                        {

                            if(date.equals(date2) && time.equals(time2))
                            {
                                update(titlee,title, date, time,status);
                            }
                            else
                            {
                                update(titlee,title, date, time,String.valueOf(cal.getTimeInMillis()));
                            }

                        }
                        else
                        {
                            insertDataToDb(title, date, time,String.valueOf(cal.getTimeInMillis()));
                        }

                        scheduleNotification(getNotification(title), cal.getTimeInMillis());
//                        countDownStart(cal.getTimeInMillis());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    toastMsg("Oops, Cannot set an empty Event!!!",MainActivity.this);
                }
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                dialog.cancel();
            }
        });


        AlertDialog b = dialogBuilder.create();
        Animation animSlideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        animSlideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Fires when animation starts
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // ...
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // ...
            }
        });
        dialogView.startAnimation(animSlideUp);
        b.show();
    }


    @Override
    public void dialog(String title, String date, String time,String status) {
               showAddDialog(true,title,date,time,status);
    }
}
