package net.zno_ua.app.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import net.zno_ua.app.R;
import net.zno_ua.app.util.UiUtils;

import java.util.Calendar;

import static java.lang.String.valueOf;
import static net.zno_ua.app.provider.ZNOContract.Answer;
import static net.zno_ua.app.provider.ZNOContract.Testing;
import static net.zno_ua.app.provider.ZNOContract.Testing.COLUMN_ID;
import static net.zno_ua.app.provider.ZNOContract.Testing.buildTestingItemUri;
import static net.zno_ua.app.ui.TestingActivity.Action;
import static net.zno_ua.app.ui.TestingActivity.Extra;

public class MainActivity extends AppCompatActivity
        implements SubjectsFragment.OnSubjectSelectedListener,
        NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setUpActionBar(toolbar);
        setUpNavigationDrawerLayout(toolbar);

        getLoaderManager().initLoader(0, null, this);
    }

    private void setUpActionBar(Toolbar toolBar) {
        setSupportActionBar(toolBar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setUpNavigationDrawerLayout(Toolbar toolBar) {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(
                UiUtils.getThemeAttribute(this, R.attr.colorPrimaryDark).resourceId
        );
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolBar, 0, 0);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.inflateHeaderView(R.layout.drawer_header);

        ImageView backgroundImage = (ImageView) headerView.findViewById(R.id.image);
        Picasso.with(this).load(R.drawable.ic_zno)
                .fit()
                .centerCrop()
                .into(backgroundImage);

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR) + ((calendar.get(Calendar.MONTH) < 7) ? 0 : 1);

        TextView headerTitle = (TextView) headerView.findViewById(R.id.text);
        headerTitle.setText(String.format("%s %d", getString(R.string.zno), year));
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Testing.CONTENT_URI,
                Testing.PROJECTION,
                Testing.STATUS + " = ?",
                new String[]{valueOf(Testing.IN_PROGRESS)},
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
        if (data.moveToFirst()) {
            new MaterialDialog.Builder(this)
                    .title(R.string.start_unfinished_test_question)
                    .content(R.string.start_unfinished_test_description)
                    .positiveText(R.string.continuee)
                    .negativeText(R.string.delete)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            continueTesting(data);
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            deleteTesting(data.getLong(COLUMN_ID.ID));
                        }
                    })
                    .cancelable(false)
                    .show();
        }
    }

    private void continueTesting(Cursor data) {
        Intent intent = new Intent(MainActivity.this, TestingActivity.class);
        intent.setAction(Action.CONTINUE_PASSAGE_TEST);
        intent.putExtra(Extra.TEST_ID, data.getLong(COLUMN_ID.TEST_ID));
        intent.putExtra(Extra.TESTING_ID, data.getLong(COLUMN_ID.ID));
        intent.putExtra(Extra.TIMER_MODE, !data.isNull(COLUMN_ID.ELAPSED_TIME) &&
                data.getLong(COLUMN_ID.ELAPSED_TIME) != -1);
        startActivity(intent);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void deleteTesting(long id) {
        getContentResolver().delete(buildTestingItemUri(id), null, null);
        getContentResolver().delete(Answer.CONTENT_URI, Answer.TESTING_ID + " = ?",
                new String[]{valueOf(id)});
    }
}
