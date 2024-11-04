package io.amotech.bleexperimentation.ui.config;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.switchmaterial.SwitchMaterial;

import io.amotech.bleexperimentation.MainApplication;
import io.amotech.bleexperimentation.R;
import io.amotech.bleexperimentation.databinding.FragmentConfigBinding;

public class ConfigFragment extends Fragment {

    private FragmentConfigBinding binding;
    private boolean isVisible;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ConfigViewModel configViewModel = new ViewModelProvider(this).get(ConfigViewModel.class);

        binding = FragmentConfigBinding.inflate(inflater, container, false);

        View root = binding.getRoot();
        SwitchMaterial switchEnableBle = root.findViewById(R.id.switch_enable_ble);
        RadioGroup radioGroupBleMode = root.findViewById(R.id.radio_group_ble_mode);
        RadioButton radioRoleNone = root.findViewById(R.id.radio_role_none);
        RadioButton radioRolePeripheral = root.findViewById(R.id.radio_role_peripheral);
        RadioButton radioRoleCentral = root.findViewById(R.id.radio_role_central);

        // initial state
        MainApplication app = (MainApplication) getActivity().getApplication();
        switchEnableBle.setChecked(app.isAppEnabled());
        radioRoleNone.setEnabled(app.isAppEnabled());
        radioRoleNone.setChecked(app.getDeviceRole() == MainApplication.DeviceRole.ROLE_NONE);
        radioRolePeripheral.setEnabled(app.isAppEnabled());
        radioRolePeripheral.setChecked(app.getDeviceRole() == MainApplication.DeviceRole.ROLE_PERIPHERAL);
        radioRoleCentral.setEnabled(app.isAppEnabled());
        radioRoleCentral.setChecked(app.getDeviceRole() == MainApplication.DeviceRole.ROLE_CENTRAL);

        switchEnableBle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isVisible) {
                    // set status to app
                    app.setAppEnabled(isChecked);
                    app.setBluetoothDeviceRole(MainApplication.DeviceRole.ROLE_NONE);
                    app.setAdvKind(MainApplication.AdvKind.ADV_KIND_OFF);
                    // update switch
                    switchEnableBle.setChecked(app.isAppEnabled());
                    // update radio buttons
                    radioRoleNone.setEnabled(app.isAppEnabled());
                    radioRoleNone.setChecked(app.getDeviceRole() == MainApplication.DeviceRole.ROLE_NONE);
                    radioRolePeripheral.setEnabled(app.isAppEnabled());
                    radioRolePeripheral.setChecked(app.getDeviceRole() == MainApplication.DeviceRole.ROLE_PERIPHERAL);
                    radioRoleCentral.setEnabled(app.isAppEnabled());
                    radioRoleCentral.setChecked(app.getDeviceRole() == MainApplication.DeviceRole.ROLE_CENTRAL);
                }
            }
        });

        radioGroupBleMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (isVisible) {
                    app.setAdvKind(MainApplication.AdvKind.ADV_KIND_OFF);
                    if (checkedId == R.id.radio_role_none) {
                        app.setBluetoothDeviceRole(MainApplication.DeviceRole.ROLE_NONE);
                    } else if (checkedId == R.id.radio_role_peripheral) {
                        app.setBluetoothDeviceRole(MainApplication.DeviceRole.ROLE_PERIPHERAL);
                    } else if (checkedId == R.id.radio_role_central) {
                        app.setBluetoothDeviceRole(MainApplication.DeviceRole.ROLE_CENTRAL);
                    }
                }
            }
        });

        return root;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume(); // first  statement!
        isVisible = true; // last statement!
    }

    @Override
    public void onPause() {
        isVisible = false; // first  statement!
        super.onPause(); // last statement!
    }

}