package com.smile.restwebservicetest;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private Button loginButton;
    private EditText webUrl;
    private EditText userName;
    private EditText password;
    private TextView jsonString;
    private TextView messageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webUrl = (EditText) findViewById(R.id.webSiteText);
        // webUrl.setText("http://192.168.0.103:5050/Authenticate/LoginFromAndroid");   // ASP.NET Core
        webUrl.setText("http://ec2-13-59-195-3.us-east-2.compute.amazonaws.com:8080/MVCofJDBCsmsong/LoginForRESTfulServlet");
        // webUrl.setText("http://192.168.0.102:8080/MVCofJDBCsmsong/LoginForRESTfulServlet");
        userName = (EditText) findViewById(R.id.userNameText);
        userName.setText("chaolee");
        password = (EditText) findViewById(R.id.passwordText);
        password.setText("86637971");

        jsonString = (TextView) findViewById(R.id.jsonString);
        messageText = (TextView) findViewById(R.id.messageText);

        loginButton = (Button)findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start login process
                new LoginAsyncTask().execute();
            }
        });
    }

    private class LoginAsyncTask extends AsyncTask<Void, Void, String[]> {

        private String TAG = "LoginAsyncTask";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String[] doInBackground(Void... voids) {

            String[] result = {"",""};

            String webSite = webUrl.getText().toString();
            Log.i(TAG,"webUrl = " + webSite);

            String user = userName.getText().toString();
            Log.i(TAG,"userName = " + user);

            String pass = password.getText().toString();
            Log.i(TAG,"password = " + pass);

            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", user);       // Web's user name field
                jsonObject.put("password", pass);       // Web's password field

                String params = getQueryStringFromJSON(jsonObject);

                URL url = new URL(webSite);
                HttpURLConnection myConnection = (HttpURLConnection)url.openConnection();
                // myConnection.setReadTimeout(15000);
                // myConnection.setConnectTimeout(15000);
                // myConnection.setRequestMethod("GET");   // get method is default if not set
                myConnection.setRequestMethod("POST");
                myConnection.setDoInput(true);
                myConnection.setDoOutput(true);  // for write data to web

                Log.i(TAG, "Getting OutputStream ....");

                OutputStream outputStream = myConnection.getOutputStream();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
                outputStreamWriter.write(params);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                outputStream.close();

                Log.i(TAG, "OutputStream closing ....");

                int responseCode = myConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // succeeded to request
                    Log.i(TAG, "REST Web Service -> Succeeded to connect.");
                    InputStream inputStream = myConnection.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream,"UTF-8");

                    StringBuilder sb = new StringBuilder("");
                    int readBuff = -1;
                    while((readBuff=inputStreamReader.read()) != -1) {
                        sb.append((char)readBuff);
                    }
                    result[0] = "Succeeded";
                    result[1] = sb.toString();

                    Log.i(TAG, "Web status -> " + result[0]);
                    Log.i(TAG, "Web output -> " + result[1]);

                    inputStreamReader.close();
                    inputStream.close();

                } else {
                    Log.i(TAG, "REST Web Service -> Failed to connect.");
                    result[0] = "Failed";
                }

            } catch (Exception ex) {
                String errorMsg = ex.toString() + "\n" + ex.getStackTrace();
                Log.d(TAG, "REST Web Service -> Exception occurred." + "\n" + errorMsg);
                result[0] = "Exception";
            }

            return result;
        }


        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            String result = "";
            String status = strings[0].toUpperCase();
            if (status.equals("SUCCEEDED")) {
                // Succeeded
                result = "";
                try {
                    JSONObject jObject = new JSONObject(strings[1]);

                    result = "Connected to JDBC: ";
                    if (jObject.getBoolean("isConnectedToJDBC")) {
                        result += "Yes";
                    } else {
                        result += "No";
                    }
                    result += "\n" + "User Name: " + jObject.getString("userName");
                    result += "\n" + "User Email: " + jObject.getString("userEmail");
                    result += "\n" + "User State: " + jObject.getString("userState");
                } catch(JSONException ex) {
                    String errorMsg = ex.toString() + "\n" + ex.getStackTrace();
                    Log.d(TAG, "Failed to parse JSONObject from the result." + "\n" + errorMsg);
                    result = "Failed to parse JSONObject from the result.";
                }

            } else if (status.equals("FAILED")) {
                // Failed
                result = "REST Web Service -> Failed to connect.";
            } else {
                // Exception
                result = "REST Web Service -> Exception occurred.";
            }

            jsonString.setText(strings[1]);
            messageText.setText(result);
        }

        private String getQueryStringFromJSON(JSONObject jsonObject) {

            boolean first = true;

            StringBuilder result = new StringBuilder("");

            try {
                Iterator<String> itr = jsonObject.keys();
                while (itr.hasNext()) {
                    String key = itr.next();
                    Object value = jsonObject.get(key);

                    if (!first) {
                        result.append("&");
                    }
                    result.append(URLEncoder.encode(key,"UTF-8"));
                    result.append("=");
                    result.append(URLEncoder.encode(value.toString(),"UTF-8"));

                    first = false;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            String res = result.toString();
            Log.i(TAG, "StringBuilder result = " + res);

            return res;
        }
    }
}
