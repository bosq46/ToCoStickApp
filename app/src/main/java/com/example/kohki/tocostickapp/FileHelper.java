package com.example.kohki.tocostickapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created by Kohki on 2016/05/09.
 */
public class FileHelper {

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
}
