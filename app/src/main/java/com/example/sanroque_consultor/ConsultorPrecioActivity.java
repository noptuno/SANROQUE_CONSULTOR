package com.example.sanroque_consultor;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elo.device.DeviceManager;
import com.elo.device.enums.BcrEnableControl;
import com.elo.device.inventory.Inventory;
import com.elo.device.peripherals.BarCodeReader;
import com.elotouch.library.EloPeripheralEventListener;
import com.elotouch.library.EloPeripheralManager;
import com.example.sanroque_consultor.Clases.Producto;
import com.example.sanroque_consultor.ParsearXML.ParserXmlElo;
import com.example.sanroque_consultor.apiadapter.ActivityMonitor;
import com.example.sanroque_consultor.apiadapter.ApiAdapter;
import com.example.sanroque_consultor.apiadapter.ApiAdapterFactory;


import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.IllegalFormatCodePointException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ConsultorPrecioActivity extends AppCompatActivity {
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private boolean boleananimationview = true;
    private boolean boleananimationbusqeudaview = false;
    private boolean boleanlinearprecio = false;
    private OkHttpClient Pickinghttp;
    private String sucursal = "11";
    private String m_ip = "11";
    private Handler m_handler = new Handler(); // Main thread
    private Request RequestPicking;
    private ProgressDialog dialog;
    private Button btnon, btnoff;
    private EditText editcodigobarramanual;
    private boolean lectorHabilitado = false;
    private TextView txtdescripcion1, txtdescripcion2, txtprecioproducto, txtcodigoproducto, txtcodigobarraproducto;
    ActionBar actionBar;
    ConstraintLayout constrain;
    LinearLayout linearprecio;
    ImageView imgescaner;
    LottieAnimationView animationview;
    LottieAnimationView animationbusquedaview;
    boolean visible = false;
    private Inventory inventory;
    private ApiAdapter apiAdapter;
    private TextView txtversion;

    private EloPeripheralManager mEloPeripheralManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consultor_precio);
//TEST
        constrain = findViewById(R.id.constainlayot);
        actionBar = getSupportActionBar();
        linearprecio = findViewById(R.id.linear_precio);
        animationview = findViewById(R.id.animation_view);
        animationbusquedaview = findViewById(R.id.animationbusqueda_view);
        btnon = findViewById(R.id.btn_on);
        btnoff = findViewById(R.id.btn_off);
        editcodigobarramanual = findViewById(R.id.edit_codigo_barra_manual);
        txtdescripcion1 = findViewById(R.id.txt_descripcion_1);
        txtdescripcion2 = findViewById(R.id.txt_descripcion_2);
        txtprecioproducto = findViewById(R.id.txt_precio_producto);
        txtcodigoproducto = findViewById(R.id.txt_codigo_producto);
        txtcodigobarraproducto = findViewById(R.id.txt_codigo_barra_producto);
        txtversion = findViewById(R.id.txt_version);
        constrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hidebarras();
            }
        });

