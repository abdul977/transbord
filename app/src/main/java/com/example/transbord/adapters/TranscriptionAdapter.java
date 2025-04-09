package com.example.transbord.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.transbord.R;
import com.example.transbord.data.Transcription;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TranscriptionAdapter extends ListAdapter<Transcription, TranscriptionAdapter.TranscriptionViewHolder> {
    
    private final OnTranscriptionClickListener listener;
    private final OnTranscriptionOptionsClickListener optionsListener;
    
    public TranscriptionAdapter(OnTranscriptionClickListener listener, OnTranscriptionOptionsClickListener optionsListener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        this.optionsListener = optionsListener;
    }
    
    private static final DiffUtil.ItemCallback<Transcription> DIFF_CALLBACK = new DiffUtil.ItemCallback<Transcription>() {
        @Override
        public boolean areItemsTheSame(@NonNull Transcription oldItem, @NonNull Transcription newItem) {
            return oldItem.getId() == newItem.getId();
        }
        
        @Override
        public boolean areContentsTheSame(@NonNull Transcription oldItem, @NonNull Transcription newItem) {
            return oldItem.getText().equals(newItem.getText()) &&
                    oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getTimestamp() == newItem.getTimestamp();
        }
    };
    
    @NonNull
    @Override
    public TranscriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transcription, parent, false);
        return new TranscriptionViewHolder(itemView);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TranscriptionViewHolder holder, int position) {
        Transcription current = getItem(position);
        holder.bind(current);
    }
    
    public Transcription getTranscriptionAt(int position) {
        return getItem(position);
    }
    
    class TranscriptionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvDate;
        private final TextView tvPreview;
        private final ImageView ivMore;
        
        public TranscriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvPreview = itemView.findViewById(R.id.tv_preview);
            ivMore = itemView.findViewById(R.id.iv_more);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTranscriptionClick(getItem(position));
                }
            });
            
            ivMore.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && optionsListener != null) {
                    optionsListener.onOptionsClick(getItem(position), ivMore);
                }
            });
        }
        
        public void bind(Transcription transcription) {
            tvTitle.setText(transcription.getTitle());
            
            // Format date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(new Date(transcription.getTimestamp()));
            tvDate.setText(formattedDate);
            
            // Set preview text (first 100 characters)
            String previewText = transcription.getText();
            if (previewText.length() > 100) {
                previewText = previewText.substring(0, 100) + "...";
            }
            tvPreview.setText(previewText);
        }
    }
    
    public interface OnTranscriptionClickListener {
        void onTranscriptionClick(Transcription transcription);
    }
    
    public interface OnTranscriptionOptionsClickListener {
        void onOptionsClick(Transcription transcription, View anchorView);
    }
}
