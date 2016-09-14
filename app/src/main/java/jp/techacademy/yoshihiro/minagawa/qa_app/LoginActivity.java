package jp.techacademy.yoshihiro.minagawa.qa_app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText mEmailEditText;
    EditText mPasswordEditText;
    EditText mNameEditText;
    ProgressDialog mProgress;

    FirebaseAuth mAuth;
    OnCompleteListener<AuthResult> mCreateAccountListener;
    OnCompleteListener<AuthResult> mLoginListener;
    DatabaseReference mDatabaseReference;

    //アカウント作成時にフラグを立て、ログイン処理後に名前をFirebaseに保存する
    boolean mIsCreateAccount = false;

    //onCreateメソッドでの処理
    //データベースのリファレンスの処理
    //FireBaseAuthクラスへのインスタンスへの取得
    //アカウント作成処理のリスナーを作成
    //ログイン処理のリスナーの作成
    //タイトルバーのタイトルの変更
    //UIをメンバ変数に保持
    //アカウント作成ボタンとログインボタンのOnClickListenerを設定
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        //FirebashAuthのオブジェクトを取得する
        mAuth = FirebaseAuth.getInstance();

        //アカウント作成処理のリスナー(Firebaseの処理を受け取る)
        //引数で渡ってくるTaskクラスのisSuccessfulメソッでで成功したかどうかを確認する
        //アカウント作成が成功した際にはそのままログイン処理を行うため、loginメソッドを呼び出す。
        mCreateAccountListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //成功した場合
                    //ログインを行う
                    String email = mEmailEditText.getText().toString();
                    String password = mPasswordEditText.getText().toString();
                    login(email, password);
                }else {
                    //失敗した場合
                    //エラーを表示する
                    View view = findViewById(android.R.id.content);
                    Snackbar.make(view, "アカウント作成に失敗しました", Snackbar.LENGTH_LONG).show();

                    //プログレスダイアログを非表示にする
                    mProgress.dismiss();
                }
            }
        };

        //ログイン処理のリスナー(Firebaseの処理を受け取る)
        //ログインに成功したときはmIsCreateAccountを使ってアカウント作成ボタンを押してからのログイン処理か、
        //ログインボタンをタップの場合かで処理を分けます。
        //アカウント作成ボタンを押した場合は表示名をFirebaseとPreferenceに保存します。
        //ログインボタンをタップしたときは、firebaseからの表示名を取得してPreferenceに保存する。
        //Firebaseからデータを一度だけ取得する場合はDatabaseReferenceクラスが実装しているQueryクラスの
        //addListenerForSingleValueEventメソッドを使う。
        mLoginListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    //成功した場合
                    FirebaseUser user = mAuth.getCurrentUser();
                    DatabaseReference userRef = mDatabaseReference.child(Const.UsersPATH).child(user.getUid());

                    if (mIsCreateAccount) {
                        //アカウント作成の時は表示名をFirebaseに保存する
                        String name = mNameEditText.getText().toString();

                        Map<String, String> data = new HashMap<String, String>();
                        data.put("name", name);
                        userRef.setValue(data);

                        //表示名をPreferenceに保存する
                        saveName(name);

                    } else {
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                Map data = (Map) snapshot.getValue();
                                saveName((String) data.get("name"));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    //プログレスダイアログを非表示にする
                    mProgress.dismiss();

                    //アクティビティを閉じる
                    finish();

                }else{
                    //失敗した場合
                    //エラーを表示する
                    View view = findViewById(android.R.id.content);
                    Snackbar.make(view, "ログインに失敗しました", Snackbar.LENGTH_LONG).show();

                    //プログレスダイアログを非表示にする
                    mProgress.dismiss();

                }
            }
        };

        //UIの準備
        //ログイン時に表示名を保存するようにmIsCreateAccountにtrueを設定する
        //createAccountメソッドを呼び出してアカウント作成処理を開始させます。
        setTitle("ログイン");

        mEmailEditText = (EditText)findViewById(R.id.emailText);
        mPasswordEditText = (EditText)findViewById(R.id.passwordText);
        mNameEditText = (EditText)findViewById(R.id.nameText);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("処理中...");

        Button createButton = (Button)findViewById(R.id.createButton);
        createButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //キーボードが出てたら閉じる
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                String email = mEmailEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();
                String name = mNameEditText.getText().toString();

                if(email.length() != 0 && password.length() >= 6 && name.length() !=0){
                    //ログイン時に表示名を保存するようにフラグを立てる
                    mIsCreateAccount = true;
                    createAccount(email, password);
                }else{
                    //エラーを表示する
                    Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show();
                }

            }
        });

        Button loginButton = (Button)findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //キーボードが出てたら閉じる
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                String email = mEmailEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();

                if(email.length() != 0 && password.length() >= 6){
                    //フラグを落としておく
                    mIsCreateAccount = false;
                }else{
                    //エラーを表示する
                    Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show();

                }
            }
        });

    }

    //アカウント作成を行うcreateAccountメソッドではProgressDialogクラスのshowメソッドを呼び出して
    //ダイアログを表示させ、FirebaseAuthクラスのcreateUserWithEmailAndPassWordメソッドでアカウントの作成を行う。
    //更にcreateUserWithEmailAndPasswordメソッドの引数にメールアドレス、パスワードを与え、更にリスナーを設定する。
    private void createAccount(String email, String password){
        //プログレスダイアログを表示する
        mProgress.show();

        //アカウントを作成する
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(mCreateAccountListener);
    }

    //ログイン処理を行うloginメソッドではProgressdialogクラスのshowメソッドを呼び出してダイアログを表示し、
    //FirebaseAuthクラスのsignInWithEmailAndPasswordメソッドでログイン処理を行う。
    private void login(String email, String password){
        //プログレスダイアログを表示する
        mProgress.show();

        //ログインする
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(mLoginListener);
    }


    //Preferenceに引数を保存する。Commitで保存処理の反映
    private void saveName(String name){
        //Preferenceに保存する
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Const.NameKEY, name);
        editor.commit();
    }

}
