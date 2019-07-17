package android.my.speechcontrol;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
/*
* 参考文献：https://www.cnblogs.com/yukino/p/4095833.html
* */
public class GuideActivity extends AppCompatActivity {
    private ViewPager vp;
    private ViewPagerAdapter vpAdapter;     //

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }
}