//ULTIMO
        mEloPeripheralManager = new EloPeripheralManager(this, new EloPeripheralEventListener() {
            @Override
            public void onEvent(int i, String s) {
                Log.e("uno", i+ " "+ s);

                conexionApi2023(s);
                editcodigobarramanual.setText("");
            }

            @Override
            public void onEvent(int i, int i1) {
                Log.e("dos", i+ " "+ i1);
            }

            @Override
            public void onEvent(int i) {
                Log.e("tres", ""+i);
            }

        });


        btnon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //  apiAdapter.setBarCodeReaderEnabled(true);

                mEloPeripheralManager.activeBcr();
                mEloPeripheralManager.enableInputEvents();
                //turnOnLaser();
                Log.e("ON", mEloPeripheralManager.toString());

            }
        });

        btnoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //  apiAdapter.setBarCodeReaderEnabled(false);
                //  hidelinearprecio();
                // delayedHide(AUTO_HIDE_DELAY_MILLIS);
                // EnableDialog(true,"mostrando",false);

                mEloPeripheralManager.disactiveBcr();
                mEloPeripheralManager.disableInputEvents();

                Log.e("OFF", mEloPeripheralManager.toString());

            }
        });

            editcodigobarramanual.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    // Verifica si se presionó el botón "Hecho"
                    if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_NONE) {
                        // Aquí colocas el código que deseas ejecutar cuando se presiona "Hecho"
                        presionarboton();
                        return true;
                    }
                    return false;
                }
            });


        hidebarras();
        cargardatos();

        txtversion.setText(String.format("Version: %s", getVersionName()));

      //  inventory();



    }

    public String getVersionName(){
        return BuildConfig.VERSION_NAME;
    }
    private void cargardatos() {

        SharedPreferences pref = getSharedPreferences("CONFCONSULTOR", Context.MODE_PRIVATE);
        String ipp = pref.getString("IP", "11");
        String succ = pref.getString("SUC", "11");
        String configurado = pref.getString("CONFIGURADO", "NO");

        m_ip = ipp;
        sucursal = succ;

        if (m_ip.equals("11") || sucursal.equals("11") || configurado.equals("NO")){

            SharedPreferences.Editor editor = pref.edit();
            editor.putString("IP", "11");
            editor.putString("SUC", "11");
            editor.putString("CONFIGURADO", "NO");
            editor.apply();
            finish();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       // turnBcrOff();
    }

    @Override
    protected void onResume() {
        super.onResume();
       // apiAdapter.getActivityMonitor().onActivityEvent(ActivityMonitor.EVENT_ON_RESUME);

        //updatePaperStatus();

        mEloPeripheralManager.OnResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
      //  apiAdapter.getActivityMonitor().onActivityEvent(ActivityMonitor.EVENT_ON_PAUSE);

        mEloPeripheralManager.OnPause();

    }

    @Override
    protected void onStart() {
        super.onStart();
      //  apiAdapter.getActivityMonitor().onActivityEvent(ActivityMonitor.EVENT_ON_START);
    }

    @Override
    protected void onStop() {
        super.onStop();
       // apiAdapter.getActivityMonitor().onActivityEvent(ActivityMonitor.EVENT_ON_STOP);
    }

    public void turnBcrOff() {
        if (inventory.barCodeReaderEnableControl() == BcrEnableControl.FULL) {
            if (inventory.barCodeReaderSupportsVComMode()) {

                // Can't do much otherwise
                apiAdapter.setBarCodeReaderEnabled(false);
                apiAdapter.setBarCodeReaderCallback(null);

            } else {
                if (apiAdapter.isBarCodeReaderEnabled()) {
                    apiAdapter.setBarCodeReaderEnabled(false);
                }
            }
        }
    }

    private void inventory() {

        inventory = DeviceManager.getInventory(this);

        if (!inventory.isEloSdkSupported()) {
            Toast.makeText(this, "Platform not recognized or supported, sorry", Toast.LENGTH_LONG).show();
        }else{
             //productInfo = DeviceManager.getPlatformInfo();
            // EloPlatform platform = productInfo.eloPlatform;

            apiAdapter = ApiAdapterFactory.getInstance(this).getApiAdapter(inventory);

           // apiAdapter.setBarCodeReaderEnabled(true);

            if (apiAdapter == null) {
                Log.d("TAF", "Cannot find support for this platform");
            }
            Log.e("ENTRO", "AA" + inventory.barCodeReaderEnableControl().toString());

           if (inventory.barCodeReaderEnableControl() == BcrEnableControl.FULL) {
               Log.e("ENTRO", "BB" + inventory.barCodeReaderEnableControl().toString());
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                      //  turnBcrOn();
                    }
                });
            }else{

               editcodigobarramanual.setImeOptions(EditorInfo.IME_ACTION_DONE);
           }

        }

    }


    private void presionarboton() {

        String codigoimprimir = editcodigobarramanual.getText().toString().trim();
        if (!codigoimprimir.equals("")) {
          //  request(codigoimprimir);
            Log.e("leyendo", "3");
            conexionApi2023(codigoimprimir);
            editcodigobarramanual.getText().clear();
            ocultarteclado();
        }
        if (editcodigobarramanual.isFocused()) {
            ocultarteclado();
        }
    }

    public void ocultarteclado() {

        View view = this.getCurrentFocus();

        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        hidebarras();
    }



