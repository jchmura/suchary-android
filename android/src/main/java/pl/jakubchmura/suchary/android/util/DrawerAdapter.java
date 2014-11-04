package pl.jakubchmura.suchary.android.util;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.jetbrains.annotations.Nullable;

public class DrawerAdapter<T> extends ArrayAdapter<T> {

    private final Context mContext;
    private int mTextId;
    private final int mImageId;
    private int[] mImageRes;

    public DrawerAdapter(Context context, int resource, int textViewResourceId, int imageViewResourceId, T[] strings, int[] imageRes) {
        super(context, resource, textViewResourceId, strings);
        mContext = context;
        mTextId = textViewResourceId;
        mImageId = imageViewResourceId;
        mImageRes = imageRes;
    }

    @Nullable
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView text;
        ImageView image;
        text = (TextView) view.findViewById(mTextId);
        Typeface typeface = FontCache.get("fonts/Roboto-Light.ttf", mContext);
        text.setTypeface(typeface);

        image = (ImageView) view.findViewById(mImageId);
        image.setImageResource(mImageRes[position]);

        return view;
    }
}
