package io.amotech.bleexperimentation.ui.peripheral;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PeripheralViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public PeripheralViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is \"Peripheral\" fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}