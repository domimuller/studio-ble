package io.amotech.bleexperimentation;

import android.Manifest;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.PeriodicAdvertisingParameters;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class MainApplication extends Application {

    public static final int MANUFACTURER_ID = 0xBD60;

    public enum DeviceRole {ROLE_NONE, ROLE_PERIPHERAL, ROLE_CENTRAL}

    public enum AdvKind {ADV_KIND_OFF, ADV_KIND_LEGACY, ADV_KIND_EXTENDED}

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;

    private boolean isAppEnabled;
    private boolean isBluetoothEnabled;
    private DeviceRole deviceRole;
    private AdvKind advKind;
    private BroadcastReceiver bluetoothReceiver;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeAdvertiser bluetoothAdvertiser;
    private BluetoothLeScanner bluetoothScanner;

    private byte[] advPayload;


    public MainApplication() {
        deviceRole = DeviceRole.ROLE_NONE;
        advKind = AdvKind.ADV_KIND_OFF;
    }

    public boolean isAppEnabled() {
        return isAppEnabled;
    }

    public void setAppEnabled(boolean flag) {
        if (isAppEnabled != flag) { // transition
            if (flag) { // enable
                // todo (if anything)
            } else { // disable
                stopAdvertising();
            }
        }
        isAppEnabled = flag;
    }

    public boolean isBluetoothEnabled() {
        return isBluetoothEnabled;
    }

    public void checkBluetoothEnabled() {
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        } else {
            boolean flag = bluetoothAdapter.isEnabled();
            if (isBluetoothEnabled != flag) { // transition
                if (flag) { // enable
                    // todo
                } else { // disable
                    // todo
                }
            }
            isBluetoothEnabled = flag;
        }
    }

    public DeviceRole getDeviceRole() {
        return deviceRole;
    }

    public AdvKind getAdvKind() {
        return advKind;
    }

    public void setAdvKind(AdvKind kind) {
        if (advKind != kind) { // transition
            switch (kind) {
                case ADV_KIND_EXTENDED:
                    startExtendedAdvertising();
                    break;
                case ADV_KIND_LEGACY:
                    startLegacyAdvertising();
                    break;
                default: // OFF
                    stopAdvertising();
            }
        }
        advKind = kind;
    }

    public void terminate() { // user pressed the "terminate" button
        setAppEnabled(false);
        // todo: terminate the process
    }

    public void setBluetoothDeviceRole(DeviceRole role) {
        if (deviceRole != role) { // transition
            switch (role) {
                case ROLE_PERIPHERAL:
                    break;
                case ROLE_CENTRAL:
                    break;
                default: // NONE
            }
        }
        deviceRole = role;
    }

    private BroadcastReceiver createBluetoothReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:
                            // Bluetooth is turned off
                            checkBluetoothEnabled();
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            // Bluetooth is turning off
                            checkBluetoothEnabled();
                            break;
                        case BluetoothAdapter.STATE_ON:
                            // Bluetooth is turned on
                            checkBluetoothEnabled();
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            // Bluetooth is turning on
                            checkBluetoothEnabled();
                            break;
                        default:
                            // probably an error
                            checkBluetoothEnabled();
                    }
                }
            }
        };
    }

    private BluetoothAdapter findBluetoothAdapter() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        return bluetoothManager != null ? bluetoothManager.getAdapter() : null;
    }

    private BluetoothLeAdvertiser findBluetoothAdvertiser() {
        return bluetoothAdapter != null ? bluetoothAdapter.getBluetoothLeAdvertiser() : null;
    }

    private void startLegacyAdvertising() {

        if (bluetoothAdvertiser == null) {
            Log.e("BLE-experiment", "Failed to create Bluetooth advertiser");
            return;
        }

        if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            Log.e("BLE-experiment", "Failed to obtain Bluetooth permissions");
            return;
        }

        Log.i("BLE-experiment", "Start legacy Bluetooth advertising");

        AdvertisingSetParameters primaryParameters = new AdvertisingSetParameters.Builder()
                .setLegacyMode(true)
                .setDiscoverable(true)
                .setScannable(true)
                .setConnectable(true)
                .setPrimaryPhy(BluetoothDevice.PHY_LE_1M)
                // .setTxPowerLevel(0)
                .build();

        AdvertiseData primaryData = new AdvertiseData.Builder()
                .build();

        AdvertiseData scanData = new AdvertiseData.Builder()
                .addManufacturerData(MANUFACTURER_ID, advPayload)
                //.setIncludeTxPowerLevel(true)
                .build();

        AdvertisingSetCallback callback = new AdvertisingSetCallback() {
            @Override
            public void onAdvertisingSetStarted(AdvertisingSet advertisingSet, int txPower, int status) {
                Log.i("BLE-experiment", "Bluetooth advertising started: txPower=" + txPower + ", status=" + status);
            }

            @Override
            public void onAdvertisingSetStopped(AdvertisingSet advertisingSet) {
                Log.i("BLE-experiment", "Bluetooth advertising stopped");
            }
        };

        bluetoothAdvertiser.startAdvertisingSet(
                primaryParameters, primaryData,
                scanData,
                null, null,
                callback);

    }

    private void startExtendedAdvertising() {

        if (bluetoothAdvertiser == null) {
            Log.e("BLE-experiment", "Failed to create Bluetooth advertiser");
            return;
        }

        if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            Log.e("BLE-experiment", "Failed to obtain Bluetooth permissions");
            return;
        }

        int secondaryPhy = 0;
        if (bluetoothAdapter.isLeCodedPhySupported()) {
            secondaryPhy = BluetoothDevice.PHY_LE_CODED;
            Log.i("BLE-experiment", "Start extended Bluetooth advertising (coded PHY)");
        } else {
            secondaryPhy = BluetoothDevice.PHY_LE_1M;
            Log.i("BLE-experiment", "Start extended Bluetooth advertising (1M PHY)");
        }

        AdvertisingSetParameters primaryParameters = new AdvertisingSetParameters.Builder()
                .setLegacyMode(false)
                .setDiscoverable(true)
                .setScannable(false)
                .setConnectable(true)
                .setPrimaryPhy(BluetoothDevice.PHY_LE_1M)
                .setSecondaryPhy(secondaryPhy)
                .setInterval(AdvertisingSetParameters.INTERVAL_HIGH)
                .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_MAX)
                .build();

        AdvertiseData primaryData = new AdvertiseData.Builder()
                .build();

        Log.d("BLE-experiment", primaryData.toString());

        PeriodicAdvertisingParameters extendedParameters = new PeriodicAdvertisingParameters.Builder()
                .setIncludeTxPower(true)
                .build();

        AdvertiseData extendedData = new AdvertiseData.Builder()
                .addManufacturerData(MANUFACTURER_ID, advPayload)
                .setIncludeTxPowerLevel(true)
                .build();

        Log.d("BLE-experiment", extendedData.toString());

        AdvertisingSetCallback callback = new AdvertisingSetCallback() {
            @Override
            public void onAdvertisingSetStarted(AdvertisingSet advertisingSet, int txPower, int status) {
                Log.i("BLE-experiment", "Bluetooth advertising started: txPower=" + txPower + ", status=" + status);
            }

            @Override
            public void onAdvertisingSetStopped(AdvertisingSet advertisingSet) {
                Log.i("BLE-experiment", "Bluetooth advertising stopped");
            }
        };

        bluetoothAdvertiser.startAdvertisingSet(
                primaryParameters, primaryData,
                null,
                extendedParameters, extendedData,
                callback);

    }

    public void startAdvertising() {
        switch (advKind) {
            case ADV_KIND_EXTENDED:
                startExtendedAdvertising();
                break;
            case ADV_KIND_LEGACY:
                startLegacyAdvertising();
                break;
            default:
                // ignore
        }
    }

    public void stopAdvertising() {

        if (bluetoothAdvertiser == null) {
            Log.e("BLE-experiment", "Failed to create Bluetooth advertiser");
            return;
        }

        Log.i("BLE-experiment", "Stop Bluetooth advertising");

        if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            Log.e("BLE-experiment", "Failed to obtain Bluetooth permissions");
            return;
        }

        bluetoothAdvertiser.stopAdvertisingSet(new AdvertisingSetCallback() {
            @Override
            public void onAdvertisingSetStopped(AdvertisingSet advertisingSet) {
                Log.i("BLE-experiment", "Bluetooth advertising stopped");
            }
        });

    }

    private void startScanning() {

        if (bluetoothScanner == null) {
            Log.e("BLE-experiment", "Failed to create Bluetooth scanner");
            return;
        }

        if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.e("BLE-experiment", "Failed to obtain Bluetooth permissions");
            return;
        }

        ScanCallback scanCallback = new ScanCallback() {

            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                processScanResult(result);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                for (ScanResult result : results) {
                    processScanResult(result);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.e("BLE-experiment", "Scan failed with error: " + errorCode);
            }

        };

        bluetoothScanner.startScan(scanCallback);
        
    }

    private void processScanResult(ScanResult result) {
        ScanRecord scanRecord = result.getScanRecord();
        if (scanRecord != null) {
            byte[] manufacturerData = scanRecord.getManufacturerSpecificData(MANUFACTURER_ID);
            if (manufacturerData != null) {
                Log.i("BLE-experiment", "Manufacturer Data: " + Arrays.toString(manufacturerData));
                // Further processing of manufacturer-specific data
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // assign Bluetooth objects
        bluetoothReceiver = createBluetoothReceiver();
        bluetoothAdapter = findBluetoothAdapter();
        bluetoothAdvertiser = findBluetoothAdvertiser();
        bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();

        // create dummy payload
        advPayload = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes(StandardCharsets.UTF_8); // 26 bytes
        // Register the receiver
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        // Ask for permissions
        Intent intent = new Intent(this, PermissionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        // logging
        Log.i("BLE-experiment", "Application started");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // Unregister the receiver
        unregisterReceiver(bluetoothReceiver);
        // logging
        Log.i("BLE-experiment", "Application stopped");
    }

}
