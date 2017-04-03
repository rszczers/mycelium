#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif
#define PI 3.1415926535897932384626433832795
uniform int u_posSize;
uniform vec2 u_positions[60];
uniform vec2 u_resolution;
uniform sampler2D u_buf;
uniform sampler2D u_background;


float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

void main() {
	vec2 st = gl_FragCoord.xy/u_resolution.xy;
	vec2 texPos = vec2(gl_FragCoord.x/u_resolution.x, 1.0 - gl_FragCoord.y/u_resolution.y);
    float pct = 0.0;

    for (int i=0; i<u_posSize; i++) {
        vec2 pos = u_positions[i]/u_resolution - st;
        vec2 tC = u_positions[i]/u_resolution - st;
//        float blender = (1 - smoothstep(0.0, 0.01, length(tC)));
        float blender = smoothstep(0.0, 0.5, pow(tC.x*tC.x+tC.y*tC.y, -0.5)/2000);
        pct += blender;
    }
//	gl_FragColor = vec4(pct, pct, pct, pct) + texture2D(u_background, texPos);
	gl_FragColor = vec4(pct, pct, pct, pct) + texture2D(u_buf, texPos) * 0.98;
}