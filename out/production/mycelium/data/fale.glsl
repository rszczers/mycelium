#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform int u_posSize;
uniform vec2 u_positions[60];
uniform vec2 u_resolution;
uniform vec2 u_mouse;
uniform float u_time;


void main() {
	vec2 st = gl_FragCoord.xy/u_resolution.xy;
    float pct = 0.0;

    for (int i=0; i<u_posSize; i++) {
        vec2 tC = u_positions[i]/u_resolution - st;

        float blender = (1 - smoothstep(0.0, 0.02, length(tC)));

        pct += blender;
    }

	gl_FragColor = vec4(pct);


}