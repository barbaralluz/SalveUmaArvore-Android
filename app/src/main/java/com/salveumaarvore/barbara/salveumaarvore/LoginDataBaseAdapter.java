package com.salveumaarvore.barbara.salveumaarvore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;

/**
 * Created by barbara on 08/09/15.
 */
public class LoginDataBaseAdapter {

    static final String DATABASE_NAME = "salveumaarvore.db";
    static final int DATABASE_VERSION = 1;
    public static final int NAME_COLUMN = 1;
    // TODO: Create public field for each column in your table.
    // SQL Statement to create a new database.
    static final String DATABASE_CREATE = "create table "+"LOGIN"+
            "( " +"ID"+" integer primary key autoincrement,"+ "USERNAME  text, EMAIL text, PASSWORD text); ";
    // Variable to hold the database instance
    public SQLiteDatabase db;
    // Context of the application using the database.
    private final Context context;
    // Database open/upgrade helper
    private DataBaseHelper dbHelper;
    public LoginDataBaseAdapter(Context _context)
    {
        context = _context;
        dbHelper = new DataBaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public LoginDataBaseAdapter open() throws SQLException
    {
        db = dbHelper.getWritableDatabase();
        return this;
    }
    public void close()
    {
        db.close();
    }

    public  SQLiteDatabase getDatabaseInstance()
    {
        return db;
    }

    public void insertEntry(String userName, String email, String password, String geral, String luz, String raiz, String man, String alt)
    {
        ContentValues newValues = new ContentValues();
        // Assign values for each row.
        newValues.put("USERNAME", userName);
        newValues.put("EMAIL",email);
        newValues.put("PASSWORD",password);

        // Insert the row into your table
        db.insert("LOGIN", null, newValues);
        ///Toast.makeText(context, "Reminder Is Successfully Saved", Toast.LENGTH_LONG).show();
    }
    public int deleteEntry(String UserName)
    {
        //String id=String.valueOf(ID);
        String where="USERNAME=?";
        int numberOFEntriesDeleted= db.delete("LOGIN", where, new String[]{UserName}) ;
        // Toast.makeText(context, "Number fo Entry Deleted Successfully : "+numberOFEntriesDeleted, Toast.LENGTH_LONG).show();
        return numberOFEntriesDeleted;
    }
    public String getSinlgeEntry(String userName)
    {
        Cursor cursor=db.query("LOGIN", null, " USERNAME=?", new String[]{userName}, null, null, null);
        if(cursor.getCount()<1) // UserName Not Exist
        {
            cursor.close();
            return "NOT EXIST";
        }
        cursor.moveToFirst();
        String password= cursor.getString(cursor.getColumnIndex("PASSWORD"));
        cursor.close();
        return password;
    }
    public void  updateEntry(String userName,String password)
    {
        // Define the updated row content.
        ContentValues updatedValues = new ContentValues();
        // Assign values for each row.
        updatedValues.put("USERNAME", userName);
        updatedValues.put("PASSWORD", password);

        String where="USERNAME = ?";
        db.update("LOGIN", updatedValues, where, new String[]{userName});
    }

    public String getAllDataAndGenerateJSON() throws JSONException, FileNotFoundException {

        String query = "select USERNAME, EMAIL, PASSWORD from LOGIN";
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        JSONObject Root = new JSONObject();
        JSONArray ContactArray = new JSONArray();
        File f = new File(Environment.getExternalStorageDirectory()
                + "/users.json");
        FileOutputStream fos = new FileOutputStream(f,true);
        PrintStream ps = new PrintStream(fos);


        int i = 0;
        while (!c.isAfterLast()) {


            JSONObject contact = new JSONObject();
            try {
                contact.put("Username", c.getString(c.getColumnIndex("USERNAME")));
                contact.put("Email", c.getString(c.getColumnIndex("EMAIL")));
                contact.put("Password", c.getString(c.getColumnIndex("PASSWORD")));



                c.moveToNext();

                ContactArray.put(i, contact);
                i++;

            } catch (JSONException e) {

                e.printStackTrace();
            }



        }
        Root.put("CONTACTDETAILS", ContactArray);
        ps.append(Root.toString());

        return Root.toString();
    }
}
