#version 110

varying vec4 vVaryingColor;
uniform sampler3D volumeTexture;
uniform sampler1D transfer;
uniform vec4 eye;
uniform vec2 minMax;
uniform vec2 stepDown;

vec4 composite(vec4 initial, vec4 sample) {
	vec4 result = initial;
	result.rgb   +=  ((1.0-initial.a)*sample.rgb*sample.a);
	result.a     += ((1.0-initial.a)*sample.a);

	return result;
}

vec4 composite_for_downsampling(vec4 sample, int downSamp) {
	for (int j = 1; j< downSamp; j++) {
			sample = composite(sample,sample);
	}
	
	return sample;
}


void main() {
	//get starting co-ord on cube from colour
	vec4 coordinate = vVaryingColor;
	vec4 value = texture3D(volumeTexture,(coordinate.xyz));

	float val = clamp((value.r-minMax.x)/(minMax.y-minMax.x),0.0,1.0);
	vec4 t =texture1D(transfer,val);
	value = t;

	value.a = val*val;
	float step = stepDown.x;
	int downSamp = int(stepDown.y);

	float maxlength = 1.8;
	int number = int(maxlength/step);

	value = composite_for_downsampling(value,downSamp);

	for (int i = 1; i < number; i++) {
		vec3 tmp = coordinate.xyz;
		tmp = tmp - eye.xyz*step*float(i);
		if (tmp.x > 1.0 || tmp.y > 1.0 || tmp.z > 1.0 || tmp.x < 0.0 || tmp.y < 0.0 || tmp.z < 0.0) {
			break;
		}
		vec4 v = texture3D(volumeTexture,tmp);
		float val0 = clamp((v.r-minMax.x)/(minMax.y-minMax.x),0.0,1.0);
		vec4 t0 =texture1D(transfer,val0);

		v = t0;
		v.a = val0*val0;

		v = composite_for_downsampling(v,downSamp);
		value = composite(value,v);

		if (value.a > 0.99) {
			gl_FragColor = value;
			return;
		}
	}

	gl_FragColor = value;
}


