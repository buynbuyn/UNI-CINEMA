package com.example.uni_cinema.ui.quatang;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.uni_cinema.R;

public class BankQuaTangFragment extends Fragment {

    public BankQuaTangFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bank_qua_tang, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Nhận dữ liệu từ Bundle
        Bundle args = getArguments();
        if (args != null) {
            String ticketType = args.getString("ticket_type");
            int price = args.getInt("price");

            // Ánh xạ TextView trong fragment_bank_qua_tang.xml (cần tạo sẵn)
            TextView tvType = view.findViewById(R.id.tv_ticket_type);
            TextView tvPrice = view.findViewById(R.id.tv_ticket_price);

            tvType.setText("Loại vé: " + ticketType);
            tvPrice.setText("Giá: " + price + " VND");
        }
    }
}
