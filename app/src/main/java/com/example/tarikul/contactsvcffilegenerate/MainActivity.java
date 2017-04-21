package com.example.tarikul.contactsvcffilegenerate;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.text.VCardReader;
import ezvcard.property.FormattedName;
import ezvcard.property.Telephone;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
public class MainActivity extends AppCompatActivity {
    Button btnPickContact ;
    TextView txtVContactNumber;
    public final int RQS_PICK_CONTACT =1;
    String mContactFileName = "";
    File vcfFile ;
    String name="";
    String number = "";
    String typeMobile = "";
    String typeHome = "";
    String typeWork = "";
    String typeWorkMobile = "";
    String typeOther = "";
    String typeMain = "";
    String typeWorkFax = "";
    String typeHomeFax = "";
    String typePager = "";

    // Declare
    static final int PICK_CONTACT=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnPickContact =(Button)findViewById(R.id.pickcontact);
        txtVContactNumber = (TextView) findViewById(R.id.contactnumber);
        btnPickContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType(Phone.CONTENT_ITEM_TYPE);
//                startActivityForResult(intent, 1);
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, RQS_PICK_CONTACT);

                //getVcardString();
                //readVCF();

       //         ReadPhoneContacts(MainActivity.this);
            }
        });

        try {
            readVCF();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //generateContactsVcard();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RQS_PICK_CONTACT) {
            if (resultCode == RESULT_OK) {

                Uri contactData = data.getData();
                Cursor cursor = managedQuery(contactData, null, null, null, null);

                Integer contactsCount = cursor.getCount(); // get how many contacts you have in your contacts list
                if (contactsCount > 0)
                {
                    while(cursor.moveToNext())
                    {
                        String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                        name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                        {
                            //the below cursor will give you details for multiple contacts
                            Cursor pCursor =getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                                    new String[]{id}, null);
                            // continue till this cursor reaches to all phone numbers which are associated with a contact in the contact list
                            while (pCursor.moveToNext())
                            {
                                int phoneType 		= pCursor.getInt(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

                                number 	= pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                //you will get all phone numbers according to it's type as below switch case.
                                //Logs.e will print the phone number along with the name in DDMS. you can use these details where ever you want.
                                switch (phoneType)
                                {
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                                        Log.e(name + ": TYPE_MOBILE", " " + number);
                                        typeMobile =number;
                                        //number = typeMobile;
                                        break;
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                                        Log.e(name + ": TYPE_HOME", " " + number);
                                       typeHome = number;
                                       // number = typeHome;
                                        break;
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                                        Log.e(name + ": TYPE_WORK", " " + number);
                                        typeWork = number;
                                       // number = typeWork;
                                        break;
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
                                        Log.e(name + ": TYPE_MAIN", " " + number);
                                        typeMain = number;
                                       // number = typeWorkMobile;
                                        break;
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
                                        Log.e(name + ": TYPE_FAX_WORK", " " + number);
                                        typeWorkFax = number;
                                        // number = typeWorkMobile;
                                        break;
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
                                        Log.e(name + ": TYPE_FAX_HOME", " " + number);
                                        typeHomeFax = number;
                                        // number = typeWorkMobile;
                                        break;
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER:
                                        Log.e(name + ": TYPE_PAGER", " " + number);
                                        typePager = number;
                                        // number = typeWorkMobile;
                                        break;
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
                                        Log.e(name + ": TYPE_OTHER", " " + number);
                                        typeOther = number;
                                       // number = typeOther;
                                        break;
                                    default:
                                        break;
                                }
                            }
                            pCursor.close();
                        }

                    }

                }
                String randomFileName = generateContactFile();
                setContactFilename(randomFileName);
                vcfFile =new File(randomFileName);

                 //setContactFilename(vcfFile);
                try{
                    FileWriter fw = new FileWriter(vcfFile);
                    fw.write("BEGIN:VCARD\r\n");
                    fw.write("VERSION:2.1\r\n");
                    fw.write("N:" + name + "\r\n");
                    fw.write("FN:" + name + "\r\n");
                    fw.write("TEL;CELL:" + typeMobile + "\r\n");
                    fw.write("TEL;TYPE=HOME,VOICE:" + typeHome + "\r\n");
                    fw.write("TEL;TYPE=WORK,VOICE:" + typeWork + "\r\n");
                    fw.write("TEL;TYPE=MAIN,VOICE:" + typeMain + "\r\n");
                    fw.write("TEL;TYPE=TYPE_FAX_WORK,VOICE:" + typeWorkFax + "\r\n");
                    fw.write("TEL;TYPE=TYPE_FAX_HOME,VOICE:" + typeHomeFax + "\r\n");
                    fw.write("TEL;TYPE=TYPE_PAGER,VOICE:" + typePager + "\r\n");
                    fw.write("TEL;TYPE=TYPE_OTHER,VOICE:" + typeOther + "\r\n");
                    fw.write("END:VCARD"+"\r\n");
                    fw.close();


                }catch (Exception e){
                    e.printStackTrace();
                }
                cursor.close();


            }
        }
    }
    //create contacts vcard file
    public void generateContactsVcard() {

    }

    private String generateContactFile() {

        String root = Environment.getExternalStorageDirectory().getPath();
        File myDir = new File(root + "/Contacts");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }

        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "contacts" + n + ".vcf";
        //File file = new File(myDir, fname);
        //mVoiceFileName = file.getAbsolutePath();
        mContactFileName = myDir.getAbsolutePath() + "/" + fname;


        return mContactFileName;
    }

    private String getContactFilename() {
        return mContactFileName;
    }

    private void setContactFilename(String filename) {
        mContactFileName = filename;
    }
    public void readVCF() throws IOException {

        File file = new File(Environment.getExternalStorageDirectory()+"/contacts8644.vcf");
        VCardReader reader = new VCardReader(file);
        try {
            VCard vcard;

            while ((vcard = reader.readNext()) != null) {
                FormattedName fn = vcard.getFormattedName();
                String name = (fn == null) ? null : fn.getValue();
                txtVContactNumber.setText(name);
            }
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            reader.close();
        }

    }


