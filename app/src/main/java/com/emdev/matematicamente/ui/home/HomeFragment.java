package com.emdev.matematicamente.ui.home;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.emdev.matematicamente.MainActivity;
import com.emdev.matematicamente.Model.ArchivosEscolares;
import com.emdev.matematicamente.Model.Usuario;
import com.emdev.matematicamente.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class HomeFragment extends Fragment {

    private static final int RESULT_OK = -1 ;
    private HomeViewModel homeViewModel;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    private Usuario usuario;
    CardView menuPerfilDocente, btnSubirMisDocumentos, btnAdmMisDoc, btnSubirVideo, btnAdmVideos, btnSubirTrabajo, btnAdmTrabajos, btnVerTrabajo;

    //Para subir un trabajo
    StorageReference storageReference;
    EditText edtNombreCreador, edtNombreTrabajo;
    Spinner spin_establecimiento_agregar_trab, spin_curso_agregar_trab, spin_asig_agregar_trab;
    Button btnSelect, btnUpload;
    String escuela="";
    String cur="";
    String mat="";
    RadioButton btnYes, btnNo;
    String compartido = "";
    ArchivosEscolares trabajoPDF;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("ArchivosPDF");

        menuPerfilDocente = root.findViewById(R.id.menuPerfilDocente);

        cargarUsuario(mAuth.getCurrentUser().getUid());

        btnSubirMisDocumentos = root.findViewById(R.id.btnSubirMisDocumentos);
        btnAdmMisDoc = root.findViewById(R.id.btnAdmMisDoc);
        btnSubirVideo = root.findViewById(R.id.btnSubirVideo);
        btnAdmVideos = root.findViewById(R.id.btnAdmVideos);
        btnSubirTrabajo = root.findViewById(R.id.btnSubirTrabajo);
        btnAdmTrabajos = root.findViewById(R.id.btnAdmTrabajos);
        btnVerTrabajo = root.findViewById(R.id.btnVerTrabajo);

        btnSubirMisDocumentos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSubirTrabajo(usuario);
            }
        });

        return root;
    }

    private void cargarUsuario(String uid) {
        db.collection("Usuario").document(uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        usuario = snapshot.toObject(Usuario.class);
                        if (usuario.getAdmin().equals("true")){
                            menuPerfilDocente.setVisibility(View.VISIBLE);
                        } else {
                            menuPerfilDocente.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void dialogSubirTrabajo(Usuario usuario) {
        final String[] array = {"Si", "No"};

        final AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity())
                .setTitle("Subir trabajo ... ")
                .setMessage("Asegúrese que el archivo\nsea en formato PDF para que sea\ncorrecta su lectura.")
                .setCancelable(false);
                /*.setSingleChoiceItems(array, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        compartido = array[which];
                        Toast.makeText(getActivity(), compartido, Toast.LENGTH_SHORT).show();
                    }
                });*/

        LayoutInflater inflater = this.getLayoutInflater();
        View agregar_trabajo = inflater.inflate(R.layout.agregar_trabajo, null);

        /*RadioButton botonSeleccionado;
        RadioGroup rg = agregar_trabajo.findViewById(R.id.rg_share);
        int selectButton = rg.getCheckedRadioButtonId();
        botonSeleccionado = rg.findViewById(selectButton);

        Toast.makeText(getActivity(), botonSeleccionado.getText(), Toast.LENGTH_SHORT).show();*/


        edtNombreTrabajo = agregar_trabajo.findViewById(R.id.edtNombreTrabajo);
        edtNombreCreador = agregar_trabajo.findViewById(R.id.edtNombreCreador);
        edtNombreCreador.setText(usuario.getNombre());
        edtNombreCreador.setEnabled(false);

        //Spinners
        spin_establecimiento_agregar_trab = agregar_trabajo.findViewById(R.id.spinEstablecimiento);
        spin_curso_agregar_trab = agregar_trabajo.findViewById(R.id.spinCurso);
        spin_asig_agregar_trab = agregar_trabajo.findViewById(R.id.spinMateria);
        ArrayAdapter<CharSequence> adapterEstab = ArrayAdapter.createFromResource(getActivity(),
                R.array.Establecimientos, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapterCursos = ArrayAdapter.createFromResource(getActivity(),
                R.array.Cursos, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapterAsignatura = ArrayAdapter.createFromResource(getActivity(),
                R.array.Asignaturas, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterCursos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterAsignatura.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spin_establecimiento_agregar_trab.setAdapter(adapterEstab);
        spin_curso_agregar_trab.setAdapter(adapterCursos);
        spin_asig_agregar_trab.setAdapter(adapterAsignatura);

        spin_establecimiento_agregar_trab.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                escuela = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        spin_curso_agregar_trab.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cur = String.valueOf(parent.getItemIdAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spin_asig_agregar_trab.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mat = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnSelect = agregar_trabajo.findViewById(R.id.btnSelect);
        btnUpload = agregar_trabajo.findViewById(R.id.btnUpload);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtNombreCreador.getText().toString().equals("")
                        || edtNombreTrabajo.getText().toString().equals("")
                        || escuela.equals("") || escuela.equals("Seleccione")
                        || mat.equals("") || mat.equals("Seleccione")
                        || cur.equals("") || cur.equals("0")){
                    Toast.makeText(getActivity(), "Complete Nombre, Asignatura y Curso", Toast.LENGTH_SHORT).show();
                } else {
                    seleccionarPDF();
                }

            }
        });

        alerta.setView(agregar_trabajo);
        alerta.setPositiveButton("CONFIRMAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (edtNombreCreador.getText().toString().equals("")
                        || edtNombreTrabajo.getText().toString().equals("")
                        || mat.equals("") || mat.equals("Seleccione") || cur.equals("") || cur.equals("0")) {
                    Toast.makeText(getActivity(), "Complete todos los campos", Toast.LENGTH_SHORT).show();
                } else if (trabajoPDF != null){
                    String uid = String.valueOf(System.currentTimeMillis());
                    db.collection("ArchivosEscolares").document(uid).set(trabajoPDF);
                    Toast.makeText(getActivity(), "Agregado correctamente", Toast.LENGTH_SHORT).show();
                }
            }
        });

        alerta.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alerta.show();
    }

    private void seleccionarPDF() {
        Intent selectWork = new Intent();
        selectWork.setType("application/pdf");
        selectWork.setAction(selectWork.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(selectWork, "SELECCIONAR PDF"), 12);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 12 && resultCode == RESULT_OK && data != null && data.getData() != null){
            btnUpload.setEnabled(true);
            btnSelect.setText("Archivo selec.");
            btnSelect.setEnabled(false);

            btnUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    subirArchivoPDF(data.getData());
                    btnUpload.setText("ARC. almac.");
                    btnUpload.setEnabled(false);
                    Log.d("Nombre uri", data.toString());

                }
            });

        }

        /*if (requestCode == PICK_IMAGE  && resultCode == -1) {
            CropImage.activity(CropImage.getPickImageResultUri(this, data)).setGuidelines(CropImageView.Guidelines.ON).setRequestedSize(400, 200).setAspectRatio(2, 1).start((Activity) this);
            bandera = "NOTICIA";
        }

        if (requestCode == PICK_IMAGE_FOTO  && resultCode == -1) {
            CropImage.activity(CropImage.getPickImageResultUri(this, data)).setGuidelines(CropImageView.Guidelines.ON).start((Activity) this);
            bandera = "FOTO";
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE ) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            //Uri resultUri = result.getUri();
            if (resultCode == -1) {
                File file = new File(result.getUri().getPath());

                btnUpload.setEnabled(true);
                btnSelect.setText("IMG selec.");
                btnSelect.setEnabled(false);

                String finalBandera = bandera;
                btnUpload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (finalBandera.equals("NOTICIA")){
                            subirNoticia(result.getUri());
                            btnUpload.setText("IMG. almac.");
                            btnUpload.setEnabled(false);

                            Log.d("Nombre uri", data.toString());
                        } else if (finalBandera.equals("FOTO")){

                            if (edtNombreFotografia.getText().toString().equals("")){
                                Toast.makeText(MenuDocentesActivity.this, "Necesita ingresar\nun nombre a la foto", Toast.LENGTH_SHORT).show();
                            } else {
                                subirFoto(result.getUri());
                                btnUpload.setText("IMG. almac.");
                                btnUpload.setEnabled(false);
                                Log.d("Nombre uri", data.toString());
                            }
                        } else {
                            Toast.makeText(MenuDocentesActivity.this, "Cancelado", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        }*/



    }

    private void subirArchivoPDF(Uri data) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Subir archivo");
        progressDialog.show();

        //Ruta en el Storage
        StorageReference reference = storageReference.child(mat +"/" + edtNombreTrabajo.getText().toString() + ".pdf");

        reference.putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isComplete());
                        Uri uri = uriTask.getResult();

                        SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
                        Date todayDate = new Date();
                        String thisDate = currentDate.format(todayDate);

                        //String id, String nombre, String escuela, String materia, String curso, String compartido, String fecha, String url
                        String id = mAuth.getUid();
                        trabajoPDF = new ArchivosEscolares(id, edtNombreTrabajo.getText().toString(), escuela, mat, cur, /*compartido*/"false", thisDate, uri.toString());

                        Toast.makeText(getActivity(), "Archivo subido a la nube\nPresione CONFIRMAR", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();

                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                        progressDialog.setMessage("Subiendo archivo... " + (int) progress + "%");

                    }
                });
    }
}