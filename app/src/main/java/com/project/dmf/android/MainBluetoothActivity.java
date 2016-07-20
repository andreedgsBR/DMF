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

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.*;

public class MainBluetoothActivity extends ActionBarActivity {

    public static int ENABLE_BLUETOOTH = 1;
    public static int SELECT_PAIRED_DEVICE = 2;
    public static int SELECT_DISCOVERED_DEVICE = 3;

    static TextView statusMessage;
    static TextView textSpace;
    ConnectionThread connect;

    SQLiteDatabase db;
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

    public static Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");
            //String dataString= new String(data);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            Date dia = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(dia);
            Date dataAtual = cal.getTime();
            String DataCompleta = ("&DataHora=" + dateFormat.format(dataAtual));

            try {
                String dataString = new String(data, "UTF-8"); // for UTF-8 encoding
                if(dataString.equals("---N"))
                    statusMessage.setText("Ocorreu um erro durante a conexão!");
                else if(dataString.equals("---S"))
                    statusMessage.setText("Conectado.");
                else {
                    //textSpace.setText(new String(data));

                    dataString.trim();
                    String[] sp = dataString.split("\\|");

                    if (sp[1].equals("") || (!sp[1].equals("Token=") )){
                        sp[1] = "teste";
                    }
                    if (sp[1].equals("Token=") && (sp[2].equals("&CodigoCenario=40")) ){
                        concatenaString = sp[1] + sp[2] + sp[3]+ sp[4] + DataCompleta + sp[5];
                        textSpace.setText(concatenaString);
                    } else {
                        textSpace.setText("Aguardando Dados.");
                    }
                    //ULTIMO Q FUNCIONO CARAI
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
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

    private Cursor consultaBanco(){
        Cursor c = db.rawQuery("SELECT * FROM lista", null);
        //retorna a query
        return c;
    }

    private void updateBanco(String delete){
        //recebe uma string como parametro, no caso a string que faltou enviar quando estava sem conexao
        db.execSQL("DELETE FROM lista WHERE dados = '" + delete + "'", null);
    }
}