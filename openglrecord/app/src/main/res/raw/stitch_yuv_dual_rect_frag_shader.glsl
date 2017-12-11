
#define PI 3.141592653589793
#define PI_HALF 1.570796326794897

precision highp float;

uniform int invpolLength;
uniform float frontInvpols[64];
uniform float backInvpols[64];

uniform vec2 frontCenter;
uniform mat3 extFrontRotationMatrix;
uniform vec3 extFrontTranslateVector;

uniform vec2 backCenter;
uniform mat3 extBackRotationMatrix;
uniform vec3 extBackTranslateVector;

uniform vec3 frontAffineParam;
uniform vec3 backAffineParam;
uniform mat4 userRotationMatrix;

uniform float radius;
uniform float uvWidth;
uniform float joinValue;
uniform int revertX;

uniform sampler2D tex_y;
uniform sampler2D tex_u;
uniform sampler2D tex_v;

varying vec4 Position;

vec2 world2cam(vec3 cam, int isFront){
    float norm = sqrt(cam.x * cam.x + cam.y * cam.y);
    float theta = atan(cam.z / norm);
    float t;
    float t_i;
    float rho;
    float invnorm;
    int i;
    float x;
    float y;
    float pointX;
    float pointY;
    if(norm != 0.0){
        invnorm = 1.0 / norm;
        t = theta;
        t_i = 1.0;
        if(isFront == 1){
                rho = frontInvpols[0];
                for(i=1; i < invpolLength; i++){
                    t_i *= t;
                    rho += t_i * frontInvpols[i];
                }
                x = cam.x * invnorm * rho;
                y = cam.y * invnorm * rho;
        
                pointY = x * frontAffineParam.x + y * frontAffineParam.y + frontCenter.x;
                pointX = x * frontAffineParam.z + y + frontCenter.y;
        } else {
                rho = backInvpols[0];
                for(i=1; i < invpolLength; i++){
                    t_i *= t;
                    rho += t_i * backInvpols[i];
                }
                x = cam.x * invnorm * rho;
                y = cam.y * invnorm * rho;
        
                pointY = x * backAffineParam.x + y * backAffineParam.y + backCenter.x;
                pointX = x * backAffineParam.z + y + backCenter.y;
        }
    } else {
    if(isFront == 1){
        pointY = frontCenter.x;
        pointX = frontCenter.y;
    } else {
        pointY = backCenter.x;
        pointX = backCenter.y;
    }
    }
    return vec2(pointX,pointY);
}


vec2 point_coords_conversion(vec4 position, int isFront){
    vec3 sphere;
    vec3 cam;
    sphere.x = position.x;
    sphere.y = position.y;
    sphere.z = position.z;
    if(isFront == 1){
    cam = extFrontRotationMatrix * sphere;
    cam = cam + extFrontTranslateVector;
    } else {
    cam = extBackRotationMatrix * sphere;
    cam = cam + extBackTranslateVector;
    }
    return world2cam(cam, isFront);
}

vec4 get_new_position(vec4 oldPosition, float radius){
    vec4 sphere;
    float theta = PI_HALF - PI * oldPosition.y;
    float phi = PI * oldPosition.x;
    sphere.x = -cos(theta) * sin(phi) * radius;
    sphere.y = sin(theta) * radius;
    sphere.z = cos(theta) * cos(phi) * radius;
    sphere.w = 1.0;
    return userRotationMatrix * sphere;
}

vec4 getResultColor(vec2 textCoord){
    vec3 yuv;
    yuv.x = texture2D(tex_y, textCoord).r;
    yuv.y = texture2D(tex_u, textCoord).r - 0.5;
    yuv.z = texture2D(tex_v, textCoord).r - 0.5;
    vec3 rgb = mat3( 1,       1,         1,
                 0,       -0.39465,  2.03211,
                 1.13983, -0.58060,  0) * yuv;
    return vec4(rgb, 1);
}

void main() {
    vec4 newPosition = get_new_position(Position,radius);
    vec2 frontUV;
    vec2 backUV;
    float s;
    float t;
    vec4 resultColor;
    if(newPosition.z <= -joinValue){
        frontUV = point_coords_conversion(newPosition, 1);
        s = frontUV.x / uvWidth;
        if(revertX > 0){
           s = 1.0 - s;
        }
        t = 0.5 * frontUV.y / uvWidth;
        resultColor = getResultColor(vec2(s,t));
    }
    if(newPosition.z >= joinValue){
        backUV = point_coords_conversion(newPosition, 0);
        s = 1.0 - backUV.x / uvWidth;
        if(revertX > 0){
           s = 1.0 - s;
        }
        t = 1.0 - 0.5 * backUV.y / uvWidth;
        resultColor = getResultColor(vec2(s,t));
    }
    if(newPosition.z > -joinValue && newPosition.z < joinValue){
        frontUV = point_coords_conversion(newPosition, 1);
        backUV = point_coords_conversion(newPosition, 0);
        s = frontUV.x / uvWidth;
        if(revertX > 0){
           s = 1.0 - s;
        }
        t = 0.5 * frontUV.y / uvWidth;
        vec4 FrontColor = getResultColor(vec2(s,t));
        s = 1.0 - backUV.x / uvWidth;
        if(revertX > 0){
           s = 1.0 - s;
        }
        t = 1.0 - 0.5 * backUV.y / uvWidth;
        vec4 BackColor = getResultColor(vec2(s,t));
        resultColor = (0.5 + newPosition.z / joinValue / 2.0) * BackColor + (0.5 - newPosition.z / joinValue / 2.0) * FrontColor;
    }
    gl_FragColor = resultColor;
}

