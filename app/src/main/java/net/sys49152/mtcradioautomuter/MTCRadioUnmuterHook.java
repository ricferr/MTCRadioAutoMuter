package net.sys49152.mtcradioautomuter;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import de.robv.android.xposed.XC_MethodHook;

/**
 *
 * Hook the onResume of the radio activity so we can switch back the mixer to the radio.
 *
 * Created by storm on 25-08-2016.
 */
public class MTCRadioUnmuterHook extends XC_MethodHook {

    private static final String tag = "MTCRadioUnmuterHook";

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        Context ctx = (Context) param.thisObject;
        AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);

        am.setParameters("ctl_radio_mute=false");
        am.setParameters("av_channel_exit=sys");
        am.setParameters("av_channel_enter=fm");

        MTCRadioAutoMuter.log(tag, "Unmuded MTC Radio!");

        // TODO Again we should change mtcapp but it's not acessible

        Intent setRadioOn = new Intent();
        setRadioOn.setAction(MediaKeysHook.setCurrentModeToRadio);
        ctx.sendBroadcast(setRadioOn);
    }
}
