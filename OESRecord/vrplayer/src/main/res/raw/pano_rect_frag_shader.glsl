
#define PI 3.141592653589793
#define PI_HALF 1.570796326794897

precision highp float;

uniform mat4 userRotationMatrix;
varying vec4 Position;
uniform sampler2D singleTexture;

uniform bool useSTMatrix;
uniform mat4 stMatrix;

vec2 point_coords_conversion(vec4 position){
    float norm = sqrt(position.x * position.x + position.z * position.z);
    float theta = asin(position.y);
    float phiHori = acos(position.z / norm);
    if(position.x > 0.0) {
        phiHori = 2.0 * PI - phiHori;
    }
    return vec2(1.0 - phiHori / (2.0 * PI), theta / PI + 0.5);
}

vec4 get_new_position(vec4 oldPosition){
    vec4 sphere;
    float theta = PI_HALF - PI * oldPosition.y;
    float phi = PI * oldPosition.x;
    sphere.x = -cos(theta) * sin(phi);
    sphere.y = sin(theta);
    sphere.z = cos(theta) * cos(phi);
    sphere.w = 1.0;
    return userRotationMatrix * sphere;
}

void main() {
    vec4 newPosition = get_new_position(Position);
    vec2 resultUV = point_coords_conversion(newPosition);
    if(useSTMatrix){
        resultUV.y = 1.0 - resultUV.y;
        vec2 st = (stMatrix * vec4(resultUV,0.0,1.0)).xy;
        gl_FragColor = texture2D(singleTexture,st);
    } else {
        gl_FragColor = texture2D(singleTexture, resultUV);
    }
}

