package net.zno_ua.app.provider;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * @author Vojko Vladimir
 */
public class ZNODatabase extends SQLiteAssetHelper {

    /*
    * TODO: change DB name after finishing migration.
    * */
    private static final String DATABASE_NAME = "zno.db";
    private static final int DATABASE_VERSION = 1;

    interface Tables {
        String SUBJECT = "subject";
        String TEST = "test";
        String QUESTION = "question";
    }

    public ZNODatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        /*
         * TODO: set version.
         * */
        setForcedUpgrade(/* version */);
    }
}