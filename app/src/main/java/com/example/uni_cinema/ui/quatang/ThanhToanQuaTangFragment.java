package com.example.uni_cinema.ui.quatang;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.uni_cinema.R;

import java.text.NumberFormat;
import java.util.Locale;

public class ThanhToanQuaTangFragment extends Fragment {

    public ThanhToanQuaTangFragment() {}

    private final NavOptions fadeAnim = new NavOptions.Builder()
            .setEnterAnim(R.anim.fade_in)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.fade_out)
            .build();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_thanh_toan_qua_tang, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        // Nhận dữ liệu từ Bundle
        Bundle args = getArguments();
        String ticketType = "";
        int price = 0;
        int imageRes = R.drawable.error_image;

        if (args != null) {
            ticketType = args.getString("ticket_type", "Không xác định");
            price = args.getInt("price", 0);
            imageRes = args.getInt("image_res", R.drawable.error_image);
        }

        // Hiển thị dữ liệu lên giao diện
        ImageView imgTicket = view.findViewById(R.id.img_ticket);
        TextView tvName = view.findViewById(R.id.tv_ticket_type); // hoặc tìm đúng ID tương ứng với tên phim
        TextView tvPrice = view.findViewById(R.id.tv_ticket_price); // cập nhật đúng ID


        imgTicket.setImageResource(imageRes);
        tvName.setText(ticketType);

        String formattedPrice = NumberFormat.getInstance(new Locale("vi", "VN")).format(price);
        tvPrice.setText("Giá: " + formattedPrice + " VNĐ");

        // Tổng tiền
        TextView tvTotalMoney = view.findViewById(R.id.footerLayout).findViewById(R.id.tv_ticket_price); // cập nhật đúng ID
        if (tvTotalMoney != null) {
            tvTotalMoney.setText(formattedPrice + " VNĐ");
        }

        // Nút quay lại
        ImageButton btnBack = view.findViewById(R.id.btn_back_home);
        btnBack.setOnClickListener(v -> {
            navController.popBackStack(); // hoặc navigate lên màn trước
        });

        // Nút Thanh toán
        view.findViewById(R.id.btnThanhToan).setOnClickListener(v -> {
            // Xử lý logic thanh toán ở đây
            // Ví dụ: thông báo, chuyển tới màn hình cảm ơn, hoặc tích lũy quà tặng
        });
        // Khai báo các thành phần
        LinearLayout optionQR = view.findViewById(R.id.option_qr);
        LinearLayout optionBank = view.findViewById(R.id.option_bank);
        LinearLayout optionWallet = view.findViewById(R.id.option_wallet);
        AppCompatButton btnThanhToan = view.findViewById(R.id.btnThanhToan);

        // Vô hiệu hóa nút thanh toán ban đầu
        btnThanhToan.setEnabled(false);
        btnThanhToan.setAlpha(0.4f); // làm mờ nút

        // Tạo bộ chọn dùng chung
        View.OnClickListener paymentSelectListener = v -> {
            // Khi người dùng chọn 1 phương thức, làm nổi bật lựa chọn và bật nút
            optionQR.setBackgroundColor(Color.TRANSPARENT);
            optionBank.setBackgroundColor(Color.TRANSPARENT);
            optionWallet.setBackgroundColor(Color.TRANSPARENT);

            v.setBackgroundResource(R.drawable.bg_selected_payment); // bạn cần tạo bg_selected_payment.xml nếu muốn hiệu ứng đẹp

            btnThanhToan.setEnabled(true);
            btnThanhToan.setAlpha(1f); // bật lại độ trong suốt
        };

        // Gắn sự kiện chọn cho từng phương thức
        optionQR.setOnClickListener(paymentSelectListener);
        optionBank.setOnClickListener(paymentSelectListener);
        optionWallet.setOnClickListener(paymentSelectListener);
    }
}
