#version 130

in vec4 vt;
uniform sampler1D transfer;
uniform vec4 min_max;
out vec4 vVaryingColor;
out vec4 vMappedColor;

void main() {

	gl_Position=gl_ProjectionMatrix*gl_ModelViewMatrix*vt;

	vMappedColor=texture1D(transfer,(vt.z-min_max.x)/(min_max.y-min_max.x));
	vVaryingColor = gl_Color;
	//vVaryingColor=texture1D(transfer,vt.z/(40));
	//vVaryingColor=texture1D(transfer,(vt.z-min_max.x)/(min_max.y-min_max.x));
	
}


