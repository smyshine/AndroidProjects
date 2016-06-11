package com.example.smy.clienttest;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity implements View.OnClickListener {

    private Button sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendBtn = (Button) findViewById(R.id.send);
        sendBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        Book book = new Book();
        book.setBookName("Cherry Says");
        book.setAuthor("Smy");
        book.setPages(900);
        book.setPrice(81.00);

        URL url = null;
        ObjectOutputStream oos = null;
        try
        {
            url = new URL("http://192.168.62.216:8080/ServerTest/servlet/TestServlet");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestMethod("POST");
            oos = new ObjectOutputStream(connection.getOutputStream());
            oos.writeObject(book);
            InputStreamReader read = new InputStreamReader(connection.getInputStream());
            BufferedReader br = new BufferedReader(read);
            String line = "";
            String message = "";
            while((line = br.readLine()) != null)
            {
                message += line;
            }
            Toast.makeText(getApplicationContext(), "Receive message:\n" + message, Toast.LENGTH_LONG)
                    .show();
            br.close();
            connection.disconnect();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {

        }
    }
}
