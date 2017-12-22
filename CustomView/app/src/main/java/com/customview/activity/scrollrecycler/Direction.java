package com.customview.activity.scrollrecycler;

/**
 * Created by SMY on 2017/12/21.
 */

public enum Direction {
    START {
        @Override
        public int applyTo(int delta) {
            return delta * -1;
        }

        @Override
        public boolean sameAs(int direction) {
            return direction < 0;
        }
    },
    END{
        @Override
        public int applyTo(int delta) {
            return delta;
        }

        @Override
        public boolean sameAs(int direction) {
            return direction > 0;
        }
    }
    ;

    public abstract int applyTo(int delta);

    public abstract boolean sameAs(int direction);

    public static Direction fromDelta(int delta){
        return delta > 0 ? END : START;
    }
}
