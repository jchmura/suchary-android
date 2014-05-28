package pl.jakubchmura.suchary.android.util;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.jetbrains.annotations.Nullable;

public class DrawerAdapter<T> extends ArrayAdapter<T> {

    /**
     * If the inflated resource is not a TextView, {@link #mFieldId} is used to find
     * a TextView inside the inflated views hierarchy. This field must contain the
     * identifier that matches the one defined in the resource file.
     */
    private int mFieldId;

    private int mSelectedItem = 0;

    private final Context mContext;

    public DrawerAdapter(Context context, int resource, int textViewResourceId, T[] objects) {
        super(context, resource, textViewResourceId, objects);
        mContext = context;
        mFieldId = textViewResourceId;
    }

    @Nullable
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView text;
        try {
            text = (TextView) view.findViewById(mFieldId);
        } catch (ClassCastException e) {
            Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    "ArrayAdapter requires the resource ID to be a TextView", e);
        }
        selectItem(position, text);

        return view;
    }

    private void selectItem(int position, TextView view) {
        Typeface typeface;
        if (position == mSelectedItem) {
            typeface = Typeface.DEFAULT_BOLD;
        } else {
            typeface = FontCache.get("fonts/Roboto-Light.ttf", mContext);
        }
        view.setTypeface(typeface);
    }

    public void setItemChecked(int position) {
        mSelectedItem = position;
    }
}
