package smy.com.vrplayer.strategy.interactive;


import smy.com.vrplayer.render.AbstractRenderer;
import smy.com.vrplayer.strategy.IModeStrategy;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public abstract class AbsInteractiveStrategy implements IModeStrategy, IInteractiveMode {

    private InteractiveModeManager.Params params;

    public AbsInteractiveStrategy(InteractiveModeManager.Params params) {
        this.params = params;
    }

    public InteractiveModeManager.Params getParams() {
        return params;
    }

    public AbstractRenderer getVRRender(){
        return params.mVRRender;
    }
}
