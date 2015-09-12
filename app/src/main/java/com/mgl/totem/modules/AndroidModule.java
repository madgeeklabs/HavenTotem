package com.mgl.totem.modules;

/**
 * Created by goofyahead on 9/8/14.
 */

import android.app.DownloadManager;
import android.content.Context;

import com.mgl.totem.activities.MainActivity;
import com.mgl.totem.activities.RegisterActivity;
import com.mgl.totem.activities.StartActivity;
import com.mgl.totem.interfaces.TotemApiInterface;
import com.mgl.totem.utils.Constants;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RestAdapter;

@Module(injects = {
        MainActivity.class,
        StartActivity.class,
        RegisterActivity.class
},
        library = true)

public class AndroidModule {
    private final Context mContext;

    public AndroidModule(Context context) {
        this.mContext = context;
    }

    /**
     * Allow the application context to be injected but require that it be annotated with
     * {@link @Annotation} to explicitly differentiate it from an activity context.
     */
    @Provides
    @Singleton
    Context provideApplicationContext() {
        return mContext;
    }

//    @Provides
//    @Singleton
//    RequestQueue provideQueue() {
//        return Volley.newRequestQueue(mContext);
//    }

//    @Provides
//    @Singleton
//    ImageLoader provideImageLoader(RequestQueue mRequestQueue) {
//        return new ImageLoader(mRequestQueue, new BitmapLruCache(50));
//    }

//    @Provides
//    @Singleton
//    KaprikaSharedPrefs provideSharedPrefs() {
//        return new KaprikaSharedPrefs(mContext);
//    }


    @Provides
    @Singleton
    DownloadManager provideDownloadManager() {
        return (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    @Provides
    @Singleton
    RestAdapter provideRestAdapter() {
        return new RestAdapter.Builder()
                .setEndpoint(Constants.SERVER_URL)
                .build();
    }

    @Provides
    @Singleton
    TotemApiInterface provideApi() {
        return provideRestAdapter().create(TotemApiInterface.class);
    }
}