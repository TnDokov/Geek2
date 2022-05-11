package com.example.geek;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class Device extends AppCompatActivity {
    private static final String TAG ="Device";
    private static final boolean D = false;

    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_devices_list2);

        setResult(Activity.RESULT_CANCELED);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this,R.layout.device_name);
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this,R.layout.device_name);

        ListView pairedListView = (ListView)findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        ListView newDevicesListView = (ListView)findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        IntentFilter lf = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver,lf);

        lf = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver,lf);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if(pairedDevices.size() > 0){
            for(BluetoothDevice device : pairedDevices){
                if (device.getName().contains("DWL")){
                    mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
        }else{
            String noDevices = getResources().getText(R.string.nodeviceswasfound).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    doDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mBluetoothAdapter != null){
            mBluetoothAdapter.cancelDiscovery();
        }
        this.unregisterReceiver(mReceiver);
    }

    private void doDiscovery() {

        setProgressBarIndeterminateVisibility(true);
        setTitle(getResources().getText(R.string.scanning).toString());
        setTitle("Scanning");

        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }

        mBluetoothAdapter.startDiscovery();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            mBluetoothAdapter.cancelDiscovery();

                    String info = ((TextView) view).getText().toString();
                    String model = info.substring(0,7);
                    if(info.contains(getString(R.string.nodeviceswasfound))==false) {
                        if (model.contentEquals("DWL3000")) {
                            Toast.makeText(Device.this, "DWL3000", Toast.LENGTH_SHORT).show();
                        } else if (model.contentEquals("DWL3500")) {
                            Toast.makeText(Device.this, "DWL3500", Toast.LENGTH_SHORT).show();
                        } else if (model.contentEquals("DWL8500")) {
                            Toast.makeText(Device.this, "DWL8500", Toast.LENGTH_SHORT).show();
                        }
                        String address = info.substring(info.length() - 17);

                        Intent intent = new Intent();
                        intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
        }
    };

    private final BroadcastReceiver mReceiver= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    if ((device.getName() != null) && (device.getName().length() > 0)) {
                        if (device.getName().contains("DWL")) {
                            mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());

                        }
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    setProgressBarIndeterminateVisibility(false);
                    setTitle(getResources().getText(R.string.selectdevices).toString());
                    if (mNewDevicesArrayAdapter.getCount() == 0) {
                        String nodevices = getResources().getText(R.string.nodeviceswasfound).toString();
                        mNewDevicesArrayAdapter.add(nodevices);
                    }
                }

            }
        }
    };

    }