//    public void ReadPhoneContacts(Context cntx) //This Context parameter is nothing but your Activity class's Context
//    {
//        Cursor cursor = cntx.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
//        Integer contactsCount = cursor.getCount(); // get how many contacts you have in your contacts list
//        if (contactsCount > 0)
//        {
//            while(cursor.moveToNext())
//            {
//                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
//                String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
//                {
//                    //the below cursor will give you details for multiple contacts
//                    Cursor pCursor = cntx.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
//                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
//                            new String[]{id}, null);
//                    // continue till this cursor reaches to all phone numbers which are associated with a contact in the contact list
//                    while (pCursor.moveToNext())
//                    {
//                        int phoneType 		= pCursor.getInt(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
//                        //String isStarred 		= pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED));
//                        String phoneNo 	= pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                        //you will get all phone numbers according to it's type as below switch case.
//                        //Logs.e will print the phone number along with the name in DDMS. you can use these details where ever you want.
//                        switch (phoneType)
//                        {
//                            case Phone.TYPE_MOBILE:
//                                Log.e(contactName + ": TYPE_MOBILE", " " + phoneNo);
//                                break;
//                            case Phone.TYPE_HOME:
//                                Log.e(contactName + ": TYPE_HOME", " " + phoneNo);
//                                break;
//                            case Phone.TYPE_WORK:
//                                Log.e(contactName + ": TYPE_WORK", " " + phoneNo);
//                                break;
//                            case Phone.TYPE_WORK_MOBILE:
//                                Log.e(contactName + ": TYPE_WORK_MOBILE", " " + phoneNo);
//                                break;
//                            case Phone.TYPE_OTHER:
//                                Log.e(contactName + ": TYPE_OTHER", " " + phoneNo);
//                                break;
//                            default:
//                                break;
//                        }
//                    }
//                    pCursor.close();
//                }
//            }
//            cursor.close();
//        }
//    }
}
