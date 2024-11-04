package io.amotech.bleexperimentation.ui.peripheral;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import io.amotech.bleexperimentation.MainApplication;
import io.amotech.bleexperimentation.R;
import io.amotech.bleexperimentation.databinding.FragmentPeripheralBinding;

public class PeripheralFragment extends Fragment {

    private FragmentPeripheralBinding binding;
    private boolean isVisible;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inflate view
        PeripheralViewModel peripheralViewModel = new ViewModelProvider(this).get(PeripheralViewModel.class);
        binding = FragmentPeripheralBinding.inflate(inflater, container, false);

        // set up the radio group for advertising mode
        RadioGroup radioGroupAdvertisingMode = binding.radioGroupAdvertisingMode;
        radioGroupAdvertisingMode.setOnCheckedChangeListener((group, checkedId) -> {
            if (isVisible) {
                MainApplication app = (MainApplication) getActivity().getApplication();
                if (checkedId == R.id.radio_adv_off) {
                    app.setAdvKind(MainApplication.AdvKind.ADV_KIND_OFF);
                } else if (checkedId == R.id.radio_adv_legacy) {
                    app.setAdvKind(MainApplication.AdvKind.ADV_KIND_LEGACY);
                } else if (checkedId == R.id.radio_adv_extended) {
                    app.setAdvKind(MainApplication.AdvKind.ADV_KIND_EXTENDED);
                }
            }
        });

        // done
        return binding.getRoot();

    }


    private void setButtonState() {

        // Set up the RadioGroup for Advertising Mode
        RadioButton radioAdvOff = binding.radioAdvOff;
        RadioButton radioAdvLegacy = binding.radioAdvLegacy;
        RadioButton radioAdvExtended = binding.radioAdvExtended;

        // initial state
        MainApplication app = (MainApplication) getActivity().getApplication();
        radioAdvOff.setEnabled(app.isAppEnabled());
        radioAdvLegacy.setEnabled(app.isAppEnabled());
        radioAdvExtended.setEnabled(app.isAppEnabled());
        if (app.getAdvKind() == MainApplication.AdvKind.ADV_KIND_OFF && !radioAdvOff.isChecked()) {
            radioAdvOff.setChecked(app.getAdvKind() == MainApplication.AdvKind.ADV_KIND_OFF);
        }
        if (app.getAdvKind() == MainApplication.AdvKind.ADV_KIND_LEGACY && !radioAdvLegacy.isChecked()) {
            radioAdvLegacy.setChecked(app.getAdvKind() == MainApplication.AdvKind.ADV_KIND_LEGACY);
        }
        if (app.getAdvKind() == MainApplication.AdvKind.ADV_KIND_EXTENDED && !radioAdvExtended.isChecked()) {
            radioAdvExtended.setChecked(app.getAdvKind() == MainApplication.AdvKind.ADV_KIND_EXTENDED);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume(); // first  statement!
        setButtonState();
        isVisible = true; // last statement!
    }

    @Override
    public void onPause() {
        isVisible = false; // first  statement!
        super.onPause(); // last statement!
    }

}
