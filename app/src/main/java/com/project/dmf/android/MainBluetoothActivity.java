package com.project.dmf.android;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
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

public class MainBluetoothActivity extends ActionBarActivity {

    public static int ENABLE_BLUETOOTH = 1;
    public static int SELECT_PAIRED_DEVICE = 2;
    public static int SELECT_DISCOVERED_DEVICE = 3;

    static TextView statusMessage;
    static TextView textSpace;
    ConnectionThread connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Array pra pegar oq ta guardado no banco
        ArrayList<String> dados = null;
        DB_Manager dbManager = new DB_Manager(this);

        for (int i=0; i<10; i++) {
            dbManager.addItens("Item " + 1);
        }

        dados = dbManager.getAllItens();

        //cria os widgets
        statusMessage = (TextView) findViewById(R.id.statusMessage);
        textSpace = (TextView) findViewById(R.id.textSpace);

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
            String DataCompleta = dateFormat.format(dataAtual);

            try {
                String dataString = new String(data, "UTF-8"); // for UTF-8 encoding
                if(dataString.equals("---N"))
                    statusMessage.setText("Ocorreu um erro durante a conexão!");
                else if(dataString.equals("---S"))
                    statusMessage.setText("Conectado.");
                else {
                    //textSpace.setText(new String(data));

                    String[] sp = dataString.split("\\|");
                    textSpace.setText(sp[1] + "\n"
                            + sp[2] + "\n"
                            + sp[3] + "\n"
                            + sp[4] + "\n"
                            + DataCompleta + "\n"
                            + sp[5]);

                    textSpace.setText(dataString);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

    public  boolean verificaConexao() {
        boolean conectado;
        ConnectivityManager conectivtyManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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

        }
        return conectado;
    };

    /*//metodo de chamar um toast
    private void msg(String s){
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }*/

}