package net.zno_ua.app.ui;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.zno_ua.app.R;
import net.zno_ua.app.util.UiUtils;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements SubjectsFragment.OnSubjectSelectedListener,
        NavigationView.OnNavigationItemSelectedListener {

    private Toolbar mToolBar;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        initToolBar();
        initDrawerLayout();
        initMainContent();
    }

    private void initToolBar() {
        mToolBar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(mToolBar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void initDrawerLayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(
                UiUtils.getThemeAttribute(this, R.attr.colorPrimaryDark).resourceId
        );

        NavigationView navigationView =
                (NavigationView) mDrawerLayout.findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        ImageView backgroundImage = (ImageView) navigationView.findViewById(R.id.image);
        Picasso.with(this).load(R.drawable.ic_zno)
                .fit()
                .centerCrop()
                .into(backgroundImage);

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR) + ((calendar.get(Calendar.MONTH) >= 7) ? 0 : 1);

        ((TextView) navigationView.findViewById(R.id.text))
                .setText(String.format("%s %d", getString(R.string.zno), year));

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolBar, 0, 0);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void initMainContent() {
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.testing);
        Fragment fragment = SubjectsFragment.newInstance();
        getFragmentManager().beginTransaction()
                .add(R.id.main_content, fragment)
                .commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;

        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.navigation_item_testing:
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
        }
        return false;
    }

    @Override
    public void onSubjectSelected(long id) {
        Intent intent = new Intent(this, SubjectActivity.class);
        intent.putExtra(SubjectActivity.EXTRA_SUBJECT_ID, id);
        startActivity(intent);
    }
}
