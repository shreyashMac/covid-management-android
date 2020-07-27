package com.example.covid_management_android.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covid_management_android.R;
import com.example.covid_management_android.activity.userActivity.QuestionActivity;
import com.example.covid_management_android.activity.userActivity.UserProfileActivity;
import com.example.covid_management_android.model.CovidQuestionResult;
import com.example.covid_management_android.model.QAnswerOption;
import com.example.covid_management_android.model.Question;
import com.example.covid_management_android.model.User;
import com.example.covid_management_android.model.UserAnswerResponse;

import com.example.covid_management_android.service.UserClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Header;

import static android.content.Context.MODE_PRIVATE;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    List<Question> myQuestions;
    Context context;
    HashMap<Integer, Integer> myResponses;
    JSONArray filledResponses;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Button myResponseButton;
    RetrofitUtil retrofitUtil;
    Retrofit retrofit;
    UserClient userClient;


    public QuestionAdapter(List<Question> myQuestions, Context mycontext, SharedPreferences sharedPreferences, Button myResponseButton, SharedPreferences.Editor editor, JSONArray list) {
        this.myQuestions = myQuestions;
        this.context = mycontext;
        this.sharedPreferences = sharedPreferences;
        this.myResponseButton = myResponseButton;
        this.editor = editor;
        this.filledResponses = list;
        this.myResponses = new HashMap<>();
    }


    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View myview = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_question, parent, false);
        QuestionViewHolder myviewholder = new QuestionViewHolder(myview);
        return myviewholder;
    }

    @Override
    public void onBindViewHolder(@NonNull final QuestionViewHolder holder, final int position) {
        List<QAnswerOption> optionList = myQuestions.get(position).getQAnswerOptions();
        holder.myquestion.setText(myQuestions.get(position).getQuestion());
        final int count = myQuestions.get(position).getQAnswerOptions().size();
        for (int i = 0; i < count; i++) {
            QAnswerOption currentOption = optionList.get(i);
            RadioButton r1 = new RadioButton(context);
            r1.setText(currentOption.getOptionContent());
            holder.mylayout.removeAllViews();
            holder.myoptions.addView(r1);
            try {
                for(int j = 0; j <filledResponses.length(); j++){
                    JSONObject currentResp = filledResponses.getJSONObject(j);
                    if (currentResp.get("optionId")  == currentOption.getId()) {
                        r1.setChecked(true);
                        holder.myquestion.setTag( currentResp.get("id"));
                    }
                }
            } catch (Exception e) {
                Log.i("Error", e.getMessage());
            }
            holder.mylayout.addView(holder.myoptions);
        }
        holder.myoptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                Integer n = checkedId;
                Log.i("MycheckedId", n.toString());
                RadioButton rb = group.findViewById(checkedId);
                Integer optionId = 0;
                Integer questionId = 0;
                if (rb.isChecked()) {
                    questionId = myQuestions.get(position).getId();
                    String mySelectedoption = rb.getText().toString();
                    for (Question option : myQuestions) {
                        for (QAnswerOption myoption : option.getQAnswerOptions()) {
                            if (myoption.getOptionContent().contains(mySelectedoption)) {
                                optionId = myoption.getId();
                            }
                        }
                    }
                    Log.i("My radio button", rb.getText().toString() + myQuestions.get(position).getQuestion() + " " + optionId.toString());
                    if(filledResponses.length() > 0){
                       myResponses.put(Integer.parseInt(holder.myquestion.getTag().toString()),optionId);

                    }else {
                        myResponses.put(questionId, optionId);
                    }
                    Integer m = sharedPreferences.getInt("userId", 1);
                    Log.i("MyResponses", myResponses.toString());
                }
            }

        });

        myResponseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                retrofitUtil = new RetrofitUtil("http://10.0.2.2:5050/api/v1/user/questionResponse/");
                //retrofitUtil = new RetrofitUtil("http://192.168.0.105:5050/api/v1/user/questionResponse/");
                retrofit = retrofitUtil.getRetrofit();
                userClient = retrofit.create(UserClient.class);
                retrofitUtil.setContext(context);
                String token = sharedPreferences.getString("token", null);
                String refreshToken = sharedPreferences.getString("refreshToken", null);
                if(filledResponses.length()>0)
                {
                   updateUserResponses(token,refreshToken);
                }
                else
                {
                    createUserResponse(token,refreshToken);
                }
            }
        });

    }

    private void createUserResponse(String token,String refreshToken) {

        Toast.makeText(context, "Hello there", Toast.LENGTH_LONG).show();
        UserAnswerResponse myUserResponse = new UserAnswerResponse();
        Integer userId = sharedPreferences.getInt("userId", 1);
        myUserResponse.setUserId(userId);
      //  myUserResponse.setUserAnswers(myResponses);
        if(token.split("JWT ").length ==1){
             token = "JWT "+token;
        }
        Call<CovidQuestionResult> call = userClient.createReport(token,refreshToken,myUserResponse);
        call.enqueue(new Callback<CovidQuestionResult>() {
            @Override
            public void onResponse(Call<CovidQuestionResult> call, Response<CovidQuestionResult> response) {
                Log.i("My Response", response.body().getResult());
                Toast.makeText(context, "Survey Submitted", Toast.LENGTH_LONG).show();
                // To Raise a flag ,indicating user has submitted the questionnaire...
                editor.putInt("QuestionnaireSubmission", 1);
                editor.apply();

                if (response.body().getResult().contains("Positive")) {
                    AppUtil appUtil = new AppUtil();
                    appUtil.diplayAlert(context,"hello");
                }

            }

            @Override
            public void onFailure(Call<CovidQuestionResult> call, Throwable t) {
                Log.i("My Response", t.getMessage());
            }
        });
    }

    private void updateUserResponses(String token,String refreshToken) {
        UserAnswerResponse myUserResponse = new UserAnswerResponse();
        Integer userId = sharedPreferences.getInt("userId", 1);
        myUserResponse.setUserId(userId);
        myUserResponse.setUserAnswers(myResponses);

        if(token.split("JWT ").length ==1){
            token = "JWT "+token;
        }

        Call<CovidQuestionResult> call  = userClient.updateUserQuestionnarire(token,refreshToken,myUserResponse);
        call.enqueue(new Callback<CovidQuestionResult>() {
            @Override
            public void onResponse(Call<CovidQuestionResult> call, Response<CovidQuestionResult> response) {
                if(response.isSuccessful())
                {
                   String myResponse = response.body().getResult();
                   AppUtil appUtil = new AppUtil();
                   appUtil.diplayAlert(context,myResponse);

                }
            }

            @Override
            public void onFailure(Call<CovidQuestionResult> call, Throwable t) {

                Log.i("Failure", t.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return myQuestions.size();
    }

    public static class QuestionViewHolder extends RecyclerView.ViewHolder {

        public TextView myquestion;
        public RadioGroup myoptions;
        public RadioButton option1;
        public LinearLayout mylayout;

        public QuestionViewHolder(View itemView) {

            super(itemView);
            myquestion = itemView.findViewById(R.id.questionId);
            myoptions = itemView.findViewById(R.id.myradiooptionsgroup);
            mylayout = itemView.findViewById(R.id.myoptionslayout);

        }

    }

}
