
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

uniform bool useSTMatrix;
uniform mat4 stMatrix;

varying vec4 Position;

uniform sampler2D singleTexture;

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

vec2 getST(vec2 uv, bool front){
    float s;
    float t;
    if(front){
        s = uv.x / uvWidth;
    } else {
        s = 1.0 - uv.x / uvWidth;
    }
    if(revertX > 0){
        s = 1.0 - s;
    }
    if(front){
        if(useSTMatrix){
            t = 1.0 - 0.5 * uv.y / uvWidth;
        } else {
            t = 0.5 * uv.y / uvWidth;
        }
    } else {
        if(useSTMatrix){
           t = 0.5 * uv.y / uvWidth;
        } else {
           t = 1.0 - 0.5 * uv.y / uvWidth;
        }
    }
    return vec2(s,t);
}

vec4 getResultColor(vec2 st){
    vec4 resultColor;
    if(useSTMatrix){
        vec4 finalST = stMatrix * vec4(st,0.0,1.0);
        resultColor = texture2D(singleTexture, finalST.xy);
    } else {
        resultColor = texture2D(singleTexture, st);
    }
    return resultColor;
}

void main() {
    vec4 newPosition = get_new_position(Position,radius);
    vec2 frontUV;
    vec2 backUV;
    float s;
    float t;
    vec4 resultColor;
    if(newPosition.z <= -joinValue){
        frontUV = point_coords_conversion(newPosition,  1);
        resultColor = getResultColor(getST(frontUV,true));
    }
    if(newPosition.z >= joinValue){
        backUV = point_coords_conversion(newPosition, 0);
        resultColor = getResultColor(getST(backUV,false));
    }
    if(newPosition.z > -joinValue && newPosition.z < joinValue){
        frontUV = point_coords_conversion(newPosition, 1);
        backUV = point_coords_conversion(newPosition, 0);
        vec4 FrontColor = getResultColor(getST(frontUV, true));;
        vec4 BackColor = getResultColor(getST(backUV,false));
        resultColor = (0.5 + newPosition.z / joinValue / 2.0) * BackColor + (0.5 - newPosition.z / joinValue / 2.0) * FrontColor;
    }
    gl_FragColor = resultColor;
}

