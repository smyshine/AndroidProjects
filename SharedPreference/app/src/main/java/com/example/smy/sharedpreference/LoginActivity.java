package com.example.smy.sharedpreference;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by SMY on 2016/6/9.
 */
public class LoginActivity extends Activity {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private EditText accountEdit;
    private EditText passwordEdit;
    private Button loginButton;
    private CheckBox rememberPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        accountEdit = (EditText) findViewById(R.id.account);
        passwordEdit = (EditText) findViewById(R.id.password);
        loginButton = (Button) findViewById(R.id.login);
        rememberPwd = (CheckBox) findViewById(R.id.remember_pwd);
        boolean isRemember = preferences.getBoolean("remember_pwd", false);
        if(isRemember)
        {
            String account = preferences.getString("account", "");
            String pwd = preferences.getString("password", "");
            accountEdit.setText(account);
            passwordEdit.setText(pwd);
            rememberPwd.setChecked(true);
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = accountEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                if(account.equals("admin") && password.equals("123456"))
                {
                    editor = preferences.edit();
                    if(rememberPwd.isChecked())
                    {
                        editor.putString("account", account);
                        editor.putString("password", password);
                        editor.putBoolean("remember_pwd", true);
                    }
                    else
                    {
                        editor.clear();
                    }
                    editor.commit();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "account or password is invalid", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }
}
