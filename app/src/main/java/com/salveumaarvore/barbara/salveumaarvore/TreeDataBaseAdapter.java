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
public class TreeDataBaseAdapter {

    static final String DATABASE_NAME = "salveumarvore.db";
    static final int DATABASE_VERSION = 1;
    public static final int NAME_COLUMN = 1;
    // TODO: Create public field for each column in your table.
    // SQL Statement to create a new database.
    static final String DATABASE_CREATE = "create table "+"TREE"+
            "( " +"ID integer primary key autoincrement, LONLAT text, "
                 +"REFERENCIA text, ESPECIE text, ALTURA text, COND_GERAL text, COND_RAIZ text, "
                 +"COND_LUZ text, MANUTENCAO text, DESCRICAO text, FOTO text); ";
    // Variable to hold the database instance
    public SQLiteDatabase db;
    // Context of the application using the database.
    private final Context context;
    // Database open/upgrade helper
    private DataBaseHelper dbHelper;
    public TreeDataBaseAdapter(Context _context)
    {
        context = _context;
        dbHelper = new DataBaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public TreeDataBaseAdapter open() throws SQLException
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

    public void insertEntry(String lat, String lon, String referencia, String especie,
                            String altura, String cond_geral, String cond_raiz, String cond_luz,
                            String manutencao, String descricao, String foto) {

        ContentValues newValues = new ContentValues();
        // Assign values for each row.
        newValues.put("LONLAT", lon + ", "+lat);
        newValues.put("REFERENCIA", referencia);
        newValues.put("ESPECIE", especie);
        newValues.put("ALTURA", altura);
        newValues.put("COND_GERAL", cond_geral);
        newValues.put("COND_RAIZ", cond_raiz);
        newValues.put("COND_LUZ", cond_luz);
        newValues.put("MANUTENCAO", manutencao);
        newValues.put("DESCRICAO", descricao);
        newValues.put("FOTO", foto);

        // Insert the row into your table
        db.insert("TREE", null, newValues);
    }

    public String getAllDataAndGenerateJSON() throws JSONException, FileNotFoundException {

        String query = "select LONLAT, REFERENCIA, ESPECIE, ALTURA, COND_GERAL, COND_RAIZ, COND_LUZ, MANUTENCAO, DESCRICAO, FOTO from TREE";
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        JSONObject Root = new JSONObject();
        JSONArray treeArray = new JSONArray();
        File f = new File(Environment.getExternalStorageDirectory()
                + "/trees.json");

        if (f.exists()) {
            f.delete();
        }

        FileOutputStream fos = new FileOutputStream(f, true);
        PrintStream ps = new PrintStream(fos);

        int i = 0;
        while (!c.isAfterLast()) {


            JSONObject tree = new JSONObject();
            try {
                tree.put("Lonlat", c.getString(c.getColumnIndex("LONLAT")));
                tree.put("Referencia", c.getString(c.getColumnIndex("REFERENCIA")));
                tree.put("Especie", c.getString(c.getColumnIndex("ESPECIE")));
                tree.put("Altura", c.getString(c.getColumnIndex("ALTURA")));
                tree.put("Condição Geral", c.getString(c.getColumnIndex("COND_GERAL")));
                tree.put("Condição da Raíz", c.getString(c.getColumnIndex("COND_RAIZ")));
                tree.put("Presença de Luz", c.getString(c.getColumnIndex("COND_LUZ")));
                tree.put("Manutenção", c.getString(c.getColumnIndex("MANUTENCAO")));
                tree.put("Descrição", c.getString(c.getColumnIndex("DESCRICAO")));
                tree.put("Foto", c.getString(c.getColumnIndex("FOTO")));

                c.moveToNext();

                treeArray.put(i, tree);
                i++;

            } catch (JSONException e) {

                e.printStackTrace();
            }
        }
        Root.put("TREES", treeArray);
        ps.append(Root.toString());

        return Root.toString();
    }
}
