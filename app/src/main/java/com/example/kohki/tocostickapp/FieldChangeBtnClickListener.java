package com.example.kohki.tocostickapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Kohki on 2017/02/05.
 */

public class FieldChangeBtnClickListener  implements View.OnClickListener {
    private AlertDialog.Builder builder;
    public static AlertDialog alertDialog;
    private static final String TAG = "FieldCListener";
    private FileContract mID;
    @Override
    public void onClick(View v) {
        final Context context = ChartActivity.getInstance();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.input_field, null);
        mID = new FileContract(ChartActivity.getInstance());

        //set current ID
        final TextView tv_now_field_id = (TextView) layout.findViewById(R.id.now_field_id);
        String now_field_id =mID.getFieldName()+"("+mID.getGateWayID()+":"+mID.getNodeID()+")";
        tv_now_field_id.setText(now_field_id);

        //set ID list
        ListView lv_field_id = (ListView) layout.findViewById(R.id.lv_field_id);
        List l_field_id = mID.getFieldID();
        final ArrayList field_id_list = new ArrayList();
        for(int i=0;i<l_field_id.size();i++){
            String[] row = (String[])l_field_id.get(i);
            for(int j=2;j<row.length;j++){
                field_id_list.add(row[0]+" "+row[1]+" "+row[j]);
            //    Log.d(TAG,"read asset id file "+row[0]+" "+row[1]+" "+row[j]);
            }
        //    Log.d(TAG,row[0]+" "+row[1]);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_expandable_list_item_1, field_id_list);
        lv_field_id.setAdapter(adapter);
        lv_field_id.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str_id   = field_id_list.get(position).toString();
                String[] arr_id = str_id.split(" ");

                EditText et_name = (EditText)layout.findViewById(R.id.et_field_name);
                et_name.setText(arr_id[0]);
                EditText et_gateway = (EditText)layout.findViewById(R.id.et_gateway_id);
                et_gateway.setText(arr_id[1]);
                EditText et_node = (EditText)layout.findViewById(R.id.et_node_id);
                et_node.setText(arr_id[2]);
            }
        });

        try {
            builder = new AlertDialog.Builder(context);
            builder.setTitle("ゲートウェイ・ノードIDの変更");
            builder.setView(layout);
            builder.setCancelable(true);
            builder.setPositiveButton("変更する", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {

                    EditText edit = (EditText)layout.findViewById(R.id.et_field_name);
                    SpannableStringBuilder ssb = (SpannableStringBuilder)edit.getText();
                    String name =  ssb.toString();

                    edit = (EditText)layout.findViewById(R.id.et_gateway_id);
                    ssb = (SpannableStringBuilder)edit.getText();
                    int gateway =  Integer.parseInt(ssb.toString());

                    edit = (EditText)layout.findViewById(R.id.et_node_id);
                    ssb = (SpannableStringBuilder)edit.getText();
                    int node =  Integer.parseInt(ssb.toString());

                    mID.setFieldID(name, gateway, node);
                    Log.d("save field ID", name+"("+gateway+":"+ node+")");
                    }catch (NumberFormatException e){
                        Toast.makeText(ChartActivity.getInstance(),"数字を入力して下さい",Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    ChartActivity.createChart();
                }
            });
            builder.setNegativeButton("閉じる", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alertDialog = builder.show();
        } catch (Exception e) {
        }
    }
}
