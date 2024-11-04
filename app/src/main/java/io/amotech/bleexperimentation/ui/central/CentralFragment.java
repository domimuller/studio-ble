package io.amotech.bleexperimentation.ui.central;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import io.amotech.bleexperimentation.databinding.FragmentCentralBinding;

public class CentralFragment extends Fragment {

    private FragmentCentralBinding binding;
    private boolean isVisible;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CentralViewModel centralViewModel =
                new ViewModelProvider(this).get(CentralViewModel.class);

        binding = FragmentCentralBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textCentral;
        centralViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
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