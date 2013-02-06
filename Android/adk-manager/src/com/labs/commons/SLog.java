package com.labs.commons;

import android.util.Log;
import com.labs.adk.BuildConfig;

/**
 * Wrapper class for {@link android.util.Log}.<br/>
 * The reason there are so many overloading methods is to minimize the use of the Argument parameter method (the one with the three dots). <br/>
 * What happens behind the scene when calling such methods is an instantiation of a new array of objects which may be a huge waste if the application
 * calls this class often
 *
 * @author Amir Lazarovich
 */
public class SLog {
    ///////////////////////////////////////////////
    // Members
    ///////////////////////////////////////////////
    private enum Type {
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    ///////////////////////////////////////////////
    // Public
    ///////////////////////////////////////////////
    public static void d(String tag, String msg) {
        if (!BuildConfig.DEBUG || msg == null) {
            return;
        }

       log(Type.DEBUG, tag, msg, null);
    }

    public static void d(String tag, String formattedMessage, Object arg1) {
        if (!BuildConfig.DEBUG || formattedMessage == null) {
            return;
        }

        log(Type.DEBUG, tag, String.format(formattedMessage, arg1), null);
    }

    public static void d(String tag, String formattedMessage, Object arg1, Object arg2) {
        if (!BuildConfig.DEBUG || formattedMessage == null) {
            return;
        }

        log(Type.DEBUG, tag, String.format(formattedMessage, arg1, arg2), null);
    }

    public static void d(String tag, String formattedMessage, Object arg1, Object arg2, Object arg3) {
        if (!BuildConfig.DEBUG || formattedMessage == null) {
            return;
        }

        log(Type.DEBUG, tag, String.format(formattedMessage, arg1, arg2, arg3), null);
    }

    public static void d(String tag, String formattedMessage, Object... args) {
        if (!BuildConfig.DEBUG || formattedMessage == null) {
            return;
        }

        log(Type.DEBUG, tag, String.format(formattedMessage, args), null);
    }

    public static void i(String tag, String msg) {
        if (!BuildConfig.DEBUG || msg == null) {
            return;
        }

        log(Type.INFO, tag, msg, null);
    }

    public static void i(String tag, String formattedMessage, Object arg1) {
        if (!BuildConfig.DEBUG || formattedMessage == null) {
            return;
        }

        log(Type.INFO, tag, String.format(formattedMessage, arg1), null);
    }

    public static void i(String tag, String formattedMessage, Object arg1, Object arg2) {
        if (!BuildConfig.DEBUG || formattedMessage == null) {
            return;
        }

        log(Type.INFO, tag, String.format(formattedMessage, arg1, arg2), null);
    }

    public static void i(String tag, String formattedMessage, Object arg1, Object arg2, Object arg3) {
        if (!BuildConfig.DEBUG || formattedMessage == null) {
            return;
        }

        log(Type.INFO, tag, String.format(formattedMessage, arg1, arg2, arg3), null);
    }

    public static void i(String tag, String formattedMessage, Object... args) {
        if (!BuildConfig.DEBUG || formattedMessage == null) {
            return;
        }

        log(Type.INFO, tag, String.format(formattedMessage, args), null);
    }

    public static void w(String tag, String msg) {
        if (!BuildConfig.DEBUG || msg == null) {
            return;
        }

        log(Type.WARN, tag, msg, null);
    }

    public static void w(String tag, String formattedMessage, Object arg1) {
        if (!BuildConfig.DEBUG || formattedMessage == null) {
            return;
        }

        log(Type.WARN, tag, String.format(formattedMessage, arg1), null);
    }

    public static void w(String tag, String formattedMessage, Object arg1, Object arg2) {
        if (!BuildConfig.DEBUG || formattedMessage == null) {
            return;
        }

        log(Type.WARN, tag, String.format(formattedMessage, arg1, arg2), null);
    }

    public static void w(String tag, String formattedMessage, Object arg1, Object arg2, Object arg3) {
        if (!BuildConfig.DEBUG || formattedMessage == null) {
            return;
        }

        log(Type.WARN, tag, String.format(formattedMessage, arg1, arg2, arg3), null);
    }

    public static void w(String tag, String formattedMessage, Object... args) {
        if (!BuildConfig.DEBUG || formattedMessage == null) {
            return;
        }

        log(Type.WARN, tag, String.format(formattedMessage, args), null);
    }

    public static void e(String tag, String msg, Throwable e) {
        if (!BuildConfig.DEBUG || msg == null) {
            return;
        }

        log(Type.ERROR, tag, msg, e);
    }

    public static void e(String tag, Throwable e, String formattedMessage, Object arg1) {
        if (!BuildConfig.DEBUG || formattedMessage == null) {
            return;
        }

        log(Type.ERROR, tag, String.format(formattedMessage, arg1), e);
    }

    public static void e(String tag, Throwable e, String formattedMessage, Object arg1, Object arg2) {
        if (!BuildConfig.DEBUG || formattedMessage == null) {
            return;
        }

        log(Type.ERROR, tag, String.format(formattedMessage, arg1, arg2), e);
    }

    public static void e(String tag, Throwable e, String formattedMessage, Object arg1, Object arg2, Object arg3) {
        if (!BuildConfig.DEBUG || formattedMessage == null) {
            return;
        }

        log(Type.ERROR, tag, String.format(formattedMessage, arg1, arg2, arg3), e);
    }

    public static void e(String tag, Throwable e, String formattedMessage, Object... args) {
        if (!BuildConfig.DEBUG || formattedMessage == null) {
            return;
        }

        log(Type.ERROR, tag, String.format(formattedMessage, args), e);
    }

    public static boolean isEnabled() {
        return BuildConfig.DEBUG;
    }

    ///////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////
    private static void log(Type type, String tag, String msg, Throwable e) {
        switch (type) {
             case DEBUG:
                 Log.d(tag, msg);
                 break;

            case INFO:
                Log.i(tag, msg);
                break;

            case WARN:
                Log.w(tag, msg);
                break;

            case ERROR:
                if (e == null) {
                    Log.e(tag, msg);
                } else {
                    Log.e(tag, msg, e);
                }
                break;
        }
    }
}
