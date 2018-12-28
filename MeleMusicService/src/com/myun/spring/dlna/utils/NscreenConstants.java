package com.myun.spring.dlna.utils;

/**
 * 
 * 鍜孨+搴旂敤杩涜瀵规帴,鍖呮嫭闊充箰锛岃棰戯紝鍥剧墖
 * 
 * @author 娆ч槼鍗犳煴
 * @date 2012-4-12 涓嬪崍03:16:40
 * 
 */
public class NscreenConstants {

    /**
     * 杩滅▼鎾斁鍣ㄦ挱鏀剧被鍨�
     * 
     */
    public static final String REMOTE_APP_TYPE_MUSIC = "music";
    /**
     * 杩滅▼鎾斁鍣ㄤ俊鎭殑閫氱敤鍒嗛殧绗�
     */
    public static final String NORMAL_FIELD_SEPARATOR = ";";
    /**
     * 杩滅▼鎾斁鍣ㄤ俊鎭殑鐗规畩鍒嗛殧绗�
     */
    public static final String SPECIAL_FIELD_SEPARATOR = "#";
    /**
     * 杩滅▼璁块棶鎾斁鍣ㄥ繀椤昏閰嶇疆鐨凙CTION锛岀敤浜庤繙绋嬬涓�鍚姩搴旂敤鏃舵斁鍑虹殑Intent涓殑Action
     */
    public static final String REMOTE_PLAY_ACTION_AUDIO = "com.qvod.nscreen.intent.action.remote_play_music";
    /**
     * 寮�惎搴旂敤鏃讹紝姝屾洸璺緞浼犻�鎵�紩鐢ㄧ殑閿�
     */
    public static final String REMOTE_SONG_PATH = "REMOTE_SONG_PATH";
    /**
     * 寮�惎搴旂敤鏃讹紝姝岃瘝璺緞浼犻�鎵�紩鐢ㄧ殑閿�
     */
    public static final String REMOTE_LYRIC_PATH = "REMOTE_LYRIC_PATH";
    /**
     * 寮�惎搴旂敤鏃讹紝缂╃暐鍥捐矾寰勪紶閫掓墍寮曠敤鐨勯敭鍊�
     */
    public static final String REMOTE_THUMB_PATH = "REMOTE_THUMB_PATH";
    /**
     * 寮�惎搴旂敤鏃讹紝鎾斁姝屾洸鏂囦欢鍚嶇О浼犻�鎵�紩鐢ㄧ殑閿�
     */
    public static final String REMOTE_SONG_NAME = "REMOTE_SONG_NAME";
    /**
     * 寮�惎搴旂敤鏃讹紝姝屾洸鍚嶄紶閫掓墍寮曠敤鐨勯敭鍊�
     */
    public static final String REMOTE_SONG_TAG_NAME = "REMOTE_SONG_TAG_NAME";
    /**
     * 寮�惎搴旂敤鏃讹紝鑹烘湳瀹朵紶閫掓墍寮曠敤鐨勯敭鍊�
     */
    public static final String REMOTE_SONG_TAG_ARTISTS = "REMOTE_SONG_TAG_ARTISTS";
    /**
     * 寮�惎搴旂敤鏃讹紝涓撹緫鍚嶄紶閫掓墍寮曠敤鐨勯敭鍊�
     */
    public static final String REMOTE_SONG_TAG_ALBUM = "REMOTE_SONG_TAG_ALBUM";
    /**
     * 寮�惎搴旂敤鏃讹紝褰撳墠鎾斁鏃跺埢鐐逛紶閫掓墍寮曠敤鐨勯敭鍊�
     */
    public static final String REMOTE_SONG_POSITION = "REMOTE_SONG_POSITION";

    public static final String REMOTE_APP_PACKAGE_NAME_PHOTO = "com.qvod.nscreen.adapter.media";
    /*
     * 杩滅瀹夎鐨勫寘鍚�
     */
    public static final String REMOTE_PLAY_ACTION_PHOTO = "com.qvod.nscreen.intent.action.remote_play_picture";

    /*
     * 浼犻�鏂囦欢璺緞鍏抽敭瀛�
     */
    public static final String REMOTE_PIC_PATH = "REMOTE_PIC_PATH";

    /*
     * 浼犻�鎿嶄綔鍛戒护鍏抽敭瀛�
     */
    public static final String REMOTE_PIC_CMD = "REMOTE_PIC_CMD";

