package com.easymusic.entity.constants;

public class Constants {
    public static final String ZERO_STR = "0";

    public static final Integer ZERO = 0;

    public static final Integer ONE = 1;

    public static final Integer LENGTH_5 = 5;
    public static final Integer LENGTH_8 = 8;
    public static final Integer LENGTH_14 = 14;
    public static final Integer LENGTH_15 = 15;
    public static final Integer LENGTH_12 = 12;
    public static final Integer LENGTH_20 = 20;

    public static final Integer LENGTH_30 = 30;

    public static final String[] IMAGES_SUFFIX = {".jpeg", ".jpg", ".png", ".gif", ".bmp", ".webp"};

    public static final String FILE_FOLDER_FILE = "file/";

    public static final String FILE_FOLDER_TEMP = "/temp/";

    public static final String FILE_FOLDER_AVATAR_NAME = "avatar/";

    public static final String IMAGE_SUFFIX = ".jpg";

    public static final String AUDIO_SUFFIX = ".mp3";

    public static final String DEFAULT_AVATAR_PATH = "/avatar/%d.png";

    public static final String AVATAR_SUFIX = ".png";

    public static final Integer ORDER_TIMEOUT_MIN = 10;

    /**
     * redis key 相关
     */

    /**
     * 过期时间 1分钟
     */
    public static final Long REDIS_KEY_EXPIRES_ONE_MIN = 60L;

    /**
     * 过期时间 1天
     */
    public static final Long REDIS_KEY_EXPIRES_DAY = REDIS_KEY_EXPIRES_ONE_MIN * 60 * 24;

    private static final String REDIS_KEY_PREFIX = "easymusic:";

    public static final String REDIS_KEY_CHECK_CODE = REDIS_KEY_PREFIX + "checkcode:";

    public static final String REDIS_KEY_TOKEN_WEB_USER = REDIS_KEY_PREFIX + "token:";

    public static final String REDIS_KEY_TOKEN_ADMIN_USER = REDIS_KEY_PREFIX + "token:admin:";

    public static final String REDIS_KEY_SYS_DICT = REDIS_KEY_PREFIX + "sysdict:";

    public static final String REDIS_KEY_ORDER_DELAY_QUEUE = REDIS_KEY_PREFIX + "order:delay:queue:";

    public static final String REDIS_KEY_ORDER_HAVE_PAY = REDIS_KEY_PREFIX + "order:havepay:";

    public static final String REDIS_KEY_USER_PROFILE_DIRTY = REDIS_KEY_PREFIX + "user:profile:dirty:";
}
