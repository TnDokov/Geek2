package com.example.geek;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.geek.databinding.ActivityDashboardBinding;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;

public class Dashboard extends DrawerBaseActivity {
    ActivityDashboardBinding adb;
    private LeDeviceListAdapter leDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning = false;
    private Handler mHandler;
    static Handler mHandler_routine = new Handler();
    private boolean location_enabled = false;

    private static final long SCAN_PERIOD = 500;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;
    private static final int REQUEST_ENABLE_BT = 1;

    private Button btn;
    private TextView tv;

    public void btn_scan(View view){
        isLocationEnabled();
        if(location_enabled){
            if(!mScanning){
                System.out.println("Hey mScanning false");
                scanLeDevice(true);
                mScanning=true;
            }else{
                System.out.println("Hey mScanning True");
                scanLeDevice(false);
                mScanning=false;

                if(leDeviceListAdapter.getCount()>0){
                    leDeviceListAdapter.clear();
                }
            }
        }else{
           new SweetAlertDialog(Dashboard.this,SweetAlertDialog.WARNING_TYPE)
                   .setTitleText("Location ?")
                   .setContentText("Please turn on location for bluetooth scanning")
                   .setConfirmText("OK")
                   .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                       @Override
                       public void onClick(SweetAlertDialog sweetAlertDialog) {
                           sweetAlertDialog.dismissWithAnimation();
                           Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                           startActivity(i);
                       }
                   });
        }

    }

    public void btn_scanagain(View view){
        if(mScanning){
            scanLeDevice(false);
            if(leDeviceListAdapter.getCount()>0){
                leDeviceListAdapter.clear();
            }
            Intent i = getIntent();
            startActivity(i);
        }else{
            if(leDeviceListAdapter.getCount()>0){
                leDeviceListAdapter.clear();
            }
            Intent i = getIntent();
            startActivity(i);
        }
    }



    private void isLocationEnabled() {
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        location_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        adb = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(adb.getRoot());
    }


    protected void onStart() {
        super.onStart();
    }


    protected void onRestart() {
        super.onRestart();
    setContentView(R.layout.activity_dashboard);
    }


    protected void onResume() {
        super.onResume();
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }else{
            if(areLocationServiceEnabled(this)){
                mHandler = new Handler();
                final BluetoothManager bm = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                mBluetoothAdapter = bm.getAdapter();

                if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
                    Toasty.warning(this,"Bluetooth low energy is not supported",Toast.LENGTH_SHORT,true).show();
                    finish();
                }else{
                    if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()){
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
                    }else{
                        leDeviceListAdapter = new LeDeviceListAdapter();
                    }
                }
            }
        }

        btn = (Button)findViewById(R.id.btncancel);
        tv = (TextView)findViewById(R.id.scan_info);

    }


    protected void onPause() {
        super.onPause();
    }

    protected void onStop() {
        super.onStop();
        if(mScanning){
            scanLeDevice(false);
            if(leDeviceListAdapter.getCount()>0){
                leDeviceListAdapter.clear();
            }
        }else{
            try{
                if(leDeviceListAdapter.getCount()>0){
                    leDeviceListAdapter.clear();
                }
            }catch (Exception e){}
        }
    }


    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean areLocationServiceEnabled(Context context) {
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

        try {
            return  lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private void scanLeDevice(final boolean enable) {
        if(enable){
            System.out.println("Hey start scanning");
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            btn.setText(getResources().getText(R.string.cancelscan).toString());
            tv.setText(getResources().getText(R.string.scan_info).toString());
            mScanning = true;
        }else{
            System.out.println("Hey stop scanning");
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            btn.setText(getResources().getText(R.string.startscan).toString());
            tv.setText(getResources().getText(R.string.start_scan_info).toString());
            mScanning = false;
        }
    }


    final Runnable r = new Runnable() {
        public void run() {
            try{

                if(leDeviceListAdapter.getCount() > 0)
                {
                    showDevice();
                }

                mHandler_routine.postDelayed(this, SCAN_PERIOD);
            }
            catch(Exception e){ }
        }
    };
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int i, byte[] bytes) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String devicename = device.getName();
                            if(devicename == null) devicename = "";
                            if(devicename.contains("DWL") || devicename.contains("Digi")) {
                                leDeviceListAdapter.addDevice(device);
                                leDeviceListAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            };


    private void showDevice() {
        if(leDeviceListAdapter.getCount()>0){
            setContentView(R.layout.activity_device);

            ListView lv = (ListView)findViewById(R.id.list_view);
            lv.setAdapter(leDeviceListAdapter);
            lv.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String add = leDeviceListAdapter.getDevice(i).getAddress();
                    String name = leDeviceListAdapter.getDevice(i).getName();
                    if(mScanning){
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        mScanning = false;
                        mHandler_routine.removeCallbacks(r);
                    }

                    if(name == null){
                        return ;
                    }else if(name.contains("Digi")){
                        Toast.makeText(Dashboard.this, "Produk Digi Boss", Toast.LENGTH_SHORT).show();
                    }else if(name.contains("DWL1300")){
                        Toast.makeText(Dashboard.this, "Produk DWL1300XY", Toast.LENGTH_SHORT).show();
                    }else if(name.contains("DWL1500")){
                        Toast.makeText(Dashboard.this, "Produk DWL1500XY", Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }
    }


    public class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflater;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflater = Dashboard.this.getLayoutInflater();
        }

        public void clear() {
            mLeDevices.clear();
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        @Override
        public int getCount() {
            if (mLeDevices == null) {
                return 0;
            } else {
                return mLeDevices.size();
            }
        }

        @Override
        public Object getItem(int i) {
            if (mLeDevices == null) {
                return null;
            }
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final ViewHolder holder;
            if (view == null) {
                view = mInflater.inflate(R.layout.activity_device_list, null);
                holder = new ViewHolder();
                holder.deviceName = (TextView) view.findViewById(R.id.device_name);
                holder.deviceModel = (TextView) view.findViewById(R.id.device_model);
                holder.deviceSerialNumber = (TextView) view.findViewById(R.id.device_serial_number);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0) {
                String deviceNameFull = String.valueOf(deviceName);
                String deviceNamePart = deviceNameFull.substring(deviceNameFull.length() - 8, deviceNameFull.length());

                if (deviceName.contains("Digi")) {
                    holder.deviceName.setText(R.string.smart_cube_name);
                    holder.deviceModel.setText(R.string.smart_cube_model);
                } else if (deviceName.contains("DWL1")) {
                    holder.deviceName.setText(R.string.machinist_name);
                    holder.deviceModel.setText(R.string.machinist_model);
                } else if (deviceName.contains("DWL3")) {
                    holder.deviceName.setText(R.string.level_sync_name);
                    holder.deviceModel.setText(R.string.level_sync_model);
                }
            }else {
                    holder.deviceName.setText("Unknown Device");
                }
                return view;
        }
    }

    public static class ViewHolder {
        TextView deviceName;
        TextView deviceModel;
        TextView deviceSerialNumber;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mScanning)
            {
                scanLeDevice(false);
                if(leDeviceListAdapter.getCount() > 0)
                {
                    leDeviceListAdapter.clear();
                }
            }
            else{
                if(leDeviceListAdapter.getCount() > 0)
                {
                    leDeviceListAdapter.clear();
                }
            }

            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
