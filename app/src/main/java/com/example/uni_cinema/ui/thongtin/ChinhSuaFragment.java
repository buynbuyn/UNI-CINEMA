package com.example.uni_cinema.ui.thongtin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.uni_cinema.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChinhSuaFragment extends Fragment {

    private final NavOptions fadeAnim = new NavOptions.Builder()
            .setEnterAnim(R.anim.fade_in)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.fade_out)
            .build();

    private EditText etName, edtBirthday, etAddress, etPhone;
    private TextView tvEmail;
    private RadioButton rbNam, rbNu, rbKhac;
    private RadioGroup rgGender;
    private Spinner spinnerCity, spinnerDistrict;
    private Button btnSave;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chinh_sua, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);
        ImageButton btnBack = view.findViewById(R.id.btn_back_home);
        btnBack.setOnClickListener(v -> navController.navigate(R.id.nav_home, null, fadeAnim));

        // Ánh xạ view
        etPhone = view.findViewById(R.id.tvPhone);  // là TextView nhưng bạn cho phép chỉnh -> EditText
        tvEmail = view.findViewById(R.id.tvEmail);
        etName = view.findViewById(R.id.etName);
        edtBirthday = view.findViewById(R.id.edtBirthday);
        etAddress = view.findViewById(R.id.etAddress);
        rbNam = view.findViewById(R.id.rbNam);
        rbNu = view.findViewById(R.id.rbNu);
        rbKhac = view.findViewById(R.id.rbKhac);
        rgGender = view.findViewById(R.id.rgGender);
        spinnerCity = view.findViewById(R.id.spinnerCity);
        spinnerDistrict = view.findViewById(R.id.spinnerDistrict);
        btnSave = view.findViewById(R.id.btnSave);

        loadUserData();

        btnSave.setOnClickListener(v -> saveUserData());
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(uid);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("full_name");
                String phone = documentSnapshot.getString("phone");
                String email = documentSnapshot.getString("email");
                String birthday = documentSnapshot.getString("birthday");
                String gender = documentSnapshot.getString("gender");
                String address = documentSnapshot.getString("address");
                String city = documentSnapshot.getString("city");
                String district = documentSnapshot.getString("district");

                etName.setText(name);
                etPhone.setText(phone);
                tvEmail.setText(email);
                edtBirthday.setText(birthday);
                etAddress.setText(address);

                if ("Nam".equalsIgnoreCase(gender)) rbNam.setChecked(true);
                else if ("Nữ".equalsIgnoreCase(gender)) rbNu.setChecked(true);
                else rbKhac.setChecked(true);

                // Chọn đúng city/district nếu có dữ liệu
                if (city != null) setSpinnerSelection(spinnerCity, city);
                if (district != null) setSpinnerSelection(spinnerDistrict, district);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(uid);

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String birthday = edtBirthday.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String city = spinnerCity.getSelectedItem().toString();
        String district = spinnerDistrict.getSelectedItem().toString();

        String gender = "";
        int checkedId = rgGender.getCheckedRadioButtonId();
        if (checkedId == R.id.rbNam) gender = "Nam";
        else if (checkedId == R.id.rbNu) gender = "Nữ";
        else gender = "Khác";

        userRef.update(
                "full_name", name,
                "phone", phone,
                "birthday", birthday,
                "gender", gender,
                "address", address,
                "city", city,
                "district", district
        ).addOnSuccessListener(unused -> {
            Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}
