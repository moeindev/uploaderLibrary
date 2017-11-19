package ir.learn_net.fileuploader;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by moein on 11/19/17.
 */

public class uploadToHost implements PreferenceManager.OnActivityResultListener {
    private static final int PICK_FILE_REQUEST = 1;
    private Context context;
    private Activity activity;
    private String UploadURL;
    private String SuccessResult;
    private String UnSuccessResult;
    private String selectedFilePath;
    private String UPLOAD_TAG = "learn-net uploader library";
    private String fileAddress;

    public uploadToHost(Activity activity,Context c,String uploadURL,String fileAddress,String successResult,
                        String unSuccessResult){
        this.activity = activity;
        this.context = c;
        this.UploadURL = uploadURL;
        this.SuccessResult = successResult;
        this.UnSuccessResult = unSuccessResult;
        this.fileAddress = fileAddress;
    }

    public void showFileChooser() {
        Intent intent = new Intent();
        //sets the select file to all types of files
        intent.setType("*/*");
        //allows to select data and return it
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //starts new activity to select file and return data
        activity.startActivityForResult(Intent.createChooser(intent,"Choose File to Upload.."),PICK_FILE_REQUEST);
        Log.e(UPLOAD_TAG,"file chooser started");
    }


    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == PICK_FILE_REQUEST){
                if(data == null){
                    //no data present
                    Toast.makeText(context,"no file selected",Toast.LENGTH_SHORT).show();
                }
                Uri selectedFileUri = data.getData();
                selectedFilePath = FilePath.getPath(context,selectedFileUri);
                Log.i(UPLOAD_TAG,"Selected File Path:" + selectedFilePath);

                if(selectedFilePath != null && !selectedFilePath.equals("")){
                    getSelectedFilePath();
                }else{
                    Toast.makeText(context,"Cannot upload file to server",Toast.LENGTH_SHORT).show();
                }
            }
        }
        return true;
    }

    public String getSelectedFilePath(){
        return selectedFilePath;
    }

    public int uploadFile(final String selectedFilePath,ProgressDialog dialog){

        int serverResponseCode = 0;

        final HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";


        int bytesRead,bytesAvailable,bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = new File(selectedFilePath);


        String[] parts = selectedFilePath.split("/");
        final String fileName = parts[parts.length-1];

        if (!selectedFile.isFile()){
        dialog.dismiss();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String st = "Source File Doesn't Exist: " + selectedFilePath;
                Toast.makeText(context,st,Toast.LENGTH_SHORT).show();
                Log.e(UPLOAD_TAG,st);
            }
        });
        return 0;
        }else{
            try{
            FileInputStream fileInputStream = new FileInputStream(selectedFile);
            URL url = new URL(UploadURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);//Allow Inputs
            connection.setDoOutput(true);//Allow Outputs
            connection.setUseCaches(false);//Don't use a cached Copy
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("ENCTYPE", "multipart/form-data");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            connection.setRequestProperty("uploaded_file",selectedFilePath);

            //creating new dataoutputstream
            dataOutputStream = new DataOutputStream(connection.getOutputStream());

            //writing bytes to data outputstream
            dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                    + selectedFilePath + "\"" + lineEnd);

            dataOutputStream.writeBytes(lineEnd);

            //returns no. of bytes present in fileInputStream
            bytesAvailable = fileInputStream.available();
            //selecting the buffer size as minimum of available bytes or 1 MB
            bufferSize = Math.min(bytesAvailable,maxBufferSize);
            //setting the buffer as byte array of size of bufferSize
            buffer = new byte[bufferSize];

            //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
            bytesRead = fileInputStream.read(buffer,0,bufferSize);

            //loop repeats till bytesRead = -1, i.e., no bytes are left to read
            while (bytesRead > 0){
                //write the bytes read from inputstream
                dataOutputStream.write(buffer,0,bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable,maxBufferSize);
                bytesRead = fileInputStream.read(buffer,0,bufferSize);
            }

            dataOutputStream.writeBytes(lineEnd);
            dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();

            Log.i(UPLOAD_TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

            //response code of 200 indicates the server status OK
            if(serverResponseCode == 200){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String st = "File Upload completed.\n\n You can see the uploaded file here: \n\n" + fileAddress+"/uploads/"+ fileName;
                        Toast.makeText(context,st,Toast.LENGTH_SHORT).show();
                        Log.e(UPLOAD_TAG,st);
                    }
                });
            }

            //closing the input and output streams
            fileInputStream.close();
            dataOutputStream.flush();
            dataOutputStream.close();



        } catch (FileNotFoundException e) {
            e.printStackTrace();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context,"File Not Found",Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Toast.makeText(context, "URL error!", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();
        }
        dialog.dismiss();
        return serverResponseCode;
    }

    }
}
