package com.go.driver.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by rsr23 on 8/2/2018.
 */

public class FCMClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient(String fcmURL){

        if(retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(fcmURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
