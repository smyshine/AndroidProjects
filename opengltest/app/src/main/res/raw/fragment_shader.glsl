#version 120
/*
precision mediump float;
varying vec2 vTextCoord;
uniform sampler2D sTexture;

void main() {
    gl_FragColor = texture2D(sTexture, vTextCoord);
}*/

precision mediump float;
void main() {
    gl_FragColor = vec4(0,0.5,0.5,1);
}
