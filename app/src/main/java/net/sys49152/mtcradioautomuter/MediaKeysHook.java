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

    private static final String tag = "MediaKeysHook";

    private static void cmdPlayer(Context ctx, String cmd) {

        AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);

        if (!am.isMusicActive()) {
            // Supposedly no music playing so we wont continue
            MTCRadioAutoMuter.log(tag, "No music playing!");
            return;
        }

        if (cmd.equals("play")) {
            am.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
            am.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
        } else if (cmd.equals("next")) {
            am.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
            am.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));
        } else if (cmd.equals("prev")) {
            am.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
            am.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
        } else if (cmd.equals("stop")) {
            am.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_STOP));
            am.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_STOP));
        }
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
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("com.microntek.irkeyDown");
        intentfilter.addAction("com.microntek.irkeyUp");
        ctx.registerReceiver(mtckeyproc, intentfilter);
    }
}
