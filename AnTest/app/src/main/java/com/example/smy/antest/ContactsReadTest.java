package com.example.smy.antest;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.ArrayList;

/**
 * Created by SMY on 2016/6/1.
 */
public class ContactsReadTest {

    private static final Uri CONTACTS_URI = ContactsContract.Contacts.CONTENT_URI;
    private static final Uri PHONES_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    private static final Uri EMAIL_URI = ContactsContract.CommonDataKinds.Email.CONTENT_URI;

    private static final String _ID = ContactsContract.Contacts._ID;
    private static final String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
    private static final String HAS_PNUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
    private static final String CONTACT_ID = ContactsContract.Data.CONTACT_ID;

    private static final String PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    private static final String PHONE_TYPE = ContactsContract.CommonDataKinds.Phone.TYPE;
    private static final String EMAIL_DATA = ContactsContract.CommonDataKinds.Email.DATA;
    private static final String EMAIL_TYPE = ContactsContract.CommonDataKinds.Email.TYPE;

    private ArrayList<String> names  = new ArrayList<String>();
    private ArrayList<String> phones = new ArrayList<String>();
    private ArrayList<String> emails = new ArrayList<String>();
    String displayName = "";

    public String getDisplayName()
    {
        return displayName;
    }

    public ArrayList<String> getPhoneContacts()
    {
        return phones;
    }

    public ArrayList<String> getEmailContacts()
    {
        return emails;
    }

    public void testReadContacts(ContentResolver resolver)
    {
        //ContentResolver resolver = getContext().getContentResolver();
        Cursor c = resolver.query(CONTACTS_URI, null, null, null, null);
        while(c.moveToNext())
        {
            int _id = c.getInt(c.getColumnIndex(_ID));
            displayName = c.getString(c.getColumnIndex(DISPLAY_NAME));

            String selection = CONTACT_ID + "=" + _id;

            //get phone numbers
            int hasPhoneNumber = c.getInt(c.getColumnIndex(HAS_PNUMBER));
            if(hasPhoneNumber > 0)
            {
                Cursor phc = resolver.query(PHONES_URI, null, selection, null, null);
                while(phc.moveToNext())
                {
                    String phoneNumber = phc.getString(phc.getColumnIndex(PHONE_NUMBER));
                    int phoneType = phc.getInt(phc.getColumnIndex(PHONE_TYPE));
                    phones.add(getPhoneTypeById(phoneType) + " : " + phoneNumber);
                }
                phc.close();
            }

            //get emails
            Cursor emc = resolver.query(EMAIL_URI, null, selection, null, null);
            while(emc.moveToNext())
            {
                String emailData = emc.getString(emc.getColumnIndex(EMAIL_DATA));
                int emailType = emc.getInt(emc.getColumnIndex(EMAIL_TYPE));
                emails.add(getEmailTypeById(emailType) + " : " + emailData);
            }
            emc.close();
        }
        c.close();
    }

    private String getPhoneTypeById(int id)
    {
        switch (id)
        {
            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME: return "home";
            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE: return "mobile";
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK: return "work";
            default:return "none";
        }
    }

    private String getEmailTypeById(int id)
    {
        switch (id)
        {
            case ContactsContract.CommonDataKinds.Email.TYPE_HOME: return "home";
            case ContactsContract.CommonDataKinds.Email.TYPE_OTHER: return "other";
            case ContactsContract.CommonDataKinds.Email.TYPE_WORK: return "work";
            default:return "none";
        }
    }
}