    /*
     * 杩滅宸︽棆
     */
    public static final int REMOTE_LEFT_ROTATE = 0x101;
    /*
     * 杩滅鍙虫棆
     */
    public static final int REMOTE_RIGHT_ROTATE = 0x102;
    /*
     * 杩滅缂╁皬
     */
    public static final int REMOTE_ZOOM_IN = 0x103;
    /*
     * 杩滅缂╁皬
     */
    public static final int REMOTE_ZOOM_OUT = 0x104;
    /*
     * 鏈湴閫�嚭搴旂敤鏃堕�鍑哄綋鍓嶇殑搴旂敤
     */
    public static final int REMOTE_EXIT = 0x105;
    /*
     * 鍒犻櫎鏈湴鍥剧墖鏃讹紝濡傛灉鏂囦欢澶逛腑娌℃湁鏂囦欢锛屽垯杩滅娌℃湁鏄剧ず锛屽垹闄よ繙绔瓨鍦ㄧ殑鏈湴宸茬粡鍒犻櫎鐨勬枃浠�
     */
    public static final int HIDE_CURRENT = 0x106;
    /*
     * 绗竴娆″惎鍔ㄩ�閰嶅櫒绔簲鐢�
     */
    public static final int MOVE_TO_CURRENT = 0x107;
    /*
     * 鎵嬫満绔簲鐢ㄥ悜鍚庢粦鍔�骞荤伅鐗囨粦鍔ㄦ椂涔熺敤娑堟伅
     */
    public static final int MOVE_TO_NEXT = 0x108;
    /*
     * 鎵嬫満绔簲鐢ㄥ悜鍓嶆粦鍔�
     */
    public static final int MOVE_TO_PREV = 0x109;

    /*
     * 杩滅▼瀹夎鐨勮繑鍥炲洖璋冨鐞嗘秷鎭�
     */
    public static final int REMOTE_INSTALL_RESULT = 2;

//    public static final String SPLITCODE = "鈾�;

    /**
     * 杩滅▼鎾斁鍣ㄦ挱鏀剧被鍨�
     */
    public static final String REMOTE_APP_TYPE_VIDEO = "video";
    /**
     * 杩滅▼閫傞厤鍣ㄧ殑APP_ID鍙凤紝涓昏鐢ㄦ潵鍒ゆ柇锛岄�閰嶅櫒瑙嗛绋嬪簭鏄惁宸茬粡鍚姩
     */
    public static final String REMOTE_APP_ID = "com.qvod.nscreen.player.itvplayer";
    /**
     * 杩滅▼璁块棶鎾斁鍣ㄥ繀椤昏閰嶇疆鐨凙CTION锛岀敤浜庤繙绋嬬涓�鍚姩搴旂敤鏃舵斁鍑虹殑Intent涓殑Action
     */
    public static final String REMOTE_PLAY_ACTION_VIDEO = "com.qvod.nscreen.intent.action.remote_play_video";
    /**
     * 鐢ㄤ簬杩滅▼绗竴娆″惎鍔ㄥ簲鐢ㄦ椂浼犻�杩囨潵鐨凨EY鍚嶇О锛屾涓烘挱鏀炬枃浠剁殑璺緞
     */
    public static final String REMOTE_VIDEO_PATH = "REMOTE_VIDEO_PATH";
    /**
     * 鐢ㄤ簬杩滅▼绗竴娆″惎鍔ㄥ簲鐢ㄦ椂浼犻�杩囨潵鐨凨EY鍚嶇О锛屾涓虹偣鎾殑绫诲瀷
     */
    public static final String REMOTE_PLAY_TYPE = "REMOTE_PLAY_TYPE";
    /**
     * 鐢ㄤ簬杩滅▼绗竴娆″惎鍔ㄥ簲鐢ㄦ椂浼犻�杩囨潵鐨凨EY鍚嶇О锛屾涓烘挱鏀剧殑鍚嶇О
     */
    public static final String REMOTE_VIDEO_NAME = "REMOTE_VIDEO_NAME";
    /**
     * 鐢ㄤ簬杩滅▼绗竴娆″惎鍔ㄥ簲鐢ㄦ椂浼犻�杩囨潵鐨凨EY鍚嶇О锛屾涓烘挱鏀剧殑鏃堕棿鐐�
     */
    public static final String REMOTE_VIDEO_POSITION = "REMOTE_VIDEO_POSITION";

    /**
     * 杩滅▼瑙嗛鎾斁鎾斁绫诲瀷锛屾湰鍦拌棰戞枃浠�
     */
    public static final int REMOTE_PLAYTYPE_LOCAL = -1;
    /**
     * 杩滅▼瑙嗛鎾斁鎾斁绫诲瀷锛屾櫘閫氱殑HTTP娴�
     */
    public static final int REMOTE_PLAYTYPE_HTTP = 0;
    /**
     * 杩滅▼瑙嗛鎾斁鎾斁绫诲瀷锛孭2P鎾斁
     */
    public static final int REMOTE_PLAYTYPE_P2P = 1;

    /**
     * N+搴旂敤閫�嚭ACTION
     */
    public static final String EXIT_ACTION = "com.qvod.nscreen.app.adapter";
    public static final String SHOW_PHOTO_ACTION = "com.qvod.nscreen.SHOW_PHOTO";
    
    public static final String KEY_1 = "action";
    public static final String KEY_2 = "type";
    
    /**
     * 鏄剧ず鍥剧墖URL
     */
    public static final String APLLICATION_URL_KEY = "show_url";
    /**
     * 鍥剧墖鏄惁鏄剧ず鎴愬姛
     */
    public static final String APLLICATION_URL_SUCCESS_KEY = "show_url_success";
}
