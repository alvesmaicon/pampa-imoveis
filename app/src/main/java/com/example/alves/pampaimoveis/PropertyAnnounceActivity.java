package com.example.alves.pampaimoveis;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.util.UUID;

public class PropertyAnnounceActivity extends CommonActivity {

    private static final int PICK_PHOTO_FOR_ANNOUNCE = 0;

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference storageRef;
    private FirebaseStorage storage;
    private Property property;

    private Spinner tipo;
    private TextView cidade;
    private TextView bairro;
    private TextView rua;
    private TextView cep;
    private TextView numero;
    private TextView complemento;
    private TextView preco;
    private TextView area;
    private TextView quartos;
    private TextView banheiros;
    private ScrollView scrollView;

    private ImageButton button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_announce);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initView();

        databaseReference = Utils.getDatabase().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();

        // Create a storage reference from our app
        storageRef = storage.getReferenceFromUrl("gs://pampa-imoveis-dev.appspot.com/images/property-images");

        property = new Property(firebaseUser.getUid());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendAnnounce();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initView(){
        tipo = (Spinner) findViewById(R.id.spinner);
        scrollView = (ScrollView) findViewById(R.id.scrollViewAnnounce);
        cidade = (EditText) findViewById(R.id.editTextCity);
        bairro = (EditText) findViewById(R.id.editTextNeighborhood);
        rua = (EditText) findViewById(R.id.editTextStreet);
        cep = (EditText) findViewById(R.id.editTextCEP);
        numero = (EditText) findViewById(R.id.editTextNumber);
        complemento = (EditText) findViewById(R.id.editTextComplement);
        preco = (EditText) findViewById(R.id.editTextPrice);
        quartos = (EditText) findViewById(R.id.editTextRooms);
        banheiros = (EditText) findViewById(R.id.editTextBathrooms);
        area = (EditText) findViewById(R.id.editTextArea);
        progressBar = (ProgressBar) findViewById(R.id.progressBar3);



    }

    private void updateProperty(){

        property.setType(tipo.getSelectedItem().toString());
        property.setCity(cidade.getText().toString().trim());
        property.setNeighborhood(bairro.getText().toString().trim());
        property.setStreet(rua.getText().toString().trim());
        property.setCep(cep.getText().toString().trim());
        property.setNumber(numero.getText().toString().trim());
        property.setComplement(complemento.getText().toString().trim());
        property.setPrice(preco.getText().toString().trim());
        property.setArea(area.getText().toString().trim());
        property.setRooms(quartos.getText().toString().trim());
        property.setBathrooms(banheiros.getText().toString().trim());

    }

    public void sendAnnounce(){
        openProgressBar();
        updateProperty();

        if(tipo.getSelectedItem().toString().equals("Selecione o tipo do anúncio")){
            Snackbar.make(scrollView, "Você precisa selecionar o tipo do anúncio.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            closeProgressBar();
            return;
        }
        savePropertyToDataBase(property);

    }

    private void savePropertyToDataBase(Property property){
        databaseReference.child("property").child(property.getId()).setValue(property)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d("Success", "Property saved to database.");
                            showDialogSuccess();
                            closeProgressBar();
                        }
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FirebaseCrash.report( e );
                showSnackbar( e.getMessage() );
                closeProgressBar();
            }
        });
    }

    public void pickImage(View view) {
        button = (ImageButton) findViewById(view.getId());
        // TODO upgrade this method to handle with photo replacement and photo limit
        // TODO upload images only in save announce.
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO_FOR_ANNOUNCE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //uploading image from galery


        InputStream inputStream = null;
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FOR_ANNOUNCE && resultCode == Activity.RESULT_OK) {
            openProgressBar();
            if (data == null) {
                //Display an error
                Snackbar.make(scrollView, "Não foi possivel abrir a imagem.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                closeProgressBar();
                return;
            }
            try {
                inputStream = getApplicationContext().getContentResolver().openInputStream(data.getData());
            } catch (FileNotFoundException e) {
                Snackbar.make(scrollView, "Não foi possivel abrir a imagem.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                e.printStackTrace();

            }

            scrollView.fullScroll(View.FOCUS_DOWN); //scrolling view to down
            Bitmap image = BitmapFactory.decodeStream(inputStream);

            image = Utils.cropToSquare(image); //CROPPING IMAGE TO SQUARE

            BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), image);
            button.setBackground(bitmapDrawable);
            button.setImageResource(R.drawable.ic_empty);
            //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...

            //Generating random ID to image
            String uuid = UUID.randomUUID().toString();


            StorageReference photoReference = storageRef.child(uuid + ".jpeg");

            // Get the data from an Bitmap image
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] byteData = baos.toByteArray();

            // Uploading transformed image
            UploadTask uploadTask = photoReference.putBytes(byteData);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Snackbar.make(scrollView, "Não foi possivel enviar a imagem.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    closeProgressBar();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @SuppressWarnings("VisibleForTests")
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Snackbar.make(scrollView, "Imagem enviada com sucesso.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    //adding photourl to property.photolist
                    property.getPhotoList().add(taskSnapshot.getDownloadUrl().toString());
                    closeProgressBar();
                }
            });

            /*

            UploadTask uploadTask = null;
            try {
                uploadTask = photoReference.putStream(getApplicationContext().getContentResolver().openInputStream(data.getData()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Snackbar.make(scrollView, "Não foi possivel enviar a imagem.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    closeProgressBar();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                @SuppressWarnings("VisibleForTests")
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Snackbar.make(scrollView, "Imagem enviada com sucesso.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    //adding photourl to property.photolist
                    property.getPhotoList().add(taskSnapshot.getDownloadUrl().toString());
                    closeProgressBar();
                }
            });
            */

        }
    }

    private void showDialogSuccess(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Anuncio cadastrado!");
        builder.setMessage("O seu anúncio foi cadastrado e aparecerá na lista de imóveis disponíveis. Você poderá atualiza-lo posteriormente indo até \"meus anúncios\".")
                .setCancelable(false)
                .setPositiveButton("Entendi", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //and back to main
                        finish();
                        closeProgressBar();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
