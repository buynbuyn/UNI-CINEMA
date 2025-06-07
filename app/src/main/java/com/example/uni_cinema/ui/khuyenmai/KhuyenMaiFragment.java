package com.example.uni_cinema.ui.khuyenmai;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.uni_cinema.R;

public class KhuyenMaiFragment extends Fragment {
    public KhuyenMaiFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_khuyen_mai, container, false);
    }
}
