/**
 * Copyright (C) 2016 NetDragon Websoft Inc.
 */
package com.nd.pad.longimagecompose;

import android.os.FileObserver;

import org.greenrobot.eventbus.EventBus;

/**
 * 由于写入图片有一个时间，所以需要在图片被写完之后再告诉RecyclerView来更新视图
 * 否则视图会更新失败
 *
 * @author hustdhg
 * @since 2016/09/21
 */
public class MyFileObserver extends FileObserver {


    public MyFileObserver(String path){

        super(path);

    }

    @Override
    public void onEvent(int eventtype, String path) {

        if(eventtype==FileObserver.CLOSE_WRITE){
            UIMessage msg=new UIMessage();
            msg.name=path;

            EventBus.getDefault().post(msg);


        }

    }
}