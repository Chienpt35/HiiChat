package com.example.hiichat.Service;

import com.example.hiichat.Notification.MyResponse;
import com.example.hiichat.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAOD4J4-8:APA91bEgomhUvT7H65SzmXHCTM1q9zCgh6xdTM5_aAWz0b84CUSxCGjAFRMv9IFtVdwFwR7DmML4I8694iqjoFSUFK9PFcs-cC6kQV-x9j9g4NQkVJzd6prJkYgcLJ4fJGQbbYU7Oi43"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
