#version 130

in vec4 vVaryingColor;
in vec4 vMappedColor;
out vec4 vFragColor;


void main() {

		//vec4 d = abs(dFdx(vVaryingColor)) + abs(dFdy(vVaryingColor));
		vec4 d = fwidth(vVaryingColor);
		vec4 a3 = smoothstep(vec4(0.0),d*1,vVaryingColor);
		float m = min(min(a3.x,a3.y),a3.z);
		
		if (true) {
		vFragColor = mix(vMappedColor,vec4(1.0),m);
		} else {
		 vFragColor = vec4(1.0);
		}
		
		return;

		//if (a3.x > 0.99) {
		//	vFragColor = vVaryingColor;
		//	return;
		//}
		
		//if (vVaryingColor.g > 0.99) {
		//	vFragColor = vVaryingColor;
		//	return;
		//}

		if (a3.g == 0 || a3.r == 0) {
		vFragColor = vMappedColor;
		return;
		}
		
		if (a3.g == 1) {
		vFragColor = mix(vec4(1.0),vMappedColor,1-(a3.r));
		return;
		}
		
		//if (a3.r == 1) {
		//vFragColor = vec4(1.0,0.0,1.0,1.0);
		//vFragColor = mix(vec4(1.0),vMappedColor,1-(a3.g));
		//return;
		//}

		vFragColor = vec4(1.0,1.0,1.0,1.0);
		
		
		//vFragColor = d*10.0;

}


