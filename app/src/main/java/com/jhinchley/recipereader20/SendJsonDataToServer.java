package com.jhinchley.recipereader20;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by jhinchley on 5/5/16.
 */
public  class SendJsonDataToServer extends AsyncTask<String,String,String> {
    String JsonResponse = null;
    AsyncResponse delegate = null;
    @Override
    protected String doInBackground(String... params) {
        //variables to hold json response and data respectively
        Log.d("doinBackground","Got here!");
        String JsonDATA = params[0];

        HttpURLConnection httpURLConnection = null;
        BufferedReader bufferedReader = null;
        try {
            //create a url object
            URL url = new URL("http://10.254.4.218/httppost.php");

            //open a connection
            httpURLConnection = (HttpURLConnection) url.openConnection();
            Log.d("doinBackground","opended connection");
            //allow the connection to output
            httpURLConnection.setDoOutput(true);

            //set the connection type
            httpURLConnection.setRequestMethod("POST");

            //set the headers
            httpURLConnection.setRequestProperty("Content-Type","application/json");
            httpURLConnection.setRequestProperty("Accept","application/json");

            //create buffered write object on httpurlconnection's output stream
            Writer writer = new BufferedWriter(new OutputStreamWriter(httpURLConnection.getOutputStream(),"UTF-8"));

            //write data to output
            writer.write(JsonDATA);

            //close output stream
            writer.close();

            //get inputstream from httpurlconnection object
            InputStream inputStream = httpURLConnection.getInputStream();

            //string buffer to store buffer input as its read the inputstream
            StringBuffer stringBuffer = new StringBuffer();

            if (inputStream==null){
                //nothing to do
                return null;
            }

            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String inputLine;
            while ( (inputLine=bufferedReader.readLine()) != null){

                //write a line to the string buffer
                stringBuffer.append(inputLine+"\n");

                if (stringBuffer.length()==0){
                    //stream was empty no point in parsing
                    return null;
                }

                //get the response from the string buffer
                JsonResponse = stringBuffer.toString();

                Log.d("JSONReponse",JsonResponse);

                try {
                    //send to post execute

                    return JsonResponse;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (httpURLConnection!=null){
                httpURLConnection.disconnect();
            }
            if (bufferedReader!=null){
                try {
                    bufferedReader.close();
                } catch (final IOException e) {
                    Log.e("IO Exception","Error closing stream",e);
                }
            }
        }

        return null;
    }

    public SendJsonDataToServer(AsyncResponse asyncResponse){
        delegate =asyncResponse;
    }
    @Override
    protected void onPostExecute(String s){
        Log.d("post",s);
        Log.d("post jr",JsonResponse);
        if (JsonResponse!=null){
            delegate.processResponse(JsonResponse);
        }
        else{
            delegate.processResponse("No JsonResponse");
        }
    }
}
