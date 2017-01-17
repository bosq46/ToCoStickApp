package com.example.kohki.tocostickapp;

import android.content.Context;
import android.content.res.AssetManager;
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
    public static boolean writeAsBinFile(String file_file, int[] data){
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
        String[] arr_sen_data = readFile(context, file_name).split("\n");
        if(arr_sen_data.length <= 1){
            Log.v("readCsvFile","データが少ない、または、取得できない");
        }
        for (int i=1;i<arr_sen_data.length;i++) {
            String[] all_elements = arr_sen_data[i].split(",");
            String[] elements = new String[6];
            elements[0] = all_elements[0];//time
            elements[1] = all_elements[1];//temp
            elements[2] = all_elements[2];//humi
            elements[3] = all_elements[9];//radiation
            elements[4] = all_elements[12];//TODO: where?
            if(i == 1)
                elements[5] = all_elements[1];//積算温度
            else if(al_csv_data.size() >= 1) {
                String el[] = (String[]) al_csv_data.get(al_csv_data.size() - 1);
                try {
                    elements[5] = Double.parseDouble(el[5]) + Double.parseDouble(all_elements[1]) + "";//積算温度
                }catch (NumberFormatException e){
                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
            al_csv_data.add(elements);
        }
        return al_csv_data;
    }
}
