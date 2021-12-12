package com.example.itracker;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.Objects;


public class MainActivity extends AppCompatActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Button btn = findViewById(R.id.loginBtn);
        btn.setOnClickListener(v -> {

            CollectionReference collectionReference = db.collection("users");

            TextView usernameTxt = findViewById(R.id.usernameTxt);
            TextView passwdTxt = findViewById(R.id.passwdTxt);

            Query query = collectionReference.whereEqualTo("username",usernameTxt.getText().toString().trim())
                    .whereEqualTo("password",passwdTxt.getText().toString().trim());
            // execute query
            query.get().addOnCompleteListener(task -> {
                // if query was successful
                if (Objects.requireNonNull(task.getResult()).isEmpty()) {

                    // Create a new intent object
                    Intent intent = new Intent(this, HomePage.class);

                    startActivity(intent);
                }
        });

    });
    }
}