package com.infantshervin.docshield;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private SharedPreferences prefs;
    private ProgressBar progressBar;
    private TextView statusText;
    private MaterialButton scanBtn;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("DocShieldPrefs", MODE_PRIVATE);
        progressBar = findViewById(R.id.progress_bar);
        statusText = findViewById(R.id.status_text);
        scanBtn = findViewById(R.id.scan_btn);

        scanBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        if (findViewById(R.id.toolbar) != null) {
            findViewById(R.id.toolbar).setOnClickListener(null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_history) {
            startActivity(new Intent(this, HistoryActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            prefs.edit().remove("token").apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri fileUri = data.getData();
            prepareAndUploadFile(fileUri);
        }
    }

    private void prepareAndUploadFile(Uri uri) {
        progressBar.setVisibility(View.VISIBLE);
        statusText.setVisibility(View.VISIBLE);
        scanBtn.setEnabled(false);

        executorService.execute(() -> {
            File file = getFileFromUri(uri);
            runOnUiThread(() -> {
                if (file == null) {
                    progressBar.setVisibility(View.GONE);
                    statusText.setVisibility(View.GONE);
                    scanBtn.setEnabled(true);
                    Toast.makeText(this, "Could not read file", Toast.LENGTH_SHORT).show();
                } else {
                    uploadFile(file, uri);
                }
            });
        });
    }

    private void uploadFile(File file, Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        if (mimeType == null) mimeType = "image/jpeg";
        
        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        String token = "Bearer " + prefs.getString("token", "");
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.analyze(token, body).enqueue(new Callback<ApiService.AnalysisResponse>() {
            @Override
            public void onResponse(Call<ApiService.AnalysisResponse> call, Response<ApiService.AnalysisResponse> response) {
                progressBar.setVisibility(View.GONE);
                statusText.setVisibility(View.GONE);
                scanBtn.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ResultActivity.currentResult = response.body();
                    startActivity(new Intent(MainActivity.this, ResultActivity.class));
                } else {
                    Toast.makeText(MainActivity.this, "Scan Failed: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.AnalysisResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                statusText.setVisibility(View.GONE);
                scanBtn.setEnabled(true);
                Toast.makeText(MainActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File getFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            File tempFile = File.createTempFile("upload", ".jpg", getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
