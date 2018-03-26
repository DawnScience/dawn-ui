#version 130

in vec4 vt;
uniform sampler1D transfer;
uniform vec4 min_max;
out vec4 vVaryingColor;

void main() {

	gl_Position=gl_ProjectionMatrix*gl_ModelViewMatrix*vt;

	if (min_max.y == 0.0) {
	vVaryingColor = vec4(1.0,0,0,0);
	//return;
	}
	//vVaryingColor=texture1D(transfer,vt.z/(40));
	vVaryingColor=texture1D(transfer,(vt.z-min_max.x)/(min_max.y-min_max.x));
	
}


