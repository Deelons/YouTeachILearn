/**
 * 文件名：MyScrollView
 * 描 述：自定义ScrollView的子类，实现ScrollView附加其他功能
 * 作 者：DJZ
 * 时 间：2015-11-23
 */

package com.netease.nim.demo.main;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.netease.nim.demo.R;
import com.netease.nim.demo.main.activity.DiscoverdetailActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Admin on 2015/11/15.
 */

public class  MyScrollView extends ScrollView implements View.OnTouchListener {
    public static final int PAGE_SIZE = 15; //每页设定加载的最大图片数量
    public static int page; //记录当前加载的页数
    private int columnWidth; //每一列的宽度
    private int firstColumnHeight; //第一列的高度
    private int secondColumnHeight; //第二列的高度
    private int thirdColumnHeight; //第三列的高度
    private boolean loadOnce; //记录是否已经加载过，等同于初始化
    private ImageManager imageManager; //创建图片管理工具类的对象
    private LinearLayout firstColumn; //第一列LinearLayout布局对象
    private LinearLayout secondColumn; //第二列LinearLayout布局对象
    private LinearLayout thirdColumn; //第三列LinearLayout布局对象
    private static View scrollLayout; //MyscrollView下的子布局对象
    private static int scrollViewHeight; //MyscrollView布局的高度
    private static int lastSrollY = -2; //记录垂直滚动的距离
    private List<MyTextView> textViewList = new ArrayList<>();//创建List<MyTextView>类型容器，用以记录界面上加载的图片
    private static Set<LoadImageTask> taskCollection; //记录当前正在下载或等待下载的任务

