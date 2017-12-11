precision mediump float;


attribute vec4 vPosition;
varying vec4 Position;
uniform mat4 mvpMatrix;

void main() {
    gl_Position = mvpMatrix * vec4(1.0 - vPosition.x, 2.0 * vPosition.y - 1.0, 0.0, 1.0);
    Position = vPosition;
}