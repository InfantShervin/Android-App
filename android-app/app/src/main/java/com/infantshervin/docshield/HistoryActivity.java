package com.infantshervin.docshield;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecycler;
    private HistoryAdapter adapter;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        prefs = getSharedPreferences("DocShieldPrefs", MODE_PRIVATE);
        historyRecycler = findViewById(R.id.history_recycler);
        historyRecycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HistoryAdapter(new ArrayList<>());
        historyRecycler.setAdapter(adapter);

        fetchHistory();
    }

    private void fetchHistory() {
        String token = "Bearer " + prefs.getString("token", "");
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getHistory(token).enqueue(new Callback<List<ApiService.AnalysisResponse>>() {
            @Override
            public void onResponse(Call<List<ApiService.AnalysisResponse>> call, Response<List<ApiService.AnalysisResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateData(response.body());
                } else {
                    Toast.makeText(HistoryActivity.this, "Failed to load history", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ApiService.AnalysisResponse>> call, Throwable t) {
                Toast.makeText(HistoryActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<ApiService.AnalysisResponse> items;

        public HistoryAdapter(List<ApiService.AnalysisResponse> items) { this.items = items; }

        public void updateData(List<ApiService.AnalysisResponse> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ApiService.AnalysisResponse item = items.get(position);
            holder.filename.setText(item.filename);
            holder.risk.setText(item.risk_level);
            holder.score.setText((int)item.exposure_score + "%");
            
            holder.itemView.setOnClickListener(v -> {
                ResultActivity.currentResult = item;
                startActivity(new Intent(HistoryActivity.this, ResultActivity.class));
            });
        }

        @Override
        public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView filename, risk, score;
            ViewHolder(View v) {
                super(v);
                filename = v.findViewById(R.id.history_filename);
                risk = v.findViewById(R.id.history_risk);
                score = v.findViewById(R.id.history_score);
            }
        }
    }
}
