package net.zno_ua.app.ui;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import net.zno_ua.app.R;
import net.zno_ua.app.service.ZNOApiServiceHelper;
import net.zno_ua.app.ui.widget.AspectRatioImageView;
import net.zno_ua.app.util.UiUtils;

import static net.zno_ua.app.provider.ZNOContract.Subject;
import static net.zno_ua.app.provider.ZNOContract.Subject.buildSubjectUri;

public class SubjectActivity extends AppCompatActivity
        implements SubjectTestsFragment.OnTestSelectedListener {
    public static final String EXTRA_SUBJECT_ID = "net.zno_ua.app.ui.SUBJECT_ID";

    private long subjectId;

    private ZNOApiServiceHelper mApiServiceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);

        subjectId = getIntent().getLongExtra(EXTRA_SUBJECT_ID, -1);

        init();
    }

    private void init() {
        initToolbar();
        initToolbarLayout();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            findViewById(R.id.status_bar_background).setVisibility(View.VISIBLE);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Cursor cursor = getContentResolver()
                .query(buildSubjectUri(subjectId), new String[]{Subject.NAME}, null, null, null);
        cursor.moveToFirst();
        getSupportActionBar().setTitle(cursor.getString(0));
        cursor.close();

        Fragment fragment = SubjectTestsFragment.newInstance(subjectId);
        getFragmentManager().beginTransaction()
                .add(R.id.main_content, fragment)
                .commit();
        mApiServiceHelper = ZNOApiServiceHelper.getInstance(this);
    }

    private void initToolbar() {
        Toolbar appBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(appBar);
    }

    private void initToolbarLayout() {
        AspectRatioImageView subjectImageView =
                (AspectRatioImageView) findViewById(R.id.subject_image);
        Picasso.with(this)
                .load(UiUtils.SUBJECT_IMAGE_RES_ID[(int) subjectId])
                .fit()
                .centerCrop()
                .into(subjectImageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_subject, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStartPassingTest(final long id) {
        new MaterialDialog.Builder(this)
                .title(R.string.start_test_question)
                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog materialDialog, View view,
                                               int i, CharSequence charSequence) {
                        return false;
                    }
                })
                .items(R.array.start_test_choices)
                .positiveText(R.string.start)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        startPassingTest(id, dialog.getSelectedIndex() == 0);
                    }
                })
                .show();
    }

    @Override
    public void onStartDownloadingTest(final long id) {
        new MaterialDialog.Builder(this)
                .title(R.string.download_test)
                .content(R.string.download_test_description)
                .positiveText(R.string.download)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        startDownloadingTest(id);
                    }
                }).show();
    }

    @Override
    public void onReStartDownloadingTest(final long id) {
        new MaterialDialog.Builder(this)
                .title(R.string.download_test)
                .content(R.string.restart_download_test_description)
                .positiveText(R.string.download)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        startDownloadingTest(id);
                    }
                }).show();
    }

    @Override
    public void onStartDeletingTest(final long id) {
        new MaterialDialog.Builder(this)
                .title(R.string.delete_test_question)
                .content(R.string.delete_test_description)
                .positiveText(R.string.delete)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        startDeletingTest(id);
                    }
                }).show();
    }

    private void startPassingTest(long id, boolean withTimer) {
        Intent intent = new Intent(this, TestingActivity.class);
        intent.setAction(TestingActivity.Action.PASS_TEST);
        intent.putExtra(TestingActivity.Extra.TEST_ID, id);
        intent.putExtra(TestingActivity.Extra.TIMER_MODE, withTimer);
        startActivity(intent);
    }

    private void startDownloadingTest(long id) {
        mApiServiceHelper.getTest(id);
    }

    private void startDeletingTest(long id) {
        mApiServiceHelper.deleteTest(id);
    }
}
