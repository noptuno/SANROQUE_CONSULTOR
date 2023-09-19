package com.example.sanroque_consultor;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.sanroque_consultor.R;
import com.example.sanroque_consultor.sdkstar.Communication;
import com.starmicronics.starioextension.ICommandBuilder;
import com.starmicronics.starioextension.StarIoExt;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MainDispensador extends AppCompatActivity {

    private Button btnimprimir;
    private String m_printerMAC = null;
    ConstraintLayout constrain;
    ActionBar actionBar;
    byte[] printData;
    private ProgressDialog mProgressDialog;
    SharedPreferences pref;
    private int numero = 00;
    private TextView numeromostrar;

    private Context context;

    private LottieAnimationView animacion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dispensador);
        constrain = findViewById(R.id.constraintdis);
        btnimprimir = findViewById(R.id.btnimprimir);
        numeromostrar = findViewById(R.id.txtNum);


        animacion = findViewById(R.id.animation_view2);


        btnimprimir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                imprimirNumero();

            }
        });

        Bundle parametros = getIntent().getExtras();

        if (parametros != null) {
            m_printerMAC = (parametros.getString("mac"));

        } else {
            Toast.makeText(getApplicationContext(), "No hay datos a mostrar", Toast.LENGTH_LONG).show();
        }



        actionBar = getSupportActionBar();

        hidebarras();

        constrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hidebarras();
            }
        });

        context = getApplicationContext();




    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        pref = getSharedPreferences("CONTADOR", Context.MODE_PRIVATE);
        numero = Integer.parseInt(pref.getString("NUMERO", "0"));
        numeromostrar.setText(""+numero);
        hidebarras();

    }

    void imcrementar(){

        if (numero<99){

            numero++;
            pref = getSharedPreferences("CONTADOR", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("NUMERO", String.valueOf(numero));
            editor.apply();
            numeromostrar.setText(""+ numero);

        }else{

            numero = 0;
            pref = getSharedPreferences("CONTADOR", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("NUMERO", String.valueOf(numero));
            editor.apply();
            numeromostrar.setText(""+ numero);
        }

    }

    void hidebarras() {
        constrain.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private void imprimirNumero() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());
        Date date = new Date();

        String fecha = dateFormat.format(date);

        animacion.setVisibility(View.VISIBLE);
        btnimprimir.setEnabled(false);

        Charset encoding = Charset.forName("CP437");

        byte[] nombreproducto= "Su Turno es: ".getBytes(encoding);
        byte[] numeroimprimir = (""+numero).getBytes();

        Bitmap starLogoImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.san_logo);

        ICommandBuilder builder = StarIoExt.createCommandBuilder(StarIoExt.Emulation.StarPRNT);
        //builder.appendCodePage(ICommandBuilder.CodePageType.CP437);
        builder.beginDocument();

        builder.appendBitmap(starLogoImage, false);
        builder.appendLineFeed();

        //*********************************
        builder.appendAlignment(ICommandBuilder.AlignmentPosition.Center);
        builder.appendMultiple(1, 1);
        builder.appendAbsolutePosition(nombreproducto,0);
        builder.appendLineFeed();
        builder.appendLineSpace(50);
        builder.appendAlignment(ICommandBuilder.AlignmentPosition.Center);
        builder.appendMultiple(10, 10);
        builder.appendAbsolutePosition(numeroimprimir,0);
        builder.appendLineFeed();
        builder.appendAlignment(ICommandBuilder.AlignmentPosition.Center);

        builder.appendMultiple(0, 0);
        builder.appendAbsolutePosition(("Fecha: " + fecha).getBytes(),0);
        builder.appendLineFeed();
        //**********************

        builder.appendCutPaper(ICommandBuilder.CutPaperAction.PartialCutWithFeed);
        builder.endDocument();

        printData = builder.getCommands();

        Communication.sendCommands(this, printData, "BT:" + m_printerMAC, "", 500, 30000, MainDispensador.this, mCallback);     // 10000mS!!!
    }

    private final Communication.SendCallback mCallback = new Communication.SendCallback() {
        @Override
        public void onStatus(Communication.CommunicationResult communicationResult) {

            String a =  communicationResult.getResult().toString();;
            Log.e("Mensaje", a);

            if (a.equals("Success")){

                Toast.makeText(MainDispensador.this,
                        "Impresión Correcta",
                        Toast.LENGTH_SHORT).show();
                imcrementar();

            }else if (a.equals("ErrorOpenPort")){

                Toast.makeText(MainDispensador.this,
                        "Error por Bluetooth",
                        Toast.LENGTH_SHORT).show();
            }else if(a.equals("ErrorBeginCheckedBlock")){

                Toast.makeText(MainDispensador.this,
                        "Error Tapa Abierta",
                        Toast.LENGTH_SHORT).show();
            }else if(a.equals("ErrorEndCheckedBlock")) {

                Toast.makeText(MainDispensador.this,
                        "Error por interrupción",
                        Toast.LENGTH_SHORT).show();

            }else{

                Toast.makeText(MainDispensador.this,
                        "Error Desconocido",
                        Toast.LENGTH_SHORT).show();
            }

                btnimprimir.setEnabled(true);
                animacion.setVisibility(View.INVISIBLE);

                Log.e("mensaje", "cerrar dialog");


        }
    };


}