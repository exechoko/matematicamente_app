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
import android.widget.Toast;

import com.emdev.matematicamente.Model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    EditText edtNombreProf, edtCorreoProf, edtPassProf, edtRepassProf;
    Button btnRegistrarseProf;

    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    //DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        RegisterActivity.this.setTitle("Registro");
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#9FB9FC"));
        Objects.requireNonNull(RegisterActivity.this.getSupportActionBar()).setBackgroundDrawable(colorDrawable);
        Objects.requireNonNull(RegisterActivity.this.getSupportActionBar()).setElevation(0f);

        edtNombreProf = findViewById(R.id.edtNombre);
        edtCorreoProf = findViewById(R.id.edtEmail);
        edtPassProf = findViewById(R.id.edtPass);
        edtRepassProf = findViewById(R.id.edtRePass);
        btnRegistrarseProf = findViewById(R.id.btnRegistrar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnRegistrarseProf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtNombreProf.getText().toString().equals("") ||
                        edtCorreoProf.getText().toString().equals("") ||
                        edtPassProf.getText().toString().equals("") ||
                        edtRepassProf.getText().toString().equals("")){
                    Toast.makeText(RegisterActivity.this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
                } else if (!edtPassProf.getText().toString().equals(edtRepassProf.getText().toString())){
                    Toast.makeText(RegisterActivity.this, "No coinciden las contraseñas", Toast.LENGTH_SHORT).show();
                } else if (edtPassProf.getText().toString().length() < 6){
                    Toast.makeText(RegisterActivity.this, "Su contraseña es muy corta,\n necesita mínimo 6 caracteres", Toast.LENGTH_SHORT).show();
                } else {
                    crearUsuario(edtCorreoProf.getText().toString(), edtPassProf.getText().toString());
                }
            }
        });
    }

    private void crearUsuario(String correo, String pass) {
        mAuth.createUserWithEmailAndPassword(correo, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("AUTHENTICATION", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            //Crea la cuenta estudiante y la agrega a la coleccion Usuarios
                            Usuario usuario = new Usuario(user.getUid(), edtNombreProf.getText().toString(), pass, correo);
                            //SolicitudProfesor nuevaSolicitud = new SolicitudProfesor(edtNombreProf.getText().toString(),edtDniProf.getText().toString(), correo, edtTelefonoProf.getText().toString(), user.getUid());
                            String uid = user.getUid();
                            db.collection("Usuario").document(uid).set(usuario);
                            //db.collection("SolicitudesProfesores").document(uid).set(nuevaSolicitud);

                            //Una vez creada la cuenta redigir al Menu Principal
                            Intent irAlPrincipio = new Intent(RegisterActivity.this,MainActivity.class);
                            startActivity(irAlPrincipio);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("AUTHENTICATION", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}