/*
    private BarCodeReader.BarcodeReadCallback callback = new BarCodeReader.BarcodeReadCallback() {
        @Override
        public void onBarcodeRead(byte[] bytes) {
            String output;
            try {
                output = new String(bytes, "US-ASCII");
            } catch (UnsupportedEncodingException e) {
                output = "--UnReadable--";
            }
            final String outputCopy = output;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("leyendo", "4");
                    conexionApi2023(outputCopy);

                }
            });

        }
    };
*/




    private RequestQueue requestQueue;
    void conexionApi2023(final String codigoverificar) {

        requestQueue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(
                com.android.volley.Request.Method.POST,"http://"+ m_ip +"/WSSREtiquetas/EtiquetaService.asmx",
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            Log.e("leyendo", "2");
                            ParserXmlElo parserXmlPicking = new ParserXmlElo(ConsultorPrecioActivity.this);
                            Document doc = toXmlDocument(response);
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            Source xmlSource = new DOMSource(doc);
                            Result outputTarget = new StreamResult(outputStream);
                            TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
                            InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
                            Producto a = parserXmlPicking.parsear(is);
                            mostrar_datos_view(a);

                            EnableDialog(false, "mostrando", false);


                        } catch (Exception e) {

                            DisplayPrintingStatusMessage("Hubo un problema con los datos..");
                            EnableDialog(false, "limpiando", false);


                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        DisplayPrintingStatusMessage("Error con la conexion Wifi.. Reintentar");
                        EnableDialog(false, "limpiando", false);



                    }
                })
        {
            @Override
            public String getBodyContentType() {
                return "text/xml; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return ("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" +
                        "<soap:Envelope " +
                        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                        "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"" +
                        " xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n  " +
                        "  <soap:Body>\r\n    " +
                        "  <ObtenerDatosArticuloEtiquetas xmlns=\"http://tempuri.org/\">\r\n " +
                        "<p_suc>"+sucursal+"</p_suc>\r\n          " +
                        "  <p_producto>"+codigoverificar+"</p_producto>\r\n   " +
                        "     </ObtenerDatosArticuloEtiquetas>\r\n  " +
                        "  </soap:Body>\r\n" +
                        "</soap:Envelope>").getBytes();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "text/xml");
                headers.put("SOAPAction", "http://tempuri.org/ObtenerDatosArticuloEtiquetas");
                return headers;
            }
        };
        EnableDialog(true, "cargando", false);
        request.setRetryPolicy(new DefaultRetryPolicy(3000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);



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
        visible = true;
    }

    public void EnableDialog(final boolean value, final String mensaje, final Boolean cancelar) {
        m_handler.post(new Runnable() {
            @Override
            public void run() {
                if (mensaje.equals("cargando")) {

                    animationview.setVisibility(View.INVISIBLE);
                    animationbusquedaview.setVisibility(View.VISIBLE);
                    linearprecio.setVisibility(View.INVISIBLE);

                    boleananimationview = false;
                    boleananimationbusqeudaview = true;
                    boleanlinearprecio = false;

                    Log.e("ENTRO A","ENTRO1");

                } else if (mensaje.equals("mostrando")){

                    animationview.setVisibility(View.INVISIBLE);
                    animationbusquedaview.setVisibility(View.INVISIBLE);
                    linearprecio.setVisibility(View.VISIBLE);

                    boleananimationview = false;
                    boleananimationbusqeudaview = false;
                    boleanlinearprecio = true;

                    Log.e("ENTRO B","ENTRO1");
                    ocultando();

                }else{
                    //limpiando
                    animationview.setVisibility(View.VISIBLE);
                    animationbusquedaview.setVisibility(View.INVISIBLE);
                    linearprecio.setVisibility(View.INVISIBLE);

                    boleananimationview = true;
                    boleananimationbusqeudaview = false;
                    boleanlinearprecio = false;

                    Log.e("ENTRO C","ENTRO1");
                }
            }
        });
    }
    private final Handler mHideHandler = new Handler();

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {

            animationview.setVisibility(View.VISIBLE);
            animationbusquedaview.setVisibility(View.INVISIBLE);
            linearprecio.setVisibility(View.INVISIBLE);

            boleananimationview = true;
            boleananimationbusqeudaview = false;
            boleanlinearprecio = false;

            Log.e("ENTRO H2","ENTRO1");
        }
    };



    private void ocultando() {
        Log.e("ENTRO H","ENTRO1");
        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, 20000);
    }

    private static Document toXmlDocument(String str) throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document document = docBuilder.parse(new InputSource(new StringReader(str)));
        return document;

    }

    public void DisplayPrintingStatusMessage(final String MsgStr) {

        m_handler.post(new Runnable() {
            public void run() {
                showToast(MsgStr);//2018 PH
            }// run()
        });

    }

    public void showToast(final String toast) {
        Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
    }

    private void mostrar_datos_view(final Producto a) {


        m_handler.post(new Runnable() {
            public void run() {

                if (a != null) {

                    try {
                        String oferta = "";
                        txtdescripcion1.setText(a.getDescArticulo_1());
                        txtdescripcion2.setText(a.getDescArticulo_2());
                        txtcodigoproducto.setText(a.getCodProd());
                        txtcodigobarraproducto.setText(a.getCodBarras());
                        txtprecioproducto.setText(a.getPrecio());
                        oferta = (a.getTxt_oferta());


                        if (a.getOff_available().toString().equals("N")) {

                            oferta = "";
                            linearprecio.setBackground(ContextCompat.getDrawable(ConsultorPrecioActivity.this, R.drawable.ic_tag_60x30_consultor_impreso));

                        } else {
                            oferta = a.getTxt_oferta();
                            linearprecio.setBackground(ContextCompat.getDrawable(ConsultorPrecioActivity.this, R.drawable.ic_tag_60x30_amarillo));
                        }
                        // showlinearprecio();

                       // DisplayPrintingStatusMessage("correcto");

                    } catch (Exception e) {
                        DisplayPrintingStatusMessage("No disponible");
                        txtdescripcion1.setText("");
                        txtdescripcion2.setText("");
                        txtcodigoproducto.setText("");
                        txtcodigobarraproducto.setText("");
                        txtprecioproducto.setText("");

                        // hidelinearprecio();
                    }


                }
            }// run()
        });


    }

    private native static boolean getJNIBCROn();

    @SuppressWarnings("JniMissingFunction")
    private native static void setJNIBarCodeReader();

    private static final String TAG = "SDK_API_BCR";

    private static final String VERSION = "1.0.1";
    private static final boolean USE_SDK_API = true;

    private EloPeripheralManager mEloManager;


    public String getVersion() {
        Log.d(TAG, "SDK API's BCR layer version: " + VERSION);
        return VERSION;
    }

    public void turnOnLaser()
    {
        Log.i(TAG, "turnOnLaser()");
        if (!USE_SDK_API) {
            setJNIBarCodeReader();
        } else {
            if (!isBcrOn())
                mEloManager.activeBcr();
            else
                mEloManager.disactiveBcr();
        }
    }

    public boolean isBcrOn() {
        boolean bOn;
        if (!USE_SDK_API) {
            bOn = getJNIBCROn();
        } else {
            bOn = mEloManager.isBcrOn();
        }
        Log.i(TAG, "isBcrOn(): " + bOn);
        return bOn;
    }

    static {
        if (!USE_SDK_API) {
            Log.i(TAG, "Loading so");
            System.loadLibrary("barcodereaderjni");
        }
    }
}