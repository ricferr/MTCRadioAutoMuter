package net.sys49152.mtcradioautomuter;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setIntField;

/**
 * Created by rferreira on 17-08-2016.
 *
 * Auto mutes MTC Radio everytime MediaPlayer.start() is called.
 *
 * Based in parts on ExposedMTC by agentdr8 (https://github.com/agentdr8/XMTC)
 */
public class MTCRadioAutoMuter implements IXposedHookLoadPackage {

    private static AudioManager am;
    private static Context mContext;
    private static Object microntekServer;
    private static final String tag = "MTCRadioAutoMute";
    private static boolean gotAllObjects = false;

    public static void log(String tag, String msg) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String formattedDate = df.format(c.getTime());
        XposedBridge.log("[" + formattedDate + "] " + tag + ": " + msg);
    }

    public final XC_MethodHook getMTCObjects = new XC_MethodHook() {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {

            log(tag, "In MicrontekServer.onCreate");

            microntekServer = param.thisObject;
            mContext = (Context) getObjectField(param.thisObject, "mContext");
            // TODO This can probably be asked from the object above, but ok
            am = (AudioManager) getObjectField(param.thisObject, "am");

            gotAllObjects = true;
        }
    };

    public final XC_MethodHook muteMTCRadio = new XC_MethodHook() {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {

            log(tag, "In MediaPlayer.start()");

//            Intent killradio = new Intent("com.microntek.bootcheck");
//            String mode = intent.getStringExtra("mode");
//            String srcpkg = intent.getStringExtra("pkg");
//            killradio.putExtra("class", "");

            am.setParameters("ctl_radio_mute=true");
            am.setParameters("av_channel_exit=fm");
            am.setParameters("av_channel_enter=sys");
            setIntField(microntekServer, "mtcappmode", 3);

            Intent intent = new Intent("com.microntek.canbusdisplay");
            intent.putExtra("type", "off");
            mContext.sendBroadcast(intent);
        }
    };

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        String targetmtc = "android.microntek.service";
        String radiopkg = "com.microntek.radio";

        if (loadPackageParam.packageName.equals(targetmtc)) {
            String TARGET_CLASS = "android.microntek.service.MicrontekServer";
            findAndHookMethod(TARGET_CLASS, loadPackageParam.classLoader, "onCreate", getMTCObjects);
        }

        if (!gotAllObjects) return;

        // Hook the method
        // Try to always hook it because we cant know all the media apps
        try {
            XposedHelpers.findAndHookMethod("android.media.MediaPlayer", loadPackageParam.classLoader, "start", muteMTCRadio);
            log(tag, "Successfully hooked MediaPlayer.start()!");
        } catch (XposedHelpers.ClassNotFoundError ex) {
            log(tag, "Failed hooking MediaPlayer.start()!");
        }
    }
}
