package iiec.ditzdev.fourumusic.models;

import android.os.Parcel;
import android.os.Parcelable;

public class MusicModels implements Parcelable {
    private String title;
    private String path;
    private String duration;
    
    public MusicModels(String title, String path, String duration) {
        this.title = title;
        this.path = path;
        this.duration = duration;
    }

    protected MusicModels(Parcel in) {
        title = in.readString();
        path = in.readString();
        duration = in.readString();
    }
    public static final Creator<MusicModels> CREATOR = new Creator<MusicModels>() {
        @Override
        public MusicModels createFromParcel(Parcel in) {
            return new MusicModels(in);
        }

        @Override
        public MusicModels[] newArray(int size) {
            return new MusicModels[size];
        }
    };

    public String getTitle() {
        return title;
    }
    
    public String getPath() {
        return path;
    }
    
    public String getDuration() {
        return duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(path);
        dest.writeString(duration);
    }
}