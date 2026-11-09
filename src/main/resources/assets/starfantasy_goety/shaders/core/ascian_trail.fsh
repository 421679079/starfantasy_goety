#version 150
uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main(){
 vec4 tex=texture(Sampler0,vec2(mix(.08,.92,texCoord.x),mix(.08,.92,texCoord.y)));
 float white=tex.r,tail=smoothstep(0.,.65,texCoord.y);
 float ends=smoothstep(0.,.045,texCoord.x)*(1.-smoothstep(.955,1.,texCoord.x));
 float lead=pow(smoothstep(.91,1.,texCoord.y),2.),edge=exp(-pow((texCoord.x-.94)/.022,2.));
 float alpha=clamp((.25+.65*white+.18*lead+.12*edge)*tex.a*tail*ends*vertexColor.a,0.,.9);
 vec3 tint=vertexColor.rgb;
 vec3 color=tint*(.50+white*.65)+vec3(lead*.1+edge*.08);
 if(max(tint.r,max(tint.g,tint.b))<.5)color=mix(vec3(.025,.03,.035),tint,white*.6+edge*.35+lead*.12);
 fragColor=vec4(color,alpha)*ColorModulator;
}
