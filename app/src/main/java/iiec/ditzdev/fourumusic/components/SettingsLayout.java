package iiec.ditzdev.fourumusic.components;

import iiec.ditzdev.fourumusic.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;

public class SettingsLayout extends LinearLayout {
    private static final String TAG = "SettingsLayout";
    
    private ImageView iconView;
    private TextView titleView;
    private TextView subtitleView;

    private String title = "";
    private String subtitle = "";
    private int iconResource = 0;

    public SettingsLayout(Context context) {
        super(context);
        init(context, null, 0);
    }

    public SettingsLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public SettingsLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        try {
            setOrientation(LinearLayout.HORIZONTAL);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null) {
                inflater.inflate(R.layout.settings_layout, this, true);
            } else {
                Log.e(TAG, "LayoutInflater is null");
                return;
            }
            
            try {
                iconView = findViewById(R.id.settings_icon);
                titleView = findViewById(R.id.settings_title);
                subtitleView = findViewById(R.id.settings_subtitle);
            } catch (Exception e) {
                Log.e(TAG, "Error finding views: " + e.getMessage());
                return;
            }
            
            if (attrs != null) {
                try {
                    TypedArray a = context.getTheme().obtainStyledAttributes(
                        attrs,
                        R.styleable.SettingsLayout,
                        defStyleAttr, 0
                    );

                    try {
                        title = a.getString(R.styleable.SettingsLayout_title) == null ? "" : 
                               a.getString(R.styleable.SettingsLayout_title);
                        subtitle = a.getString(R.styleable.SettingsLayout_subtitle) == null ? "" : 
                                 a.getString(R.styleable.SettingsLayout_subtitle);
                        iconResource = a.getResourceId(R.styleable.SettingsLayout_icon, 0);
                    } finally {
                        a.recycle();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error reading attributes: " + e.getMessage());
                }
            }
            updateViews();

        } catch (Exception e) {
            Log.e(TAG, "Error in init: " + e.getMessage());
        }
    }

    private void updateViews() {
        try {
            if (iconResource != 0 && iconView != null) {
                try {
                    iconView.setImageResource(iconResource);
                    iconView.setVisibility(VISIBLE);
                } catch (Exception e) {
                    Log.e(TAG, "Error setting icon: " + e.getMessage());
                    iconView.setVisibility(GONE);
                }
            } else if (iconView != null) {
                iconView.setVisibility(GONE);
            }

            if (titleView != null) {
                titleView.setText(title);
            }

            if (subtitleView != null) {
                if (subtitle != null && !subtitle.isEmpty()) {
                    subtitleView.setText(subtitle);
                    subtitleView.setVisibility(VISIBLE);
                } else {
                    subtitleView.setVisibility(GONE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating views: " + e.getMessage());
        }
    }

    public void setTitle(String title) {
        this.title = title != null ? title : "";
        if (titleView != null) {
            titleView.setText(this.title);
        }
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle != null ? subtitle : "";
        if (subtitleView != null) {
            if (!this.subtitle.isEmpty()) {
                subtitleView.setText(this.subtitle);
                subtitleView.setVisibility(VISIBLE);
            } else {
                subtitleView.setVisibility(GONE);
            }
        }
    }

    public void setIcon(int resourceId) {
        this.iconResource = resourceId;
        if (iconView != null) {
            if (resourceId != 0) {
                try {
                    iconView.setImageResource(resourceId);
                    iconView.setVisibility(VISIBLE);
                } catch (Exception e) {
                    Log.e(TAG, "Error setting icon: " + e.getMessage());
                    iconView.setVisibility(GONE);
                }
            } else {
                iconView.setVisibility(GONE);
            }
        }
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }
}