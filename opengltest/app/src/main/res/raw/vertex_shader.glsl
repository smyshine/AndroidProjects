#version 120
/*
attribute vec4 aPosition;
attribute vec2 aTextCoord;
varying vec2 vTextCoord;
uniform mat4 uMatrix;
void main(){
    vTextCoord = aTextCoord;
    gl_Position = uMatrix * aPosition;
}*/

attribute vec4 aPosition;
void main() {
  gl_Position = aPosition;
}