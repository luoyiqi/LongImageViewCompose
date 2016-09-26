/**
 * Copyright (C) 2016 NetDragon Websoft Inc.
 */
package com.nd.pad.longimagecompose;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 该类用于处理文件系统和图片交互
 *
 *
 * 使用inSampleSize来缩放十分不可靠！！
 *
 * @author hustdhg
 * @since 2016/09/21
 */
public class FileSystem {

    private OnMakeListener listener;

    private static FileSystem dot;

    public static final String PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

    public static final String DIR_NAME="longimage";

    private FileSystem() {


    }

    public static FileSystem getInstance() {

        if (dot == null) {

            synchronized (FileSystem.class) {

                if (dot == null) {
                    dot = new FileSystem();
                }
            }
        }

        return dot;

    }

    /**
     * 获得当前目录下所有图片的路径
     *
     * @return 图片路径的集合
     */
    public List<String> getAllImgs() {
        List<String> list = new ArrayList<>();

        File file = new File(PATH, "longimage");
        if (!file.exists()) {
            file.mkdirs();
        }
        File[] files = file.listFiles();

        for (File f : files) {
            if (!f.isDirectory() && (f.getName().endsWith(".jpg") || f.getName().endsWith(".png"))) {
                list.add(f.getAbsolutePath());

            }

        }

        return list;
    }

    /**
     * 合并图片
     *
     * @param paths
     */
    public void composeImages(List<String> paths) {
        if (listener != null) {
            listener.startMake();
        }
        //---------   开始制作  --------
        /**
         *
         * 图片越多，那么图片质量就越低~
         *
         * 3张以下就原比例
         *
         * 6张一下就1280
         *
         * 6张以上就720
         *
         */
        final float w = getWidth(paths);
        Log.d("this is log", w + "");
        int height = 0;


        new Thread(() -> {


            /// 把bitmap转换成目标大小
            List<Bitmap> targets = new ArrayList<>();
            for (int i = 0; i < paths.size(); i++) {
                BitmapFactory.Options op = new BitmapFactory.Options();
                op.inJustDecodeBounds = true;
                Bitmap bt = BitmapFactory.decodeFile(paths.get(i), op);
                int temp_width=op.outWidth;
                int temp_height=op.outHeight;
                float scale = temp_width * 1.0f / w;
                op.inJustDecodeBounds=false;
                Bitmap temp=getScaledBitmap(BitmapFactory.decodeFile(paths.get(i),op),(int)w,(int)(w*temp_height/temp_width));
                targets.add(temp);
            }


            int temp_height=0;// 计数高度
            int total_height=0; //总高度
            for(int i=0;i<targets.size();i++){
                total_height+=targets.get(i).getHeight();
            }
            Log.d("tag","总height为"+total_height);
            Bitmap final_bitmap=Bitmap.createBitmap((int)w,total_height, Bitmap.Config.RGB_565);
            Canvas canvas=new Canvas(final_bitmap);
            for(int j=0;j<targets.size();j++){
                Bitmap n=targets.get(j);
                canvas.drawBitmap(n,0,temp_height,null);
                temp_height+=n.getHeight();
            }
            canvas.save(Canvas.ALL_SAVE_FLAG);
            for(Bitmap b:targets){
                b.recycle();
            }


            // 保存到本地，并且更新recyclerViewd视图
            File file_dir=new File(PATH,DIR_NAME);
            final String name= System.currentTimeMillis()+new Random().nextInt(100)+".jpg";
            File newfile=new File(file_dir,name);
            if(!newfile.exists()){
                try {
                    BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(newfile));
                    final_bitmap.compress(Bitmap.CompressFormat.JPEG,30,bos);
                    bos.flush();
                    bos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }catch (IOException e ){
                    e.printStackTrace();
                }
            }



        }).start();






    }

    private Bitmap getScaledBitmap(Bitmap b,int dstWidth,int dstHeight){
        return Bitmap.createScaledBitmap(b,dstWidth,dstHeight,false);

    }


    private float getWidth(List<String> paths) {

        if (paths.size() <= 4) {
            BitmapFactory.Options bo = new BitmapFactory.Options();
            bo.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(paths.get(0), bo);
            return bo.outWidth;

        } else if (paths.size() <= 7) {
            return 1280;

        } else {
            return 720;

        }

    }


    public void setMakeListener(OnMakeListener listener) {
        this.listener = listener;
    }


    public interface OnMakeListener {
        void startMake();

        void endMake(String path);

    }

}