package io.amotech.bleexperimentation;

import android.app.Application;

public class MainApplication extends Application {

    public enum DeviceRole {ROLE_NONE, ROLE_PERIPHERAL, ROLE_CENTRAL}

    public enum AdvKind {ADV_KIND_OFF, ADV_KIND_LEGACY, ADV_KIND_EXTENDED}

    private boolean isBluetoothEnabled;
    private DeviceRole deviceRole;
    private AdvKind advKind;

    public MainApplication() {
        isBluetoothEnabled = false;
        deviceRole = DeviceRole.ROLE_NONE;
        advKind = AdvKind.ADV_KIND_OFF;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialization code here
    }

    public boolean isBluetoothEnabled() {
        return isBluetoothEnabled;
    }

    public void setBluetoothEnabled(boolean flag) {
        if (isBluetoothEnabled != flag) { // transition
            if (flag) { // enable

            } else { // disable

            }
        }
        isBluetoothEnabled = flag;
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
                    break;
                case ADV_KIND_LEGACY:
                    break;
                default: // OFF
            }
        }
        advKind = kind;
    }

    public void terminate() {
        setBluetoothEnabled(false);
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

}
