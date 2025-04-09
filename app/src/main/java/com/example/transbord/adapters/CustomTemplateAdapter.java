package com.example.transbord.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.transbord.R;
import com.example.transbord.utils.TemplateManager;

import java.util.List;

public class CustomTemplateAdapter extends RecyclerView.Adapter<CustomTemplateAdapter.ViewHolder> {

    private final List<TemplateManager.CustomTemplate> templates;
    private final OnTemplateEditListener editListener;
    private final OnTemplateDeleteListener deleteListener;

    public interface OnTemplateEditListener {
        void onTemplateEdit(int position, TemplateManager.CustomTemplate template);
    }

    public interface OnTemplateDeleteListener {
        void onTemplateDelete(int position);
    }

    public CustomTemplateAdapter(List<TemplateManager.CustomTemplate> templates,
                                OnTemplateEditListener editListener,
                                OnTemplateDeleteListener deleteListener) {
        this.templates = templates;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_custom_template, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TemplateManager.CustomTemplate template = templates.get(position);
        holder.tvTemplateName.setText(template.getName());

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onTemplateEdit(position, template);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onTemplateDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return templates.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTemplateName;
        ImageButton btnEdit;
        ImageButton btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvTemplateName = itemView.findViewById(R.id.tv_template_name);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
