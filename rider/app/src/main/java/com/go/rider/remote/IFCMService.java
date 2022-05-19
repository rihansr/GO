package com.go.rider.remote;

import com.go.rider.model.notification.FCMResponse;
import com.go.rider.model.notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAGDPvGJY:APA91bG2Du1IN-6OexOwMCtXXqQT-c49kV4tJD3lDEUOcb-EjLc3lgati8iedaJPQIrIuZnWnSjoLrzZBNf-vpQmgYD_kOpGboCV0xgYwa9WbSoFxB_31-iyI0boGKh29DUAXgWaEDf4"
    })

    @POST("fcm/send")
    Call<FCMResponse> sendNotification(@Body Sender body);
}
