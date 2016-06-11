package com.example.smy.contacts;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AlphabetIndexer;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private LinearLayout titleLayout;
    private TextView title;
    private ListView contactsListView;
    private ContactAdapter adapter;
    private AlphabetIndexer indexer;
    private List<Contact> contacts = new ArrayList<Contact>();
    private String alphabet = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private int lastFirstVisibleItem = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new ContactAdapter(this, R.layout.contact_item, contacts);
        titleLayout = (LinearLayout) findViewById(R.id.title_layout);
        title = (TextView) findViewById(R.id.my_title);
        contactsListView = (ListView) findViewById(R.id.contacts_list_view);
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor = getContentResolver().query(uri,
                new String[]{"display_name", "sort_key"}, null, null, "sort_key");
        if(cursor.moveToFirst())
        {
            do {
                String name = cursor.getString(0);
                String sortKey = getSortKey(cursor.getString(1));
                Contact contact = new Contact();
                contact.setName(name);
                contact.setSortKey(sortKey);
                contacts.add(contact);
            }while(cursor.moveToNext());
        }
        startManagingCursor(cursor);
        indexer = new AlphabetIndexer(cursor, 1, alphabet);
        adapter.setIndexer(indexer);
        if(contacts.size() > 0)
        {
            setupContactsListView();
        }
    }

    private void setupContactsListView()
    {
        contactsListView.setAdapter(adapter);
        contactsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int section = indexer.getSectionForPosition(firstVisibleItem);
                int nextSecPosition = indexer.getPositionForSection(section + 1);
                if(firstVisibleItem != lastFirstVisibleItem)
                {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) titleLayout.getLayoutParams();
                    params.topMargin = 0;
                    titleLayout.setLayoutParams(params);
                    title.setText(String.valueOf(alphabet.charAt(section)));
                }
                if(nextSecPosition == firstVisibleItem + 1)
                {
                    View childView = view.getChildAt(0);
                    if(childView != null) {
                        int titleHeight = titleLayout.getHeight();
                        int bottom = childView.getBottom();
                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) titleLayout.getLayoutParams();
                        if (bottom < titleHeight)
                        {
                            float pushedDis = bottom - titleHeight;
                            params.topMargin = (int) pushedDis;
                            titleLayout.setLayoutParams(params);
                        }
                        else
                        {
                            if(params.topMargin != 0)
                            {
                                params.topMargin = 0;
                                titleLayout.setLayoutParams(params);
                            }
                        }
                    }
                }
                lastFirstVisibleItem = firstVisibleItem;
            }
        });
    }

    private String getSortKey(String sortKey)
    {
        String key = sortKey.substring(0, 1).toUpperCase();
        if(key.matches("[A-Z]"))
        {
            return key;
        }
        return "#";
    }
}
