package com.example.uni_cinema.ui.voucher;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uni_cinema.MainActivity;
import com.example.uni_cinema.R;
import com.example.uni_cinema.login.LoginActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VoucherFragment extends Fragment {
    private RecyclerView recyclerView;
    private VoucherAdapter voucherAdapter;
    private List<Voucher> voucherList = new ArrayList<>();
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để xem quà tặng", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getContext(), LoginActivity.class));
            requireActivity().finish();
            return null;
        }

        View view = inflater.inflate(R.layout.fragment_voucher, container, false);
        recyclerView = view.findViewById(R.id.recyclerVoucher); // Đảm bảo bạn có recyclerVoucher trong XML

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ImageButton btnBack = view.findViewById(R.id.btn_back_home);
        btnBack.setOnClickListener(v ->
                        Navigation.findNavController(v).navigate(R.id.nav_home));

        voucherAdapter = new VoucherAdapter(voucherList);
        recyclerView.setAdapter(voucherAdapter);

        loadVouchersFromFirestore();

        return view;
    }

    private void loadVouchersFromFirestore() {
        String uid = mAuth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("discounts")
                .whereEqualTo("idUser", uid)
                .get()
                .addOnSuccessListener(result -> {
                    voucherList.clear();
                    for (QueryDocumentSnapshot doc : result) {
                        String code = doc.getString("code");

                        // Lấy timestamp từ Firestore
                        Timestamp tsStart = doc.getTimestamp("dateTimeStart");
                        Timestamp tsEnd = doc.getTimestamp("dateTimeEnd");

                        // Chuyển thành Date → format thành chuỗi
                        Date startDate = tsStart != null ? tsStart.toDate() : null;
                        Date endDate = tsEnd != null ? tsEnd.toDate() : null;

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        String timeStart = startDate != null ? sdf.format(startDate) : "Không rõ";
                        String timeEnd = endDate != null ? sdf.format(endDate) : "Không rõ";

                        Long discountLong = doc.getLong("priceDiscount");
                        int discount = discountLong != null ? discountLong.intValue() : 0;

                        Voucher voucher = new Voucher(code, timeStart, timeEnd, discount);
                        voucherList.add(voucher);
                    }

                    voucherAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi tải voucher: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
