package com.example.covid_management_android.activity.appointments;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.covid_management_android.R;
import com.example.covid_management_android.activity.userActivity.CovidQuestionnaireRedirection;
import com.example.covid_management_android.activity.userActivity.HospitalList;
import com.example.covid_management_android.activity.userActivity.QuestionActivity;
import com.example.covid_management_android.adapter.AppUtil;
import com.example.covid_management_android.adapter.RetrofitUtil;
import com.example.covid_management_android.adapter.TimeslotAdapter;
import com.example.covid_management_android.model.appointments.Timeslot;
import com.example.covid_management_android.service.AppointmentClient;
import com.example.covid_management_android.service.UserClient;
import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.List;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.example.covid_management_android.constants.Constants.BASE_URL;

public class AppointmentBookingActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, TimeslotAdapter.TimeSlotCardListener {
    HorizontalCalendar calender;
    Calendar startDate, endDate;
    SharedPreferences sharedPreferences;
    AppUtil appUtil = new AppUtil();
    RetrofitUtil retrofitUtil;
    Retrofit retrofit;
    AppointmentClient appointmentClient;
    RecyclerView.LayoutManager myLayoutManager;

    int hospitalId;
    RecyclerView slotListRecycler;
    List<Timeslot> list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_booking);

        sharedPreferences = getSharedPreferences("covidManagement", MODE_PRIVATE);
        startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE, 0);
        Log.i("start d is --- ", startDate.getTime().toString());
        endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE, 14);


        calender = new HorizontalCalendar.Builder(this, R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(1)
                .mode(HorizontalCalendar.Mode.DAYS)
                .build();
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        appUtil.checkMenuItems(navigationView.getMenu(), AppointmentBookingActivity.this);
        navigationView.setNavigationItemSelectedListener(this);
        retrofitUtil = new RetrofitUtil(BASE_URL + "/appointment/");
        retrofit = retrofitUtil.getRetrofit();
        retrofitUtil.setContext(AppointmentBookingActivity.this);
        appointmentClient = retrofit.create(AppointmentClient.class);
        slotListRecycler = findViewById(R.id.slotListRecyclerView);

        hospitalId = getIntent().getIntExtra("hospitalId", -1);

        if (hospitalId <= 0) {
            Intent intent = new Intent(AppointmentBookingActivity.this, HospitalList.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        Log.i("selected date is --- ", calender.getSelectedDate().getTime().toString());
        updateTimeslotList(startDate);
        calender.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                updateTimeslotList(date);
            }


        });


    }

    private void updateTimeslotList(Calendar date) {
        String token = sharedPreferences.getString("token", null);
        String refreshToken = sharedPreferences.getString("refreshToken", null);
        assert token != null;
        if (token.split("JWT ").length == 1) {
            token = "JWT " + token;
        }
        final Call<List<Timeslot>> timeSlotList;
        timeSlotList = appointmentClient.getAvailableTimeslots(token, refreshToken, date.getTimeInMillis(), hospitalId);
        timeSlotList.enqueue(new Callback<List<Timeslot>>() {
            @Override
            public void onResponse(@NotNull Call<List<Timeslot>> call, @NotNull Response<List<Timeslot>> response) {
                list = response.body();
                assert list != null;
                Log.i("List is == ", list.toString());
                TimeslotAdapter timeslotAdapter = new TimeslotAdapter(AppointmentBookingActivity.this, list, AppointmentBookingActivity.this);
                myLayoutManager = new GridLayoutManager(AppointmentBookingActivity.this, 3);
                slotListRecycler.setLayoutManager(myLayoutManager);
                slotListRecycler.setAdapter(timeslotAdapter);

            }

            @Override
            public void onFailure(@NotNull Call<List<Timeslot>> call, @NotNull Throwable t) {
                t.printStackTrace();
            }
        });


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        appUtil.createMenuItems(menuItem, AppointmentBookingActivity.this);
        return true;
    }

    @Override
    public void oncardClick(int position, CardView currentCard, List<CardView> cardList) {
        Log.i("slot on position -- ", position + " " + list.get(position).getSlot());


        for (CardView card : cardList) {
            card.setCardBackgroundColor(Color.parseColor("#ffffff"));
        }
        currentCard.setCardBackgroundColor(Color.parseColor("#bdfff4"));

    }
}

