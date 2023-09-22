package com.example.sanroque_consultor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sanroque_consultor.dispensador.Configuracion.DMRPrintSettings;
import com.example.sanroque_consultor.dispensador.Configuracion.DOPrintMainActivity;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.FirmwareUpdateEvent;
import com.zebra.scannercontrol.IDcsSdkApiDelegate;
import com.zebra.scannercontrol.SDKHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Button iniciarconsultor;
    Button iniciardis;
    Button btnbt, btnrestablecer;
    EditText ip,suc,numero;
    SharedPreferences pref;

    String ApplicationConfigFilename = "applicationconfigsanroque.dat";
    private String m_printerMode = null;
    private String m_printerMAC = null;
    private String m_ip = null;
    private String m_sucursal;
    private TextView txtsucursal,txtmac;
    private String password;
    private SharedPreferences sharedPref;
    private int m_printerPort = 515;
    private static final int REQUEST_PICK_CONFIGURACION = 2;
    DMRPrintSettings g_appSettings = new DMRPrintSettings("", 0, 0, "0", "0", "0", 0);
    private int m_configurado;
    String fechainicio;

    public static SDKHandler sdkHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        txtmac = findViewById(R.id.txtmacmostrar);
        iniciarconsultor = findViewById(R.id.btniniciar);
        iniciardis = findViewById(R.id.btniniciardispen);
        btnbt = findViewById(R.id.btnbluetooth);
        ip = findViewById(R.id.edit_ip);
        suc = findViewById(R.id.edit_sucursal);
        numero = findViewById(R.id.numeroeditable);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date date2 = new Date();
        fechainicio = dateFormat.format(date2);





        iniciardis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (m_configurado != 0) {

                    Intent intent2 = new Intent(MainActivity.this, MainDispensador.class);
                    intent2.putExtra("mac", m_printerMAC);

                    pref = getSharedPreferences("CONTADOR", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();

                    if (numero.getText().toString().isEmpty() || numero.getText().toString().equals("") || numero.getText().toString().equals(" ") ){
                        editor.putString("NUMERO", "0");
                        numero.setText("0");
                    }else{
                        editor.putString("NUMERO", numero.getText().toString());
                    }

                    editor.apply();
                    GuardarFecha();
                    startActivity(intent2);

                } else {

                    Toast.makeText(MainActivity.this, "Debe configurar las opciones de la aplicacion", Toast.LENGTH_SHORT).show();

                }


            }
        });

        btnbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, DOPrintMainActivity.class);
                startActivityForResult(intent, REQUEST_PICK_CONFIGURACION);

            }
        });


        iniciarconsultor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

        if (!ip.getText().equals("") && !suc.getText().equals("")){
            Intent intent2 = new Intent(MainActivity.this, ConsultorPrecioActivity.class);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("IP", ip.getText().toString());
            editor.putString("SUC", suc.getText().toString());
            editor.putString("CONFIGURADO", "SI");
            editor.apply();
            startActivity(intent2);
        }

            }
        });

        pref = getSharedPreferences("CONFCONSULTOR", Context.MODE_PRIVATE);
        String ipp = pref.getString("IP", "11");
        String succ = pref.getString("SUC", "11");
        String configurado = pref.getString("CONFIGURADO", "NO");
        ip.setText(ipp);
        suc.setText(succ);

        if (configurado.equals("SI")){
            Intent intent2 = new Intent(MainActivity.this, ConsultorPrecioActivity.class);
            startActivity(intent2);
        }


    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
       // cargardatos();
       // ValidarFecha();
    }


  void ValidarFecha(){

        pref = getSharedPreferences("CONTADOR", Context.MODE_PRIVATE);
        String ultimafecha = pref.getString("FECHA", "no/no/no");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date date = new Date();

        String fechaactual = dateFormat.format(date);

        if (!ultimafecha.equals(fechaactual)){
            numero.setText("0");
        }
    }

    void GuardarFecha(){

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date date = new Date();

        String fechaactual = dateFormat.format(date);

        pref = getSharedPreferences("CONTADOR", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("FECHA", fechaactual);
        editor.apply();
    }

    public void cargardatos() {


        DMRPrintSettings appSettings = ReadApplicationSettingFromFile();

        if (appSettings != null) {

            g_appSettings = appSettings;
            m_printerMAC = g_appSettings.getPrinterMAC();
            m_printerMode = g_appSettings.getSelectedPrintMode();//2018 PH
            m_sucursal = g_appSettings.getSuc();
            m_ip = g_appSettings.getIpwebservice();
            m_configurado = g_appSettings.getConfigurado();
            txtmac.setText(m_printerMAC);

        } else {

            Toast.makeText(MainActivity.this,
                    "Debe configurar las opciones de la aplicación",
                    Toast.LENGTH_SHORT).show();

        }


        pref = getSharedPreferences("CONTADOR", Context.MODE_PRIVATE);
        numero.setText(pref.getString("NUMERO", "00"));



    }
    public void showToast(final String toast) {
        Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
    }

    DMRPrintSettings ReadApplicationSettingFromFile() {
        DMRPrintSettings ret = null;
        InputStream instream;

        try {

            instream = openFileInput(ApplicationConfigFilename);

        } catch (FileNotFoundException e) {

            Log.e("DOPrint", e.getMessage(), e);
            showToast("Configurar");

            return null;
        }

        try {
            ObjectInputStream ois = new ObjectInputStream(instream);

            try {

                ret = (DMRPrintSettings) ois.readObject();

            } catch (ClassNotFoundException e) {

                Log.e("DOPrint", e.getMessage(), e);
                ret = null;

            }

        } catch (Exception e) {

            Log.e("DOPrint", e.getMessage(), e);
            ret = null;

        } finally {

            try {

                if (instream != null)
                    instream.close();

            } catch (IOException ignored) {
            }

        }
        return ret;
    }

}