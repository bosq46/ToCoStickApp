package com.example.kohki.tocostickapp;

/**
 * Created by Kohki on 2016/05/13.
 *
 * Description: The purpose of this class is Analysis of received sensor data
 * data is contain id, kind of sensor, num of data, data, parity
 */
public class DataAnalyzer {
    private static byte[] sensor_data_;
    private static String message_;

    public DataAnalyzer(String message){
        message_ = message;
    }
}
