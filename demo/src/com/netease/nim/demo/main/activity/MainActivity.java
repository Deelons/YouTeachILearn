package com.netease.nim.demo.main.activity;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.netease.nim.demo.R;
import com.netease.nim.demo.main.fragment.DiscoverFragment;
import com.netease.nim.demo.main.fragment.LessonFragment;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private RadioGroup fragMain_rdoGp;
    private RadioButton fragUser_rdoBtn, fragLesson_rdoBtn,fragDiscover_rdoBtn,fragContacts_rdoBtn;

    private LessonFragment lessonFragment;
    private DiscoverFragment discoverFragment;

    private static final int FRAG_DISCOVER=1;
    private static final int FRAG_LESSON = 2;
    private static final int FRAG_USER = 4;
    private static final int FRAG_Contacts = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);//键盘隐藏

        initWiget();//用于初始化控件
        setFragItem(FRAG_DISCOVER);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragDiscover_rdoBtn:
                setFragItem(FRAG_DISCOVER);
                break;
            case R.id.fragLesson_rdoBtn:
                setFragItem(FRAG_LESSON);
                break;
            case R.id.fragUser_rdoBtn:
                setFragItem(FRAG_USER);
                break;
            case R.id.fragContacts_rdoBtn:
                setFragItem(FRAG_Contacts);
        }
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


    void initWiget() {
        fragMain_rdoGp = (RadioGroup) findViewById(R.id.fragMain_rdoGp);
        fragUser_rdoBtn = (RadioButton) findViewById(R.id.fragUser_rdoBtn);
        fragUser_rdoBtn.setOnClickListener(this);
        fragLesson_rdoBtn = (RadioButton) findViewById(R.id.fragLesson_rdoBtn);
        fragLesson_rdoBtn.setOnClickListener(this);
        fragDiscover_rdoBtn=(RadioButton)findViewById(R.id.fragDiscover_rdoBtn);
        fragDiscover_rdoBtn.setOnClickListener(this);
        fragContacts_rdoBtn = (RadioButton) findViewById(R.id.fragContacts_rdoBtn);
        fragContacts_rdoBtn.setOnClickListener(this);

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void setFragItem(int item) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        switch (item) {
            case FRAG_DISCOVER:
                discoverFragment=new DiscoverFragment();
                ft.replace(R.id.fragContainer_lv,discoverFragment);
                break;
            case FRAG_LESSON:
                lessonFragment = new LessonFragment();
                ft.replace(R.id.fragContainer_lv, lessonFragment);
                break;
            case FRAG_USER:

                break;
            case FRAG_Contacts:
                //跳转到联系人界面
                ContactsActivity.start(MainActivity.this,null);
                break;
            default:
                break;
        }

        ft.commit();

    }
}
