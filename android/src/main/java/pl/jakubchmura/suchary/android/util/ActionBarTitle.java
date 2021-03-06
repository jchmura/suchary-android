package pl.jakubchmura.suchary.android.util;

import android.os.Build;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import pl.jakubchmura.suchary.android.R;

public class ActionBarTitle {

    private AppCompatActivity mActivity;

    public ActionBarTitle(AppCompatActivity activity) {
        mActivity = activity;
    }

    public void setTitle(@StringRes int resource) {
        setTitle(mActivity.getString(resource));
    }

    public void setTitle(CharSequence title) {
        android.support.v7.app.ActionBar actionBar = mActivity.getSupportActionBar();
        if (actionBar != null) {
            if (supportSpanTitle()) {
                SpannableString s = new SpannableString(title);
                s.setSpan(new TypefaceSpan(mActivity, "RobotoCondensed-Regular.ttf"), 0, s.length(),
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                s.setSpan(new ForegroundColorSpan(mActivity.getResources().getColor(R.color.action_bar_title)),
                        0, s.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                actionBar.setTitle(s);
            } else {
                actionBar.setTitle(title);
            }
        }
        setScreenName(title.toString());
    }

    public void setSubTitle(CharSequence subtitle) {
        android.support.v7.app.ActionBar actionBar = mActivity.getSupportActionBar();
        if (actionBar != null) {
            if (supportSpanTitle()) {
                SpannableString s = new SpannableString(subtitle);
                s.setSpan(new TypefaceSpan(mActivity, "RobotoCondensed-Regular.ttf"), 0, s.length(),
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                s.setSpan(new ForegroundColorSpan(mActivity.getResources().getColor(R.color.action_bar_subtitle)),
                        0, s.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                actionBar.setSubtitle(s);
            } else {
                actionBar.setSubtitle(subtitle);
            }
        }
    }

    private boolean supportSpanTitle() {
        int api = Build.VERSION.SDK_INT;
        return api >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    private void setScreenName(String name) {
        if (!name.equals(mActivity.getString(R.string.app_name))) {
            Analytics.setScreenName(name);
        }
    }
}
