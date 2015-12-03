/**
 * 文件名：MyTextView
 * 描 述：自定义TextView的子类，实现为TextView附加其他功能
 * 作 者：DJZ
 * 时 间：2015-11-23
 */

package com.netease.nim.demo.main;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Admin on 2015/11/22.
 */

public class MyTextView extends TextView {
    public static int Id = 0; //设置静态变量Id
    public MyTextView(Context context) {
        super(context);
        setId(Id++);//为每一个创建的MyTextView设置ID
    }


    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 方法名：DecodeBitmapFromResource
     * 功 能：解析获取的图片，并分配内存
     * 参 数：Pathname pathName - 指定的文件路径 ； int reqWidth - 指定的图片缩放尺寸（宽度）
     * 返回值：BitmapDrawble - 解析出的对象转换为BitmapDrawble类型
     */
    public static BitmapDrawable DecodeBitmapFromResource (String pathName, int reqWidth) {

        //第一次将inJustDecodeBounds属性设置为true，禁止分配内存，获取图片
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        //只用已经定义过的方法来计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options,reqWidth);
        //用获取的inSampleSize再次解析图片，这次分配内存
        options.inJustDecodeBounds = false;
        //解析图片并创建BitmapDrawable对象
        BitmapDrawable bitmapDrawable = new BitmapDrawable(BitmapFactory.decodeFile(pathName, options));
        return bitmapDrawable;

    }

    /**
     * 方法名：calculateInSampleSize
     * 功 能：根据给定需要的缩放数值来计算缩放比例
     * 参 数：BitmapFactory.Options options - Bitmap工具类类型参数，存放设定数据 ； int reqWidth - 指定的图片缩放尺寸（宽度）*/
    public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth) {
        //计算源图片的长，宽
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        int a = 2;
        if(width>reqWidth) {
            //计算实际宽高和要求宽高的比率
            final int widthRation = Math.round((float) height / (float) reqWidth);
            //选择最小的比率作为最终所需要图片的宽高比率
            inSampleSize =widthRation;

        }

        return inSampleSize;
    }

}
