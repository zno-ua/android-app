package net.zno_ua.app.activity;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.squareup.picasso.Picasso;

import net.zno_ua.app.R;
import net.zno_ua.app.ZNOApplication;
import net.zno_ua.app.fragment.BaseFragment;
import net.zno_ua.app.fragment.SubjectsFragment;
import net.zno_ua.app.fragment.TestingResultFragment;
import net.zno_ua.app.service.GcmRegistrationService;
import net.zno_ua.app.util.Utils;

import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import static java.lang.String.valueOf;
import static net.zno_ua.app.provider.ZNOContract.Answer;
import static net.zno_ua.app.provider.ZNOContract.Testing;
import static net.zno_ua.app.provider.ZNOContract.Testing.COLUMN_ID;
import static net.zno_ua.app.provider.ZNOContract.Testing.buildTestingItemUri;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor>, BaseFragment.OnTitleChangeListener {
    private static final String KEY_SELECTED_NAVIGATION_ITEM_ID = "KEY_SELECTED_NAVIGATION_ITEM_ID";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private int mSelectedNavigationItemId;
    private String[] mQuotes;
    private final Random mRandom = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mQuotes = getResources().getStringArray(R.array.quotes);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setUpActionBar(toolbar);
        setUpNavigationDrawerLayout(toolbar);
        getLoaderManager().initLoader(0, null, this);
        if (savedInstanceState == null) {
            mSelectedNavigationItemId = R.id.navigation_item_testing;
            onNavigationItemSelected(mNavigationView.getMenu().findItem(mSelectedNavigationItemId));
        } else {
            mSelectedNavigationItemId = savedInstanceState.getInt(KEY_SELECTED_NAVIGATION_ITEM_ID);
        }
        if (checkPlayServices()) {
            startService(new Intent(this, GcmRegistrationService.class));
        }
    }

    private void setUpActionBar(Toolbar toolBar) {
        setSupportActionBar(toolBar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setUpNavigationDrawerLayout(Toolbar toolBar) {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(
                Utils.getThemeAttribute(this, R.attr.colorPrimaryDark).resourceId
        );
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolBar, 0, 0);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        final View headerView = mNavigationView.inflateHeaderView(R.layout.view_drawer_header);
        final ImageView backgroundImage = (ImageView) headerView.findViewById(R.id.image);
        Picasso.with(this).load(R.drawable.ic_zno).fit().centerCrop().into(backgroundImage);
        final TextView tvQuote = (TextView) headerView.findViewById(R.id.quote);
        tvQuote.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                final String quote = mQuotes[mRandom.nextInt(mQuotes.length)];
                tvQuote.setText(String.format(getString(R.string.quote_format), quote));
            }
        });
        tvQuote.performClick();
        final Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR) + ((calendar.get(Calendar.MONTH) < 7) ? 0 : 1);
        final TextView headerTitle = (TextView) headerView.findViewById(R.id.text);
        headerTitle.setText(String.format(Locale.US, "%s %d", getString(R.string.zno), year));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_NAVIGATION_ITEM_ID, mSelectedNavigationItemId);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
    public boolean onNavigationItemSelected(MenuItem item) {
        boolean isChecked = false;
        switch (item.getItemId()) {
            case R.id.navigation_item_testing:
                onTestingSelected();
                isChecked = true;
                break;
            case R.id.navigation_item_testing_results:
                onTestingResultsSelected();
                isChecked = true;
                break;
        }
        setNavigationItemSelected(item, isChecked);
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return isChecked;
    }

    private void setNavigationItemSelected(MenuItem item, boolean isChecked) {
        if (isChecked) {
            item.setChecked(true);
            if (mSelectedNavigationItemId != item.getItemId()) {
                mNavigationView.getMenu().findItem(mSelectedNavigationItemId).setChecked(false);
            }
        }
        mSelectedNavigationItemId = item.getItemId();
    }

    private void onTestingSelected() {
        final Fragment fragment = getMainContentFragment();
        if (!(fragment instanceof SubjectsFragment)) {
            replaceMainContent(SubjectsFragment.newInstance());
        }
    }

    private void onTestingResultsSelected() {
        final Fragment fragment = getMainContentFragment();
        if (!(fragment instanceof TestingResultFragment)) {
            replaceMainContent(TestingResultFragment.newInstance());
        }
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
        if (data != null && data.moveToFirst()) {
            new MaterialDialog.Builder(this)
                    .title(R.string.start_unfinished_test_question)
                    .content(R.string.start_unfinished_test_description)
                    .positiveText(R.string.continuee)
                    .negativeText(R.string.delete)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog md, @NonNull DialogAction da) {
                            continueTesting(data);
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog md, @NonNull DialogAction da) {
                            deleteTesting(data.getLong(COLUMN_ID.ID));
                        }
                    })
                    .cancelable(false)
                    .show();
        }
    }

    private void continueTesting(Cursor data) {
        final Intent intent = new Intent(MainActivity.this, TestingActivity.class);
        intent.setAction(TestingActivity.Action.CONTINUE_PASSAGE_TEST);
        intent.putExtra(TestingActivity.Key.TEST_ID, data.getLong(COLUMN_ID.TEST_ID));
        intent.putExtra(TestingActivity.Key.TESTING_ID, data.getLong(COLUMN_ID.ID));
        intent.putExtra(TestingActivity.Key.TIMER_MODE, !data.isNull(COLUMN_ID.ELAPSED_TIME) &&
                data.getLong(COLUMN_ID.ELAPSED_TIME) != -1);
        startActivity(intent);
        overridePendingTransition(R.anim.activity_open_translate_right, R.anim.activity_close_alpha);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void deleteTesting(long id) {
        getContentResolver().delete(buildTestingItemUri(id), null, null);
        getContentResolver().delete(Answer.CONTENT_URI, Answer.TESTING_ID + " = ?",
                new String[]{valueOf(id)});
    }

    @Override
    public void onTitleChanged(String title) {
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(title);
    }

    public void replaceMainContent(@NonNull Fragment fragment) {
        final FragmentManager fm = getSupportFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.main_content, fragment);
        ft.commit();
    }

    @Nullable
    public Fragment getMainContentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.main_content);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        final GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

}
