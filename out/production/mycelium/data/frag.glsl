#ifdef GL_ES
precision mediump float;
#endif

uniform vec2 u_mouse;
uniform vec2 u_resolution;
uniform sampler2D u_buf;

#define rmouse (u_mouse.xy/u_resolution.xy)

float d = 1./length(vec2(1.));

vec2 position(vec2 v) {
  return vec2(v.x/u_resolution.x, v.y/u_resolution.y);
}

void main( void ) {
	vec2 pos = position(gl_FragCoord.xy);
	vec2 dx  = position(vec2(1,0));
	vec2 dy  = position(vec2(0,1));

	float dist = length(rmouse.xy-gl_FragCoord.xy);
	float i = 1.-smoothstep(0.,15.,dist);

	vec3 me = texture2D(u_buf,pos).rgb;
	vec3 a = me + texture2D(u_buf,pos+dx).r+texture2D(u_buf,pos+dx+dy).rgb*d
	+ texture2D(u_buf,pos-dx).rgb+texture2D(u_buf,pos-dx+dy).rgb*d
	+ texture2D(u_buf,pos+dy).rgb+texture2D(u_buf,pos+dx-dy).rgb*d
	+ texture2D(u_buf,pos-dy).rgb+texture2D(u_buf,pos-dx-dy).rgb*d;
	a*=.1278;

	me.g+=a.r-me.r;
	me.g*=.999;
	me.g -=.0009;
	me.r+=me.g-log(1.+(a.r)*0.0009);
	me.b =log(3.*(me.r+me.g)+1.);


	gl_FragColor = vec4(vec3(me) * vec3(0.1*i,0.,0.),1.);
}
