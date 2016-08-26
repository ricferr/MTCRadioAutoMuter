package net.sys49152.mtcradioautomuter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setIntField;

/**
 * Created by rferreira on 17-08-2016.
 *
 * Mutes MTC Radio every time a new audio session is started. is called.
 * The idea is that this way it is not needed to exit MTCRadio before starting playback in another app.
 *
 * Based in parts on ExposedMTC by agentdr8 (https://github.com/agentdr8/XMTC)
 */

public class MTCRadioAutoMuter implements IXposedHookLoadPackage {

    private static final String tag = "MTCRadioAutoMuter";
    private final MTCRadioUnmuterHook mtcRadioUnmuterHook = new MTCRadioUnmuterHook();
    private final MTCRadioMuterHook mtcRadioMuterHook = new MTCRadioMuterHook();

    public static void log(String tag, String msg) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String formattedDate = df.format(c.getTime());
        XposedBridge.log("[" + formattedDate + "] " + tag + ": " + msg);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {

        String radiopkg = "com.microntek.radio";

        //log(tag, "In handleLoadPackage!");
        log(tag, "Loading: " + loadPackageParam.packageName);

        if (loadPackageParam.packageName.equals(radiopkg)) {
            String TARGET_CLASS = "com.microntek.radio.RadioActivity";

            try {
                findAndHookMethod(TARGET_CLASS, loadPackageParam.classLoader, "onResume", mtcRadioUnmuterHook);
                log(tag, "Successfully hooked RadioActivity.onResume()!");
            } catch (XposedHelpers.ClassNotFoundError ex) {
                log(tag, "Failed to hook RadioActivity.onResume()!");
            }
        } else {
            try {
                XposedHelpers.findAndHookMethod(ContextWrapper.class, "sendBroadcast", Intent.class, mtcRadioMuterHook);
                log(tag, "Successfully hooked ContextWrapper.sendBroadcast!");
            } catch (XposedHelpers.ClassNotFoundError ex) {
                log(tag, "Failed hooking ContextWrapper.sendBroadcast!");
            }
        }
    }
}
