package ir.learn_net.network;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ir.learn_net.fileuploader.uploadToHost;

public class MainActivity extends AppCompatActivity {
    Button upload,choose;
    uploadToHost uploadToHost;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        choose = (Button) findViewById(R.id.choose);
        upload = (Button) findViewById(R.id.upload);
        uploadToHost = new uploadToHost(MainActivity.this,MainActivity.this,"http://kermanshahapp.ir/uptest/uploadFile.php","http://kermanshahapp.ir/uptest/upload/","success","error");

        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadToHost.showFileChooser();
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String fs = uploadToHost.getSelectedFilePath();
                if (fs != null && !fs.equals("")){
                    dialog = ProgressDialog.show(MainActivity.this,"","Uploading File...",true);
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            //creating new thread to handle Http Operations
                            uploadToHost.uploadFile(fs,dialog);
                        }
                    }).start();
                }else {
                    uploadToHost.showFileChooser();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uploadToHost.onActivityResult(requestCode,resultCode,data);
    }
}
