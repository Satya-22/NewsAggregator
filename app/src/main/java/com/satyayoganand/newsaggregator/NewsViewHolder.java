package com.satyayoganand.newsaggregator;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NewsViewHolder extends RecyclerView.ViewHolder {

    TextView author;
    TextView title;
    TextView description;
    TextView publishedAt;
    TextView counter;
    ImageView image;

    public NewsViewHolder(@NonNull View itemView) {
        super(itemView);
        author = itemView.findViewById(R.id.articleAuthor);
        title = itemView.findViewById(R.id.newsHeadline);
        image = itemView.findViewById(R.id.articleImage);
        description = itemView.findViewById(R.id.articleText);
        publishedAt = itemView.findViewById(R.id.articleDate);
        counter = itemView.findViewById(R.id.articleCounter);
    }
}
