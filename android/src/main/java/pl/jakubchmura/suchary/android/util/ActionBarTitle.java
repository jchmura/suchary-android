package pl.jakubchmura.suchary.android.util;

import android.app.ActionBar;
import android.app.Activity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import pl.jakubchmura.suchary.android.R;

public class ActionBarTitle {

    private Activity mActivity;

    public ActionBarTitle(Activity activity) {
        mActivity = activity;
    }

    public void setTitle(int resource) {
        setTitle(mActivity.getResources().getString(resource));
    }

    public void setTitle(CharSequence title) {
        SpannableString s = new SpannableString(title);
        s.setSpan(new TypefaceSpan(mActivity, "RobotoCondensed-Bold.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new ForegroundColorSpan(mActivity.getResources().getColor(R.color.action_bar_title)),
                0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the action bar title with the TypefaceSpan instance
        ActionBar actionBar = mActivity.getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(s);
        }
    }

    public void setSubTitle(CharSequence subtitle) {
        SpannableString s = new SpannableString(subtitle);
        s.setSpan(new TypefaceSpan(mActivity, "RobotoCondensed-Regular.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new ForegroundColorSpan(mActivity.getResources().getColor(R.color.action_bar_subtitle)),
                0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the action bar title with the TypefaceSpan instance
        ActionBar actionBar = mActivity.getActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(s);
        }
    }
}
