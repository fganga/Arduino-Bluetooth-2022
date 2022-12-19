package cl.inacaptemuco.arduinobt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class DispositivosActivity extends AppCompatActivity {
    BluetoothAdapter btAdapter;
    ListView lstDispositivos;
    public static String DISPOSITIVO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispositivos);

        vincularElementos();
        iniciarBluetooth();
        cargarDispositivos();
    }

    private void vincularElementos() {
        lstDispositivos = findViewById(R.id.lst_dispositivos);
    }

    @SuppressLint("MissingPermission")
    private void cargarDispositivos() {
        Toast.makeText(getApplicationContext(), "Cargando...", Toast.LENGTH_SHORT).show();

        @SuppressLint("MissingPermission") Set<BluetoothDevice> dispositivosBT = btAdapter.getBondedDevices();
        final ArrayList listaDispositivos = new ArrayList();

        if(dispositivosBT.size() > 0){
            Toast.makeText(getApplicationContext(), "Dispositivos encontrados :" + dispositivosBT.size(), Toast.LENGTH_SHORT).show();
            for (BluetoothDevice dispositivo : dispositivosBT){
                listaDispositivos.add(dispositivo.getName() + " \n " + dispositivo.getAddress());
            }
        }else{
            Toast.makeText(getApplicationContext(), "No hay dispositivos cercanos", Toast.LENGTH_SHORT).show();
        }
        ArrayAdapter<String> adapterBT = new ArrayAdapter<>(DispositivosActivity.this, android.R.layout.simple_list_item_1,listaDispositivos);
        lstDispositivos.setAdapter(adapterBT);
        lstDispositivos.setOnItemClickListener(dispositivoSeleccionado);

    }
    private AdapterView.OnItemClickListener dispositivoSeleccionado = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            String nombre = ((TextView) view).getText().toString();
            String address = nombre.substring(nombre.length()-17);
            Intent intento = new Intent(DispositivosActivity.this,MainActivity.class);
            intento.putExtra(DISPOSITIVO,address);
            startActivity(intento);
        }
    };

    private void iniciarBluetooth() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }
}