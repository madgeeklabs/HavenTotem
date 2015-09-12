package com.mgl.totem.base;

import android.app.Application;
import android.content.Context;

import com.mgl.totem.modules.AndroidModule;
import com.mgl.totem.modules.CustomModule;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;

/**
 * Created by goofyahead on 9/12/15.
 */
public class TotemApplication extends Application{
    private ObjectGraph graph;
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        graph = ObjectGraph.create(getModules().toArray());
    }

    public static Context getAppContext() {
        return mContext;
    }

    protected List<Object> getModules() {
        return Arrays.asList(
                new AndroidModule(this),
                new CustomModule()// you can add more modules here
        );
    }

    public void inject(Object object) {
        graph.inject(object);
    }
}
