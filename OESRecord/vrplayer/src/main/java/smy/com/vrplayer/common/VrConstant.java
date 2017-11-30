package smy.com.vrplayer.common;

/**
 * Created by hzb on 16-12-2.
 */
public class VrConstant {
    //以下两个参数必须是16的倍数
    public static final int SPHERE_SAMPLE_STEP = 16;
    public static final int FULL_VIDEO_WIDTH = 120 * SPHERE_SAMPLE_STEP;  //1920
    public static final int FULL_VIDEO_HEIGHT = FULL_VIDEO_WIDTH / 2;  //960
    /*注意,由于小星球模式下是旋转球体（视点不在中心），普通模式下是旋转视角，所以重力感应下会有问题，所以小星球模式下禁用重力感应，禁用VR模式*/

    public static final int RENDER_MODE_DEFAULT = 0;
    public static final int RENDER_MODE_SPHERE = 1;
    public static final int RENDER_MODE_PLANET = 2;
    public static final int RENDER_MODE_VR = 3;
    public static final int RENDER_MODE_OVERALL = 4;
    public static final int RENDER_MODE_SPHERE_OUTSIDE = 5;

    public static final int SOURCE_FILE_TYPE_DUAL = 0;
    public static final int SOURCE_FILE_TYPE_PANO = 1;
    public static final int SOURCE_FILE_TYPE_STERE = 2;

    public static final float FIELD_OF_VIEW_PLANET_MAX = 155.0f;
    public static final float FIELD_OF_VIEW_PLANET_MIN = 125.0f;
    public static final float FIELD_OF_VIEW_NORMAL_MAX = 110.0f;
    public static final float FIELD_OF_VIEW_NORMAL_MIN = 50.0f;
    public static final float INIT_POSITION = 1;
    public static final float INIT_POSITION_PLANET = 450;
    public static final float INIT_POSITION_OUTSIDE = 900;
    public static final float INIT_FIELD_VIEW = 90.0f;
    public static final float INIT_FIELD_VIEW_SOUTSIDE = 105.0f;
    public static final float INIT_FIELD_VIEW_SOUTSIDE_LANDSCAPE = 75.0f;
    public static final float INIT_FIELD_VIEW_PLANET = FIELD_OF_VIEW_PLANET_MAX;

    public static final float GESTURE_X_ANGLE_NORMAL_MAX = 360.0f;
    public static final float GESTURE_X_ANGLE_NORMAL_MIN = -360.0f;

    public static final float ROTATE_DAMPER = 18f;

    public static final int FLOAT_SIZE = 4;
    public static final int SHORT_SIZE = 2;

    // interactive mode
    public static final int INTERACTIVE_MODE_MOTION = 1;
    public static final int INTERACTIVE_MODE_TOUCH = 2;
    public static final int INTERACTIVE_MODE_MOTION_WITH_TOUCH = 3;
    public static final int INTERACTIVE_MODE_CARDBOARD_MOTION = 4;
    public static final int INTERACTIVE_MODE_CARDBOARD_MOTION_WITH_TOUCH = 5;
}
