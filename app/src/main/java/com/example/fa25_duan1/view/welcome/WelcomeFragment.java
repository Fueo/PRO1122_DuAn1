// WelcomeFragment.java
package com.example.fa25_duan1.view.welcome;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fa25_duan1.R;

public class WelcomeFragment extends Fragment {
    // Key để lưu layoutId trong Bundle
    private static final String ARG_LAYOUT_ID = "ARG_LAYOUT_ID";

    @LayoutRes
    private int layoutId;

    private OnWelcomeActionListener listener;

    /**
     * Interface để Activity có thể lắng nghe các sự kiện click
     * từ Fragment này.
     */
    public interface OnWelcomeActionListener {
        void onSkipClicked();
        void onNextClicked();
        void onRegisterClicked();
        void onLoginClicked();
        void onGuestClicked(); // Đã có sẵn trong code của bạn
    }

    public WelcomeFragment() {
        // Required empty public constructor
    }

    public static WelcomeFragment newInstance(@LayoutRes int layoutId) {
        WelcomeFragment fragment = new WelcomeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_ID, layoutId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnWelcomeActionListener) {
            listener = (OnWelcomeActionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnWelcomeActionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            layoutId = getArguments().getInt(ARG_LAYOUT_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (layoutId == 0) {
            throw new IllegalArgumentException("Invalid layoutId provided to WelcomeFragment");
        }
        return inflater.inflate(layoutId, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Tìm các View ---

        // Các nút của Màn 1 & 2
        TextView btnSkip = view.findViewById(R.id.btn_skip);
        Button btnNext = view.findViewById(R.id.btn_next);

        // Các nút của Màn 3
        Button btnRegister = view.findViewById(R.id.btn_register);
        TextView btnLogin = view.findViewById(R.id.btn_login);

        // [MỚI] Tìm nút Guest
        TextView btnGuest = view.findViewById(R.id.btn_guest);

        // --- Gán Listener (Kiểm tra null trước khi gán) ---

        if (btnSkip != null) {
            btnSkip.setOnClickListener(v -> {
                if (listener != null) listener.onSkipClicked();
            });
        }

        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                if (listener != null) listener.onNextClicked();
            });
        }

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> {
                if (listener != null) listener.onRegisterClicked();
            });
        }

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                if (listener != null) listener.onLoginClicked();
            });
        }

        // [MỚI] Xử lý sự kiện nút Guest
        if (btnGuest != null) {
            btnGuest.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGuestClicked();
                }
            });
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}