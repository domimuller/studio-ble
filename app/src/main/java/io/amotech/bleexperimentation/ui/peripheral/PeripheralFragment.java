package io.amotech.bleexperimentation.ui.peripheral;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import io.amotech.bleexperimentation.MainApplication;
import io.amotech.bleexperimentation.R;
import io.amotech.bleexperimentation.databinding.FragmentPeripheralBinding;

public class PeripheralFragment extends Fragment {

    private FragmentPeripheralBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inflate view
        PeripheralViewModel peripheralViewModel = new ViewModelProvider(this).get(PeripheralViewModel.class);
        binding = FragmentPeripheralBinding.inflate(inflater, container, false);

        // set buttons from app
        setButtonState("view    ");

        // set up the radio group for advertising mode
        RadioGroup radioGroupAdvertisingMode = binding.radioGroupAdvertisingMode;
        radioGroupAdvertisingMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                MainApplication app = (MainApplication) getActivity().getApplication();
                if (checkedId == R.id.radio_adv_off) {
                    app.setAdvKind(MainApplication.AdvKind.ADV_KIND_OFF);
                } else if (checkedId == R.id.radio_adv_legacy) {
                    app.setAdvKind(MainApplication.AdvKind.ADV_KIND_LEGACY);
                } else if (checkedId == R.id.radio_adv_extended) {
                    app.setAdvKind(MainApplication.AdvKind.ADV_KIND_EXTENDED);
                }
                debug("listener");
            }
        });

        // done
        return binding.getRoot();

    }

    @Override
    public void onResume() {
        super.onResume();
        setButtonState("resume  ");
    }

    private void setButtonState(String tag) {

        // Set up the RadioGroup for Advertising Mode
        RadioGroup radioGroupAdvertisingMode = binding.radioGroupAdvertisingMode;
        RadioButton radioAdvOff = binding.radioAdvOff;
        RadioButton radioAdvLegacy = binding.radioAdvLegacy;
        RadioButton radioAdvExtended = binding.radioAdvExtended;

        // initial state
        MainApplication app = (MainApplication) getActivity().getApplication();
        radioAdvOff.setEnabled(app.isBluetoothEnabled());
        radioAdvLegacy.setEnabled(app.isBluetoothEnabled());
        radioAdvExtended.setEnabled(app.isBluetoothEnabled());
        if (app.getAdvKind() == MainApplication.AdvKind.ADV_KIND_OFF && !radioAdvOff.isChecked()) {
            radioAdvOff.setChecked(app.getAdvKind() == MainApplication.AdvKind.ADV_KIND_OFF);
        }
        if (app.getAdvKind() == MainApplication.AdvKind.ADV_KIND_LEGACY && !radioAdvLegacy.isChecked()) {
            radioAdvLegacy.setChecked(app.getAdvKind() == MainApplication.AdvKind.ADV_KIND_LEGACY);
        }
        if (app.getAdvKind() == MainApplication.AdvKind.ADV_KIND_EXTENDED && !radioAdvExtended.isChecked()) {
            radioAdvExtended.setChecked(app.getAdvKind() == MainApplication.AdvKind.ADV_KIND_EXTENDED);
        }

        // debug output
        debug(tag);

    }

    private void debug(String tag) {
        MainApplication app = (MainApplication) getActivity().getApplication();
        TextView textLog = binding.textLog;
        textLog.setText(textLog.getText() + "\n" + String.format("[%s] enabled: %b%ndevice role: %s%nadv kind: %s", tag, app.isBluetoothEnabled(), app.getDeviceRole(), app.getAdvKind())); // Initialize with empty text or any initial log content
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
