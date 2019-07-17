package android.my.speechcontrol;
/*
    UI框架中的snackBar和status可以用一下，争取将progress的表盘整上去
* */
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;

import com.xuexiang.xui.XUI;
import com.xuexiang.xui.widget.guidview.GuideCaseView;

public class UiActicivity extends AppCompatActivity {
    CoordinatorLayout container;

    @Override
    protected void onCreate(Bundle bundle){
        super.onCreate(bundle);
        XUI.init(getApplication());
        XUI.debug(true);
        setContentView(R.layout.activity_main);


    }


}
