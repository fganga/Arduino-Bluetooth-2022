package cl.inacaptemuco.arduinobt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnDispositivos;
    BluetoothAdapter btAdapter;
    TextView txvStatus,txvTemperatura;

    public String address = null;
    BluetoothSocket btSocket = null;
    static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean btConnected = false;
    public ProgressDialog progressDialog;
    private InputStream inputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Adaptador bluetooth

        vincularElementos();
        habilitarListener();
        iniciarBluetooth();
    }

    private void iniciarBluetooth() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        final Intent intento = getIntent();
        address = intento.getStringExtra(DispositivosActivity.DISPOSITIVO);

        if (!btAdapter.isEnabled()){
            txvStatus.setText("No conectado");
        }else{
            txvStatus.setText("Conectado");
        }
        //Permisos
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);

        new ConectaBT().execute();

        txvTemperatura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // txvTemperatura.setVisibility(View.INVISIBLE);

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while(true){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i("MSG","write start");
                                        if(btSocket != null){
                                            try {
                                               InputStream tmpIn = null;
                                                tmpIn = btSocket.getInputStream();
                                                DataInputStream mmInStream = new DataInputStream(tmpIn);
                                                Log.i("recibido",mmInStream.toString());
                                                byte[] buffer = new byte[256];
                                                int bytes = mmInStream.read(buffer);
                                                String readMessage = new String(buffer, 0, bytes);
                                                Log.i("recibido","TMP "+readMessage);
                                                txvTemperatura.setText(readMessage);
                                               // inputStream = btSocket.getInputStream();
                                                //byte [] buffer = new byte[2];
                                                //int temperatura = inputStream.read(buffer);
                                                //Log.i("recibido","TMP "+temperatura);


                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        Log.i("MSG","write end");
                                    }
                                });
                                Thread.sleep(1000);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                t.start();
            }
        });


    }

    private void habilitarListener() {
        btnDispositivos.setOnClickListener(this);
        //txvTemperatura.setOnClickListener(this);
    }

    private void vincularElementos() {
        btnDispositivos = findViewById(R.id.btn_dispositivos);
        txvStatus = findViewById(R.id.txv_status);
        txvTemperatura = findViewById(R.id.txv_temperatura);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_dispositivos) {
            Intent intento = new Intent(MainActivity.this, DispositivosActivity.class);
            startActivity(intento);
        }


    }

    private void mostrarTemperatura() {
        //txvTemperatura.setVisibility(View.INVISIBLE);
        Toast.makeText(MainActivity.this, "Esperando...", Toast.LENGTH_SHORT).show();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (btSocket != null){
                                    try {
                                        inputStream = btSocket.getInputStream();
                                        byte [] buffer = new byte[2];
                                        int temperatura = inputStream.read(buffer);
                                        String temperatura_string = new String(buffer,0,temperatura);
                                        txvTemperatura.setText(temperatura_string);
                                        Toast.makeText(MainActivity.this, "Temperatura" + temperatura_string, Toast.LENGTH_SHORT).show();

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                        });
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
                thread.start();
    }

    private class ConectaBT extends AsyncTask<Void,Void,Void>{

        private boolean conectado = true;
        @Override
        protected void onPreExecute(){
            progressDialog = progressDialog.show(MainActivity.this,"Conexión Bluetooth","Conectando....");
        }

        @SuppressLint("MissingPermission")
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if(btSocket == null || !btConnected){
                    BluetoothDevice dispositivoBT = btAdapter.getRemoteDevice(address);
                    btSocket = dispositivoBT.createRfcommSocketToServiceRecord(mUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (Exception e) {
                conectado = false;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void resultado){
            super.onPostExecute(resultado);
            if(!conectado){
                Toast.makeText(MainActivity.this, "Sin Conexión", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(MainActivity.this, "Conectado....", Toast.LENGTH_SHORT).show();

            }
            progressDialog.dismiss();
        }
    }
    private void desconectar(){
        if (btSocket != null){
            try {
                btSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}