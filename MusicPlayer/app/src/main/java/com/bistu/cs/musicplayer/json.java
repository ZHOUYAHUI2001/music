package com.bistu.cs.musicplayer;

import java.io.Serializable;
import java.util.List;

public class json implements Serializable {

    private int code;
    private List<DataBean> data;
    private json.ResultBean result;

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public json.ResultBean getResult() {
        return result;
    }

    public void setResult(json.ResultBean result) {
        this.result = result;
    }

    public static class DataBean implements Serializable {
        private int id;
        private String url;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class ResultBean implements Serializable {

        private boolean hasMore;
        private int songCount;
        private List<json.ResultBean.SongsBean> songs;

        public boolean isHasMore() {
            return hasMore;
        }

        public void setHasMore(boolean hasMore) {
            this.hasMore = hasMore;
        }

        public int getSongCount() {
            return songCount;
        }

        public void setSongCount(int songCount) {
            this.songCount = songCount;
        }

        public List<json.ResultBean.SongsBean> getSongs() {
            return songs;
        }

        public void setSongs(List<json.ResultBean.SongsBean> songs) {
            this.songs = songs;
        }

        public static class SongsBean implements Serializable {

            private int id;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }
        }
    }
}