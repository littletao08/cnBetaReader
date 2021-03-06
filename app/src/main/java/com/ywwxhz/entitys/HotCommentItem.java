package com.ywwxhz.entitys;

import java.util.Map;

/**
 * cnBetaReader
 *
 * Created by 远望の无限(ywwxhz) on 15-3-23 17:53.
 */
public class HotCommentItem {
    private String title;
    private String description;
    private String from;
    private String url;
    private String newstitle;
    private Map<String,String> user;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNewstitle() {
        return newstitle;
    }

    public void setNewstitle(String newstitle) {
        this.newstitle = newstitle;
    }

    public Map<String, String> getUser() {
        return user;
    }

    public void setUser(Map<String, String> user) {
        this.user = user;
    }
}
