package com.example.hiichat.Notification;


import com.google.firebase.messaging.FirebaseMessaging;

public class FCMInit {

   FirebaseMessaging firebaseMessaging;

    public FCMInit() {
        this.firebaseMessaging = FirebaseMessaging.getInstance();
    }

    private static FCMInit instance;

    public static FCMInit getInstance() {
        return instance;
    }

    public static void setInstance(FCMInit instance) {
        FCMInit.instance = instance;
    }

    public FirebaseMessaging getFirebaseMessaging() {
        return firebaseMessaging;
    }

    public static void initialize() {
        new FCMInit();
    }

    public static FCMInit get() {
        if (getInstance() == null) {
            return new FCMInit();
        }
        return getInstance();
    }
}
