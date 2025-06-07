package com.example.uni_cinema.ui.quatang;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.uni_cinema.R;

public class QuaTangFragment extends Fragment {
    public QuaTangFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qua_tang, container, false);
    }
}
