package com.example.eventsnapqr;

import androidx.test.espresso.IdlingResource;

import java.util.concurrent.atomic.AtomicBoolean;

public class FirebaseIdlingResource implements IdlingResource {

    private final AtomicBoolean isIdleNow = new AtomicBoolean(true);
    private volatile ResourceCallback resourceCallback;

    @Override
    public String getName() {
        return FirebaseIdlingResource.class.getName();
    }

    @Override
    public boolean isIdleNow() {
        return isIdleNow.get();
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        this.resourceCallback = callback;
    }

    public void setIdleState(boolean isIdle) {
        isIdleNow.set(isIdle);
        if (isIdle && resourceCallback != null) {
            resourceCallback.onTransitionToIdle();
        }
    }
}
