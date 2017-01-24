package com.example.kohki.tocostickapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.zip.InflaterInputStream;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Kohki on 2016/05/09.
 */
public class FileHelper {
    private static String TAG = "FileHelper";
    private static final int TIME_COLUMN_NUM         = 0;
    private static final int TEMPERATURE_COLUMN_NUM = 1;
    private static final int HUMIDITY_COLUMN_NUM    = 2;
    private static final int RADIATION_COLUMN_NUM   = 9;
    private static final int MOISTURE_COLUMN_NUM    = 12;
    public static final SimpleDateFormat sdf_csv = new SimpleDateFormat("yyyy'/'MM'/'dd HH:mm", Locale.JAPAN);
    public static final SimpleDateFormat sdf_ymd = new SimpleDateFormat("yyyy'/'MM'/'dd", Locale.JAPAN);

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

    public static String  readFile(Context context, String file_name){
        StringBuilder sb_data = new StringBuilder();
        try {
            AssetManager assetManager = context.getResources().getAssets();
            InputStream is = assetManager.open(file_name);
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
        for (int i=0;i<arr_sen_data.length;i++) {
            String[] all_elements = arr_sen_data[i].split(",");
            String[] elements = new String[6];
            elements[0] = all_elements[0];
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

    public static void setDbFromCsv(Context context, String file_name, SQLiteDatabase mDb){
        StringBuilder sb_data = new StringBuilder();
        try {
            // FileInputStream is = context.openFileInput(file_file);
            AssetManager assetManager = context.getResources().getAssets();
            InputStream is = assetManager.open(file_name);
            BufferedReader reader = new BufferedReader( new InputStreamReader( is , "UTF-8") );
            String line;
            int i=0;
            double cumulative_temp = 0;

            String[] all_elements;
            ContentValues values;
            Date date;
            Calendar now;
            int first_loop = 0;
            while( (line = reader.readLine()) != null ){
                if(first_loop == 0) {
                    first_loop++;
                    continue;
                }
                all_elements = line.split(",");
                values = new ContentValues();
                try{

                    date = sdf_csv.parse(all_elements[TIME_COLUMN_NUM]);
                    now = Calendar.getInstance();
                    now.setTime(date);
                    values.put(SensorDBContract.SensorData.COL_MEASURED_YEAR,  now.get(Calendar.YEAR));
                    values.put(SensorDBContract.SensorData.COL_MEASURED_MONTH, now.get(Calendar.MONTH) + 1);
                    values.put(SensorDBContract.SensorData.COL_MEASURED_DATE,  now.get(Calendar.DATE));
                    values.put(SensorDBContract.SensorData.COL_MEASURED_DATE,  now.get(Calendar.HOUR_OF_DAY)+":"+now.get(Calendar.MINUTE));
                    cumulative_temp += Double.parseDouble(all_elements[TEMPERATURE_COLUMN_NUM]);
                    values.put(SensorDBContract.SensorData.COL_MEASURED_YEAR, cumulative_temp);
                    values.put(SensorDBContract.SensorData.COL_MEASURED_YEAR,  all_elements[HUMIDITY_COLUMN_NUM]);
                    values.put(SensorDBContract.SensorData.COL_MEASURED_YEAR,  all_elements[RADIATION_COLUMN_NUM]);
                    values.put(SensorDBContract.SensorData.COL_MEASURED_YEAR,  all_elements[MOISTURE_COLUMN_NUM]);
                    SQLiteDatabase database = mDb;
                    if(first_loop == 1){
                        Log.d(TAG,now.get(Calendar.YEAR)+"");
                        Log.d(TAG,now.get(Calendar.MONTH) + 1+"");
                        Log.d(TAG, now.get(Calendar.DATE)    +"");
                        Log.d(TAG, now.get(Calendar.HOUR_OF_DAY)+":"+now.get(Calendar.MINUTE)   +"");
                        Log.d(TAG, cumulative_temp   +"");
                        Log.d(TAG,all_elements[HUMIDITY_COLUMN_NUM]    );
                        Log.d(TAG,  all_elements[RADIATION_COLUMN_NUM] );
                        Log.d(TAG, all_elements[MOISTURE_COLUMN_NUM]  );
                    }first_loop++;
                    database.insert(
                            SensorDBContract.SensorData.TABLE_NAME,
                            null,  values);
                }catch (ParseException e){
                    Log.e(TAG, e.toString());
                }catch (NumberFormatException e){
                    Log.e(TAG, e.toString());
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG,e+"");
        } catch (IOException e) {
            Log.e(TAG,e+"");
        }
    }
    public static void moveFromAssetsToLocal(Context context, String assets_file, String local_file){
        try {
            AssetManager assetManager = context.getResources().getAssets();
            InputStream input = assetManager.open(assets_file);
         //   FileOutputStream fos = new FileOutputStream(new File(local_file));
            FileOutputStream output = context.openFileOutput(local_file, MODE_PRIVATE);
            byte[] buffer = new byte[1024];
            int n = 0;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }

            output.close();
            input.close();
            assetManager.close();
        }catch (IOException e){
            Log.e(TAG,e.toString());
        }
    }
    public static ArrayList readFile(Context context,String file_name,int i){
        ArrayList file_lines = new ArrayList();
        BufferedReader in = null;
        try {
            FileInputStream file = context.openFileInput(file_name);
            in = new BufferedReader(new InputStreamReader(file));
            String line;
            while ( (line = in.readLine()) != null){
                file_lines.add(line);
            }
            in.close();
        }catch(FileNotFoundException e){
            Log.e(TAG,e.toString());
        }catch(IOException e){
            Log.e(TAG,e.toString());
        }
        return file_lines;
    }
    public static void pickUpDistinguishingValue(String source_file_name, String every_day_file_name) {
        BufferedReader in = null;
        try {
            FileInputStream file = ChartActivity.getInstance().openFileInput(source_file_name);
            in = new BufferedReader(new InputStreamReader(file));
            String line;
            FileOutputStream fs_ave = ChartActivity.getInstance().openFileOutput(every_day_file_name, MODE_PRIVATE);
            PrintWriter every_day_file = new PrintWriter(fs_ave);
            double d_ave = 0;
            double d_hig = 0;
            double d_min = 0;
            String date_ymd = "";
            int cnt_days = 1;
            int cnt_row = 0;
            Date date;
            while ((line = in.readLine()) != null) {
                if(cnt_row==0){
                    cnt_row++;
                    continue;
                }
                String elements[] = line.split(",");
                date = sdf_csv.parse(elements[TIME_COLUMN_NUM]);
                if (cnt_row == 1) {
                    date_ymd = sdf_ymd.format(date);
                    Log.d(TAG+cnt_row,date_ymd);
                } else {
                    if (!date_ymd.equals(sdf_ymd.format(date))) {
                        d_ave /= cnt_days;
                        String new_row = date_ymd+","+d_ave+","+d_hig+","+d_min;
                        Log.d(TAG+cnt_row,"def "+new_row);
                        every_day_file.println(new_row);
                        cnt_days = 1;
                        date_ymd = sdf_ymd.format(date);
                    }
                }
                Double line_temp = Double.parseDouble(elements[TEMPERATURE_COLUMN_NUM]);
                if (cnt_days == 1) {
                    d_ave = line_temp;
                    d_hig = line_temp;
                    d_min = line_temp;
                } else {
                    d_ave += line_temp;
                    if (d_hig < line_temp)
                        d_hig = line_temp;
                    if (d_min > line_temp)
                        d_min = line_temp;
                }
                cnt_row++;
                cnt_days++;
            }
            in.close();
            every_day_file.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } catch (ParseException e) {
            Log.e(TAG, e.toString());
        }
    }
}
