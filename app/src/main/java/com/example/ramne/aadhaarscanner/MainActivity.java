package com.example.ramne.aadhaarscanner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;

public class MainActivity extends AppCompatActivity {

    String uid,name;
    EditText id_number,id_name,email,mobile;
    LinearLayout fields;
    Button scan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);

        id_number=(EditText)findViewById(R.id.id_number);
        id_name=(EditText)findViewById(R.id.id_name);
        email=(EditText)findViewById(R.id.email);
        mobile=(EditText)findViewById(R.id.mobile);

        fields=(LinearLayout)findViewById(R.id.fields);
        scan=(Button)findViewById(R.id.scan);

        Spinner dropdown = (Spinner)findViewById(R.id.spinner);
        String[] items = new String[]{ "PAN Card","Aadhaar Card", "Passport"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                //String selectedItem = parent.getItemAtPosition(position).toString();
                if(position==1)
                {
                    fields.setVisibility(View.GONE);
                    scan.setVisibility(View.VISIBLE);

                }
                else
                {
                    fields.setVisibility(View.VISIBLE);
                    scan.setVisibility(View.GONE);
                    id_number.setText("");
                    id_name.setText("");
                    email.setText("");
                    mobile.setText("");
                }
            } // to close the onItemSelected
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });


    }

    public void scanNow( View view){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Scan a Aadharcard QR Code");
        integrator.setResultDisplayDuration(500);
        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.initiateScan();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //retrieve scan result
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult != null) {
            //we have a result
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();

            // process received data
            if(scanContent != null && !scanContent.isEmpty()){
                processScannedData(scanContent);
            }else{
                Toast toast = Toast.makeText(getApplicationContext(),"Scan Cancelled", Toast.LENGTH_SHORT);
                toast.show();
            }

        }else{
            Toast toast = Toast.makeText(getApplicationContext(),"No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    protected void processScannedData(String scanData){
        Log.d("Ram",scanData);
        XmlPullParserFactory pullParserFactory;

        try {
            // init the parserfactory
            pullParserFactory = XmlPullParserFactory.newInstance();
            // get the parser
            XmlPullParser parser = pullParserFactory.newPullParser();

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new StringReader(scanData));

            // parse the XML
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_DOCUMENT) {
                    Log.d("Ram","Start document");
                } else if(eventType == XmlPullParser.START_TAG && "PrintLetterBarcodeData".equals(parser.getName())) {
                    // extract data from tag
                    //uid
                    uid = parser.getAttributeValue(null,"uid");
                    //name
                    name = parser.getAttributeValue(null,"name");

                } else if(eventType == XmlPullParser.END_TAG) {
                    Log.d("Ram","End tag "+parser.getName());

                } else if(eventType == XmlPullParser.TEXT) {
                    Log.d("Ram","Text "+parser.getText());

                }
                // update eventType
                eventType = parser.next();
            }

            // display the data on screen
            displayScannedData();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void displayScannedData(){
        scan.setVisibility(View.GONE);
        fields.setVisibility(View.VISIBLE);

        // clear old values if any
        id_number.setText("");
        id_name.setText("");

        // update UI Elements
        id_number.setText(uid);
        id_name.setText(name);
    }


}
