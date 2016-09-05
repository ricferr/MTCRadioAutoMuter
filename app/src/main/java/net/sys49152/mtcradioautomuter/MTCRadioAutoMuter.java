package net.sys49152.mtcradioautomuter;

import android.content.ContextWrapper;
import android.content.Intent;
import android.media.AudioManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by rferreira on 17-08-2016.
 *
 * Mutes MTC Radio every time a new audio session is started.
 * The idea is that this way it is not needed to exit MTCRadio before starting playback in another app.
 *
 * Based in parts on ExposedMTC by agentdr8 (https://github.com/agentdr8/XMTC)
 */

public class MTCRadioAutoMuter implements IXposedHookLoadPackage {

    private static final String tag = "MTCRadioAutoMuter";
    private final MTCRadioUnmuterHook mtcRadioUnmuterHook = new MTCRadioUnmuterHook();
    private final OnAudioFocusMTCRadioMuterHook onAudioFocusMTCRadioMuterHook = new OnAudioFocusMTCRadioMuterHook();
    private final MediaKeysHook mediaKeysHook = new MediaKeysHook();

    public static void log(String tag, String msg) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String formattedDate = df.format(c.getTime());
        XposedBridge.log("[" + formattedDate + "] " + tag + ": " + msg);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {

        String mtcservice = "android.microntek.service";
        String radiopkg = "com.microntek.radio";

        //log(tag, "In handleLoadPackage!");
        log(tag, "Loading: " + loadPackageParam.packageName);

        if (loadPackageParam.packageName.equals(radiopkg)) {
            final String TARGET_CLASS = "com.microntek.radio.RadioActivity";

            try {
                findAndHookMethod(TARGET_CLASS, loadPackageParam.classLoader, "onResume", mtcRadioUnmuterHook);
                log(tag, "Successfully hooked RadioActivity.onResume()!");
            } catch (XposedHelpers.ClassNotFoundError ex) {
                log(tag, "Failed to hook RadioActivity.onResume()!");
            }
        } else if (loadPackageParam.packageName.equals(mtcservice)) {
            final String TARGET_CLASS = "android.microntek.service.MicrontekServer";
            try {
                findAndHookMethod(TARGET_CLASS, loadPackageParam.classLoader, "onCreate", mediaKeysHook);
                log(tag, "Successfully hooked MicrontekServer.onCreate()!");
            } catch (XposedHelpers.ClassNotFoundError ex) {
                log(tag, "Failed to hook MicrontekServer.onCreate()!");
            }
        } else {
            try {
                XposedHelpers.findAndHookMethod(AudioManager.class, "requestAudioFocus", AudioManager.OnAudioFocusChangeListener.class,
                        int.class, int.class, onAudioFocusMTCRadioMuterHook);
                log(tag, "Successfully hooked AudioManager.onRequestAudioFocus!");
            } catch (XposedHelpers.ClassNotFoundError ex) {
                log(tag, "Failed hooking AudioManager.onRequestAudioFocus!");
            }
        }
    }
}
