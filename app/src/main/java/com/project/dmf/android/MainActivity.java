package com.project.dmf.android;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.project.dmf.config.R;

public class MainActivity extends Activity {

    Button btnOn, btnOff;
    TextView textSpace;
    Handler bluetoothIn;

    final int handlerState = 0;        				 //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    static SQLiteDatabase db;
    private static String TABLE_FILA = "CREATE TABLE IF NOT EXISTS fila (" +
            "dados VARCHAR," +
            "enviado VARCHAR" +
            ");";

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Link the buttons and textViews to respective views
        textSpace = (TextView) findViewById(R.id.textSpace);

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                Date dia = new Date();
                Calendar cal = Calendar.getInstance();
                cal.setTime(dia);
                Date dataAtual = cal.getTime();
                String DataCompleta = ("&DataHora=" + dateFormat.format(dataAtual));

                if (msg.what == 0) {                                     //if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);                                      //keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
                        textSpace.setText("Data Received = " + dataInPrint);
                        //  int dataLength = dataInPrint.length();                          //get length of data received
                        //txtStringLength.setText("String Length = " + String.valueOf(dataLength));

                        if (recDataString.charAt(0) == '@')                             //if it starts with # we know it is what we are looking for
                        {

                            String[] sp = recDataString.toString().split("#");

                            String campo1 = sp[1];
                            String campo2 = sp[2];
                            String campo3 = sp[3];

                            if (!campo1.equals(null)) {
                                String concatenaString = sp[0] + sp[1] + sp[2] + sp[3] + DataCompleta + sp[4];
                                textSpace.setText(concatenaString);
                                //excutePost("http://www.rotaonline.com.br/Routing.PerforMAXXI.Temperatura.API/Temp", concatenaString);
                            }
                            if (!campo2.equals(null)) {
                                String concatenaString2 = sp[0] + sp[1] + sp[2] + sp[3] + DataCompleta + sp[4];
                                textSpace.setText(concatenaString2);
                                //excutePost("http://www.rotaonline.com.br/Routing.PerforMAXXI.Temperatura.API/Temp", concatenaString2);
                            }
                            if (!campo3.equals(null)) {
                                String concatenaString3 = sp[0] + sp[1] + sp[2] + sp[3] + DataCompleta + sp[4];
                                textSpace.setText(concatenaString3);
                                //excutePost("http://www.rotaonline.com.br/Routing.PerforMAXXI.Temperatura.API/Temp", concatenaString3);
                            }
                        } else {
                            //Cursor cursor = consultaBanco();

                            String[] sp = recDataString.toString().split("#");

                            String campo1 = sp[1];
                            String campo2 = sp[2];
                            String campo3 = sp[3];

                            if (!campo1.equals(null)) {
                                String concatenaString = sp[0] + sp[1] + sp[2] + sp[3] + DataCompleta + sp[4];
                                textSpace.setText(concatenaString);
                                //excutePost("http://www.rotaonline.com.br/Routing.PerforMAXXI.Temperatura.API/Temp", concatenaString);
                            }
                            if (!campo2.equals(null)) {
                                String concatenaString2 = sp[0] + sp[1] + sp[2] + sp[3] + DataCompleta + sp[4];
                                textSpace.setText(concatenaString2);
                                //excutePost("http://www.rotaonline.com.br/Routing.PerforMAXXI.Temperatura.API/Temp", concatenaString2);
                            }
                            if (!campo3.equals(null)) {
                                String concatenaString3 = sp[0] + sp[1] + sp[2] + sp[3] + DataCompleta + sp[4];
                                textSpace.setText(concatenaString3);
                                //excutePost("http://www.rotaonline.com.br/Routing.PerforMAXXI.Temperatura.API/Temp", concatenaString3);
                            }

                        }
                        recDataString.delete(0, recDataString.length());                    //clear all string data
                        // strIncom = " ";
                        dataInPrint = " ";
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();


        // Set up onClick listeners for buttons to send 1 or 0 to turn on/off LED
       /* btnOff.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("0");    // Send "0" via Bluetooth
                Toast.makeText(getBaseContext(), "Turn off LED", Toast.LENGTH_SHORT).show();
            }
        });

        btnOn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("1");    // Send "1" via Bluetooth
                Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
            }
        });*/
    }




    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);        	//read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }

    //metodo de chamar um toast
    private void msgToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private static void salvaBanco(String insert) {
        //recebe uma string como parametro, no caso a string concatenada inteira, pra armazenar no banco
        db.execSQL("INSERT INTO fila VALUES(" +
                "'" + insert + " " +
                "N');");
    }

    private static Cursor consultaBanco() {
        Cursor c = db.rawQuery("SELECT * FROM lista WHERE enviado = 'N'", null);
        //retorna a query
        return c;
    }

    private void updateBanco() {
        //recebe uma string como parametro, no caso a string que faltou enviar quando estava sem conexao
        db.execSQL("UPDATE FROM lista " +
                "SET enviado = 'S'" +
                "WHERE envaido = 'N'");
    }

    private void deleteBanco() {
        //recebe uma string como parametro, no caso a string que faltou enviar quando estava sem conexao
        db.execSQL("DELETE FROM lista " +
                "WHERE envaido = 'S'", null);
    }


    //metodo e tratamento POST
    public static String excutePost(String targetURL, String urlParameters) {
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

            if (connection.equals(true)) {
                connection.setFixedLengthStreamingMode(postParameters.getBytes().length); // A STRING VAI AQUI
                PrintWriter out = new PrintWriter(connection.getOutputStream());
                out.print(postParameters);
                out.close();

                postParameters.isEmpty();

            } else {
                //salvaBanco(concatenaString);
            }

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