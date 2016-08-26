package net.sys49152.mtcradioautomuter;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;

import de.robv.android.xposed.XC_MethodHook;

/**
 * Created by storm on 25-08-2016.
 */
public class MTCRadioMuterHook extends XC_MethodHook {

    private final String tag = "MTCRadioMuterHook";

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        Intent i = (Intent) param.args[0];

        // Can't be too sure.
        if (i == null) {
            return;
        }

        // Explicit Intents have null action
        if (i.getAction() == null) {
            return;
        }

        if (!i.getAction().equals(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION)) {
            return;
        }

        MTCRadioAutoMuter.log(tag, "Intercept " + i.getAction());

        // This object can call sendBroadcast so it extends Context for sure
        Context ctx = (Context) param.thisObject;
        AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        am.setParameters("ctl_radio_mute=true");
        am.setParameters("av_channel_exit=fm");
        am.setParameters("av_channel_enter=sys");

        // Try to get the radio app itself to stop
//        Intent killradio = new Intent("com.microntek.radio.power");
////            String mode = intent.getStringExtra("mode");
////            String srcpkg = intent.getStringExtra("pkg");
//        killradio.putExtra("class", "com.microntek.radio");
//        ctx.sendBroadcast(killradio);

        // TODO We should modify this to the correct value but it cant be accessed from here
        // due to the way XPosed works
//        setIntField(getMicrontekServer(), "mtcappmode", 3);

        Intent intent = new Intent("com.microntek.canbusdisplay");
        intent.putExtra("type", "off");
        ctx.sendBroadcast(intent);

        MTCRadioAutoMuter.log(tag, "Muted MTCRadio!");
    }
}
