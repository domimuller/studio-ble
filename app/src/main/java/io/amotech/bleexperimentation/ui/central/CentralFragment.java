package io.amotech.bleexperimentation.ui.central;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import io.amotech.bleexperimentation.MainApplication;
import io.amotech.bleexperimentation.R;
import io.amotech.bleexperimentation.databinding.FragmentCentralBinding;

public class CentralFragment extends Fragment {

    private FragmentCentralBinding binding;
    private boolean isVisible;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CentralViewModel centralViewModel = new ViewModelProvider(this).get(CentralViewModel.class);
        binding = FragmentCentralBinding.inflate(inflater, container, false);

        // set up the radio group for advertising mode
        RadioGroup radioGroupScanningMode = binding.radioGroupScanningMode;
        radioGroupScanningMode.setOnCheckedChangeListener((group, checkedId) -> {
            if (isVisible) {
                MainApplication app = (MainApplication) getActivity().getApplication();
                if (checkedId == R.id.radio_scan_off) {
                    app.setScanKind(MainApplication.ScanKind.SCAN_KIND_OFF);
                } else if (checkedId == R.id.radio_scan_on) {
                    app.setScanKind(MainApplication.ScanKind.SCAN_KIND_ON);
                }
            }
        });

        // done
        return binding.getRoot();

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