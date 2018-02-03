package com.smile.restwebservicetest;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
    private TextView webUrl;
    private EditText userName;
    private EditText password;
    private TextView messageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = (Button)findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start login process
                new LoginAsyncTask().execute();
            }
        });

        webUrl = (TextView) findViewById(R.id.webSiteText);
        webUrl.setText("http://192.168.0.103:5050/Authenticate/LoginFromAndroid");
        userName = (EditText) findViewById(R.id.userNameText);
        userName.setText("chaolee");
        password = (EditText) findViewById(R.id.passwordText);
        password.setText("86637971");
        messageText = (TextView) findViewById(R.id.messageText);
    }

    private class LoginAsyncTask extends AsyncTask<Void, Void, String[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String[] doInBackground(Void... voids) {

            String[] result = {""};

            String webSite = webUrl.getText().toString();
            System.out.println("webUrl = " + webSite);

            String user = userName.getText().toString();
            System.out.println("userName = " + user);

            String pass = password.getText().toString();
            System.out.println("password = " + pass);

            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", user);       // GitHub's user name field
                jsonObject.put("password", pass);   // GitHub's password field

                String params = getQueryStringFromJSON(jsonObject);

                URL url = new URL(webSite);
                HttpURLConnection myConnection = (HttpURLConnection)url.openConnection();
                // myConnection.setReadTimeout(15000);
                // myConnection.setConnectTimeout(15000);
                // myConnection.setRequestMethod("GET");   // get method is default if not set
                myConnection.setRequestMethod("POST");
                myConnection.setDoInput(true);
                myConnection.setDoOutput(true);  // for write data to web

                System.out.println("Getting OutputStream ....");

                OutputStream outputStream = myConnection.getOutputStream();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
                outputStreamWriter.write(params);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                outputStream.close();

                System.out.println("OutputStream closing ....");

                int responseCode = myConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // succeeded to request
                    System.out.println("REST Web Service -> Succeeded to connect.");
                    InputStream inputStream = myConnection.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream,"UTF-8");

                    StringBuilder sb = new StringBuilder("");
                    int readBuff = -1;
                    while((readBuff=inputStreamReader.read()) != -1) {
                        sb.append((char)readBuff);
                    }
                    result[0] = sb.toString();

                    System.out.println("Web output -> " + result[0]);

                    inputStreamReader.close();
                    inputStream.close();
                } else {
                    System.out.println("REST Web Service -> Failed to connect.");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
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

            if (strings[0].isEmpty()) {
                // Failed to connect
                messageText.setText("Failed to connect.");
            } else {
                String result = strings[0];
                messageText.setText(result);
            }
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
            System.out.println("StringBuilder result = " + res);

            return res;
        }
    }
}
