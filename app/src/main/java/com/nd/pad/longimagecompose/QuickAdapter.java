/**
 * Copyright (C) 2016 NetDragon Websoft Inc.
 */
package com.nd.pad.longimagecompose;

import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.squareup.picasso.Picasso;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hustdhg
 * @since 2016/09/21
 */
public class QuickAdapter extends BaseQuickAdapter<String>{


    public QuickAdapter(){
        super(R.layout.rcv_item,null);

    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, String s) {
        String content="还没有名字哦~";
        Pattern pa= Pattern.compile("(.*/)(.*)(.png|.jpg)$");
        Matcher ma=pa.matcher(s);
        if(ma.find()){
            content=ma.group(2);
        }
        baseViewHolder.setText(R.id.tv_content,content);
        Picasso.with(mContext).load("file://"+s).resize(200,200).centerCrop().into((ImageView)baseViewHolder.getView(R.id.iv_long));


    }
}