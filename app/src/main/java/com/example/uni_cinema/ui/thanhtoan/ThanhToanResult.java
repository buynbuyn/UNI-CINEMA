<<<<<<< HEAD:app/src/main/java/com/example/uni_cinema/ui/thanhtoan/ThanhToanResultActivity.java
=======
package com.example.uni_cinema.ui.thanhtoan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uni_cinema.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class ThanhToanResult extends AppCompatActivity {

    private ImageView ivStatusIcon;
    private TextView tvStatusTitle, tvStatusMessage;
    private Button btnGoToHome, btnViewTickets;
    private String userId;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanhtoanresult);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        context = ThanhToanResult.this;

        SharedPreferences prefs = getSharedPreferences("PaymentData", MODE_PRIVATE);

    }
}
>>>>>>> ed52fbe311397b5ad1760974e7db2916fa1036c2:app/src/main/java/com/example/uni_cinema/ui/thanhtoan/ThanhToanResult.java