    /**
     * 方法名：MyScrollView
     * 功 能：构造函数，同时创建ImageManager类型和Set类型对象实例，并实现监听
     * 参 数：Context context - 全局信息；AttributeSet attrs - 节点属性集合
     * 返回值：无
     */
    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        imageManager = ImageManager.getInstance();
        taskCollection = new HashSet<>();
        //以当前视图作为事件监听器
        setOnTouchListener(this);
    }

    /**
     * 覆盖父类或实现接口的onTouch(View v, MotionEvent event)方法
     * 方法名：onTouch(View v, MotionEvent event)
     * 功 能：触屏动作
     * 参 数：View v - 该视图的View； MotionEvent event - 手势移动事件
     * 返回值：boolean - 是否成功处理时间
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            //新建Message对象，用以处理消息
            Message message = new Message();
            //将该视图对象传递给message.obj
            message.obj = this;
            //延迟5秒后发送消息
            handler.sendMessageDelayed(message, 5);
        }
        return false;
    }

    /**
     在Handler中进行对图片可见性的检查，根据图片可见性以及滚动坐标来进行加载操作
     */
    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //通过Message对象获取到当前的M有ScrollView对象
            MyScrollView myScrollView = (MyScrollView) msg.obj;
            //获取当前Y坐标
            int scrollY = myScrollView.getScrollY();
            //如果与上次滑动位置的坐标相同
            if (scrollY == lastSrollY) {
                //如果滚动到最底部，并且当前没有进行下载的任务时，加载下一页的图片
                if (scrollViewHeight + scrollY >= scrollLayout.getHeight() && taskCollection.isEmpty()) {
                    myScrollView.loadMoreImages();
                }
                myScrollView.checkVisibility();
            }else {
                lastSrollY = scrollY;
                Message message = new Message();
                message.obj = myScrollView;
                handler.sendMessageDelayed(message,5);
            }
        }
    };

    /**
     * 覆盖父类或实现接口的onLayout(boolean changed, int l, int t, int r, int b)方法
     * 方法名：onLayout
     * 功 能：初始化，获得MyScrollView的高度，并得到第一列的宽度，开始加载图片
     * 参 数：boolean changed - 是否被更改, int l, int t, int r, int b（布局的位置）
     * 返回值：无
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed && !loadOnce) {
            //获取MyScrollView布局的高度
            scrollViewHeight = getHeight();
            scrollLayout = getChildAt(0);
            firstColumn = (LinearLayout) findViewById(R.id.first_column);
            secondColumn = (LinearLayout) findViewById(R.id.second_column);
            thirdColumn = (LinearLayout) findViewById(R.id.third_column);
            //获取第一列的宽度（后面作为每一列的宽度）
            columnWidth = firstColumn.getWidth();
            loadOnce = true;
            loadMoreImages();
        }
    }

    /**
     * 方法名：hasSDCard
     * 功 能：检查是否有SDcard
     * 参 数：无
     * 返回值：boolean - 是否存在SDcard
     */
    private boolean hasSDCard() {
        return Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
    }

    /**
     * 函数名：loadMoreImages()
     * 功 能：加载更多图片，每张图片都会开启一个异步线程去下载
     * 参 数：无
     * 返回值：无
     */
    public void loadMoreImages() {
        if (hasSDCard()) {
            int startIndex = page * PAGE_SIZE;
            int endIndex = page * PAGE_SIZE + PAGE_SIZE;
            if (startIndex < Images.imageUrls.length) {
                Toast.makeText(getContext(), "正在加载...", Toast.LENGTH_SHORT)
                        .show();
                if (endIndex > Images.imageUrls.length) {
                    endIndex = Images.imageUrls.length;
                }
                //每一张图片开启一个异步线程去
                for (int i = startIndex; i < endIndex; i++) {
                    LoadImageTask task = new LoadImageTask();
                    taskCollection.add(task);
                    task.execute(Images.imageUrls[i]);
                }
                page++;
            } else {
                Toast.makeText(getContext(), "已没有更多图片", Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            Toast.makeText(getContext(), "未发现SD卡", Toast.LENGTH_SHORT).show();
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)

    /**
     * 方法名：checkVisibility()
     * 功 能：对图片可见性的检查
     * 参 数：无
     * 返回值：无
     *  */
    public void checkVisibility() {
        for (int i = 0; i < textViewList.size(); i++) {
            MyTextView textView = textViewList.get(i);
            int borderTop = (Integer) textView.getTag(R.string.border_top);
            int borderBottom = (Integer) textView
                    .getTag(R.string.border_bottom);
            if (borderBottom > getScrollY()
                    && borderTop < getScrollY() + scrollViewHeight) {
                String imageUrl = (String) textView.getTag(R.string.image_url);
                Bitmap bitmap = imageManager.getBitmapFromMemoryCache(imageUrl);
                if (bitmap != null) {
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
                    textView.setBackground(bitmapDrawable);
                } else {
                    LoadImageTask task = new LoadImageTask(textView);
                    task.execute(imageUrl);
                }
            } else {
                textView.setBackgroundResource(R.drawable.empty_photo);
            }
        }
    }

    /**
     * 类 名：LoadImageTask
     * 功 能：自定义异步线程类，用以实现图片的异步下载
     */
    class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

        private String mImageUrl;
        private MyTextView mtextView;

        public LoadImageTask(MyTextView textView) {
            mtextView = textView;
        }

        public LoadImageTask() {

        }

        @Override
        protected Bitmap doInBackground(String... params) {
            mImageUrl = params[0];
            Bitmap imageBitmap = imageManager
                    .getBitmapFromMemoryCache(mImageUrl);
            if (imageBitmap == null) {
                imageBitmap = loadImage(mImageUrl);
            }
            return imageBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                double ratio = bitmap.getWidth() / (columnWidth * 1.0);
                int scaledHeight = (int) (bitmap.getHeight() / ratio);
                addImage(bitmap, columnWidth, scaledHeight);
            }
            taskCollection.remove(this);
        }

        /**
         * 方法名：loadImage
         * 功 能：根据传入的Url加载图片，如果图片存在于SDcard中，则从SD读取并按照给定比例解析为Bitmap对象，如果SDcard中不存在则
         *       从网上下载
         * 参 数：String imageUrl - 传入的图片Url值（String类型）
         * 返回值：Bitmap - 根据传入的Url值下载下来的图片解析为Bitmap对象
         */
        private Bitmap loadImage(String imageUrl) {
            File imageFile = new File(getImagePath(imageUrl));
            if (!imageFile.exists()) {
                downloadImage(imageUrl);
            }
            if (imageUrl != null) {
                Bitmap bitmap = ImageManager.decodeSampledBitmapFromResource(
                        imageFile.getPath(), columnWidth);
                if (bitmap != null) {
                    imageManager.addBitmapToMemoryCache(imageUrl, bitmap);
                    return bitmap;
                }
            }
            return null;
        }

        /**
         * 方法名：downloadImage
         * 功 能：打开对应的输入输出流下载图片到SDcard
         * 参 数：String imageUrl - 传入的图片Url地址
         * 返回值：无
         */
        private void downloadImage(String imageUrl) {
            HttpURLConnection con = null;
            FileOutputStream fos = null;
            BufferedOutputStream bos = null;
            BufferedInputStream bis = null;
            File imageFile = null;
            try {
                URL url = new URL(imageUrl);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(5 * 1000);
                con.setReadTimeout(15 * 1000);
                con.setDoInput(true);
                con.setDoOutput(true);
                bis = new BufferedInputStream(con.getInputStream());
                imageFile = new File(getImagePath(imageUrl));
                fos = new FileOutputStream(imageFile);
                bos = new BufferedOutputStream(fos);
                byte[] b = new byte[1024];
                int length;
                while ((length = bis.read(b)) != -1) {
                    bos.write(b, 0, length);
                    bos.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bis != null) {
                        bis.close();
                    }
                    if (bos != null) {
                        bos.close();
                    }
                    if (con != null) {
                        con.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (imageFile != null) {
                Bitmap bitmap = ImageManager.decodeSampledBitmapFromResource(
                        imageFile.getPath(), columnWidth);
                if (bitmap != null) {
                    imageManager.addBitmapToMemoryCache(imageUrl, bitmap);
                }
            }
        }


        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        /**
         * 方法名：addImage
         * 功 能：往View中添加图片
         * 参 数：Bitmap bitmap - 传入的Bitmap对象, int imageWidth - 图片宽度, int imageHeight - 图片高度*/
        private void addImage(Bitmap bitmap, int imageWidth, int imageHeight) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    imageWidth, imageHeight);
            if (mtextView != null) {
                //创建BitmapDrawable对象，并将该对象设为MyTextView的背景，即加入图片
                BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
                mtextView.setBackground(bitmapDrawable);
            } else {
                MyTextView textView = new MyTextView(getContext());
                textView.setLayoutParams(params);
                //创建BitmapDrawable对象，并将该对象设为MyTextView的背景，即加入图片
                BitmapDrawable bitmapDrawable  = new BitmapDrawable(bitmap);
                textView.setBackground(bitmapDrawable);
                textView.setPadding(10, 10, 10, 10);
                textView.setTag(mImageUrl);
                final String Tag = (String)textView.getTag();
                findColumnToAdd(textView, imageHeight).addView(textView);
                textViewList.add(textView);

                textView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (Tag) {
                            case "http://img.my.csdn.net/uploads/201309/01/1378037235_3453.jpg":
                                Intent intent = new Intent(getContext(), DiscoverdetailActivity.class);
                                getContext().startActivity(intent);
                        }
                    }
                });
            }
        }

        /**
         * 方法名：findColumnToAdd
         * 功 能：找到当前高度最小的一列并为之添加图片
         * 参 数：MyTextView textView - MyTextView对象，图片的载体； int imageHeight - 图片的高度
         * 返回值：LinearLayout - 当前高度最小的一列
         */
        private LinearLayout findColumnToAdd(MyTextView textView,
                                             int imageHeight) {
            if (firstColumnHeight <= secondColumnHeight) {
                if (firstColumnHeight <= thirdColumnHeight) {
                    textView.setTag(R.string.border_top, firstColumnHeight);
                    firstColumnHeight += imageHeight;
                    textView.setTag(R.string.border_bottom, firstColumnHeight);
                    return firstColumn;
                }
                textView.setTag(R.string.border_top, thirdColumnHeight);
                thirdColumnHeight += imageHeight;
                textView.setTag(R.string.border_bottom, thirdColumnHeight);
                return thirdColumn;
            } else {
                if (secondColumnHeight <= thirdColumnHeight) {
                    textView.setTag(R.string.border_top, secondColumnHeight);
                    secondColumnHeight += imageHeight;
                    textView
                            .setTag(R.string.border_bottom, secondColumnHeight);
                    return secondColumn;
                }
                textView.setTag(R.string.border_top, thirdColumnHeight);
                thirdColumnHeight += imageHeight;
                textView.setTag(R.string.border_bottom, thirdColumnHeight);
                return thirdColumn;
            }
        }

        /**
         * 方法名：getImagePath
         * 功 能：找到图片的本地储存地址
         * 参 数：String imageUrl - 传入的图片Url地址
         * 返回值：String - 图片的本地储存地址
         */
        private String getImagePath(String imageUrl) {
            int lastSlashIndex = imageUrl.lastIndexOf("/");
            String imageName = imageUrl.substring(lastSlashIndex + 1);
            String imageDir = Environment.getExternalStorageDirectory()
                    .getPath() + "/PhotoWallFalls/";
            File file = new File(imageDir);
            if (!file.exists()) {
                file.mkdirs();
            }
            String imagePath = imageDir + imageName;
            return imagePath;
        }
    }

}






