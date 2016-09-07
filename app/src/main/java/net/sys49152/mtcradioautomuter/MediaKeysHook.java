package net.sys49152.mtcradioautomuter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.view.KeyEvent;

import de.robv.android.xposed.XC_MethodHook;

/**
 * Based in XposedMTC code by agentdr8
 *
 * Sends Media Key events as response to IrKeys (Steering controls included)
 * but through AudioManager.dispatchKeyEvent() so it will only go to the active MediaSession.
 *
 * Created by storm on 04-09-2016.
 */
public class MediaKeysHook extends XC_MethodHook {

    public static final String setCurrentModeToRadio = "net.sys49152.intent.radio_is_on";
    public static final String setCurrentModeToMusic = "net.sys49152.intent.music_is_on";
    private static final String tag = "MediaKeysHook";
    private boolean isMusicOn = true;

    private void cmdPlayer(Context ctx, String cmd) {

        AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);

        if (!isMusicOn) {
            // Supposedly no music playing so we wont continue
            MTCRadioAutoMuter.log(tag, "No music playing!");
            return;
        }

        int keyEvent = -1;
        switch (cmd) {
            case "play":
                keyEvent = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
                break;
            case "next":
                keyEvent = KeyEvent.KEYCODE_MEDIA_NEXT;
                break;
            case "prev":
                keyEvent = KeyEvent.KEYCODE_MEDIA_PREVIOUS;
                break;
            case "stop":
                keyEvent = KeyEvent.KEYCODE_MEDIA_STOP;
                break;
        }

        am.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyEvent));
        am.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyEvent));

        MTCRadioAutoMuter.log(tag, "Sent media key!");
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        // We're hooking onCreate so this is an Activity which extends Context
        final Context ctx = (Context) param.thisObject;

        /* Create a Broadcast Receiver that listens to the IR remote keys.
        * Apparently the steering wheel controls emit the same broadcasts.
        */
        BroadcastReceiver mtckeyproc = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent)
            {
                MTCRadioAutoMuter.log(tag, "Received IRKeyUp!");
                String s = intent.getAction();
                int i = intent.getIntExtra("keyCode", -1);
                if (s.equals("com.microntek.irkeyDown"))
                    switch (i) {
                        case 13:
                            cmdPlayer(ctx, "stop");
                            return;
                        case 14:
                        case 24:
                        case 46:
                        case 62:
                            cmdPlayer(ctx, "next");
                            return;
                        case 6:
                        case 22:
                        case 45:
                        case 61:
                            cmdPlayer(ctx, "prev");
                            return;
                        case 3:
                            cmdPlayer(ctx, "play");
                    }
            }
        };
        IntentFilter keysIntentFilter = new IntentFilter();
        keysIntentFilter.addAction("com.microntek.irkeyDown");
        keysIntentFilter.addAction("com.microntek.irkeyUp");
        ctx.registerReceiver(mtckeyproc, keysIntentFilter);

        BroadcastReceiver modeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(setCurrentModeToMusic)) {
                    isMusicOn = true;
                    MTCRadioAutoMuter.log(tag, "Music set on!");
                } else if (intent.getAction().equals(setCurrentModeToRadio)) {
                    isMusicOn = false;
                    MTCRadioAutoMuter.log(tag, "Music set off!");
                }
            }
        };
        IntentFilter modeIntentFilter = new IntentFilter();
        modeIntentFilter.addAction(setCurrentModeToMusic);
        modeIntentFilter.addAction(setCurrentModeToRadio);
        ctx.registerReceiver(modeReceiver, modeIntentFilter);
    }
}
