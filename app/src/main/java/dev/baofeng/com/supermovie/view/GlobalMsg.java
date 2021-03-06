package dev.baofeng.com.supermovie.view;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import dev.baofeng.com.supermovie.domain.RunTaskInfo;

/**
 * Created by huangyong on 2018/1/29.
 */

public class GlobalMsg {
    public static final String KEY_DOWN_URL = "DOWN_URL";

    public static final String KEY_POST_IMG = "POST_IMG";

    public static final String KEY_MOVIE_TITLE ="MOVIE_TITLE";

    public static final int ITEM_TYPE_1 =1;
    public static final int ITEM_TYPE_2=2;
    public static final int ITEM_TYPE_3 =3;
    public static final String TASKID = "TASKID";
    public static final String REFRESH = "REFRESH";
    public static boolean ISSHOW = false;

    public static Queue<String> downQueue;//准备下载队列
    public static Queue<String> doneQueue;//已下载队列

    public static String ACTION = "ACTION";
    public static String ACTIONWAIT = "WAITE";
    public static DownloadService service =null;

    public static ArrayList<RunTaskInfo> runInfos = new ArrayList<>();//正在下载的队列，关联了线程池
}
