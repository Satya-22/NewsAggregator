package com.satyayoganand.newsaggregator;


import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NewsAdapter extends
        RecyclerView.Adapter<NewsViewHolder>{
    private final MainActivity mainActivity;
    private final ArrayList<News> newsList;
    public SimpleDateFormat simpleDateFormat,simpleDateFormat1;
    public String dateTime;

    public NewsAdapter(MainActivity mainActivity, ArrayList<News> newsList) {
        this.mainActivity = mainActivity;
        this.newsList = newsList;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NewsViewHolder(
                LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.news_entry, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news =  newsList.get(position);

        final int resourceId = mainActivity.getResources().
                getIdentifier(news.getUrl(), "drawable", mainActivity.getPackageName());

        if(news.getTitle() != "") {
            holder.title.setText(news.getTitle());
            holder.title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), MainActivity.class);
                    try {
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.getUrl()));
                    } catch (Exception e) {
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.getUrl()));
                    }
                    v.getContext().startActivity(intent);
                }
            });

        }
        else {
            holder.title.setVisibility(View.GONE);
        }

        holder.image.setImageResource(resourceId);

        if(news.getAuthor() != "") {
            holder.author.setText(news.getAuthor());
        }
        else{
            holder.author.setVisibility(View.GONE);
        }

        if(news.getDescription() != "") {
            holder.description.setText(news.getDescription());
            holder.description.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), MainActivity.class);
                    try {
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.getUrl()));
                    } catch (Exception e) {
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.getUrl()));
                    }
                    v.getContext().startActivity(intent);
                }
            });
        }
        else{
            holder.description.setVisibility(View.GONE);
        }


        if(news.getPublishedAt() != "") {
            String date = null;

            String DateFormat = (news.getPublishedAt().replace("T"," ").split(":")[0])+":"+(news.getPublishedAt().replace("T"," ").split(":")[1].split(":")[0]);

            simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            System.out.println("Published At : "+DateFormat);
            simpleDateFormat = new SimpleDateFormat("LLL dd, yyyy HH:mm");
            try {
                date = simpleDateFormat.format(simpleDateFormat1.parse(DateFormat));
                System.out.println("Print Date : "+date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            holder.publishedAt.setText(date);
        }
        else{
            holder.publishedAt.setVisibility(View.GONE);
        }

        holder.counter.setText(MessageFormat.format("{0} of {1}", position+1, getItemCount()));
        if(news.getIconUrl() != "") {
            Picasso.get().load(news.getIconUrl()).placeholder(R.drawable.loading).error(R.drawable.brokenimage)
                    .into(holder.image);
        }
        else{
            Picasso.get().load(R.drawable.noimage).into(holder.image);
        }
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                try {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.getUrl()));
                } catch (Exception e) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.getUrl()));
                }
                v.getContext().startActivity(intent);
            }
        });



    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }
}
