package com.example.kohki.tocostickapp;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by Kohki on 2016/05/09.
 */
public class FileHelper {
    private static String TAG = "FileHelper";
    public FileHelper(){}

    //This method is test code for confirming correct communication,
    public static void  writeAsStrFile(Context context, String file_file, String date, String sentence){
        try {
            FileOutputStream output = context.openFileOutput(file_file, context.MODE_APPEND);
            String data = date + "," + sentence + "\n";
            output.write(data.getBytes());
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static boolean writeAsBinFile(String file_file, char[] data){
        FileOutputStream out;
        try {
            out = new FileOutputStream(file_file);
            for(int i=0;i<data.length;i++) {
                out.write(data[i]);
            }
            out.close();
            return true;
        }catch(IOException e){
            return false;
        }
    }
    public static String readAsStrFile(Context context, String file_file){
        String text = null;
        try {
            FileInputStream input = context.openFileInput(file_file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
            StringBuffer strBuffer = new StringBuffer();
            String       line      = null;

            while ((line = reader.readLine()) != null) {
                line += "\n";
                strBuffer.append(line);
            }
            text = strBuffer.toString();
//            Toast.makeText(context_, text, Toast.LENGTH_SHORT).show();
            // ストリームを閉じる
            reader.close();
            // 読み込んだ文字列を返す
        }catch (IOException e){
            e.printStackTrace();
        }
        return text;
    }
    public static boolean readAsBinFile(String file_file){//TODO:not use
        FileInputStream in;
        try {
            in = new FileInputStream(file_file);
            while (true) {
                int b = in.read();
                if (b == -1) {
                    break;
                }
                System.out.print(Integer.toHexString(b) + " ");
            }
            in.close();
            return true;
        }catch(Exception e){
            return false;
        }
    }
    public static String  readFile(Context context, String file_file){
        StringBuilder sb_data = new StringBuilder();
        try {
            //---
            // FileInputStream is = context.openFileInput(file_file);
            AssetManager assetManager = context.getResources().getAssets();
            InputStream is = assetManager.open(file_file);
            //---
            BufferedReader reader = new BufferedReader( new InputStreamReader( is , "UTF-8") );
            String tmp;
            while( (tmp = reader.readLine()) != null ){
               sb_data.append(tmp + "\n");
            //    Log.v("tmp:",tmp);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG,e+"");
        } catch (IOException e) {
            Log.e(TAG,e+"");
        }
        return sb_data.toString();
    }
    public static ArrayList readCsvFile(Context context, String file_name){
        ArrayList al_csv_data = new ArrayList();
        String[] str_data = readFile(context,file_name).split("\n");
        if(str_data.length == 1){//not get data
            Toast.makeText(context,"not get data",Toast.LENGTH_LONG).show();
        }
        for (int i=1;i<500
                ;i++) {
            String[] all_elements = str_data[i].split(",");
            String[] elements = new String[5];
            elements[0] = all_elements[0];//time
            elements[1] = all_elements[1];//temp
            elements[2] = all_elements[2];//humi
            elements[3] = all_elements[9];//radiation
            elements[4] = all_elements[13];//TODO: where?
            if (i <= 50) {
//                Log.v("データ確認date", all_elements[0]);
            }
            al_csv_data.add(elements);
        }
        return al_csv_data;
    }
}
