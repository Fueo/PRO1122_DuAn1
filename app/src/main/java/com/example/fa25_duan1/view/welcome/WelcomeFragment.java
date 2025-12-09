// Welcome3Fragment.java
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
        void onGuestClicked();
    }

    /**
     * Constructor rỗng là BẮT BUỘC
     */
    public WelcomeFragment() {
        // Required empty public constructor
    }

    /**
     * Phương thức "factory" chuẩn để tạo một instance mới của Fragment
     * và truyền layoutId vào qua Bundle.
     *
     * @param layoutId ID của layout (ví dụ: R.layout.fragment_welcome1)
     * @return Một instance mới của WelcomeFragment.
     */
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
        // Gán listener, Activity chứa Fragment này PHẢI implement interface
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
        // Lấy layoutId từ arguments
        if (getArguments() != null) {
            layoutId = getArguments().getInt(ARG_LAYOUT_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Kiểm tra layoutId hợp lệ
        if (layoutId == 0) {
            throw new IllegalArgumentException("Invalid layoutId provided to WelcomeFragment");
        }
        // Inflate (vẽ) layout tương ứng
        return inflater.inflate(layoutId, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Vì các layout khác nhau, các button này có thể null (không tồn tại).
        // Chúng ta phải tìm và kiểm tra null trước khi gán listener.

        // Các nút của Màn 1 & 2
        TextView btnSkip = view.findViewById(R.id.btn_skip);
        Button btnNext = view.findViewById(R.id.btn_next);

        // Các nút của Màn 3
        Button btnRegister = view.findViewById(R.id.btn_register);
        TextView btnLogin = view.findViewById(R.id.btn_login);

        // Gán listener (chỉ gán nếu nút đó tồn tại)
        if (btnSkip != null) {
            btnSkip.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSkipClicked();
                }
            });
        }

        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNextClicked();
                }
            });
        }

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRegisterClicked();
                }
            });
        }

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLoginClicked();
                }
            });
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Hủy listener để tránh rò rỉ bộ nhớ (memory leak)
        listener = null;
    }
}