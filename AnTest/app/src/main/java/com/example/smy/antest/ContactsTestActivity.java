package com.example.smy.antest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ContactsTestActivity extends Activity implements DialogInterface.OnClickListener {

    private String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_test);

        ((TextView)(findViewById(R.id.tvContactsOutMsg))).setMovementMethod(new ScrollingMovementMethod());
    }

    public void onClickReadPhoneNumberBtn(View v)
    {
        testReadContacts();
        //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        ((TextView)findViewById(R.id.tvContactsOutMsg)).setText(msg);
    }

    private String name = "";
    private String phome = "";
    private String pmobile = "";
    private String ehome = "";
    private String ework = "";

    public void onClickWriteContactBtn(View v) throws Exception
    {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.add_contact_pop_info, (ViewGroup) findViewById(R.id.llcontactinfo));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Input contact info")
                .setView(layout)
                .setPositiveButton("    OK ", this)
                .setNegativeButton("Cancel ", null)
                .create()
                .show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        if(which == Dialog.BUTTON_POSITIVE)
        {
            AlertDialog ad = (AlertDialog) dialog;
            name = ((EditText) ad.findViewById(R.id.contact_name)).getText().toString();
            phome = ((EditText) ad.findViewById(R.id.contact_phone_home)).getText().toString();
            pmobile = ((EditText) ad.findViewById(R.id.contact_phone_mobile)).getText().toString();
            ehome = ((EditText) ad.findViewById(R.id.contact_email_home)).getText().toString();
            ework = ((EditText) ad.findViewById(R.id.contact_email_work)).getText().toString();
            //Toast.makeText(this, name + "\n" + phome + "\n" + pmobile + "\n" + ehome + "\n" + ework, Toast.LENGTH_LONG).show();

            try
            {
                testWriteContacts(name, phome, pmobile, ehome, ework);
            }
            catch (Exception e)
            {
                Toast.makeText(this, "Exception " + e.toString(), Toast.LENGTH_LONG)
                        .show();
            }

        }
    }

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

    String displayName = "";

    public void testReadContacts()
    {
        msg = "";
        ContentResolver resolver = getContentResolver();
        Cursor c = resolver.query(CONTACTS_URI, null, null, null, null);
        while(c.moveToNext())
        {
            ArrayList<String> phones = new ArrayList<String>();
            ArrayList<String> emails = new ArrayList<String>();
            int _id = c.getInt(c.getColumnIndex(_ID));
            displayName = c.getString(c.getColumnIndex(DISPLAY_NAME));
            msg += displayName;
            msg += "\n";

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
            putToMsg(phones);

            //get emails
            Cursor emc = resolver.query(EMAIL_URI, null, selection, null, null);
            while(emc.moveToNext())
            {
                String emailData = emc.getString(emc.getColumnIndex(EMAIL_DATA));
                int emailType = emc.getInt(emc.getColumnIndex(EMAIL_TYPE));
                emails.add(getEmailTypeById(emailType) + " : " + emailData);
            }
            emc.close();
            putToMsg(emails);

            msg += "\n";
        }
        c.close();
    }

    void putToMsg(ArrayList<String> al)
    {
        for(int i = 0; i < al.size(); ++i)
        {
            msg += al.get(i);
            msg += "\n";
        }
    }

    private String getPhoneTypeById(int id)
    {
        switch (id)
        {
            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME: return "Phone home";
            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE: return "Phone mobile";
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK: return "Phone work";
            default:return "none";
        }
    }

    private String getEmailTypeById(int id)
    {
        switch (id)
        {
            case ContactsContract.CommonDataKinds.Email.TYPE_HOME: return "Email home";
            case ContactsContract.CommonDataKinds.Email.TYPE_OTHER: return "Email other";
            case ContactsContract.CommonDataKinds.Email.TYPE_WORK: return "Email work";
            default:return "none";
        }
    }

    private static final Uri RAW_CONTACTS_URI = ContactsContract.RawContacts.CONTENT_URI;
    //[content://com.android.contacts/data]
    private static final Uri DATA_URI = ContactsContract.Data.CONTENT_URI;

    private static final String ACCOUNT_TYPE = ContactsContract.RawContacts.ACCOUNT_TYPE;
    private static final String ACCOUNT_NAME = ContactsContract.RawContacts.ACCOUNT_NAME;

    private static final String RAW_CONTACT_ID = ContactsContract.Data.RAW_CONTACT_ID;
    private static final String MIMETYPE = ContactsContract.Data.MIMETYPE;

    private static final String NAME_ITEM_TYPE = ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE;
    private static final String TDISPLAY_NAME = ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME;

    private static final String PHONE_ITEM_TYPE = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
    private static final String TPHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    private static final String TPHONE_TYPE = ContactsContract.CommonDataKinds.Phone.TYPE;
    private static final int PHONE_TYPE_HOME = ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
    private static final int PHONE_TYPE_MOBILE = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;

    private static final String EMAIL_ITEM_TYPE = ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE;
    private static final String TEMAIL_DATA = ContactsContract.CommonDataKinds.Email.DATA;
    private static final String TEMAIL_TYPE = ContactsContract.CommonDataKinds.Email.TYPE;
    private static final int EMAIL_TYPE_HOME = ContactsContract.CommonDataKinds.Email.TYPE_HOME;
    private static final int EMAIL_TYPE_WORK = ContactsContract.CommonDataKinds.Email.TYPE_WORK;

    private static final String AUTHORITY = ContactsContract.AUTHORITY;

    public void testWriteContacts(String name, String phome, String pmobile, String ehome, String ework) throws Exception {
        if(name.length() == 0 &&
                phome.length() == 0 &&
                pmobile.length() == 0 &&
                ehome.length() == 0 &&
                ework.length() == 0)
        {
            return ;
        }

        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        ContentProviderOperation operation = ContentProviderOperation.newInsert(RAW_CONTACTS_URI)
                .withValue(ACCOUNT_TYPE, null)
                .withValue(ACCOUNT_NAME, null)
                .build();
        operations.add(operation);

        //添加联系人名称操作
        operation = ContentProviderOperation.newInsert(DATA_URI)
                .withValueBackReference(RAW_CONTACT_ID, 0)
                .withValue(MIMETYPE, NAME_ITEM_TYPE)
                .withValue(TDISPLAY_NAME, name)
                .build();
        operations.add(operation);

        //添加家庭座机号码
        operation = ContentProviderOperation.newInsert(DATA_URI)
                .withValueBackReference(RAW_CONTACT_ID, 0)
                .withValue(MIMETYPE, PHONE_ITEM_TYPE)
                .withValue(TPHONE_TYPE, PHONE_TYPE_HOME)
                .withValue(TPHONE_NUMBER, phome)
                .build();
        operations.add(operation);

        //添加移动手机号码
        operation = ContentProviderOperation.newInsert(DATA_URI)
                .withValueBackReference(RAW_CONTACT_ID, 0)
                .withValue(MIMETYPE, PHONE_ITEM_TYPE)
                .withValue(TPHONE_TYPE, PHONE_TYPE_MOBILE)
                .withValue(TPHONE_NUMBER, pmobile)
                .build();
        operations.add(operation);

        //添加家庭邮箱
        operation = ContentProviderOperation.newInsert(DATA_URI)
                .withValueBackReference(RAW_CONTACT_ID, 0)
                .withValue(MIMETYPE, EMAIL_ITEM_TYPE)
                .withValue(TEMAIL_TYPE, EMAIL_TYPE_HOME)
                .withValue(TEMAIL_DATA, ehome)
                .build();
        operations.add(operation);

        //添加工作邮箱
        operation = ContentProviderOperation.newInsert(DATA_URI)
                .withValueBackReference(RAW_CONTACT_ID, 0)
                .withValue(MIMETYPE, EMAIL_ITEM_TYPE)
                .withValue(TEMAIL_TYPE, EMAIL_TYPE_WORK)
                .withValue(TEMAIL_DATA, ework)
                .build();
        operations.add(operation);

        ContentResolver resolver = getContentResolver();
        //批量执行,返回执行结果集
        ContentProviderResult[] results = resolver.applyBatch(AUTHORITY, operations);
    }
}
