package smy.com.vrplayer.strategy.interactive;

public class CardboardMTStrategy extends CardboardMotionStrategy {

    public CardboardMTStrategy(InteractiveModeManager.Params params) {
        super(params);
    }

    @Override
    public boolean handleDrag(float distanceX, float distanceY) {
        if(getVRRender() != null){
            getVRRender().setGestureRotateAngle(distanceX,
                    distanceY);
        }
        return false;
    }

    @Override
    public boolean handleFling(float velocityX, float velocityY) {
        if (getVRRender() != null){
            getVRRender().setFlingVelocity(velocityX, velocityY);
        }
        return false;
    }

}
