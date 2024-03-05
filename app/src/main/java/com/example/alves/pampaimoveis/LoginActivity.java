package com.example.alves.pampaimoveis;


import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;

public class LoginActivity extends CommonActivity {

    private String email, password;

    private EditText editTextEmail;
    private EditText editTextPassWord;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    // User is signed in
                    checkIfEmailVerified();
                } else {
                    // User is signed out

                }
            // ...
            }
        };

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

    private void initUser() {

        email = editTextEmail.getText().toString().trim();
        password = editTextPassWord.getText().toString().trim();
    }

    private void initView(){
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassWord = (EditText) findViewById(R.id.editTextPassWord);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }


    public void callRecoverPassword(View view){
        showRecoverPassWordDialog();
    }

    public void callSignUp(View view) {
        Intent intent = new Intent(this, CadastrarActivity.class);
        startActivity(intent);
    }

    private void callMainActivity(){
        Intent intent = new Intent( this, MainActivity.class );
        startActivity(intent);
        finish();
    }

    public void sendLoginData( View view ) {
        openProgressBar();
        initUser();
        tryLogin();
    }

    private void tryLogin(){
        if(TextUtils.isEmpty(email)){
            showSnackbar("Você precisa informar o seu email para acessar sua conta");
            closeProgressBar();
            return;
        }

        if(TextUtils.isEmpty(password)){
            showSnackbar("Você precisa informar sua senha para acessar sua conta");
            closeProgressBar();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if( !task.isSuccessful() ){
                            closeProgressBar();
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



    private void checkIfEmailVerified() {

        if (firebaseUser.isEmailVerified()) {
            // user is verified
            callMainActivity();
        }
        else
        {
            // email is not verified, so just prompt the message to the user and restart this activity.
            resendEmailVerification();
            closeProgressBar();

        }
    }


    private void resendEmailVerification(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Conta não verificada");
        builder.setMessage("Você ainda não verificou seu e-mail. Gostaria de reenviar o e-mail de verificação para " + firebaseUser.getEmail() + "?");

        // Set up the buttons
        builder.setPositiveButton("Reenviar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                firebaseUser.sendEmailVerification()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // email sent
                                    // after email is sent just logout the user and show message
                                    showSnackbar("E-mail de verificação reenviado com sucesso.");
                                }
                                else
                                {
                                    // email not sent

                                    //restart this activity
                                    showSnackbar("Ocorreu um problema ao tentar enviar e-mail de ativação da conta. Tente novamente.");

                                }
                            }
                        });

                // login out
                FirebaseAuth.getInstance().signOut();

            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                // login out
                FirebaseAuth.getInstance().signOut();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

    }

    private void showRecoverPassWordDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Redefinir senha");
        builder.setMessage("Informe o seu e-mail para redefinir a senha");

        // Set up the input

        final EditText input = new EditText(LoginActivity.this);
        input.setHint("e-mail");

        //TODO remove this margin parameters
        builder.setView(input, 30, 0, 30, 0);

        // Set up the buttons
        builder.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String emailAddress = input.getText().toString().trim();

                if(TextUtils.isEmpty(emailAddress)) {
                    showSnackbar("Informe seu e-mail para redefinir a senha");
                }
                else{

                    openProgressBar();
                    sendRecoverPassWord(emailAddress);
                }
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }


    private void sendRecoverPassWord(final String emailAddress){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            closeProgressBar();
                            showDialogSuccess(emailAddress);
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        FirebaseCrash.report( e );
                        closeProgressBar();
                        showSnackbar( e.getMessage() );
                    }
                });
    }

    private void showDialogSuccess(String email){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String message = "Nós enviamos um e-mail de redefinição de senha para " + email + ". Siga as instruções e redefina sua senha.";
        builder.setTitle("E-mail enviado");
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Entendi", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //after email sent and confirm, close this activity
                        //and back to login
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
