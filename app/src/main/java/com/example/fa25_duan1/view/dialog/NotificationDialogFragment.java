package com.example.fa25_duan1.view.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.fa25_duan1.R;
import com.google.android.material.button.MaterialButton;

public class NotificationDialogFragment extends DialogFragment {

    public static final String TYPE_SUCCESS = "success";
    public static final String TYPE_ERROR = "error";

    private static final String ARG_TYPE = "DIALOG_TYPE";
    private static final String ARG_TITLE = "TITLE";
    private static final String ARG_MESSAGE = "MESSAGE";
    private static final String ARG_BUTTON = "BUTTON";

    private String dialogType;
    private String title;
    private String message;
    private String buttonText;

    public interface DialogActionHandler {
        void onConfirm();
    }

    private DialogActionHandler actionHandler;

    // ============================
    // CREATE NEW INSTANCE (CÓ HANDLER)
    // ============================
    public static NotificationDialogFragment newInstance(
            String title,
            String message,
            String buttonText,
            String type,
            DialogActionHandler handler
    ) {
        NotificationDialogFragment fragment = new NotificationDialogFragment();
        fragment.actionHandler = handler;   // GẮN HANDLER TRỰC TIẾP

        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_BUTTON, buttonText);
        fragment.setArguments(args);

        return fragment;
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
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            dialogType = getArguments().getString(ARG_TYPE);
            title = getArguments().getString(ARG_TITLE);
            message = getArguments().getString(ARG_MESSAGE);
            buttonText = getArguments().getString(ARG_BUTTON);
        } else dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view;

        if (TYPE_SUCCESS.equals(dialogType)) {
            view = inflater.inflate(R.layout.dialog_success, container, false);
        } else {
            view = inflater.inflate(R.layout.dialog_error, container, false);
        }

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView ivIcon = view.findViewById(R.id.ivDialogIcon);
        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        MaterialButton btnConfirm = view.findViewById(R.id.btnDialogConfirm);

        tvTitle.setText(title);
        tvMessage.setText(message);
        btnConfirm.setText(buttonText);

        if (TYPE_ERROR.equals(dialogType)) {
            ivIcon.setColorFilter(getResources().getColor(R.color.red));
        }

        btnConfirm.setOnClickListener(v -> {
            if (actionHandler != null) actionHandler.onConfirm();
            dismiss();
        });
    }
}



