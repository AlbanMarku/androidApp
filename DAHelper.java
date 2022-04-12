//package com.example.myapplicationtestmapfrag;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//
//import androidx.annotation.Nullable;
//
//public class DAHelper extends SQLiteOpenHelper {
//
//    public static final String COORDS_TABLE = "COORDS_TABLE";
//    public static final String COLLUMN_COORDSLOC = "COORDSLOC";
//    public static final String COLLUMN_ID = "ID";
//    public static final String COLLUMN_DATE = "DATE";
//
//    public DAHelper(@Nullable Context context) {
//        super(context,"coordy.db",null,1);
//    }
//
//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        String createTableStatement = "CREATE TABLE " + COORDS_TABLE + " (" + COLLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLLUMN_COORDSLOC + COLLUMN_DATE + " TEXT)";
//
//        db.execSQL(createTableStatement);
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
//
//    }
//
//    public boolean addOne(DataModel dataModel) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues cv = new ContentValues();
//
//        cv.put(COLLUMN_COORDSLOC, dataModel.getCoords().toString());
//
//        long insert = db.insert(COORDS_TABLE, null, cv);
//
//        if(insert == -1) {
//            return false;
//        } else {
//            return true;
//        }
//    }
//}
