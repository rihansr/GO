package com.go.driver.remote;

import com.go.driver.model.notification.FCMResponse;
import com.go.driver.model.notification.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by rsr23 on 8/2/2018.
 */

public interface IFCMService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAGDPvGJY:APA91bG2Du1IN-6OexOwMCtXXqQT-c49kV4tJD3lDEUOcb-EjLc3lgati8iedaJPQIrIuZnWnSjoLrzZBNf-vpQmgYD_kOpGboCV0xgYwa9WbSoFxB_31-iyI0boGKh29DUAXgWaEDf4"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendNotification(@Body Sender body);
}
