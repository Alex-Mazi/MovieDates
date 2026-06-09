package com.example.moviedates.util;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.moviedates.R;
import com.google.android.material.button.MaterialButton;

public class ThankYouDialog extends DialogFragment {

    public interface OnContinueListener {
        void onContinue();
    }

    private OnContinueListener listener;

    public void setOnContinueListener(OnContinueListener listener) {
        this.listener = listener;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_thank_you, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        setCancelable(false);

        MaterialButton btn = view.findViewById(R.id.dialogButton);
        btn.setOnClickListener(v -> {
            dismiss();
            if (listener != null) listener.onContinue();
        });

    }

    @Override
    public void onStart() {

        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.88);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

    }

}
