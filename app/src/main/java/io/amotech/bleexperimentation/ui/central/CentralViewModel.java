package io.amotech.bleexperimentation.ui.central;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CentralViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public CentralViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is \"Central\" fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}