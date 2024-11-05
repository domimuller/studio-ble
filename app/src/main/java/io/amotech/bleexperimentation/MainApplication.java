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
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.amotech.bleexperimentation.air.VehiclePayload;

public class MainApplication extends Application {

    public static final int MANUFACTURER_ID = 0xBD60;

    public enum DeviceRole {ROLE_NONE, ROLE_PERIPHERAL, ROLE_CENTRAL}

    public enum AdvKind {ADV_KIND_OFF, ADV_KIND_LEGACY, ADV_KIND_EXTENDED}

    public enum ScanKind {SCAN_KIND_OFF, SCAN_KIND_ON}

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;

    private boolean isAppEnabled;
    private boolean isBluetoothEnabled;
    private DeviceRole deviceRole;
    private AdvKind advKind;
    private ScanKind scanKind;
    private BroadcastReceiver bluetoothReceiver;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeAdvertiser bluetoothAdvertiser;
    private AdvertisingSetCallback advCallback;
    private BluetoothLeScanner bluetoothScanner;
    private ScanCallback scanCallback;

    private byte[] advPayload;


    public MainApplication() {
        deviceRole = DeviceRole.ROLE_NONE;
        advKind = AdvKind.ADV_KIND_OFF;
        scanKind = ScanKind.SCAN_KIND_OFF;
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
                    // todo (if anything)
                } else { // disable
                    setAppEnabled(false);
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

    public ScanKind getScanKind() {
        return scanKind;
    }

    public void setScanKind(ScanKind kind) {
        if (scanKind != kind) { // transition
            switch (kind) {
                case SCAN_KIND_ON:
                    startScanning();
                    break;
                default: // OFF
                    stopScanning();
            }
        }
        scanKind = kind;
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

    private BluetoothLeScanner findBluetoothScanner() {
        return bluetoothAdapter != null ? bluetoothAdapter.getBluetoothLeScanner() : null;
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

        if (advCallback != null) {
            stopAdvertising();
        }

        AdvertisingSetParameters primaryParameters = new AdvertisingSetParameters.Builder()
                .setLegacyMode(true)
                .setDiscoverable(true)
                .setScannable(true) // "legacy advertisement can't be connectable and non-scannable"
                .setConnectable(true)
                .setPrimaryPhy(BluetoothDevice.PHY_LE_1M)
                .setInterval(AdvertisingSetParameters.INTERVAL_HIGH)
                .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_MAX)
                .build();

        AdvertiseData primaryData = new AdvertiseData.Builder()
                // .addManufacturerData(MANUFACTURER_ID, advPayload) // too big, must use scan data!
                .build();

        AdvertiseData scanData = new AdvertiseData.Builder()
                .addManufacturerData(MANUFACTURER_ID, advPayload)
                .build();

        advCallback = new AdvertisingSetCallback() {

            @Override
            public void onAdvertisingSetStarted(AdvertisingSet advertisingSet, int txPower, int status) {
                Log.i("BLE-experiment", "Legacy Bluetooth advertising started: txPower=" + txPower + ", status=" + status);
            }

            @Override
            public void onAdvertisingSetStopped(AdvertisingSet advertisingSet) {
                Log.i("BLE-experiment", "Legacy Bluetooth advertising stopped");
            }

        };

        bluetoothAdvertiser.startAdvertisingSet(
                primaryParameters, primaryData,
                scanData,
                null, null,
                advCallback);

        Log.i("BLE-experiment", "Start of legacy Bluetooth advertising requested");

    }

    private void startExtendedAdvertising() {

        if (bluetoothAdvertiser == null) {
            Log.e("BLE-experiment", "Failed to create Bluetooth advertiser");
            return;
        }

        if (!bluetoothAdapter.isLeExtendedAdvertisingSupported()) {
            Log.e("BLE-experiment", "Extended Bluetooth advertising is NOT supported");
            return;
        }

        if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            Log.e("BLE-experiment", "Failed to obtain Bluetooth permissions");
            return;
        }

        if (advCallback != null) {
            stopAdvertising();
        }

        int secondaryPhy = 0;
        if (bluetoothAdapter.isLeCodedPhySupported()) {
            secondaryPhy = BluetoothDevice.PHY_LE_CODED;
            Log.i("BLE-experiment", "About to start extended Bluetooth advertising (coded PHY)");
        } else {
            secondaryPhy = BluetoothDevice.PHY_LE_1M;
            Log.i("BLE-experiment", "About to start extended Bluetooth advertising (1M PHY)");
        }

        AdvertisingSetParameters primaryParameters = new AdvertisingSetParameters.Builder()
                .setLegacyMode(false)
                .setDiscoverable(true)
                .setScannable(false) // "advertisement can't be both connectable and scannable"
                .setConnectable(true)
                .setPrimaryPhy(BluetoothDevice.PHY_LE_1M)
                .setSecondaryPhy(secondaryPhy)
                .setInterval(AdvertisingSetParameters.INTERVAL_HIGH)
                .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_MAX)
                .build();

        AdvertiseData primaryData = new AdvertiseData.Builder()
                .build();

        Log.i("BLE-experiment", primaryData.toString());

        PeriodicAdvertisingParameters extendedParameters = new PeriodicAdvertisingParameters.Builder()
                .setIncludeTxPower(true)
                .build();

        AdvertiseData extendedData = new AdvertiseData.Builder()
                .addManufacturerData(MANUFACTURER_ID, advPayload)
                .setIncludeTxPowerLevel(true)
                .build();

