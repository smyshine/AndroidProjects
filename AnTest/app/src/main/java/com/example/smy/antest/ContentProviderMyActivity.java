package com.example.smy.antest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;

public class ContentProviderMyActivity extends Activity implements DialogInterface.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_provider_my);

        resolver = getContentResolver();
        listView = (ListView) findViewById(R.id.listView);

        getContentResolver().registerContentObserver(PERSON_ALL_URI, true, new PersonObserver(handler));
    }

    private ContentResolver resolver;
    private ListView listView;

    private static final String AUTHORITY = "com.example.smy.antest";
    private static final Uri PERSON_ALL_URI = Uri.parse("content://" + AUTHORITY + "/persons");

    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            requery();
        }
    };

    public void init(View v)
    {
        ArrayList<Person> persons = new ArrayList<Person>();

        Person person1 = new Person("Anna", 20, "lively girl");
        Person person2 = new Person("Bob",  22, "sunny boy");
        Person person3 = new Person("Caroline", 20, "white girl");
        Person person4 = new Person("Damon", 28, "evil handsome boy");
        Person person5 = new Person("Elena", 21, "dead sleepy girl");

        persons.add(person1);
        persons.add(person2);
        persons.add(person3);
        persons.add(person4);
        persons.add(person5);

        for(Person p : persons)
        {
            ContentValues value = new ContentValues();
            value.put("name", p.name);
            value.put("age", p.age);
            value.put("info", p.info);
            resolver.insert(PERSON_ALL_URI, value);
        }
    }

    //query all records
    public void query(View v)
    {
        final Cursor c = resolver.query(PERSON_ALL_URI, null, null, null, null);
        CursorWrapper cursorWrapper = new CursorWrapper(c){
            @Override
            public String getString(int columnIndex)
            {
                if(getColumnName(columnIndex).equals("info"))
                {
                    int age = getInt(getColumnIndex("age"));
                    return age + " years old, " + super.getString(columnIndex);
                }
                return super.getString(columnIndex);
            }
        };

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.list_item_simple_adapter,
                cursorWrapper, new String[]{"name", "info"}, new int[]{R.id.ItemTitle, R.id.ItemText}, 0);
        listView.setAdapter(adapter);

        startManagingCursor(cursorWrapper);
    }

    public void insert(View v)
    {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.add_content_provider_my, (ViewGroup) findViewById(R.id.llpersoninfo));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Input contact info")
                .setView(layout)
                .setPositiveButton("    OK ", this)
                .setNegativeButton("Cancel ", null)
                .create()
                .show();
    }


    Person mInsertPerson = new Person();
    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        if(which == Dialog.BUTTON_POSITIVE)
        {
            AlertDialog ad = (AlertDialog) dialog;
            ContentValues values = new ContentValues();
            values.put("name", ((EditText) ad.findViewById(R.id.person_name)).getText().toString());
            values.put("age", String.valueOf(((EditText) ad.findViewById(R.id.person_age)).getText().toString()));
            values.put("info", ((EditText) ad.findViewById(R.id.person_info)).getText().toString());
            resolver.insert(PERSON_ALL_URI, values);
        }
    }

    public void update(View v)
    {
        Person person = new Person();
        person.name = "Jane";
        person.age = 30;

        ContentValues values = new ContentValues();
        values.put("age", person.age);
        resolver.update(PERSON_ALL_URI, values, "name = ?", new String[]{person.name});
    }

    public void delete(View v)
    {
        Uri delUri = ContentUris.withAppendedId(PERSON_ALL_URI, 1);
        resolver.delete(delUri, null, null);
    }

    private void requery()
    {
        query(null);
    }

}
