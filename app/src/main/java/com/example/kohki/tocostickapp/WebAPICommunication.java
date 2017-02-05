package com.example.kohki.tocostickapp;

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
import java.util.Date;
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
    //private static final String APIkey = "3fd5aa9c63f9307c98b0eab8c716b2e918212400744d665adff6817b140acf1f";

    private ConcurrentMap Tokens;
    private String mGatewayNAME;
    private String mQueryPost;
    private String mDataKeys;
    private FileContract mID;
    volatile private String[] mKeys;

    WebAPICommunication() {
        setDataID();
    }
    public void setDataID(){
        mID = new FileContract(ChartActivity.getInstance());
        mGatewayNAME = "sensorData_sample_" + mID.getGateWayID();
        mQueryPost   = "\"nodeID\":" + mID.getNodeID()+"}";
        mDataKeys    = "[\"nodeID\",\"time\"";
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

    //@Params : date time
    @Override
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

    private final class TokenGetter extends AsyncTask<URL, Void, Void> {
        @Override
        protected Void doInBackground(URL... urls) {
            final URL url = urls[0];
            Log.d("HttpURLConnect", url.toString());
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");//this is not necessary. Cuz default method is GET
                con.setRequestProperty("Content-Type", "multipart/form-data");
                con.setDoInput(true);
                con.connect();
                final int status = con.getResponseCode();
                Log.d("Getting Tokens", "HTTPstatus:" + status);
                if (status == HttpURLConnection.HTTP_OK) {
                    InputStream in = con.getInputStream();
                    String str_response = InputStreamToString(in);
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
                    con.disconnect();
                }
            }
            return null;
        }
        private void encodeRequestJSon(String json_str) {
            try {
                String keys[] = {"Token", "Response","Expire"};
                JSONObject jsonData = new JSONObject(json_str);
                String st = jsonData.getString(keys[0]);
                Tokens = new ConcurrentHashMap<String,String>();
                for(int i=0;i<keys.length;i++){
                    Tokens.put(keys[i],jsonData.getString(keys[i]));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
    private final class SensorDataGetter extends AsyncTask<URL, Void, Void> {
        private String TAG = "SensDGetter";
        private int mGetPeriod = 23;
        private int download_interval = 3;
        @Override
        protected Void doInBackground(URL... urls) {
            if(Tokens == null) {
                Log.e(TAG, "Tokens is null");
            }else {
                Log.d(TAG, "Tokens is not null");
            }
            Log.d(TAG+" key", mDataKeys);
            Log.d(TAG+" name", mGatewayNAME);

            long toDateTime   = System.currentTimeMillis();
            long fromDateTime = toDateTime - (mGetPeriod*24*60*60*1000);

            Log.d(TAG+" frDateTime", (new Date(fromDateTime)).getDate() +"日"+(new Date(fromDateTime)).getHours() +"");
            Log.d(TAG+" toDateTime", (new Date(toDateTime)).getDate()   +"日"+(new Date(toDateTime)).getHours() +"");

            final String all_file_name = mID.getGateWayID()+"_"+mID.getNodeID()+".csv";
            final String day_file_name = mID.getGateWayID()+"_"+mID.getNodeID()+"_days.csv";
            File all_file = new File(ChartActivity.getInstance().getFilesDir() +"/"+all_file_name);
            File day_file = new File(ChartActivity.getInstance().getFilesDir() +"/"+day_file_name);
            //FIXME
            if(all_file.exists()) {
                Log.d(TAG," delete all_file");
                all_file.delete();
            }
            if(day_file.exists()) {
                Log.d(TAG," delete day_file");
                day_file.delete();
            }

            try {
                while (toDateTime - fromDateTime > download_interval*24*60*60*1000){
                    long interval_date = fromDateTime + download_interval*24*60*60*1000;
                    Log.d(TAG+" frDateTime", (new Date(fromDateTime)).getDate() +"日"+(new Date(fromDateTime)).getHours() +"-");
                    Log.d(TAG+" toDateTime", (new Date(interval_date)).getDate() +"日"+(new Date(interval_date)).getHours() +"");
                    String queryDate = "{\"$where\":\"this.time >= new Date("+fromDateTime+") && this.time <= new Date("+interval_date+")\",";
                    String query = URLEncoder.encode(queryDate + mQueryPost, "UTF-8");
                    String request = DATA_REQUEST +"?Name="+mGatewayNAME+"&Keys="+mDataKeys+"&Query="+query;
                    requestSensData(request);
                    fromDateTime += download_interval*24*60*60*1000;
                }
                Log.d(TAG+" last commu", "--here");
                Log.d(TAG+" frDateTime", (new Date(fromDateTime)).getDate() +"日"+(new Date(fromDateTime)).getHours() +"-");
                Log.d(TAG+" toDateTime", (new Date(toDateTime)).getDate() +"日"+(new Date(toDateTime)).getHours() +"");
                String queryDate = "{\"$where\":\"this.time >= new Date("+fromDateTime+") && this.time <= new Date("+toDateTime+")\",";
                String query = URLEncoder.encode(queryDate + mQueryPost, "UTF-8");
                String request = DATA_REQUEST +"?Name="+mGatewayNAME+"&Keys="+mDataKeys+ "&Query="+query;
                String[] result = requestSensData(request);
                if(Integer.parseInt(result[1]) == 0){
                    String range = (new Date(fromDateTime)).getDate() +"日"+(new Date(fromDateTime)).getHours() +"から"+
                            (new Date(toDateTime)).getDate() +"日"+(new Date(toDateTime)).getHours() +"までのデータがありません";
                    Toast.makeText(ChartActivity.getInstance(),range,Toast.LENGTH_SHORT).show();
                }
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
            return null;
        }
        private String[] requestSensData(String request){
            HttpURLConnection con = null;
            String[] result={};
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
                    final InputStream in = con.getInputStream();
                    String str_response = InputStreamToString(in);
                    in.close();
                    result = encodeResponseJSon(str_response);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
            return result;
        }
        private synchronized String[] encodeResponseJSon(String json_str) {
            String[] result = new String[2];
            try {
                String keys[] = {"Response", "List"};
                JSONObject ReceivedJson = new JSONObject(json_str);

                String response_result = ReceivedJson.getString(keys[0]);
                Log.d(TAG + " Request", response_result);
                result[0] = response_result;
                String response_data   = ReceivedJson.getString(keys[1]);
                result[1] = response_data.length()+"";
                if(response_data.length() == 0){
                   return result;
                }
                String regex = "(\\{.+?:.+?\\})";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(response_data);
                final String all_file_name = mID.getGateWayID()+"_"+mID.getNodeID()+".csv";
                final String day_file_name = mID.getGateWayID()+"_"+mID.getNodeID()+"_days.csv";

                SimpleDateFormat sdf_webapi= new SimpleDateFormat("yyyy'-'MM'-'dd'T'kk':'mm':'ss'.000000Z'");
                SimpleDateFormat sdf_ymdhm = new SimpleDateFormat("yyyy'/'MM'/'dd HH:mm");
                SimpleDateFormat sdf_ym    = new SimpleDateFormat("yyyy'年'MM'月'");

                String date_time;
                String date_month="";
                float cumu_temp = 0;

                float from_date = 0;
                float fromtime;
                int cnt_temp  = 0;
                while (m.find()){
                //    Log.d(TAG+" json data",m.group());
                    JSONObject json_data = new JSONObject(m.group());
                    String sentence;
                    if(json_data.has("air_temperature")) {
                        date_time   = sdf_ymdhm.format(sdf_webapi.parse(json_data.getString("time")));
                        date_month  = sdf_ym.format(   sdf_webapi.parse(json_data.getString("time")));
                        String s_temperature = json_data.getString("air_temperature");
                        float f_temperature = Float.parseFloat(s_temperature);
                        if(cnt_temp == 0){
                            cumu_temp += f_temperature;
                            from_date = sdf_webapi.parse(json_data.getString("time")).getMinutes() / 24/60/60/60/1000;
                            cnt_temp++;
                        }else {
                            float intervel = sdf_webapi.parse(json_data.getString("time")).getMinutes() / 24/60/60/60/1000 - from_date;
                            cumu_temp += f_temperature*intervel;
                        }
                        Log.d(TAG, "save "+s_temperature+",null"+"@"+date_time);
                        String got_data =s_temperature+","+cumu_temp+",null";
                        FileHelper.writeAsStrFile(ChartActivity.getInstance(), all_file_name, date_time, got_data);
                    }
                    if(json_data.has("amount_of_solar_radiation")) {
                        date_time   = sdf_ymdhm.format(sdf_webapi.parse(json_data.getString("time")));
                        date_month  = sdf_ym.format(   sdf_webapi.parse(json_data.getString("time")));

                        String radiation = json_data.getString("amount_of_solar_radiation");
                        String got_data = "null,null,"+radiation;
                        Log.d(TAG, "save null,null"+radiation+"@"+date_time);
                        FileHelper.writeAsStrFile(ChartActivity.getInstance(), all_file_name, date_time, got_data);
                    }

                }
                FileHelper.pickUpDistinguishingValue(all_file_name,   day_file_name);
                FileHelper.readAsStrFile(ChartActivity.getInstance(), day_file_name);//FIXME: delete me.

                final String latest_date = date_month;
                ChartActivity.mHandler.post(new Runnable() { //viewの変更はmHandlerから行う。
                    @Override
                    public void run() {
                        new ChartActivity().createChart(day_file_name,latest_date);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }catch (ParseException e){
                e.printStackTrace();
            }
            return result;
        }
    }
}
