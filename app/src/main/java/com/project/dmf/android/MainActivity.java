package com.project.dmf.android;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.project.dmf.config.R;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.*;

public class MainActivity extends ActionBarActivity {

    public static int ENABLE_BLUETOOTH = 1;
    public static int SELECT_PAIRED_DEVICE = 2;
    public static int SELECT_DISCOVERED_DEVICE = 3;

    static TextView statusMessage;
    static TextView textSpace;
    ConnectionThread connect;

    static SQLiteDatabase db;
    private static String TABLE_FILA = "CREATE TABLE IF NOT EXISTS fila (" +
            "dados VARCHAR" +
            ");";

    private static String concatenaString = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //cria os widgets
        statusMessage = (TextView) findViewById(R.id.statusMessage);
        textSpace = (TextView) findViewById(R.id.textSpace);

        //banco de dados
        db = openOrCreateDatabase("dmfDB", Context.MODE_PRIVATE, null);
        db.execSQL(TABLE_FILA);

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            statusMessage.setText("Bluetooth não localizado!");
        } else {
            statusMessage.setText("Bluetooth localizado!");
            if(!btAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH);
                statusMessage.setText("Solicitando ativação do Bluetooth...");
            } else {
                statusMessage.setText("Bluetooth já ativado.");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_bluetooth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == ENABLE_BLUETOOTH) {
            if(resultCode == RESULT_OK) {
                statusMessage.setText("Bluetooth ativado");
            }
            else {
                statusMessage.setText("Bluetooth não ativado!");
            }
        }
        else if(requestCode == SELECT_PAIRED_DEVICE || requestCode == SELECT_DISCOVERED_DEVICE) {
            if(resultCode == RESULT_OK) {
                statusMessage.setText("Você selecionou " + data.getStringExtra("btDevName") + "\n"
                        + data.getStringExtra("btDevAddress"));

                connect = new ConnectionThread(data.getStringExtra("btDevAddress"));
                connect.start();
            }
            else {
                statusMessage.setText("Nenhum dispositivo selecionado.");
            }
        }
    }

    public void searchPairedDevices(View view) {

        Intent searchPairedDevicesIntent = new Intent(this, PairedDevices.class);
        startActivityForResult(searchPairedDevicesIntent, SELECT_PAIRED_DEVICE);
    }

    public void discoverDevices(View view) {

        Intent searchPairedDevicesIntent = new Intent(this, DiscoveredDevices.class);
        startActivityForResult(searchPairedDevicesIntent, SELECT_DISCOVERED_DEVICE);
    }

    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");
            StringBuilder dataString = new StringBuilder();
            //String dataString= new String(data);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            Date dia = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(dia);
            Date dataAtual = cal.getTime();
            String DataCompleta = ("&DataHora=" + dateFormat.format(dataAtual));

            if (msg.what == 0) {                                     //if message is what we want
                String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                dataString.append(readMessage);                                      //keep appending to string until ~
                int endOfLineIndex = dataString.indexOf("~");                    // determine the end-of-line
                if (endOfLineIndex > 0) {                                           // make sure there data before ~
                    String dataInPrint = dataString.substring(0, endOfLineIndex);    // extract string
                    textSpace.setText("Data Received = " + dataInPrint);
                    //  int dataLength = dataInPrint.length();                          //get length of data received
                    //txtStringLength.setText("String Length = " + String.valueOf(dataLength));

                    if (dataString.charAt(0) == '@')                             //if it starts with # we know it is what we are looking for
                    {

                        String[] sp = dataString.toString().split("#");

                        String campo1 = sp[1];
                        String campo2 = sp[2];
                        String campo3 = sp[3];

                        if (!campo1.equals(null)) {
                            String concatenaString = sp[0] + sp[1] + sp[2] + sp[3] + DataCompleta + sp[4];
                            textSpace.setText(concatenaString);
                            excutePost("http://www.rotaonline.com.br/Routing.PerforMAXXI.Temperatura.API/Temp", concatenaString);
                        }
                        if (!campo2.equals(null)) {
                            String concatenaString2 = sp[0] + sp[1] + sp[2] + sp[3] + DataCompleta + sp[4];
                            textSpace.setText(concatenaString2);
                            excutePost("http://www.rotaonline.com.br/Routing.PerforMAXXI.Temperatura.API/Temp", concatenaString2);
                        }
                        if (!campo3.equals(null)) {
                            String concatenaString3 = sp[0] + sp[1] + sp[2] + sp[3] + DataCompleta + sp[4];
                            textSpace.setText(concatenaString3);
                            excutePost("http://www.rotaonline.com.br/Routing.PerforMAXXI.Temperatura.API/Temp", concatenaString3);
                        }
                    } else {
                        Cursor cursor = consultaBanco();

                        String[] sp = cursor.toString().split("#");

                        String campo1 = sp[1];
                        String campo2 = sp[2];
                        String campo3 = sp[3];

                        if (!campo1.equals(null)) {
                            String concatenaString = sp[0] + sp[1] + sp[2] + sp[3] + DataCompleta + sp[4];
                            textSpace.setText(concatenaString);
                            excutePost("http://www.rotaonline.com.br/Routing.PerforMAXXI.Temperatura.API/Temp", concatenaString);
                        }
                        if (!campo2.equals(null)) {
                            String concatenaString2 = sp[0] + sp[1] + sp[2] + sp[3] + DataCompleta + sp[4];
                            textSpace.setText(concatenaString2);
                            excutePost("http://www.rotaonline.com.br/Routing.PerforMAXXI.Temperatura.API/Temp", concatenaString2);
                        }
                        if (!campo3.equals(null)) {
                            String concatenaString3 = sp[0] + sp[1] + sp[2] + sp[3] + DataCompleta + sp[4];
                            textSpace.setText(concatenaString3);
                            excutePost("http://www.rotaonline.com.br/Routing.PerforMAXXI.Temperatura.API/Temp", concatenaString3);
                        }

                    }
                    dataString.delete(0, dataString.length());                    //clear all string data
                    // strIncom = " ";
                    dataInPrint = " ";
                }
                //ULTIMO Q FUNCIONO CARAI
            }
        }
    };

    public  boolean verificaConexao() {
        boolean conectado;
        ConnectivityManager conectivtyManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected()) {
            //conectado
            conectado = true;

            //verificar se possui algo no banco

        } else {
            //sem conexao
            conectado = false;

            //include no banco
            //chama o metodo salvaBanco e passa o concatenaString como o parametro q recebe lá no metodo
            salvaBanco(concatenaString);

        }
        return conectado;
    };

    //metodo de chamar um toast
    private void msgToast(String s){
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private void salvaBanco(String insert){
        //recebe uma string como parametro, no caso a string concatenada inteira, pra armazenar no banco
        db.execSQL("INSERT INTO fila VALUES(" +
                "'"+ insert + "');");
    }

    public static Cursor consultaBanco(){
        Cursor c = db.rawQuery("SELECT * FROM lista", null);
        //retorna a query
        return c;
    }

    private void updateBanco(String delete){
        //recebe uma string como parametro, no caso a string que faltou enviar quando estava sem conexao
        db.execSQL("DELETE FROM lista WHERE dados = '" + delete + "'", null);
    }

    //metodo e tratamento POST
    public static String excutePost(String targetURL, String urlParameters)    {
        URL url;
        HttpURLConnection connection = null;
        String postParameters = null;

        try {
            //Conexão
            url = new URL("http://www.rotaonline.com.br/Routing.PerforMAXXI.Temperatura.API/Temp");
            connection =
                    (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setFixedLengthStreamingMode(postParameters.getBytes().length); // A STRING VAI AQUI
            PrintWriter out = new PrintWriter(connection.getOutputStream());
            out.print(postParameters);
            out.close();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return postParameters;
    }
}