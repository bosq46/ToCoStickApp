package com.example.kohki.tocostickapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.IllegalFormatException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Kohki on 2017/01/23.
 */

public class WebAPICommunication  extends WebDataDLBtnClickListener{
    private static final String TAG = "WebAPICls";
    private static final String TOKEN_REQUEST = "http://www17337uj.sakura.ne.jp:3000/getToken";
    private static final String DATA_REQUEST  = "http://agridatabase.mybluemix.net/v1/json/collection/item/";
    private static final String APIkey = "3fd5aa9c63f9307c98b0eab8c716b2e918212400744d665adff6817b140acf1f";

    private ConcurrentMap Tokens;
    private String mGatewayNAME;
    private String mQueryPost;
    private String mDataKeys;
    private FileContract mID;
    volatile private String[] mKeys;

    WebAPICommunication() {
        mID = new FileContract(ChartActivity.getInstance());
        setDataID();
    }
    public void setDataID(){
        mGatewayNAME = "sensorData_sample_" + mID.getGateWayID();
        mQueryPost = "\"nodeID\":" + mID.getNodeID()+"}";
        mDataKeys = "[\"nodeID\",\"time\"";
        String env_id[] = "air_temperature relative_humidity illuminance ATPR soil_temperature soil_moisture_content amount_of_solar_radiation wind_speed wind_direction rainfall precipitation undefined undefined undefined undefined undefined undefined".split(" ");
        int i=0;
        mKeys =new String[env_id.length];
        for(String key: env_id){
            mDataKeys += ",\""+key+"\"";
            mKeys[i] = key;
            i++;
        }
        mDataKeys += "]";
    }
    public void getToken(){
        try {
            new TokenGetter().execute(new URL(TOKEN_REQUEST));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    @Override //@Params : date time
    public void downloadSensorData(){
        try {
            //TODO:比較
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
            Log.d(TAG+"InputStrToStr",line);
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }


    private synchronized  void encodeRequestJSon(String json_str) {
        try {
            String keys[] = {"Token", "Response","Expire"};
            JSONObject jsonData = new JSONObject(json_str);
            // 配列を取得する場合
//              JSONArray jsonArray = new JSONObject(json_str).getJSONArray("オブジェクト名");

            String st = jsonData.getString(keys[0]);

            Tokens = new ConcurrentHashMap<String,String>();
            for(int i=0;i<keys.length;i++){
                Tokens.put(keys[i],jsonData.getString(keys[i]));
                //      mTokens[i] = jsonData.getString(keys[i]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
           //     DATAREQUEST+"?Name="+name+"&Keys="+keys+"&Query="+query
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
                    encodeRequestJSon(str_response);
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
        private int mGetPeriod = 30;
        @Override
        protected Void doInBackground(URL... urls) {
            if(Tokens == null) {
                Log.e(TAG, "Tokens is null");
            }else {
                Log.d(TAG, "Tokens is not null");
            }
            Log.d(TAG+" key", mDataKeys);
            Log.d(TAG+" name", mGatewayNAME);

            Calendar cal = Calendar.getInstance();
            int year   = cal.get(Calendar.YEAR);
            int month  = cal.get(Calendar.MONTH)+1;
            int day    = cal.get(Calendar.DATE);

            cal.clear();
            cal.set(year, month-1, day);

            long now_millistime= System.currentTimeMillis();
            Log.d(TAG+" now",now_millistime+"");
            long toDateTime   = now_millistime+(9*60*60*1000);
            long fromDateTime = toDateTime-(20*24*60*60*1000);
            Log.d(TAG+" frDateTime", fromDateTime +"");
            Log.d(TAG+" toDateTime", toDateTime +"");
            final String all_file_name = mID.getGateWayID()+"_"+mID.getNodeID()+".csv";
            final String day_file_name = mID.getGateWayID()+"_"+mID.getNodeID()+"_days.csv";
            File all_file = new File(ChartActivity.getInstance().getFilesDir() +"/"+all_file_name);
            File day_file = new File(ChartActivity.getInstance().getFilesDir() +"/"+day_file_name);
            //FIXME
            if(day_file.exists()) {
                Log.d(TAG," delete day_file");
                day_file.delete();
            }
            if(all_file.exists()) {
                Log.d(TAG," delete all_file");
                all_file.delete();
            }
            try {

            while (toDateTime - fromDateTime > 3*24*60*60*1000){

                long after_3days = fromDateTime + 3*24*60*60*1000;
                String queryDate = "{\"$where\":\"this.time >= new Date("+fromDateTime+") && this.time <= new Date("+after_3days+")\",";
            //    String queryFromDate = "{\"$where\":\"this.time >= new Date("+fromDateTime+")\",";
                Log.d(TAG,(new Date(fromDateTime)).getDate()+"");
                Log.d(TAG,(new Date(after_3days)).getDate()+"");
                String query = URLEncoder.encode(queryDate + mQueryPost, "UTF-8");
            //    Log.d(TAG+" query", query);
                String request = DATA_REQUEST +"?Name="+mGatewayNAME+"&Keys="+mDataKeys+"&Query="+query;
                requestSensData(request);

                fromDateTime += 3*24*60*60*1000;
            }
            Log.d(TAG+" last commu", "--here");
                Log.d(TAG,(new Date(fromDateTime)).getDate()+"");
                Log.d(TAG,(new Date(toDateTime)).getDate()+"");
            String queryDate = "{\"$where\":\"this.time >= new Date("+fromDateTime+") && this.time <= new Date("+toDateTime+")\",";
            String query = URLEncoder.encode(queryDate + mQueryPost, "UTF-8");
         //   Log.d(TAG+" query", query);
            String request = DATA_REQUEST +"?Name="+mGatewayNAME+"&Keys="+mDataKeys+ "&Query="+query;
            requestSensData(request);

            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
            return null;
        }
        private void requestSensData(String request){
            HttpURLConnection con = null;
            try {
                final URL url = new URL(request);
                Log.d(TAG, url.toString());
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");//this is not necessary. Cuz default method is GET
                con.setRequestProperty("Content-Type", "multipart/form-data");
                con.setRequestProperty("Authorization", Tokens.get("Token").toString() );
                con.setDoInput(true);
                con.connect();
                final int status = con.getResponseCode();
                Log.d(TAG, "HTTPstatus:" + status);
                if (status == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "connect success");
                    final InputStream in = con.getInputStream();
                    String str_response = "";
                    str_response = InputStreamToString(in);
                    in.close();
                    encodeResponseJSon(str_response);
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
        }
        private synchronized  void encodeResponseJSon(String json_str) {
            try {
                String keys[] = {"Response", "List"};
                JSONObject ReceivedJson = new JSONObject(json_str);
                // 配列を取得する場合
//              JSONArray jsonArray = new JSONObject(json_str).getJSONArray("オブジェクト名");
           //     Log.d(TAG,ReceivedJson.toString());
                String response_result = ReceivedJson.getString(keys[0]);
                Log.d(TAG + " Request", response_result);
                String response_data = ReceivedJson.getString(keys[1]);
            //    response_data = response_data.substring(1,response_data.length()-2);
                String regex = "(\\{.+?:.+?\\})";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(response_data);
                final String all_file_name = mID.getGateWayID()+"_"+mID.getNodeID()+".csv";
                final String day_file_name = mID.getGateWayID()+"_"+mID.getNodeID()+"_days.csv";

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'MM'-'dd'T'kk':'mm':'ss'.000000Z'");
                SimpleDateFormat sdf_ymdhm = new SimpleDateFormat("yyyy'/'MM'/'dd HH:mm");
                SimpleDateFormat sdf_ymd   = new SimpleDateFormat("yyyy'/'MM'/'dd");
                String date_time;
                String date;
                int cnt_temp = 0;

                while (m.find()){
                    Log.d(TAG+" json data",m.group());
                    JSONObject json_data = new JSONObject(m.group());
                    if(json_data.has("air_temperature")) {
                        cnt_temp++;
                        date_time = sdf_ymdhm.format(sdf.parse(json_data.getString("time")));
                        date      = sdf_ymd.format(sdf.parse(json_data.getString("time")));
                        String temperature = json_data.getString("air_temperature");
                        Log.d(TAG, "save "+temperature+"@"+date_time);
                        FileHelper.writeAsStrFile(ChartActivity.getInstance(), all_file_name, date_time, temperature);
                    }
                }
                String file_content;
//                file_content = FileHelper.readAsStrFile(ChartActivity.getInstance(), all_file_name);
//                Log.d(TAG+"check1",file_content);
                FileHelper.pickUpDistinguishingValue(all_file_name, day_file_name);
                file_content = FileHelper.readAsStrFile(ChartActivity.getInstance(), day_file_name);
                Log.d(TAG+"file content",cnt_temp+" "+file_content);
                ChartActivity.mHandler.post(new Runnable() { //viewの変更はmHandlerから行う。
                    @Override
                    public void run() {
                        new ChartActivity().createChart(day_file_name);
                    }
                });
            /*
            ConcurrentHashMap request = new ConcurrentHashMap<String,String>();
            for(int i=0;i<keys.length;i++){
                request.put(keys[i],jsonData.getString(keys[i]));
          //      mTokens[i] = jsonData.getString(keys[i]);
            }
            */
                //    Log.d("data",request.get(keys[1]).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }catch (ParseException e){
                e.printStackTrace();
            }
        }
    }
}
