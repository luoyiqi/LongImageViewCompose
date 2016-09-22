/**
 * Copyright (C) 2016 NetDragon Websoft Inc.
 */
package com.nd.pad.longimagecompose;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * @author hustdhg
 * @since 2016/09/21
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }
}