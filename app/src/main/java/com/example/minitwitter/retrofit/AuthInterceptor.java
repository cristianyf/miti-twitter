package com.example.minitwitter.retrofit;

import com.example.minitwitter.common.Constantes;
import com.example.minitwitter.common.SharedPreferenceSManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        String token = SharedPreferenceSManager.getSomeStringValue(Constantes.PREF_TOKEN);
        Request request = chain.request().newBuilder().addHeader("Authorization", "Bearer " + token).build();
        return  chain.proceed(request);
    }
}
