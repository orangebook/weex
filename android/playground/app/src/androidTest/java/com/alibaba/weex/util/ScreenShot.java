package com.alibaba.weex.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Build;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ScrollView;

import com.taobao.weex.ui.view.WXScrollView;
import com.taobao.weex.ui.view.listview.WXRecyclerView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by huabing.thb on 15-10-27.
 */
public  class ScreenShot {

    public  static View rootView;
    public static ArrayList<View> allViews = new ArrayList<View>();
    public static View firstScrollView = null;
    public static View firstListView = null;


    public static View findFirstScrollerByRootView(View  rootView){

        View firstScrollView = null;

        if(null != rootView){
            allViews = ViewUtil.getAllChildViews(rootView);
            for (View view:allViews
                 ) {
                if(view instanceof ScrollView){
                    firstScrollView = view;
                    break;
                }
            }
        }
        return firstScrollView;
    }

    public static View findFirstListByRootView(View  rootView){

        View firstListView = null;

        if(null != rootView){
            allViews = ViewUtil.getAllChildViews(rootView);
            for (View view:allViews
                    ) {
                if(view instanceof RecyclerView){
                    firstListView = view;
                    break;
                }
            }
        }
        return firstListView;
    }

    private static Bitmap doSanpForListOrScroller(View sanpView){

        Bitmap b = null;

        if(sanpView!=null){

            sanpView.setDrawingCacheEnabled(true);
            sanpView.buildDrawingCache();
            Bitmap bitmap = sanpView.getDrawingCache();
            int sanpWidth = sanpView.getWidth();
            Log.e("weex_test", "sanpView.getWidth=="+ sanpWidth);

            int snapHeight = sanpView.getHeight();
            Log.e("weex_test", "sanpView.getHeight==" + snapHeight);


//            int width = activity.getWindowManager().getDefaultDisplay().getWidth();
//            int height = activity.getWindowManager().getDefaultDisplay().getHeight();

            int baseWidth = 750;
            int baseHeight = 1134;

            // 计算缩放因子
//            float heightScale = ((float) baseHeight) / scrollerHeight;
            float widthScale = ((float) baseWidth) / sanpWidth;

            // 新建立矩阵 按照宽度缩放因子自适应缩放
            Matrix matrix = new Matrix();
            matrix.postScale(widthScale, widthScale);

            b = Bitmap.createBitmap(bitmap, 0, 0, sanpWidth, snapHeight, matrix, true);
//            b = Bitmap.createBitmap(bitmap, 0, 0, scrollerWidth,
//                    scrollerHeight, matrix, true);
//            b = Bitmap.createBitmap(bitmap, 0, statusBarHeight + actionBarHeight, width,
//                    height - statusBarHeight - actionBarHeight, matrix, true);

            sanpView.destroyDrawingCache();

        }else {
            Log.e("weex_test", "snapshot view is " + sanpView);
        }
        return b;

    }
    // Gets the Activity screenshots, save to png file
    private static Bitmap takeScreenShot(Activity activity) {

        View view = activity.getWindow().getDecorView();

        rootView = view;
        firstScrollView = findFirstScrollerByRootView(rootView);
        firstListView = findFirstListByRootView(rootView);

        Bitmap snapBitmap = null;
        // 优先scroller ->list->root
        if(null !=firstScrollView ){
            snapBitmap = doSanpForListOrScroller(firstScrollView);
        }
        else {
            Log.e("weex_test", "firstScrollView is " + firstScrollView);
            if (null != firstListView){
                snapBitmap = doSanpForListOrScroller(firstListView);
            }else {
                Log.e("weex_test", "firstListView is " + firstListView);

                if(null != rootView){
//                    snapBitmap = doSanpForListOrScroller(rootView);
                }
            }
        }

        return  snapBitmap;

    }

    private static void savePic(Bitmap b, String strFileName) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(strFileName);
            if (null != fos) {
                b.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveToSD(Bitmap bmp, String dirName,String fileName) throws IOException {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File dir = new File(dirName);
            if(!dir.exists()){
                dir.mkdirs();
            }

            File file = new File(dirName + fileName);
            Log.d("AssetsFileUtil", dirName + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                if (fos != null) {
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    fos.close();
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void shoot(Activity activity, String pathName) throws IOException {

        if(SdCardHelper.isHasSdcard()){
            ScreenShot.saveToSD(ScreenShot.takeScreenShot(activity), SdCardHelper.SDCardRoot + "WeexTest/ScreenShot/", pathName /*+System.currentTimeMillis()*/ + ".png");

        }

    }

//    public static void shootAllViews(Activity activity, String pathName) throws IOException {
//        ScreenShot.saveToSD(ScreenShot.takeScreenShotIncludeDialog(activity, pathName), "sdcard/WeexTest/ScreenShot/", pathName /*+System.currentTimeMillis()*/ + ".png");
//
//    }

    public static void takeScreenShotIncludeDialog(Activity activity, String file){
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {

            File dir = new File(SdCardHelper.SDCardRoot +"WeexTest/ScreenShot/");
            if(!dir.exists()){
                dir.mkdirs();
            }

            File filePic = new File(SdCardHelper.SDCardRoot + "WeexTest/ScreenShot/"+ file + ".png");
            if (!filePic.exists()) {
                try {
                    filePic.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Falcon.takeScreenshot(activity, filePic);

        }

    }

    public static  int getActionBarHeight(Activity activity) {
        int actionBarHeight = 0;
        if(activity.getActionBar()!= null){
            actionBarHeight = activity.getActionBar().getHeight();
        }

        if (actionBarHeight != 0)
            return actionBarHeight;

        final TypedValue tv = new TypedValue();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (activity.getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, tv, true)){
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources().getDisplayMetrics());
                Log.e("actionBarHeight==",actionBarHeight +  "android.support.v7.appcompat.R.attr.actionBarSize");

            }

            else if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)){
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources().getDisplayMetrics());
                Log.e("actionBarHeight==",actionBarHeight +  "actionBarHeight is android.R.attr.actionBarSize");

            }
        }

        return actionBarHeight;
    }

    public  static int getStatusBarHeight1(Activity activity){
        int result = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);

        }
        Log.e("StatusBarHeight==", "StatusBar Height= " + result);

        return result;
    }
    public static  int getStatusBarHeight(Activity activity) {
        int result = 0;
        Rect rect = new Rect();
        Window window = activity.getWindow();
        if (window != null) {
            window.getDecorView().getWindowVisibleDisplayFrame(rect);
            android.view.View v = window.findViewById(Window.ID_ANDROID_CONTENT);

            android.view.Display display = ((android.view.WindowManager) activity.getSystemService(activity.WINDOW_SERVICE)).getDefaultDisplay();

            //return result title bar height
            int result1 = display.getHeight() - v.getBottom() + rect.top;
            int result2  = display.getHeight() - v.getBottom();
            int result3 = v.getTop() - rect.top;
            int result4 = display.getHeight() - v.getHeight();

            Log.e("StatusBarHeight==", "result1== " + result1 +" result2 = " + result2 + "result3=" + result3 + "result4=" +result4  ) ;
        }
        return result;

    }
}
