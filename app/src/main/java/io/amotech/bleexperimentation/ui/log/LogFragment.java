package io.amotech.bleexperimentation.ui.log;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import io.amotech.bleexperimentation.databinding.FragmentLogBinding;

public class LogFragment extends Fragment {

    private FragmentLogBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogViewModel logViewModel = new ViewModelProvider(this).get(LogViewModel.class);

        binding = FragmentLogBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textLog;

        textView.setMovementMethod(new ScrollingMovementMethod());

        // Enable dragging to scroll
        textView.setOnTouchListener(new View.OnTouchListener() {
            float downX, downY;
            int initialScrollX, initialScrollY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getX();
                        downY = event.getY();
                        initialScrollX = textView.getScrollX();
                        initialScrollY = textView.getScrollY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = downX - event.getX();
                        float deltaY = downY - event.getY();

                        // Calculate new scroll positions
                        int scrollX = initialScrollX + (int) deltaX;
                        int scrollY = initialScrollY + (int) deltaY;

                        // Ensure scrolling does not go beyond the top-left corner
                        if (scrollX < 0) scrollX = 0;
                        if (scrollY < 0) scrollY = 0;

                        // Ensure scrolling does not go beyond the content width and height
                        int maxScrollX = Math.max(0, textView.getLayout().getWidth() - textView.getWidth());
                        int maxScrollY = Math.max(0, textView.getLayout().getHeight() - textView.getHeight());

                        if (scrollX > maxScrollX) scrollX = maxScrollX;
                        if (scrollY > maxScrollY) scrollY = maxScrollY;

                        textView.scrollTo(scrollX, scrollY);
                        return true;
                    case MotionEvent.ACTION_UP:
                        // Call performClick when the touch event is released
                        textView.performClick();
                        return true;
                }
                return false;
            }

        });


        // Example log text
        textView.setText("This is a log output example.\nLine 2\nLine 3\n...");

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}