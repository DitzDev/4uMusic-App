package iiec.ditzdev.fourumusic.models;

public class MusicModels {
    private String title;
    private String path;
    private String duration;
    
    public MusicModels(String title, String path, String duration) {
        this.title = title;
        this.path = path;
        this.duration = duration;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getPath() {
        return path;
    }
    
    public String getDuration() {
        return duration;
    }
}