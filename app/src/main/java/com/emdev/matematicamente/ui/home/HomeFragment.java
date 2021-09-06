package com.emdev.matematicamente.ui.home;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emdev.matematicamente.Interface.ItemClickListener;
import com.emdev.matematicamente.MainActivity;
import com.emdev.matematicamente.Model.ArchivoPrivado;
import com.emdev.matematicamente.Model.ArchivosEscolares;
import com.emdev.matematicamente.Model.Usuario;
import com.emdev.matematicamente.R;
import com.emdev.matematicamente.ViewHolder.ArchivoPrivadoViewHolder;
import com.emdev.matematicamente.ViewHolder.ArchivosEscolaresViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
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
    private static final int CODE_TRABAJO = 12;
    private static final int CODE_DOCUMENTO = 13;

    private HomeViewModel homeViewModel;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    private Usuario usuario;
    CardView menuPerfilDocente, btnSubirMisDocumentos, btnAdmMisDoc, btnSubirVideo, btnAdmVideos, btnSubirTrabajo, btnAdmTrabajos, btnVerTrabajo;

    //Para subir un trabajo escolar
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

    //Para subir archivo privado
    ArchivoPrivado docPrivado;
    StorageReference storageReferencePrivates;
    EditText edtNombreCreadorDoc, edtNombreDocumento;
    Button btnSelectDoc, btnUploadDoc;

    //Administrar Trabajos Escolares
    RecyclerView recycler_trabajos;
    LinearLayoutManager layoutManager;
    FirestoreRecyclerAdapter<ArchivosEscolares, ArchivosEscolaresViewHolder> adapterArcEsc;

    //Administrar Documentos privados
    RecyclerView recycler_documentos;
    FirestoreRecyclerAdapter<ArchivoPrivado, ArchivoPrivadoViewHolder> adapterArcPrivado;

    //Ver trabajos compartidos escolares los estudiantes
    Spinner spin_escuela, spin_materia, spin_curso;
    String e="";
    String m="";
    String c="";


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("ArchivosPDF");
        storageReferencePrivates = FirebaseStorage.getInstance().getReference("ArchivosPrivados");

        menuPerfilDocente = root.findViewById(R.id.menuPerfilDocente);

        cargarUsuario(mAuth.getCurrentUser().getUid());

        //Subir y administrar trabajos escolares en PDF
        btnSubirTrabajo = root.findViewById(R.id.btnSubirMisDocumentos);
        btnAdmTrabajos = root.findViewById(R.id.btnAdmMisDoc);
        btnSubirTrabajo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSubirTrabajo(usuario);
            }
        });
        btnAdmTrabajos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogVerMisTrabajos(usuario);
            }
        });
        //--------------------------------

        //Subir y administrar videos explicativos escolares
        btnSubirVideo = root.findViewById(R.id.btnSubirVideo);
        btnAdmVideos = root.findViewById(R.id.btnAdmVideos);
        //--------------------------------

        //Subir y administrar documentación personal
        btnSubirMisDocumentos = root.findViewById(R.id.btnSubirDocPrivado);
        btnAdmMisDoc = root.findViewById(R.id.btnAdmDocPrivado);
        btnSubirMisDocumentos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSubirDocPrivado(usuario);
            }
        });
        btnAdmMisDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogVerMisDocumentos(usuario);
            }
        });

        //------------------------------------------------------------

        //Ver trabajos compartidos (app para los estudiantes)
        btnVerTrabajo = root.findViewById(R.id.btnVerTrabajo);
        spin_escuela = root.findViewById(R.id.spinnerEscuela);
        spin_curso = root.findViewById(R.id.spinnerCurso);
        spin_materia = root.findViewById(R.id.spinnerAsignatura);
        ArrayAdapter<CharSequence> adapterEscuela = ArrayAdapter.createFromResource(getActivity(),
                R.array.Establecimientos, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapterCurso = ArrayAdapter.createFromResource(getActivity(),
                R.array.Cursos, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapterMateria = ArrayAdapter.createFromResource(getActivity(),
                R.array.Asignaturas, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterEscuela.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterCurso.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterMateria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spin_escuela.setAdapter(adapterEscuela);
        spin_curso.setAdapter(adapterCurso);
        spin_materia.setAdapter(adapterMateria);

        spin_establecimiento_agregar_trab.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                e = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spin_curso_agregar_trab.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                c = String.valueOf(parent.getItemIdAtPosition(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spin_asig_agregar_trab.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                m = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btnVerTrabajo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (c.equals("")||c.equals("Seleccione")||e.equals("Seleccione")||e.equals("")||m.equals("Seleccione")||m.equals("")){
                    Toast.makeText(getActivity(), "Debe elegir escuela, curso y materia", Toast.LENGTH_SHORT).show();
                } else {
                    realizarBusqueda(e,c,m);
                }
            }
        });

        //------------------------------------------------------------
        
        return root;
    }

    private void realizarBusqueda(String esc, String cur, String mat) {


    }

    private void dialogVerMisDocumentos(Usuario usuario) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity())
                .setTitle("Mis archivos privados subidos")
                .setCancelable(false);

        LayoutInflater inflater = this.getLayoutInflater();
        View misDocumentosPrivados = inflater.inflate(R.layout.datos_en_recycler, null);

        recycler_documentos = misDocumentosPrivados.findViewById(R.id.recycler_datos);
        layoutManager = new LinearLayoutManager(getActivity());
        recycler_documentos.setLayoutManager(layoutManager);

        Query query = db.collection("ArchivosPrivados")
                .whereEqualTo("id", usuario.getId());
        //.orderBy("fecha", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ArchivoPrivado> options = new FirestoreRecyclerOptions.Builder<ArchivoPrivado>()
                .setQuery(query, ArchivoPrivado.class)
                .build();

        adapterArcPrivado = new FirestoreRecyclerAdapter<ArchivoPrivado, ArchivoPrivadoViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ArchivoPrivadoViewHolder holder, int i, @NonNull ArchivoPrivado arcPrivado) {

                holder.doc_nombre.setText(arcPrivado.getNombre());
                holder.doc_fecha.setText(arcPrivado.getFecha());

                if (arcPrivado.getUrl().contains(".pdf")){
                    Picasso.get().load(R.drawable.icon_pdf).into(holder.doc_imagen);
                } else if (arcPrivado.getUrl().contains(".jpg")){
                    Picasso.get().load(R.drawable.icon_imagen).into(holder.doc_imagen);
                }

                //String destinoPath = Environment.DIRECTORY_DOWNLOADS;//Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                holder.doc_download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(), "Boton de descarga", Toast.LENGTH_SHORT).show();
                        //VERIFICAR PERMISOS
                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                                //Denegado, solicitarlo
                                String [] permisos = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                                //Dialogo emergente
                                requestPermissions(permisos,PERMISO_ALMACENAMIENTO);

                            } else {
                                Toast.makeText(MenuDocentesActivity.this, "Espere mientras se descarga", Toast.LENGTH_SHORT).show();
                                //downloadFile(documentos);
                                otroDownload(documentos);
                            }
                        } else {
                            Toast.makeText(MenuDocentesActivity.this, "Espere mientras se descarga", Toast.LENGTH_SHORT).show();
                            otroDownload(documentos);

                        }*/
                    }
                });

                holder.doc_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(), "Boton eliminar", Toast.LENGTH_SHORT).show();
                        /*deleteMiTrabajo(adapter.getSnapshots().getSnapshot(holder.getAdapterPosition()).getId());*/
                    }
                });

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Toast.makeText(getActivity(), "Id: " + arcPrivado.getIdArchivo(), Toast.LENGTH_SHORT).show();
                        /*startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(clickEnDoc.getUrl())));*/
                    }
                });
            }

            @NonNull
            @Override
            public ArchivoPrivadoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_doc_privado,parent,false);
                return new ArchivoPrivadoViewHolder(view);
            }
        };

        recycler_documentos.setAdapter(adapterArcPrivado);
        adapterArcPrivado.startListening();

        alert.setView(misDocumentosPrivados);

        alert.setNegativeButton("CERRAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                adapterArcPrivado.stopListening();
                dialogInterface.cancel();

            }
        });

        alert.show();
    }

    private void dialogSubirDocPrivado(Usuario usuario) {
        final AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity())
                .setTitle("Subir documento privado ... ")
                .setMessage("Asegúrese que el archivo\nsea en formato PDF para que sea\ncorrecta su lectura.")
                .setCancelable(false);

        LayoutInflater inflater = this.getLayoutInflater();
        View agregar_doc_privado = inflater.inflate(R.layout.agregar_doc_privado, null);

        edtNombreDocumento = agregar_doc_privado.findViewById(R.id.edtNombreDocumento);
        edtNombreCreadorDoc = agregar_doc_privado.findViewById(R.id.edtNombreCreadorDoc);
        edtNombreCreadorDoc.setText(usuario.getNombre());
        edtNombreCreadorDoc.setEnabled(false);

        btnSelectDoc = agregar_doc_privado.findViewById(R.id.btnSelectDoc);
        btnUploadDoc = agregar_doc_privado.findViewById(R.id.btnUploadDoc);

        btnSelectDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtNombreCreadorDoc.getText().toString().equals("")
                        || edtNombreDocumento.getText().toString().equals("")){
                    Toast.makeText(getActivity(), "Complete todos los campos", Toast.LENGTH_SHORT).show();
                } else {
                    seleccionarPDF(CODE_DOCUMENTO);
                }

            }
        });

        alerta.setView(agregar_doc_privado);
        alerta.setPositiveButton("CONFIRMAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (edtNombreCreadorDoc.getText().toString().equals("")
                        || edtNombreDocumento.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "Complete todos los campos", Toast.LENGTH_SHORT).show();
                } else if (docPrivado != null){
                    String uid = docPrivado.getIdArchivo();
                    db.collection("ArchivosPrivados").document(uid).set(docPrivado);
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

    private void dialogVerMisTrabajos(Usuario usuario) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity())
                .setTitle("Mis archivos escolares subidos")
                .setCancelable(false);

        LayoutInflater inflater = this.getLayoutInflater();
        View misTrabajosEscolares = inflater.inflate(R.layout.datos_en_recycler, null);

        recycler_trabajos = misTrabajosEscolares.findViewById(R.id.recycler_datos);
        layoutManager = new LinearLayoutManager(getActivity());
        recycler_trabajos.setLayoutManager(layoutManager);

        Query query = db.collection("ArchivosEscolares")
                .whereEqualTo("id", usuario.getId());
        //.orderBy("fecha", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ArchivosEscolares> options = new FirestoreRecyclerOptions.Builder<ArchivosEscolares>()
                .setQuery(query, ArchivosEscolares.class)
                .build();

        adapterArcEsc = new FirestoreRecyclerAdapter<ArchivosEscolares, ArchivosEscolaresViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ArchivosEscolaresViewHolder holder, int i, @NonNull ArchivosEscolares arcEscolar) {

                holder.doc_nombre.setText(arcEscolar.getNombre());
                holder.doc_materia.setText(arcEscolar.getMateria());
                holder.doc_curso.setText(arcEscolar.getCurso() + "º año");
                holder.doc_fecha.setText(arcEscolar.getFecha());

                if (arcEscolar.getUrl().contains(".pdf")){
                    Picasso.get().load(R.drawable.icon_pdf).into(holder.doc_imagen);
                } else if (arcEscolar.getUrl().contains(".jpg")){
                    Picasso.get().load(R.drawable.icon_imagen).into(holder.doc_imagen);
                }

                holder.compartir.setChecked(arcEscolar.getCompartido().equals("SI"));
                compartirONo(holder.compartir, arcEscolar);
                /*holder.compartir.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (holder.compartir.isChecked()){
                            arcEscolar.setCompartido("SI");
                            //actualizar
                            db.collection("ArchivosEscolares").document(arcEscolar.getIdArchivo()).set(arcEscolar);
                            Toast.makeText(getActivity(), "Archivo compartido", Toast.LENGTH_SHORT).show();
                        } else {
                            arcEscolar.setCompartido("NO");
                            //actualizar
                            db.collection("ArchivosEscolares").document(arcEscolar.getIdArchivo()).set(arcEscolar);
                            Toast.makeText(getActivity(), "No compartido", Toast.LENGTH_SHORT).show();
                        }
                    }
                });*/



                holder.doc_escuela.setText(arcEscolar.getEscuela());

                //String destinoPath = Environment.DIRECTORY_DOWNLOADS;//Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                holder.doc_download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(), "Boton de descarga", Toast.LENGTH_SHORT).show();
                        //VERIFICAR PERMISOS
                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                                //Denegado, solicitarlo
                                String [] permisos = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                                //Dialogo emergente
                                requestPermissions(permisos,PERMISO_ALMACENAMIENTO);

                            } else {
                                Toast.makeText(MenuDocentesActivity.this, "Espere mientras se descarga", Toast.LENGTH_SHORT).show();
                                //downloadFile(documentos);
                                otroDownload(documentos);
                            }
                        } else {
                            Toast.makeText(MenuDocentesActivity.this, "Espere mientras se descarga", Toast.LENGTH_SHORT).show();
                            otroDownload(documentos);

                        }*/
                    }
                });

                holder.doc_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(), "Boton eliminar", Toast.LENGTH_SHORT).show();
                        /*deleteMiTrabajo(adapter.getSnapshots().getSnapshot(holder.getAdapterPosition()).getId());*/
                    }
                });

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Toast.makeText(getActivity(), "Id: " + arcEscolar.getIdArchivo(), Toast.LENGTH_SHORT).show();
                        /*startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(clickEnDoc.getUrl())));*/
                    }
                });
            }

            @NonNull
            @Override
            public ArchivosEscolaresViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_arc_esc,parent,false);
                return new ArchivosEscolaresViewHolder(view);
            }
        };

        recycler_trabajos.setAdapter(adapterArcEsc);
        adapterArcEsc.startListening();

        alert.setView(misTrabajosEscolares);

        alert.setNegativeButton("CERRAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                adapterArcEsc.stopListening();
                dialogInterface.cancel();

            }
        });

        alert.show();
    }

    private void compartirONo(SwitchMaterial compartir, ArchivosEscolares archivo) {

        ArchivosEscolares nuevoArchi;
        nuevoArchi = archivo;

        compartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (compartir.isChecked()){
                    nuevoArchi.setCompartido("SI");
                    //actualizar
                    db.collection("ArchivosEscolares").document(archivo.getIdArchivo()).set(nuevoArchi);
                    Toast.makeText(getActivity(), "Archivo compartido", Toast.LENGTH_SHORT).show();
                } else {
                    nuevoArchi.setCompartido("NO");
                    //actualizar
                    db.collection("ArchivosEscolares").document(archivo.getIdArchivo()).set(nuevoArchi);
                    Toast.makeText(getActivity(), "No compartido", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

        final AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity())
                .setTitle("Subir trabajo ... ")
                .setMessage("Asegúrese que el archivo\nsea en formato PDF para que sea\ncorrecta su lectura.")
                .setCancelable(false);

        LayoutInflater inflater = this.getLayoutInflater();
        View agregar_trabajo = inflater.inflate(R.layout.agregar_trabajo, null);

        RadioGroup rg = agregar_trabajo.findViewById(R.id.rg_share);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int childCount = group.getChildCount();
                for (int x = 0; x < childCount; x++) {
                    RadioButton btn = (RadioButton) group.getChildAt(x);
                    if (btn.getId() == checkedId) {
                        Log.d("selected RadioButton->",btn.getText().toString());//SI y NO
                        compartido = btn.getText().toString();

                    }
                }
            }
        });

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
        adapterEstab.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
                    seleccionarPDF(CODE_TRABAJO);
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
                    String uid = trabajoPDF.getIdArchivo();
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

    private void seleccionarPDF(int request_code) {
        Intent selectWork = new Intent();
        selectWork.setType("application/pdf");
        selectWork.setAction(selectWork.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(selectWork, "SELECCIONAR PDF"), request_code);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE_TRABAJO && resultCode == RESULT_OK && data != null && data.getData() != null){
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

        } else if (requestCode == CODE_DOCUMENTO && resultCode == RESULT_OK && data != null && data.getData() != null){
            btnUploadDoc.setEnabled(true);
            btnSelectDoc.setText("Archivo selec.");
            btnSelectDoc.setEnabled(false);

            btnUploadDoc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    subirDocumentoPDF(data.getData());
                    btnUploadDoc.setText("ARC. almac.");
                    btnUploadDoc.setEnabled(false);
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

    private void subirDocumentoPDF(Uri data) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Subir documento privado");
        progressDialog.show();

        //Ruta en el Storage
        StorageReference reference = storageReferencePrivates.child(edtNombreCreadorDoc.getText().toString() + "/" + edtNombreDocumento.getText().toString() + ".pdf");

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

                        //ArchivoPrivado(String id, String idArchivo, String nombre, String fecha, String url)
                        String id = mAuth.getUid();
                        String idArc = String.valueOf(System.currentTimeMillis());
                        docPrivado = new ArchivoPrivado(id, idArc, edtNombreDocumento.getText().toString() + "_" + edtNombreCreadorDoc.getText().toString(), thisDate, uri.toString());

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
                        String idArc = String.valueOf(System.currentTimeMillis());
                        trabajoPDF = new ArchivosEscolares(id, idArc,edtNombreTrabajo.getText().toString(), escuela, mat, cur, compartido, thisDate, uri.toString());

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