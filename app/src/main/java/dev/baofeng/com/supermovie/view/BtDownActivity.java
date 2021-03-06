package dev.baofeng.com.supermovie.view;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;
import com.xunlei.downloadlib.XLTaskHelper;
import com.xunlei.downloadlib.parameter.XLTaskInfo;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.baofeng.com.supermovie.MyApp;
import dev.baofeng.com.supermovie.R;
import dev.baofeng.com.supermovie.adapter.BtDownAdapter;
import dev.baofeng.com.supermovie.bt.BTDownloadTask;
import dev.baofeng.com.supermovie.bt.ComDownloadTask;
import dev.baofeng.com.supermovie.bt.ThreadUtils;
import dev.baofeng.com.supermovie.domain.BTParamInfo;
import dev.baofeng.com.supermovie.domain.BtInfo;
import dev.baofeng.com.supermovie.domain.MovieInfo;
import dev.baofeng.com.supermovie.domain.TaskInfo;
import dev.baofeng.com.supermovie.presenter.DownBtPresenter;
import dev.baofeng.com.supermovie.presenter.GetRecpresenter;
import dev.baofeng.com.supermovie.presenter.iview.IBtView;
import dev.baofeng.com.supermovie.presenter.iview.IMoview;
import dev.baofeng.com.supermovie.utils.BlurUtil;

/**
 * Created by huangyong on 2018/2/12.
 */

public class BtDownActivity extends AppCompatActivity implements IMoview, IBtView {

    @BindView(R.id.poster)
    ImageView poster;
    @BindView(R.id.mvname)
    TextView mvname;
    @BindView(R.id.btdownrv)
    RecyclerView btdownrv;
    @BindView(R.id.detail_img_bg)
    ImageView detailImgBg;
    private String pathurl;
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                long taskId = (long) msg.obj;
                XLTaskInfo taskInfo = XLTaskHelper.instance(getApplicationContext()).getTaskInfo(taskId);
                String a = "fileSize:" + convertFileSize(taskInfo.mFileSize)
                        + "\n"+ " downSize:" + convertFileSize(taskInfo.mDownloadSize)
                        + "\n"+ " speed:" + convertFileSize(taskInfo.mDownloadSpeed)
                        +  "\n"+"/s dcdnSoeed:" + convertFileSize(taskInfo.mAdditionalResDCDNSpeed)
                        + "\n"+ "/s filePath:" + "/sdcard/" + XLTaskHelper.instance(getApplicationContext()).getFileName(pathurl);
                List<TaskInfo> taskInfos = DataSupport.where("action=?", taskId + "").find(TaskInfo.class);
                TaskInfo info = taskInfos.get(0);
                info.setProgress(Integer.parseInt(convertFileSize(taskInfo.mDownloadSize))/Integer.parseInt(convertFileSize(taskInfo.mFileSize))*100);
                info.save();
                handler.sendMessageDelayed(handler.obtainMessage(0, taskId), 1000);
            }
        }
    };
    private DownBtPresenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_downlayout);
        ButterKnife.bind(this);
        presenter = new DownBtPresenter(this,this);
        GetRecpresenter getRecpresenter = new GetRecpresenter(this, this);
        String title = getIntent().getStringExtra(GlobalMsg.KEY_MOVIE_TITLE);
        String posterurl = getIntent().getStringExtra(GlobalMsg.KEY_POST_IMG);
        mvname.setText(title);

        getRecpresenter.getBtDetail(title);//获取详情
        Glide.with(this).load(posterurl).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                BlurUtil.setViewBg(4, 3, detailImgBg, resource);
            }
        });
        Glide.with(this).load(posterurl).into(poster);
    }



    @Override
    public void loadData(MovieInfo info) {

    }

    @Override
    public void loadError(String msg) {

    }

    @Override
    public void loadMore(MovieInfo result) {

    }

    @Override
    public void loadBtData(MovieInfo result) {

    }

    @Override
    public void loadDetail(BtInfo result) {
        BtDownAdapter adapter = new BtDownAdapter(this,result);
        adapter.setOnItemClickListener(new BtDownAdapter.onItemClick() {
            @Override
            public void onItemclicks(int position, String url) {
                try {
                   /* pathurl = url;
                    long taskId = 0;
                    try {
                        taskId = XLTaskHelper.instance(getApplicationContext()).addThunderTask(url, "/sdcard/", null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    handler.sendMessage(handler.obtainMessage(0, taskId));*/
                    BTParamInfo btParamInfo = getParams(url);
                    String id = btParamInfo.getData().get(1);
                    String action = btParamInfo.getData().get(0);
                    String uhash = btParamInfo.getData().get(2);
                    String path = btParamInfo.getData().get(3);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            presenter.getFile(id,action,uhash,path);
                        }
                    }).start();
                    Toast.makeText(BtDownActivity.this, "已添加到下载队列", Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
        btdownrv.setLayoutManager(new LinearLayoutManager(this));
        btdownrv.setAdapter(adapter);
    }
    public BTParamInfo getParams(String param){
        Gson gson = new Gson();
        String json = "{\"data\": "+param+"}";
        BTParamInfo info = gson.fromJson(json,BTParamInfo.class);
        String de = "";
        return info;
    }
    private File dir;
    private File getDir(){
        if (dir!=null && dir.exists()){
            return dir;
        }

        dir = new File(getExternalCacheDir(), "download");
        if (!dir.exists()){
            dir.mkdirs();
        }
        return dir;
    }
    public static String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f M" : "%.1f M", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f K" : "%.1f K", f);
        } else
            return String.format("%d B", size);
    }

    @Override
    public void onDownSuccess(String path) {
        File file = new File(path);
        if (file.exists()){
            TaskInfo info = new TaskInfo();
            info.setProgress(0);
            info.setAction(GlobalMsg.ACTION);
            info.setDownSize("0");
            info.setFileSize("0");
            info.setPath(path);
            ComDownloadTask task = new ComDownloadTask(BtDownActivity.this,info.getPath());
            ThreadUtils.execute(task);
        }

    }
}
