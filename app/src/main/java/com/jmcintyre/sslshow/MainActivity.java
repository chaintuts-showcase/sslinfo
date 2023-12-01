/* This file contains code for the main Android activity
*
* Author: Josh McIntyre
*/

package com.jmcintyre.sslshow;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/* This class defines the main UI */
public class MainActivity extends AppCompatActivity {

    // Constants
    final String CERTINFO_STRING = "Certificate information: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.showButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Get the hostname information from the field
                EditText editText = findViewById(R.id.hostnamePlainText);
                String hostname = editText.getText().toString();

                // Initialize the SSL info object and get certificate info
                SSLInfo sslInfo = new SSLInfo(hostname);
                String certInfo = sslInfo.getCertInfo();

                // Update the TextView with the certificate info
                TextView showTextView = findViewById(R.id.showTextView);
                showTextView.setText(CERTINFO_STRING + certInfo);
            }
        });
    }


}