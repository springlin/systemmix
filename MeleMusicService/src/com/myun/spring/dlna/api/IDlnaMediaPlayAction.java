package com.myun.spring.dlna.api;

import android.content.Intent;

/**
 * 
 * 閫氱敤鐨勬挱鏀炬帶鍒舵帴鍙ｏ紝姣斿鎾斁锛岄�鍑轰箣绫荤殑鎺ュ彛 濯掍綋鎾斁鎿嶄綔瀹氫箟鎺ュ彛锛屾瘮濡傛挱鏀捐棰戞帶鍒舵挱鏀撅紝鏆傚仠锛屽仠姝㈢殑鎿嶄綔
 * 
 * @author 娆ч槼鍗犳煴
 * @date 2012-6-8 涓嬪崍04:15:09
 * 
 */
public interface IDlnaMediaPlayAction {
    /**
     * 
     * 鍒濆鍖栧璞℃搷浣�
     * 
     * @see
     * @throws
     */
    void init();

    /**
     * 
     * 閲婃斁瀵硅薄鎿嶄綔
     * 
     * @see
     * @throws
     */
    void release();

    /**
     * 
     * 鎵ц鎾斁鎿嶄綔
     * 
     * @param intent
     *            褰撳墠鎾斁Intent淇℃伅
     * @return 鎵ц鏄惁鎴愬姛
     * @see
     * @throws
     */
    int dlnaPlay(Intent intent);

    /**
     * 
     * 閫�嚭鎾斁鎿嶄綔
     * 
     * @param exit
     *            閫�嚭閿�
     * @param packageName
     *            鍖呭悕
     * @return int 鎵ц鏄惁鎴愬姛
     * @see
     * @throws
     */
    int dlnaExit(int exit, String packageName);

    /**
     * 
     * 鏆傚仠鎿嶄綔
     * 
     * @return int 鏄惁鎴愬姛
     * @see
     * @throws
     */
    int dlnaPause();

    /**
     * 
     * 鎭㈠鎾斁鎿嶄綔
     * 
     * @return int 鏄惁鎴愬姛
     * @see
     * @throws
     */
    int dlnaResume();

    /**
     * 
     * 鍋滄鎾斁鎿嶄綔
     * 
     * @return int 鏄惁鎴愬姛
     * @see
     * @throws
     */
    int dlnaStop(boolean flag);

    /**
     * 
     * 闈欓煶鎿嶄綔
     * 
     * @return int 鏄惁鎴愬姛
     * @see
     * @throws
     */
    int dlnaMute(int isMute);

    /**
     * 
     * 闊抽噺璁剧疆鎿嶄綔
     * 
     * @return int 鏄惁鎴愬姛
     * @see
     * @throws
     */
    int dlnaSetVolume(int volume);

    /**
     * 
     * 璺冲害鎾斁鎿嶄綔
     * 
     * @return int 鏃堕棿鐐�
     * @see
     * @throws
     */
    int dlnaSeek(int times);

    /**
     * 
     * 鍒锋柊鎾斁鐘舵�鍊�
     */
    //void refreshPlayStatuInfo();

    int getCurrPositionDlna();

    int getTotalPositionDlna();

    boolean isPlayingDlna();

    boolean isDlnaPlayRuning();
    
    void corePause();
}
