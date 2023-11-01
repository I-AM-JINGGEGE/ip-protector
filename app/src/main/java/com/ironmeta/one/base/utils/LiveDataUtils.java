package com.ironmeta.one.base.utils;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

public class LiveDataUtils {
    @MainThread
    @NonNull
    public static <X> LiveData<X> copy(@NonNull LiveData<X> source) {
        final MediatorLiveData<X> result = new MediatorLiveData<>();
        result.addSource(source, x -> result.setValue(x));
        return result;
    }
}
