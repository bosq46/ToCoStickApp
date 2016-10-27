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
public class FileHandler extends AppCompatActivity {
    private String strFileName = "bin_sensor_data.txt";
    private String binFileName = "str_sensor_data.txt";
    private Context mContext;

    public FileHandler(Context context, String file_name){
        mContext = context;
    }
    public void  writeStrFile(String sentence){
        try {
            // ストリームを開く
            FileOutputStream output = mContext.openFileOutput(strFileName, MODE_APPEND);
            // 書き込み
            sentence += "\n";
            output.write(sentence.getBytes());
            // ストリームを閉じる
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean writeBinFile(char[] FileName){
        FileOutputStream out;
        try {
            out = new FileOutputStream(binFileName);
            for(int i=0;i<FileName.length;i++) {
                out.write(FileName[i]);
            }
            out.close();
            return true;
        }catch(IOException e){
            return false;
        }
    }
    public String readStrFile(){
        String text = null;
        try {
            FileInputStream input = mContext.openFileInput(strFileName);
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
    public boolean readBinFile(){
        FileInputStream in;
        try {
            in = new FileInputStream(binFileName);
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
