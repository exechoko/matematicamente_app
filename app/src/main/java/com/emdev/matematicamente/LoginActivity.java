package com.emdev.matematicamente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.emdev.matematicamente.Model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "FIRESTORE PROFESOR";
    FirebaseFirestore db;
    Usuario user;
    //String esAdmin = "";
    private FirebaseAuth mAuth;

    EditText emailProf, passProf;
    Button btnIngresarProf;
    TextView olvideClaveProf;

    private LottieAnimationView mAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        LoginActivity.this.setTitle("Iniciar sesión");
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#9FB9FC"));
        Objects.requireNonNull(LoginActivity.this.getSupportActionBar()).setBackgroundDrawable(colorDrawable);
        Objects.requireNonNull(LoginActivity.this.getSupportActionBar()).setElevation(0f);

        mAnimation = findViewById(R.id.animation);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailProf = findViewById(R.id.edtEmail);
        passProf = findViewById(R.id.edtPass);
        btnIngresarProf = findViewById(R.id.btnIngresar);
        olvideClaveProf = findViewById(R.id.olvidoClave);

        btnIngresarProf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAnimation.playAnimation();
                mAnimation.setVisibility(View.VISIBLE);

                /*mDialog.setMessage("Iniciando sesión...");
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();*/

                iniciarSesion(emailProf.getText().toString(), passProf.getText().toString());

            }
        });

        olvideClaveProf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(), "Olvide clave de profesor", Toast.LENGTH_SHORT).show();
                Intent restaurarClave = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                startActivity(restaurarClave);
                finish();
            }
        });
    }

    private void iniciarSesion(String email, String pass) {

        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Ingreso correcto", Toast.LENGTH_SHORT).show();
                            Intent irMenuPrincipal = new Intent(LoginActivity.this, MainActivity.class);
                            irMenuPrincipal.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(irMenuPrincipal);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Fallo el ingreso, verifique CORREO y CONTRASEÑA", Toast.LENGTH_SHORT).show();
                        }

                        mAnimation.cancelAnimation();
                        mAnimation.setVisibility(View.GONE);
                    }

                });
    }

}