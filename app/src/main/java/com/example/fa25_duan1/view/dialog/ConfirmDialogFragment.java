package com.example.fa25_duan1.view.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.fa25_duan1.R;
import com.google.android.material.button.MaterialButton;

public class ConfirmDialogFragment extends DialogFragment {
    private String title, content;
    private OnConfirmListener listener;
    TextView tvTitle, tvContent;

    public ConfirmDialogFragment(String title, String content, OnConfirmListener listener) {
        this.title = title;
        this.content = content;
        this.listener = listener;
    }

    // 1. Định nghĩa một Interface để giao tiếp
    public interface OnConfirmListener {
        void onConfirmed();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Chuyển dp sang pixel
            int width = (int) (350 * getResources().getDisplayMetrics().density);
            int height = (int) (300 * getResources().getDisplayMetrics().density);
            getDialog().getWindow().setLayout(width, height);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp layout fragment_logout.xml
        View view = inflater.inflate(R.layout.dialog_confirm, container, false);
        if (title != null && content != null) {
            tvTitle = view.findViewById(R.id.tvTitle);
            tvContent = view.findViewById(R.id.tvContent);

            tvTitle.setText(title);
            tvContent.setText(content);
        }

        // Thiết lập nền dialog trong suốt để thấy bo góc
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton btnNo = view.findViewById(R.id.btn_no);
        MaterialButton btnYes = view.findViewById(R.id.btn_yes);

        // Nút "KHÔNG" -> Chỉ cần tắt dialog
        btnNo.setOnClickListener(v -> {
            dismiss();
        });

        // Nút "CÓ" -> Báo cho listener và tắt dialog
        btnYes.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConfirmed(); // 3. Gửi sự kiện
            }
            dismiss();
        });
    }
}