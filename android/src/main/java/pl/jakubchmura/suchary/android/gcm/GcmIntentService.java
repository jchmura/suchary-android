package pl.jakubchmura.suchary.android.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.Date;

import pl.jakubchmura.suchary.android.R;
import pl.jakubchmura.suchary.android.joke.api.changes.ChangeHandler;
import pl.jakubchmura.suchary.android.joke.api.changes.ChangeResolver;
import pl.jakubchmura.suchary.android.joke.api.model.APIResult;
import pl.jakubchmura.suchary.android.joke.api.network.JokeRetrofitSpiceService;
import pl.jakubchmura.suchary.android.joke.api.network.requests.ChangedJokesRequest;

/**
 * An {@link android.app.IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class GcmIntentService extends IntentService {

    private static final String TAG = "IntentService";
    private static boolean mHandling = false;
    private Intent mIntent;
    private SpiceManager mSpiceManager = new SpiceManager(JokeRetrofitSpiceService.class);

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mSpiceManager.start(this);
        mIntent = intent;
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (extras != null && !extras.isEmpty()) {
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            switch (messageType) {
                case GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR:
                    break;
                case GoogleCloudMessaging.MESSAGE_TYPE_DELETED:
                    break;
                case GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE:
                    String type = extras.getString("type");
                    Log.i(TAG, "Received GCM message: " + type);
                    switch (type) {
                        case "change":
                            handleChangedJokes();
                            break;
                        case "message":
                            handleMessage(extras);
                            break;
                        default:
                            finish();
                    }
                    break;
            }
        }
    }

    private void handleChangedJokes() {
        if (mHandling) {
            GcmBroadcastReceiver.completeWakefulIntent(mIntent);
            return;
        }
        mHandling = true;
        getChanged();
    }

    private void getChanged() {
        final Date lastChange = ChangeResolver.getLastChange(this);
        ChangedJokesRequest request = new ChangedJokesRequest(lastChange);
        mSpiceManager.execute(request, new RequestListener<APIResult.APIJokes>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {}

            @Override
            public void onRequestSuccess(APIResult.APIJokes apiJokes) {
                ChangeResolver resolver = new ChangeResolver(apiJokes, lastChange);
                ChangeHandler handler = new ChangeHandler(GcmIntentService.this);
                handler.handleAll(resolver, true);
                ChangeResolver.saveLastChange(GcmIntentService.this, apiJokes.getLastChange());
                finish();
            }
        });
    }

    private void handleMessage(Bundle extras) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        builder.setVibrate(new long[]{0, 300, 400, 300});
        builder.setSmallIcon(R.drawable.ic_stat_notify);
        builder.setContentTitle(extras.getString("title"));
        builder.setContentText(extras.getString("text"));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(extras.getString("text"))
                .setBigContentTitle(extras.getString("title")));
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify("Suchary message", 0, builder.build());
    }

    private void finish() {
        mHandling = false;
        if (mSpiceManager.isStarted()) {
            mSpiceManager.shouldStop();
        }
        GcmBroadcastReceiver.completeWakefulIntent(mIntent);
    }
}
