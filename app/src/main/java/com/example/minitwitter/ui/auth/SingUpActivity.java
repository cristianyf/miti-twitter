package com.example.minitwitter.ui.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.minitwitter.R;
import com.example.minitwitter.common.Constantes;
import com.example.minitwitter.common.SharedPreferenceSManager;
import com.example.minitwitter.retrofit.request.RequestSignup;
import com.example.minitwitter.retrofit.response.ResponseAuth;
import com.example.minitwitter.retrofit.MiniTwitterClient;
import com.example.minitwitter.retrofit.MiniTwitterService;
import com.example.minitwitter.ui.DashboardActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SingUpActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnSignUp;
    TextView tvGoLogin;
    EditText etEmail, etPassword, etUserName;

    MiniTwitterClient miniTwitterClient;
    MiniTwitterService miniTwitterService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);

        //ocultar el toolbar
        getSupportActionBar().hide();

        retrofitInit();
        findViews();
        events();
    }

    private void retrofitInit() {
        miniTwitterClient = MiniTwitterClient.getInstance();
        miniTwitterService = miniTwitterClient.getMiniTwitterService();
    }

    private void findViews() {
        btnSignUp = findViewById(R.id.btnRegistrar);
        tvGoLogin = findViewById(R.id.tvGoSignUpRe);
        etEmail = findViewById(R.id.etEmailRe);
        etPassword = findViewById(R.id.etPasswordRe);
        etUserName = findViewById(R.id.etNombreRe);
    }

    private void events() {
        btnSignUp.setOnClickListener(this);
        tvGoLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.btnRegistrar:
                goToSignUp();
                break;
            case R.id.tvGoSignUpRe:
                toToLogin();
                break;

        }
    }

    private void goToSignUp() {
        String userName = etUserName.getText().toString();
        String password = etPassword.getText().toString();
        String email = etEmail.getText().toString();

        if (userName.isEmpty() ){
            etUserName.setError("El nombre de usuario es requerido");
        }else if (password.isEmpty() || userName.length() < 4){
            etPassword.setError("La contraseña es requerida");
        }else if (email.isEmpty()){
            etEmail.setError("El email es requerido");
        }else{
            String code ="UDEMYANDROID";
            RequestSignup requestSignup = new RequestSignup(userName, email, password, code);
            Call<ResponseAuth> call = miniTwitterService.doSignUp(requestSignup);

            call.enqueue(new Callback<ResponseAuth>() {
                @Override
                public void onResponse(Call<ResponseAuth> call, Response<ResponseAuth> response) {
                    if (response.isSuccessful()){


                        SharedPreferenceSManager.setSomeStringValue(Constantes.PREF_TOKEN, response.body().getToken());
                        SharedPreferenceSManager.setSomeStringValue(Constantes.PREF_USERNAME, response.body().getUsername());
                        SharedPreferenceSManager.setSomeStringValue(Constantes.PRE_EMAIL, response.body().getEmail());
                        SharedPreferenceSManager.setSomeStringValue(Constantes.PREF_PHOTOURL, response.body().getPhotoUrl());
                        SharedPreferenceSManager.setSomeStringValue(Constantes.PREF_CREATED, response.body().getCreated());
                        SharedPreferenceSManager.setSomeBooleanValue(Constantes.PREF_ACTIVE, response.body().getActive());

                        Intent i = new Intent(SingUpActivity.this, DashboardActivity.class);
                        startActivity(i);
                    }else {
                        Toast.makeText(SingUpActivity.this, "Algo ha ido mal, revise los datos", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseAuth> call, Throwable t) {
                    Toast.makeText(SingUpActivity.this, "Error en la conexion intentelo de nuevo", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void toToLogin() {
        Intent i = new Intent(SingUpActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}