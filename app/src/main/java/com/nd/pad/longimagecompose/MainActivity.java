package com.nd.pad.longimagecompose;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.Collections;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements FileSystem.OnMakeListener {

    // 制作长图的按钮
    private FloatingActionButton fab_add;

    private Toolbar toolbar;


    /**
     * 和列表视图相关
     */
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private QuickAdapter mAdapter;

    // 显示进度条
    private ProgressDialog mAlertDialog;

    // 监听文件夹变化
    private MyFileObserver myFileObserver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myFileObserver = new MyFileObserver(FileSystem.PATH + File.separator + FileSystem.DIR_NAME);
        myFileObserver.startWatching();

        EventBus.getDefault().register(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // 交互提示dialog//////////////////////////////
        mAlertDialog = new ProgressDialog(MainActivity.this);
        mAlertDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mAlertDialog.setTitle("提示");
        mAlertDialog.setMessage("图片正在制作中，请不要退出应用~");
        mAlertDialog.setIcon(R.drawable.icon);
        mAlertDialog.setCancelable(false);

        Log.d("tag","test");


        //---------         --------
        fab_add = (FloatingActionButton) findViewById(R.id.fab);
        fab_add.setOnClickListener(view -> {
                    ObjectAnimator oba = ObjectAnimator.ofFloat(fab_add, "rotation", 0f, 90f);
                    oba.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
//                             跳转到长图制作的界面
                            MultiImageSelector.create()
                                    .showCamera(false)
                                    .start(MainActivity.this, 474);
                        }
                    });
                    oba.start();
                }
        );


        // 列表初始化
        mRecyclerView = (RecyclerView) findViewById(R.id.rv);
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mAdapter = new QuickAdapter();
        mAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_LEFT);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);



        //---------   图片点击事件，图片长按事件~    --------
        mRecyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void SimpleOnItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + baseQuickAdapter.getItem(i)), "image/*");
                startActivity(intent);


            }
        });

        loadData();

        mRecyclerView.addOnItemTouchListener(new OnItemLongClickListener() {
            @Override
            public void SimpleOnItemLongClick(final BaseQuickAdapter baseQuickAdapter, View view, int i) {


                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.note)
                        .setMessage(R.string.msg)
                        .setIcon(R.drawable.icon)
                        .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // 本地文件中删除
                                if (baseQuickAdapter.getItemCount() > i) {
                                    File file = new File((String) baseQuickAdapter.getItem(i));
                                    if (file.exists()) {
                                        file.delete();
                                    }
                                }
                                // 长按触发删除操作
                                // recyclerview中删除
                                baseQuickAdapter.remove(i);

                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .create();
                dialog.show();


            }
        });
    }


    @Override
    public void startMake() {
        // 开始制作
        Toast.makeText(MainActivity.this, "start make", Toast.LENGTH_SHORT).show();
        mAlertDialog.show();


    }

    /**
     *加载数据
     *
     */
    private void loadData(){
        // 从本地文件夹加载图片并且显示出来(本地图片的名字就是要在底部显示的内容)
        Observable.just(1)
                .map(new Func1<Integer, List<String>>() {

                    @Override
                    public List<String> call(Integer integer) {
                        FileSystem fs = FileSystem.getInstance();
                        List<String> path = fs.getAllImgs();
                        Collections.reverse(path);
                        return path;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<String>>() {
                    @Override
                    public void call(List<String> strings) {
                        mAdapter.setNewData(strings);
                    }
                });


    }

    @Override
    public void endMake(String path) {
//         制作完成

        loadData();
        mAlertDialog.setMessage("图片制作好了~");
        mAlertDialog.dismiss();

        Toast.makeText(MainActivity.this, R.string.success, Toast.LENGTH_SHORT).show();

    }

    // 拦截退出按钮
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                    .setIcon(R.drawable.icon)
                    .setTitle(R.string.note)
                    .setMessage(R.string.exit)
                    .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .create();
            alert.show();


        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 474 && resultCode == RESULT_OK) {
            List<String> paths = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
            FileSystem.getInstance().setMakeListener(this);
            FileSystem.getInstance().composeImages(paths);
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UIMessage msg) {
        endMake(msg.name);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myFileObserver.stopWatching();
    }
}
