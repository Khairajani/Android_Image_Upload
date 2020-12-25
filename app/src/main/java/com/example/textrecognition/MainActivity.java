package com.example.textrecognition;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;


public class MainActivity extends AppCompatActivity{

    private Button chooseButton,connectServer;
    public TextView responseText;
    private ImageView imageView;
    private static final int imageRequest = 777;
    private Bitmap bitmap;
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        imageView = findViewById(R.id.imageView);
        chooseButton = findViewById(R.id.chooseImage);

        connectServer=findViewById(R.id.connectServer);

        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chooseImage();
            }
        });
        connectServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToServer();
            }
        });

    }


    public void connectToServer(){

        EditText ipv4AddressView = findViewById(R.id.ipAddress);
        String ipv4Address = ipv4AddressView.getText().toString();
        EditText portNumberView = findViewById(R.id.portNumber);
        String portNumber = portNumberView.getText().toString();

        String postUrl= "http://"+ipv4Address+":"+portNumber+"/";
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        //String postBodyText="Hello";
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] imageByte = byteArrayOutputStream.toByteArray();
        multipartBodyBuilder.addFormDataPart("image", "Hello.jpg", RequestBody.create(MediaType.parse("image/*jpg"), imageByte));

        RequestBody postBodyImage = multipartBodyBuilder.build();
        postRequest(postUrl, postBodyImage);
//        String postBodyText = imageToString();
//
//        MediaType mediaType = MediaType.parse("text/plain; charset=utf-8");
//        RequestBody postBody = RequestBody.create(mediaType, postBodyText);

//        postRequest(postUrl, postBody);
    }

    void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();
                //System.out.println("============================================"+e+"============================================");

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView responseText = findViewById(R.id.responseText);
                        Toast.makeText(MainActivity.this,"Failed", Toast.LENGTH_SHORT).show();
                        responseText.setText("Failed to Connect to Server");
                    }
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, final okhttp3.Response response) throws IOException {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView responseText = findViewById(R.id.responseText);
                        try {

                            Toast.makeText(MainActivity.this,"Successful", Toast.LENGTH_SHORT).show();

                            JSONObject obj = new JSONObject(response.body().string());
                            String result = (String) obj.get("result");

                            byte[] encodedString = Base64.decode((String) obj.get("byte"), Base64.DEFAULT);

                            Bitmap decodedByte = BitmapFactory.decodeByteArray(encodedString, 0, encodedString.length);
                            imageView.setImageBitmap(decodedByte);
                            responseText.setText(result);

                        } catch (Exception e) {
                            System.out.println("++++++INSIDE CATCH______");
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void chooseImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //startActivityForResult(Intent.createChooser(intent,"My Image"),imageRequest);
        startActivityForResult(intent,imageRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==imageRequest && resultCode==RESULT_OK && data!=null) {
            Uri path = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), path);
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);

                chooseButton.setText("Reset Image");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}