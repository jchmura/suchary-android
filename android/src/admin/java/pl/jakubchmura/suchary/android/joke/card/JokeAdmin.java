package pl.jakubchmura.suchary.android.joke.card;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import pl.jakubchmura.suchary.android.R;
import pl.jakubchmura.suchary.android.joke.Joke;
import pl.jakubchmura.suchary.android.joke.api.network.AdminJokeService;
import pl.jakubchmura.suchary.android.joke.api.network.RestAdminHelper;
import retrofit.RetrofitError;

public class JokeAdmin{

    private static final String TAG = "JokeAdmin";

    private final Context mContext;
    private final Joke mJoke;

    public JokeAdmin(Context context, Joke joke) {
        mContext = context;
        mJoke = joke;
    }

    public void edit() {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle(mContext.getString(R.string.edit_joke));

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View prompt = layoutInflater.inflate(R.layout.joke_editor, null);
        final EditText input = (EditText) prompt.findViewById(R.id.input);
        input.setText(mJoke.getBody());
        alert.setView(prompt);

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String body = input.getText().toString();
                Log.i("JokeEditor", "Sending for joke with key " + mJoke.getKey() + ". New body:\n" + body);

                new AsyncTask<Void, Void, Void>() {
                    private boolean success = false;
                    @Override
                    protected Void doInBackground(Void... params) {
                        AdminJokeService service = RestAdminHelper.getService(mContext, AdminJokeService.class);
                        try {
                            service.edit(mJoke.getKey(), body);
                            success = true;
                        } catch (RetrofitError e) {
                            Log.e(TAG, "{editJoke}", e);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        if (success) {
                            Toast.makeText(mContext, "Joke edited", Toast.LENGTH_SHORT).show();
                        }
                    }
                }.execute((Void) null);
            }
        });

        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("JokeEditor", "canceled");
            }
        });
        alert.show();
    }

    public void delete() {
        new AsyncTask<Void, Void, Void>() {
            private boolean success = false;
            @Override
            protected Void doInBackground(Void... params) {
                AdminJokeService service = RestAdminHelper.getService(mContext, AdminJokeService.class);
                try {
                    service.delete(mJoke.getKey());
                    success = true;
                } catch (RetrofitError e) {
                    Log.e(TAG, "{deleteJoke}", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (success) {
                    Toast.makeText(mContext, "Joke removed", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute((Void) null);
    }

}
