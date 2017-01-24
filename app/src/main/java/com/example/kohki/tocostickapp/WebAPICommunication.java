package com.example.kohki.tocostickapp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Kohki on 2017/01/23.
 */

public class WebAPICommunication {
    private static final String TAG = "WebAPICls";
    private static final String TOKEN_REQUEST = "http://www17337uj.sakura.ne.jp:3000/getToken";
    private static final String DATA_REQUEST  = "http://agridatabase.mybluemix.net/v1/json/collection/item/";
    private static final String APIkey = "3fd5aa9c63f9307c98b0eab8c716b2e918212400744d665adff6817b140acf1f";

    private static String[] tokens;
    private DataRangeDicider mDateRange;

    WebAPICommunication() {
    }
    public void getToken(){
        try {
            new TokenGetter().execute(new URL(TOKEN_REQUEST));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    public void getSensorData(){
        try {
            //new Date() < tokens[0] && token[1] == "Success"
            //NO -> new TokenGetter().execute(new URL(TOKEN_REQUEST));
            //YES -> DATAREQUEST+"?Name="+name+"&Keys="+keys+"&Query="+query
            new SensorDataGetter().execute(new URL(DATA_REQUEST));
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
    }
    // InputStream -> String
    static String InputStreamToString(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }
    private String[] encodeJSon(String json_str) {
        try {
            String keys[] = {"Expire", "Response", "Token"};
            JSONObject jsonData = new JSONObject(json_str);
            // 配列を取得する場合
//              JSONArray jsonArray = new JSONObject(json_str).getJSONArray("オブジェクト名");

            String st = jsonData.getString(keys[2]);
            Log.d(TAG + "key----", st);

            String[] arr_keys = new String[3];
            for(int i=0;i<keys.length;i++){
                arr_keys[i] = jsonData.getString(keys[i]);
            }
            return arr_keys;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
    public WebDataDLBtnClickListener generateWebDataDLLictener(){
        mDateRange = new DataRangeDicider();
        return mDateRange;
    }
    private final class TokenGetter extends AsyncTask<URL, Void, Void> {
        @Override
        protected Void doInBackground(URL... urls) {
            String str_response  ="";
            Log.d("HttpURLConnect", "http start");
            // アクセス先URL
            final URL url = urls[0];
            Log.d("HttpURLConnect", url.toString());
            HttpURLConnection con = null;
            try {
                // ローカル処理
                // コネクション取得
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");//this is not necessary. Cuz default method is GET
                con.setRequestProperty("Content-Type", "multipart/form-data");
                con.setDoInput(true);
                con.connect();
                // HTTPレスポンスコード
                final int status = con.getResponseCode();
                Log.d("HttpURLConnect", "HTTPstatus:" + status);
                if (status == HttpURLConnection.HTTP_OK) {
                    Log.d("HttpURLConnect", "connect success");

                    final InputStream in = con.getInputStream();
                    str_response = InputStreamToString(in);
                    in.close();
                    Log.d("HttpURLConnect", str_response);
                    tokens = encodeJSon(str_response);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (con != null) {
                    // コネクションを切断
                    con.disconnect();
                }
            }
            return null;
        }
    }
    private final class SensorDataGetter extends AsyncTask<URL, Void, Void> {
        private String TAG = "SensDGetter";
        @Override
        protected Void doInBackground(URL... urls) {
            String str_response = "";
            Log.d(TAG, "http start");
            final URL url = urls[0];
            Log.d(TAG, url.toString());
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");//this is not necessary. Cuz default method is GET
                con.setRequestProperty("Content-Type", "multipart/form-data");
                con.setRequestProperty("Authorization", APIkey );
                con.setDoInput(true);
                con.connect();
                final int status = con.getResponseCode();
                Log.d("HttpURLConnect", "HTTPstatus:" + status);
                if (status == HttpURLConnection.HTTP_OK) {
                    Log.d("HttpURLConnect", "connect success");
                    final InputStream in = con.getInputStream();
                    str_response = InputStreamToString(in);
                    in.close();
                    Log.d("HttpURLConnect", str_response);
                    tokens = encodeJSon(str_response);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (con != null) {
                    // コネクションを切断
                    con.disconnect();
                }
            }
            return null;
        }
    }
    public class DataRangeDicider extends WebDataDLBtnClickListener{
        @Override
        public void downloadSensorData(){
            getSensorData();//@Params : date time
        }
    }
}
