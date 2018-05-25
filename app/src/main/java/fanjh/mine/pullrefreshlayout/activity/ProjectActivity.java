package fanjh.mine.pullrefreshlayout.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.Nullable;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import fanjh.mine.pullrefreshlayout.R;

/**
 * Created by xuws on 2017/5/11.
 */

public class ProjectActivity extends Activity{

    private ProjectActivity mContext;
    Button contentFixed;
    Button btnScrollView;
    Button btnListView;
    Button btnRecycleView;
    Button btnNestScrooling;
    Button btnAutoRefreshOrLoad;
    Dialog dialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_project);
        initView();
        initListener();
        Log.i("tag1","onCreate");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i("tag1","onConfigurationChanged");
    }

    private void initView(){
        contentFixed = (Button) findViewById(R.id.btn_contentfixed);
        btnListView = (Button) findViewById(R.id.btn_contatin_listview);
        btnRecycleView = (Button) findViewById(R.id.btn_contatin_recycleview);
        btnScrollView = (Button) findViewById(R.id.btn_contatin_scrollview);
        btnNestScrooling = (Button) findViewById(R.id.btn_contatin_nestscrooling);
        btnAutoRefreshOrLoad = (Button) findViewById(R.id.btn_autoRefreshingOrLoadMore);
    }

    private void initListener(){
        contentFixed.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProjectActivity.this,ContentFixedActivity.class));
            }
        });

        btnListView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProjectActivity.this,ListViewDemoActivity.class));
            }
        });

        btnScrollView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProjectActivity.this,ScrollViewDemoActivity.class));
            }
        });

       btnRecycleView.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               startActivity(new Intent(ProjectActivity.this,RecycleViewDemoActivity.class));
           }
       });

       btnNestScrooling.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               startActivity(new Intent(ProjectActivity.this,NestScrollingDemoActivity.class));
           }
       });

        btnAutoRefreshOrLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProjectActivity.this,AutoRefreshOrLoadDemoActivity.class));
            }
        });

    }

    private void showDialog(){
        dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle("请校验指纹！");
        dialog.show();
    }

}
