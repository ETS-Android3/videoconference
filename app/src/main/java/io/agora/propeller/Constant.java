package io.agora.propeller;

import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.BeautyOptions;

public class Constant {

    public static final String MEDIA_SDK_VERSION;

    static {
        String sdk = "undefined";
        try {
            sdk = RtcEngine.getSdkVersion();
        } catch (Throwable e) {
        }
        MEDIA_SDK_VERSION = sdk;
    }

    public static final String MIX_FILE_PATH = "/assets/qt.mp3"; // in assets folder

    public static boolean SHOW_VIDEO_INFO = true;

    public static boolean DEBUG_INFO_ENABLED = true; // Show debug/log info on screen

    public static boolean BEAUTY_EFFECT_ENABLED = true; // Built-in face beautification

    public static final int BEAUTY_EFFECT_DEFAULT_CONTRAST = 1;
    public static final float BEAUTY_EFFECT_DEFAULT_LIGHTNESS = .7f;
    public static final float BEAUTY_EFFECT_DEFAULT_SMOOTHNESS = .5f;
    public static final float BEAUTY_EFFECT_DEFAULT_REDNESS = .1f;

    public static final BeautyOptions BEAUTY_OPTIONS = new BeautyOptions(BEAUTY_EFFECT_DEFAULT_CONTRAST, BEAUTY_EFFECT_DEFAULT_LIGHTNESS, BEAUTY_EFFECT_DEFAULT_SMOOTHNESS, BEAUTY_EFFECT_DEFAULT_REDNESS);

    public static final float BEAUTY_EFFECT_MAX_LIGHTNESS = 1.0f;
    public static final float BEAUTY_EFFECT_MAX_SMOOTHNESS = 1.0f;
    public static final float BEAUTY_EFFECT_MAX_REDNESS = 1.0f;

    public static final String SPACE = " ";
    public static final String CONNECT = "connect";
    public static final String DISCONNECT = "disconnect";
    public static final String EXIT = "exit";


    public static final String CALL_REQUEST = "request";
    public static final String CALL_YES = "yes";
    public static final String INVITE_SENT = "invite_sent";

    public static boolean IS_CONFERENCE_GOING = false;
    /* LIVE API URL */
 //  public static final String API_BASE_URL = "https://dev.getpasport.com";

    /* LIVE API URL */
    //public static final String API_BASE_URL = "https://digimonk.co/videocallapp/";
    public static final String API_BASE_URL = "https://dev.getpasport.com/dev/";


    /* Errors */
    public static final int SUCCESS_CODE = 200;
    public static final int UNAUTHORISED_ERROR = 401;

    /* Server response parameters*/
    public static final String REPLY_STATUS = "status";
    public static final String REPLY_MESSAGE = "message";
    public static final String DATA = "data";
    public static final String SUCCESS = "1";
    public static final String FAIL = "0";





}
