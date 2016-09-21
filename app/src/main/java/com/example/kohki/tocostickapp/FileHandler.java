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
    private String file_name_;
    private Context context_;

    public FileHandler(Context context, String file_name){
        file_name_ = file_name;
        context_ = context;
    }
    public void  saveFile(String sentence){
     //   Toast.makeText(context_, file_name_, Toast.LENGTH_LONG).show();
        outputFile(sentence);
    }
    public String readFile(){
             return inputFile();
    }

    private void outputFile(String sentence){
        try {
            // ストリームを開く
            FileOutputStream output = context_.openFileOutput(file_name_, MODE_APPEND);
            // 書き込み
            sentence += "\n";
            output.write(sentence.getBytes());
           // ストリームを閉じる
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // ファイルを読み出し
    private String inputFile() {
        String text = null;
        try {
            // ストリームを開く
            FileInputStream input = context_.openFileInput(file_name_);
            // 読み込み
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
}
