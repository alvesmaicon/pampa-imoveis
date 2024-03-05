package com.example.alves.pampaimoveis;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.crash.FirebaseCrash;

import java.util.concurrent.CountDownLatch;

public class ProfileActivity extends CommonActivity {

    private EditText name, email, phone, oldPassPhrase, newPassPhrase, repeatNewPassPhrase;
    private ImageView imageView;
    private String oldName, oldPhone, oldEmail;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openProgressBar();
                verifyUserPassPhrase();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();
        updateView();
    }

    private void initView (){
        name = (EditText) findViewById(R.id.editTextNameProfileView);
        email = (EditText) findViewById(R.id.editTextEmailProfileView);
        phone = (EditText) findViewById(R.id.editTextCelProfileView);
        imageView = (ImageView) findViewById(R.id.imageViewUserProfileView);
        oldPassPhrase = (EditText) findViewById(R.id.editTextPassWordProfileView);
        newPassPhrase = (EditText) findViewById(R.id.editTextNewPassPhrase);
        repeatNewPassPhrase = (EditText) findViewById(R.id.editTextRepeatPassPhrase);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        oldName = firebaseUser.getDisplayName();
        oldEmail = firebaseUser.getEmail();
        oldPhone = MainActivity.user.getCel();

    }

    private void updateView(){
        name.setText(MainActivity.user.getName());
        email.setText(MainActivity.user.getEmail());
        phone.setText(MainActivity.user.getCel());

        try{
            new DownloadImageTask((ImageView) findViewById(R.id.imageViewUserProfileView))
                    .execute(MainActivity.user.getPhotourl());
        } catch(Exception e) {
            showSnackbar("Não foi possível exibir a foto do usuário");
        }



    }

    private void verifyUserPassPhrase(){
        String oldPassPhraseVerify = oldPassPhrase.getText().toString().trim();

        if(!oldPassPhraseVerify.isEmpty()) {
            AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), oldPassPhraseVerify);
            firebaseUser.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if( task.isSuccessful() ){
                                sendUpdatedData();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    FirebaseCrash.report(e);
                    showSnackbar(e.getMessage());
                    closeProgressBar();
                    return;
                }
            });

        }
        else{
            showSnackbar("Você precisa informar sua senha atual para atualizar seu perfil.");
            closeProgressBar();
            return;
        }
    }

    private void sendUpdatedData(){
        final String newName = name.getText().toString().trim();
        final String newEmail = email.getText().toString().trim();
        String newPhone = phone.getText().toString().trim();
        final String newPassPhraseVerify = newPassPhrase.getText().toString().trim();
        String repeatNewPassPhraseVerify = repeatNewPassPhrase.getText().toString().trim();

        if(newName.equals(oldName )
                && newEmail.equals(oldEmail)
                && newPhone.equals(oldPhone)
                && newPassPhraseVerify.isEmpty()){
            showSnackbar("Nenhuma alteração a ser feita.");
            closeProgressBar();
            return;
        }

        //deciding how many task will be completed before save user to database
        int count = 0;

        if(!newName.equals(oldName))
            count++;
        if(!newEmail.equals(oldEmail))
            count++;
        if(!newPhone.equals(oldPhone))
            count++;
        if(!newPassPhraseVerify.isEmpty()){
            if(newPassPhraseVerify.equals(repeatNewPassPhraseVerify)){
                count++;
            }
        }
        // countdownlatch needs to be 0 to save user to database
        final CountDownLatch countDownLatch = new CountDownLatch(count);


        if(!newName.equals(oldName)){

            Thread thread1 = new Thread(){

                public void run() {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(newName).build();
                    MainActivity.firebaseUser.updateProfile(profileUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            MainActivity.user.setName(newName);
                            saveUserToDataBase(MainActivity.user);
                            countDownLatch.countDown();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showSnackbar("Não foi possível atualizar o nome do usuário, tente novamente.");
                            closeProgressBar();
                            return;
                        }
                    });
                }
            };
            thread1.start();

        }

        if(!newEmail.equals(oldEmail)){

            Thread thread2 = new  Thread(){

                @Override
                public void run() {
                    MainActivity.firebaseUser.updateEmail(newEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            MainActivity.user.setEmail(newEmail);
                            saveUserToDataBase(MainActivity.user);
                            countDownLatch.countDown();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showSnackbar(e.getMessage());
                            closeProgressBar();
                            return;
                        }
                    });
                }
            };

            thread2.start();
        }

        if(!newPhone.equals(oldPhone)){
            MainActivity.user.setCel(newPhone);
            saveUserToDataBase(MainActivity.user);
            countDownLatch.countDown();
        }

        if(!newPassPhraseVerify.isEmpty()){
            if(newPassPhraseVerify.equals(repeatNewPassPhraseVerify)){

                Thread thread3 = new Thread(){

                    @Override
                    public void run() {
                        MainActivity.firebaseUser.updatePassword(newPassPhraseVerify).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    countDownLatch.countDown();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                showSnackbar(e.getMessage());
                                closeProgressBar();
                                return;
                            }
                        });
                    }
                };

                thread3.start();
            }
            else{
                showSnackbar("Você deve repetir sua nova senha no campo ''repetir nova senha''.");
                closeProgressBar();
                return;
            }
        }


        /* iesta é uma barreira para executar este comando
        somente QUANDO todas alterações tenham sido concluídas
         */
        Thread taskFinal = new Thread(){

            @Override
            public void run() {
                try {
                    countDownLatch.await(); // this runnable is waiting all threads finish

                    if(((int) countDownLatch.getCount()) == 0) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                showDialogSuccess();
                                closeProgressBar();
                            }
                        });
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        taskFinal.start();

    }


    private void saveUserToDataBase(User user){
        Utils.getDatabase().getReference().child("users").child(MainActivity.firebaseUser.getUid()).setValue(user)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                        }
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FirebaseCrash.report( e );
                showSnackbar("Ocorreu um erro ao atualizar informações do usuário. Tente novamente.");
                closeProgressBar();
                return;
            }
        });
    }

    private void showDialogSuccess(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Atualização concluída");
        builder.setMessage("As informações do seu perfil foram atualizadas.")
                .setCancelable(false)
                .setPositiveButton("Entendi", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //and back to main
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }



}
