package com.example.minitwitter.data;

import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.example.minitwitter.common.Constantes;
import com.example.minitwitter.common.MyApp;
import com.example.minitwitter.common.SharedPreferenceSManager;
import com.example.minitwitter.retrofit.AuthTwitterService;
import com.example.minitwitter.retrofit.request.RequestCreateTweet;
import com.example.minitwitter.retrofit.response.AuthTwitterClient;
import com.example.minitwitter.retrofit.response.Like;
import com.example.minitwitter.retrofit.response.Tweet;
import com.example.minitwitter.retrofit.response.TweetDelete;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TweetRepository {

    AuthTwitterService authTwitterService;
    AuthTwitterClient authTwitterClient;
    MutableLiveData<List<Tweet>> allTweets;
    MutableLiveData<List<Tweet>> favTweets;
    String userName;

    TweetRepository(){
        authTwitterClient = AuthTwitterClient.getInstance();
        authTwitterService = authTwitterClient.getAuthTwitterService();
        allTweets = getAllTweets();
        userName = SharedPreferenceSManager.getSomeStringValue(Constantes.PREF_USERNAME);
    }

    public MutableLiveData<List<Tweet>> getAllTweets() {

        if (allTweets == null){
            allTweets = new MutableLiveData<>();
        }

        Call<List<Tweet>> call = authTwitterService.getAllTweets();
        call.enqueue(new Callback<List<Tweet>>() {
            @Override
            public void onResponse(Call<List<Tweet>> call, Response<List<Tweet>> response) {
                if (response.isSuccessful()) {
                    allTweets.setValue(response.body());
                } else {
                    Toast.makeText(MyApp.getContext(), "Algo esta mal", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Tweet>> call, Throwable t) {
                Toast.makeText(MyApp.getContext(), "Error de conexion", Toast.LENGTH_SHORT).show();
            }
        });
        return allTweets;
    }

     public  MutableLiveData<List<Tweet>> getFavTweets(){
        if (favTweets == null) {
            favTweets = new MutableLiveData<>();
        }

        List<Tweet> newFavList = new ArrayList<>();
         Iterator itTweets = allTweets.getValue().iterator();

         while (itTweets.hasNext()) {
             Tweet current = (Tweet) itTweets.next();
             Iterator itLikes = current.getLikes().iterator();
             boolean enc = false;
             while (itLikes.hasNext() && !enc) {
                 Like like = (Like)itLikes.next();
                 if (like.getUsername().equals(userName)) {
                     enc = true;
                     newFavList.add(current);
                 }
             }
         }

         favTweets.setValue(newFavList);
         return favTweets;
     }

     public void createTweet(String mensaje) {
        RequestCreateTweet requestCreateTweet = new RequestCreateTweet(mensaje);
        Call<Tweet> call = authTwitterService.createTweet(requestCreateTweet);

        call.enqueue(new Callback<Tweet>() {
            @Override
            public void onResponse(Call<Tweet> call, Response<Tweet> response) {
                if (response.isSuccessful()){
                    List<Tweet> listaClonada = new ArrayList<>();
                    //Añadimos en primer lugar el nuevo tweet que nos llega del server
                    listaClonada.add(response.body());
                    for (int i = 0; i < allTweets.getValue().size(); i++) {
                        listaClonada.add(new Tweet(allTweets.getValue().get(i)));
                    }
                    allTweets.setValue(listaClonada);
                } else {
                    Toast.makeText(MyApp.getContext(), "Algo quedo mal, intentelo de nuevo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Tweet> call, Throwable t) {
                Toast.makeText(MyApp.getContext(), "Error en la conexion, intentelo de nuevo", Toast.LENGTH_SHORT).show();
            }
        });
      }

      public void deleteTweet(final int idTweet){
            Call<TweetDelete> call = authTwitterService.deleteTweet(idTweet);

            call.enqueue(new Callback<TweetDelete>() {
                @Override
                public void onResponse(Call<TweetDelete> call, Response<TweetDelete> response) {
                    if (response.isSuccessful()) {
                        List<Tweet> clonedTweets = new ArrayList<>();
                        for (int i=0; i<allTweets.getValue().size(); i++) {
                            if (allTweets.getValue().get(i).getId() != idTweet) {
                                clonedTweets.add(new Tweet(allTweets.getValue().get(i)));
                            }
                        }

                        allTweets.setValue(clonedTweets);
                        getFavTweets();
                    } else {
                        Toast.makeText(MyApp.getContext(), "Algo quedo mal, intentelo de nuevo", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<TweetDelete> call, Throwable t) {
                    Toast.makeText(MyApp.getContext(), "Error en la conexion, intentelo de nuevo", Toast.LENGTH_SHORT).show();
                }
            });
    }

    public void likeTweet(int idTweet) {
        Call<Tweet> call = authTwitterService.likeTweet(idTweet);

        call.enqueue(new Callback<Tweet>() {
            @Override
            public void onResponse(Call<Tweet> call, Response<Tweet> response) {
                if (response.isSuccessful()){
                    List<Tweet> listaClonada = new ArrayList<>();

                    for (int i = 0; i < allTweets.getValue().size(); i++) {
                        if (allTweets.getValue().get(i).getId() == idTweet) {
                            /* si encuentra en la lista el elemento sobre el que se le da like
                            * introduce el elemento que llega del servidor*/
                            listaClonada.add(response.body());
                        } else {
                            listaClonada.add(new Tweet(allTweets.getValue().get(i)));
                        }
                    }

                    allTweets.setValue(listaClonada);
                    getFavTweets();
                } else {
                    Toast.makeText(MyApp.getContext(), "Algo quedo mal, intentelo de nuevo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Tweet> call, Throwable t) {
                Toast.makeText(MyApp.getContext(), "Error en la conexion, intentelo de nuevo", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
