package com.example.walter.mobileapp.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.walter.mobileapp.R;
import com.example.walter.mobileapp.utility.StaticInstance;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.zxing.WriterException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class CreateQRCode extends AppCompatActivity {

    String m_Text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_qrcode);

        //controllo se la funzione è stata invocata da un utente che deve scannerizzare il codice o
        //da un organizzatore che deve mostrare il qr code
        boolean haveToScan = getIntent().getBooleanExtra("scan", false);

        if (!haveToScan) {
            String code = getIntent().getStringExtra("code");
            QRGEncoder qrgEncoder = new QRGEncoder(code, null, QRGContents.Type.TEXT, 200);
            try {
                Bitmap bitmap = qrgEncoder.encodeAsBitmap();
                ((ImageView) findViewById(R.id.qr)).setImageBitmap(bitmap);
                ((TextView) findViewById(R.id.code)).setText(code);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        } else {
            AlertDialog.Builder alertadd = new AlertDialog.Builder(this);
            alertadd.setTitle("Choose the way to confirm:");
            alertadd.setPositiveButton("Scan", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    new IntentIntegrator(getActivity()).initiateScan();
                }
            }).setNegativeButton("Insert code", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Title");

                    final EditText input = new EditText(getActivity());
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    builder.setView(input);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            m_Text = input.getText().toString();
                            confirm(m_Text);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();

                }
            });
            alertadd.show();
        }
    }

    private Activity getActivity() {
        return this;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                confirm(result.getContents());
            }
        }
    }

    //aggiorno sul db la conferma in base al qr scannerizzato
    private void confirm(final String contents) {
        StaticInstance.db.collection("matches").document(contents).update("confirmed", FieldValue.arrayUnion(StaticInstance.username)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent intent = new Intent(getApplicationContext(), RatePlayer.class);
                intent.putExtra("matchcode", contents);
                startActivity(intent);
            }
        });

    }
}
