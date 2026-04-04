package com.infantshervin.docshield;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    public static ApiService.AnalysisResponse currentResult;

    private ProgressBar scoreProgress;
    private TextView scoreText, riskTag, summaryText;
    private RecyclerView entitiesRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        scoreProgress = findViewById(R.id.score_progress);
        scoreText = findViewById(R.id.score_text);
        riskTag = findViewById(R.id.risk_tag);
        summaryText = findViewById(R.id.summary_text);
        entitiesRecycler = findViewById(R.id.entities_recycler);

        if (currentResult != null) {
            displayResults();
        }
    }

    private void displayResults() {
        int score = (int) currentResult.exposure_score;
        scoreProgress.setProgress(score);
        scoreText.setText(score + "%");
        riskTag.setText(currentResult.risk_level.toUpperCase());
        
        // Dynamic risk tag color
        if (currentResult.risk_level.equalsIgnoreCase("Safe")) {
            riskTag.setBackgroundResource(R.drawable.rounded_tag_safe);
        } else if (currentResult.risk_level.equalsIgnoreCase("High") || currentResult.risk_level.equalsIgnoreCase("Critical")) {
            riskTag.setBackgroundResource(R.drawable.rounded_tag_high);
        }

        summaryText.setText(currentResult.summary);

        entitiesRecycler.setLayoutManager(new LinearLayoutManager(this));
        entitiesRecycler.setAdapter(new EntityAdapter(currentResult.entities));
    }

    // Entity RecyclerView Adapter
    private class EntityAdapter extends RecyclerView.Adapter<EntityAdapter.ViewHolder> {
        private final List<ApiService.Entity> entities;

        public EntityAdapter(List<ApiService.Entity> entities) { this.entities = entities; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entity, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ApiService.Entity entity = entities.get(position);
            holder.text.setText(entity.text);
            holder.label.setText(entity.label);
            holder.sensitivity.setText(entity.sensitivity);
            
            // Highlight based on sensitivity
            if (entity.sensitivity.equalsIgnoreCase("Critical")) {
                holder.sensitivity.setTextColor(Color.parseColor("#D32F2F"));
            } else if (entity.sensitivity.equalsIgnoreCase("High")) {
                holder.sensitivity.setTextColor(Color.parseColor("#F57C00"));
            }
        }

        @Override
        public int getItemCount() { return entities.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView text, label, sensitivity;
            ViewHolder(View v) {
                super(v);
                text = v.findViewById(R.id.entity_text);
                label = v.findViewById(R.id.entity_label);
                sensitivity = v.findViewById(R.id.entity_sensitivity);
            }
        }
    }
}