        Log.i("BLE-experiment", extendedData.toString());

        advCallback = new AdvertisingSetCallback() {

            @Override
            public void onAdvertisingSetStarted(AdvertisingSet advertisingSet, int txPower, int status) {
                Log.i("BLE-experiment", "Extended Bluetooth advertising started: txPower=" + txPower + ", status=" + status);
            }

            @Override
            public void onAdvertisingSetStopped(AdvertisingSet advertisingSet) {
                Log.i("BLE-experiment", "Extended Bluetooth advertising stopped");
            }

        };

        bluetoothAdvertiser.startAdvertisingSet(
                primaryParameters, primaryData,
                extendedData, null, null,
                //extendedParameters, extendedData,
                advCallback);

        Log.i("BLE-experiment", "Start of Extended Bluetooth advertising requested");

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

        if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            Log.e("BLE-experiment", "Failed to obtain Bluetooth permissions");
            return;
        }

        if (advCallback == null) {
            Log.e("BLE-experiment", "No need to stop Bluetooth advertising (not advertising)");
            return;
        }

        // now stop advertising
        bluetoothAdvertiser.stopAdvertisingSet(advCallback);
        advCallback = null;
        Log.i("BLE-experiment", "Stopped Bluetooth advertising");

    }

    private void startScanning() {

        if (bluetoothScanner == null) {
            Log.e("BLE-experiment", "Failed to create Bluetooth scanner");
            return;
        }

        if (scanCallback != null) {
            Log.e("BLE-experiment", "Bluetooth scanner already created");
            return;
        }

        if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.e("BLE-experiment", "Failed to obtain Bluetooth permissions");
            return;
        }

        ArrayList<ScanFilter> scanFilters = new ArrayList<>();
        scanFilters.add(new ScanFilter.Builder().setManufacturerData(MANUFACTURER_ID, new byte[]{8}, new byte[]{-1}).build());
        //scanFilters.add(new ScanFilter.Builder().setDeviceName(null).build());

        ScanSettings scanSettings = new ScanSettings.Builder()
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setLegacy(false) // Set to false to receive extended advertisements (and also legacy!)
                .setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED)
                .build();

        scanCallback = new ScanCallback() {

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

        bluetoothScanner.startScan(scanFilters, scanSettings, scanCallback);
        Log.i("BLE-experiment", "Scanning started");

    }

    private void processScanResult(ScanResult result) {
        ScanRecord scanRecord = result.getScanRecord();
        int sid = result.getAdvertisingSid();
        String addr = result.getDevice() != null ? result.getDevice().getAddress() : "";
        int rssi = result.getRssi();
        int status = result.getDataStatus();
        int interval = result.getPeriodicAdvertisingInterval();
        int primaryPhy = result.getPrimaryPhy();
        int secondaryPhy = result.getSecondaryPhy();
        int txPower = result.getTxPower();
        Log.i("BLE-experiment", String.format("sid=%d addr=%s rssi=%d status=%d interval=%d primaryPhy=%d secondaryPhy=%d txPower=%d",
                sid, addr, rssi, status, interval, primaryPhy, secondaryPhy, txPower));
        if (scanRecord != null) {
            byte[] manufacturerData = scanRecord.getManufacturerSpecificData(MANUFACTURER_ID);
            byte[] advertisementData = scanRecord.getBytes();
            int flags = scanRecord.getAdvertiseFlags();
            int txLevel = scanRecord.getTxPowerLevel();
            if (manufacturerData != null) {
                VehiclePayload payload = VehiclePayload.parse(manufacturerData);
                Log.i("BLE-experiment", String.format("Manu Data: flags=%d tx=%d rec=%s", flags, txLevel, Arrays.toString(manufacturerData)));
                Log.i("BLE-experiment", String.format("Full Data: flags=%d tx=%d rec=%s", flags, txLevel, Arrays.toString(advertisementData)));
                Log.i("BLE-experiment", String.format("Payload: %s", payload));
                // Further processing of manufacturer-specific data
            } else if (advertisementData != null) {
                Log.i("BLE-experiment", String.format("Other Data: flags=%d tx=%d rec=%s", flags, txLevel, Arrays.toString(advertisementData)));
            }
        }
    }

    private void stopScanning() {

        if (bluetoothScanner == null) {
            Log.e("BLE-experiment", "Failed to create Bluetooth scanner");
            return;
        }

        if (scanCallback == null) {
            Log.e("BLE-experiment", "No Bluetooth scanner created");
            return;
        }

        if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.e("BLE-experiment", "Failed to obtain Bluetooth permissions");
            return;
        }

        bluetoothScanner.stopScan(scanCallback);
        scanCallback = null;
        Log.i("BLE-experiment", "Scanning stopped");

    }

    @Override
    public void onCreate() {
        super.onCreate();
        // assign Bluetooth objects
        bluetoothReceiver = createBluetoothReceiver();
        bluetoothAdapter = findBluetoothAdapter();
        bluetoothAdvertiser = findBluetoothAdvertiser();
        bluetoothScanner = findBluetoothScanner();

        // create dummy payload
        VehiclePayload vehiclePayload = VehiclePayload.sample1();
        advPayload = vehiclePayload.getBytesBE(); // 28 bytes
        //advPayload = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1".getBytes(StandardCharsets.UTF_8);
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
