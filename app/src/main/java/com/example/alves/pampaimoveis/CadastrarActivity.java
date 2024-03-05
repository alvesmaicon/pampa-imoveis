package com.example.alves.pampaimoveis;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.support.annotation.NonNull;


import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

public class CadastrarActivity extends CommonActivity {

    private EditText editTextName;
    private EditText editTextCel;
    private EditText editTextEmail;
    private EditText editTextPassWord;
    private ImageButton imageButton;

    private User user;
    private String email, password;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference storageRef;
    private FirebaseStorage storage;




    private static final int PICK_PHOTO_FOR_AVATAR = 0;

    private Bitmap image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);



        storage = FirebaseStorage.getInstance();

        // Create a storage reference from our app
        storageRef = storage.getReferenceFromUrl("gs://pampa-imoveis-dev.appspot.com/images/user-images");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    // User is signed in

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(user.getName()).build();
                    firebaseUser.updateProfile(profileUpdates);
                    user.setId( firebaseUser.getUid() );
                    // Saving user to database after uploadphoto
                    uploadPhoto();


                } else {
                    // User is signed out

                }
            // ...
            }
        };

        databaseReference = Utils.getDatabase().getReference();

        user = new User();
        initView();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void initView(){
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextCel = (EditText) findViewById(R.id.editTextCel);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassWord = (EditText) findViewById(R.id.editTextPassWord);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imageButton = (ImageButton) findViewById(R.id.imageButtonUser);
    }

    private void initUser(){

        user.setName( editTextName.getText().toString().trim() );
        user.setCel( editTextCel.getText().toString().trim() );
        user.setEmail( editTextEmail.getText().toString().trim() );
        email = editTextEmail.getText().toString().trim();
        password = editTextPassWord.getText().toString().trim();



    }

    //TODO use this method to create interest method
    private void savePropertyAndInterestTest(){
        /*
        Property propriedade = new Property(firebaseUser.getUid());

        //teste de bd property list;
        Random rand = new Random();
        String number = Integer.toString(rand.nextInt((2000 - 10) + 1) + 10) ;

        propriedade.setType("Republica");
        propriedade.setCep("97547110");
        propriedade.setNeighborhood("Capão do Angico");
        propriedade.setCity("Alegrete");
        propriedade.setStreet("Butiá");
        propriedade.setNumber(number);
        propriedade.setVacancies("5");
        propriedade.setPricepervacancy("R$ 250,00");
        propriedade.setFreevacancies("2");
        propriedade.getPhotoList().add("https://luduarte.files.wordpress.com/2012/06/una-vivienda-propia-probabilidades-a-comprar-una-casa-que-en-un-futuro-no-lo-sea.jpg");


        savePropertyToDataBase(propriedade);

        Interest interesse = new Interest(firebaseUser.getUid(), propriedade);
        saveInterestToDataBase(interesse);

        */
    }

    public void sendSignUpDataToDb(View view ){
        openProgressBar();
        initUser();
        registerUser();
    }

    private void registerUser(){
        if(TextUtils.isEmpty(user.getName())){
            showSnackbar("Você precisa informar o seu nome para criar uma conta");
            closeProgressBar();
            return;
        }

        if(TextUtils.isEmpty(user.getCel())){
            showSnackbar("Você precisa informar o seu telefone para criar uma conta");
            closeProgressBar();
            return;
        }

        if(TextUtils.isEmpty(email)){
            showSnackbar("Você precisa informar o seu email para criar uma conta");
            closeProgressBar();
            return;
        }

        if(TextUtils.isEmpty(password)){
            showSnackbar("Você precisa definir uma senha para criar sua conta");
            closeProgressBar();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // success

                        }
                        else{
                            //show failure listener message
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        FirebaseCrash.report( e );
                        showSnackbar( e.getMessage() );
                    }
                });



    }

    private void sendVerificationEmail() {

        firebaseUser.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // email sent
                            // after email is sent just logout the user and show message
                            FirebaseAuth.getInstance().signOut();

                            showDialogSuccess();
                        }
                        else
                        {
                            // email not sent

                            //restart this activity
                            showSnackbar("Ocorreu um problema ao tentar enviar e-mail de ativação da conta. Tente novamente.");

                        }
                    }
                });
    }

    private void showDialogSuccess(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirme seu e-mail");
        builder.setMessage("Quase pronto! Nós enviamos para o seu e-mail instruções para ativar sua conta. Siga estas instruções para concluir seu cadastro.")
                .setCancelable(false)
                .setPositiveButton("Entendi", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //after email sent and confirm, close this activity
                        //and back to login
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    //


    public void pickImageProfile(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //uploading image from galery
        openProgressBar();

        InputStream inputStream = null;

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FOR_AVATAR && resultCode == Activity.RESULT_OK) {
            openProgressBar();
            if (data == null) {
                //Display an error
                showSnackbar("Erro ao abrir imagem.");
                closeProgressBar();
                return;
            }
            try {
                inputStream = getApplicationContext().getContentResolver().openInputStream(data.getData());
            } catch (FileNotFoundException e) {
                showSnackbar("Erro ao abrir arquivo.");
                e.printStackTrace();

            }


            image = BitmapFactory.decodeStream(inputStream);

            image = Utils.cropToSquare(image);

            BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), image);
            imageButton.setBackground(bitmapDrawable);
            imageButton.setImageResource(R.drawable.ic_empty);
            //Now you can do whatever you want with your inputstream, save it as file, upload to a server, decode a bitmap...

            closeProgressBar();


        }
    }

    // UPLOADING TRANSFORMED IMAGE
    private void uploadPhoto(){
        String uuid = UUID.randomUUID().toString();

        StorageReference photoReference = storageRef.child(uuid + "jpeg");

        // Get the data from an ImageView as bytes

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteData = baos.toByteArray();

        UploadTask uploadTask = photoReference.putBytes(byteData);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // TODO Handle unsuccessful uploads
                showSnackbar("Não foi possível enviar a imagem." + exception.getMessage());
                closeProgressBar();
                return;
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @SuppressWarnings("VisibleForTests")
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                //adding photourl to user.photourl
                user.setPhotourl(taskSnapshot.getDownloadUrl().toString());
                saveUserToDataBase(user); //saving user after image upload success

            }
        });

        /*
        UploadTask uploadTask = null;
        try {
            uploadTask = photoReference.putStream(getApplicationContext().getContentResolver().openInputStream(dataPicker.getData()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // TODO Handle unsuccessful uploads
                showSnackbar("Não foi possível enviar a imagem.");
                closeProgressBar();
                return;
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

            @SuppressWarnings("VisibleForTests")
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                //adding photourl to user.photourl
                user.setPhotourl(taskSnapshot.getDownloadUrl().toString());
                closeProgressBar();
            }
        });
        */
    }

    private void saveUserToDataBase(User user){
        databaseReference.child("users").child(firebaseUser.getUid()).setValue(user)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            sendVerificationEmail();
                        }
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FirebaseCrash.report( e );
                removeUser();
                showSnackbar("Ocorreu um erro ao cadastrar. Tente novamente.");
            }
        });
    }

    private void removeUser(){
        firebaseUser.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Success", "User account deleted.");
                        }
                    }
                });
    }

    private void savePropertyToDataBase(Property property){
        databaseReference.child("property").child(property.getId()).setValue(property)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d("Success", "Property saved to database.");
                        }
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FirebaseCrash.report( e );
                showSnackbar( e.getMessage() );
            }
        });
    }

    private void removeProperty(Property property){
        databaseReference.child("property").child(property.getId()).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Success", "Property deleted.");
                        }
                    }
                });
    }

    private void saveInterestToDataBase(Interest interest){
        databaseReference.child("interest").child(interest.getId()).setValue(interest)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d("Success", "Interest saved to database.");
                        }
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FirebaseCrash.report( e );
                showSnackbar( e.getMessage() );
            }
        });
    }

    private void removeInterest(Interest interest){
        databaseReference.child("interest").child(interest.getId()).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Success", "Interest removed.");
                        }
                    }
                });
    }


}
