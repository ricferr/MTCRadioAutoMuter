package net.sys49152.mtcradioautomuter;

import android.media.AudioManager;

import de.robv.android.xposed.XC_MethodHook;

/**
 * Hook for AudioManager.requestAudioFocus that mutes the radio if an app requests the audio focus.
 *
 * Created by storm on 28-08-2016.
 */
public class OnAudioFocusMTCRadioMuterHook extends XC_MethodHook {

    private static final String tag = "OnAudioFocusMTCRadioMuterHook";

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

        // Only proceed in case stream type is STREAM_MUSIC
        int streamType = (int) param.args[1];
        if (streamType != AudioManager.STREAM_MUSIC) {
            return;
        }

        // This is an AudioManager
        AudioManager am = (AudioManager) param.thisObject;
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

        MTCRadioAutoMuter.log(tag, "Muted MTCRadio!");
    }
}
