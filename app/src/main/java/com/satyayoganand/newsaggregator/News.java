package com.satyayoganand.newsaggregator;

import android.graphics.Bitmap;

public class News {
    private final String author;
    private final String title;
    private final String description;
    private final String iconUrl;
    private final String publishedAt;
    private final String url;

    public News(String author, String title, String description, String iconUrl, String publishedAt,String url) {
        this.author = author;
        this.title = title;
        this.description = description;
        this.iconUrl = iconUrl;
        this.publishedAt = publishedAt;
        this.url = url;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getPublishedAt() {
        return publishedAt;
    }
}
