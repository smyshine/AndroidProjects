
#define PI 3.141592653589793
#define PI_HALF 1.570796326794897

precision highp float;

uniform mat4 userRotationMatrix;
varying vec4 Position;
uniform sampler2D tex_y;
uniform sampler2D tex_u;
uniform sampler2D tex_v;

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
    vec4 newPosition = get_new_position(Position);
    vec2 resultUV = point_coords_conversion(newPosition);
    gl_FragColor = getResultColor(resultUV);
}

