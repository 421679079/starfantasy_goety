#version 150
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2; // Vanilla obsidian; resource-pack replacements also work.
uniform sampler2D Sampler3; // Vanilla end stone.
uniform sampler2D Sampler4; // Fixed block-column heights / ambient occlusion, generated once on the CPU.
uniform vec3 CameraLocal;
uniform vec2 HalfSize;
uniform vec2 QuadHalfSize;
uniform float RiftTime;
uniform float Reveal;
uniform float Opening;
uniform float RiftSeed;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;

float hash(float n) { return fract(sin(n * 127.1 + RiftSeed * 0.01) * 43758.5453); }
float halfWidth(float y) {
    return HalfSize.x * pow(max(0.0, 1.0 - pow(abs(y) / HalfSize.y, 0.68)), 1.0 / 0.68);
}

void churchBox(vec3 ro, vec3 invRay, vec3 lo, vec3 hi, float kind,
            inout float closest, inout vec3 normal, inout float material) {
    vec3 a = (lo - ro) * invRay;
    vec3 b = (hi - ro) * invRay;
    vec3 nearPlane = min(a, b);
    vec3 farPlane = max(a, b);
    float entry = max(nearPlane.x, max(nearPlane.y, nearPlane.z));
    float leave = min(farPlane.x, min(farPlane.y, farPlane.z));
    if (entry > 0.001 && entry <= leave && entry < closest) {
        closest = entry;
        material = kind;
        normal = nearPlane.x >= nearPlane.y && nearPlane.x >= nearPlane.z
               ? vec3(-sign(invRay.x), 0, 0)
               : nearPlane.y >= nearPlane.z ? vec3(0, -sign(invRay.y), 0)
                                           : vec3(0, 0, -sign(invRay.z));
    }
}

// Lightweight entrance diorama from the actual template: floor at y=8, arrival
// (70,9,164), gate piers x=58..62/78..82, z=144..158. Distant details dissolve in fog.
vec3 churchEntrance(vec2 p) {
    vec3 eye=vec3(CameraLocal.xy,-max(0.03,abs(CameraLocal.z)));
    vec3 rd=normalize(vec3(p,0)-eye), ro=vec3(p,0);
    vec3 invRay=mix(vec3(-1),vec3(1),step(vec3(0),rd))/max(abs(rd),vec3(0.00001));
    vec3 mist=vec3(0.10,0.29,0.27);
    float closest=100.0, material=-1.0;
    vec3 normal=vec3(0);
    // Bridge and flanking gate piers. Geometry, not a flat screenshot, gives parallax.
    churchBox(ro,invRay,vec3(-7.5,-3.65,-12),vec3(7.5,-2.65,94),2.0,closest,normal,material);
    for(int side=-1;side<=1;side+=2) {
        float c=float(side)*10.0;
        churchBox(ro,invRay,vec3(c-2.5,-2.65,6),vec3(c+2.5,57,22),0.0,closest,normal,material);
        float a=float(side)*12.0;
        churchBox(ro,invRay,vec3(a-1,-2.65,5),vec3(a+1,59,23),1.0,closest,normal,material);
        float w=float(side)*23.0;
        churchBox(ro,invRay,vec3(w-10,-2.65,10),vec3(w+10,58,14),4.0,closest,normal,material);
        // First nave columns and red inlays emerge through the green haze.
        for(int i=0;i<3;i++) {
            float z=32.0+float(i)*13.0;
            churchBox(ro,invRay,vec3(float(side)*24.0-2.0,-7,z),vec3(float(side)*24.0+2.0,52,z+3),1.0,closest,normal,material);
            churchBox(ro,invRay,vec3(float(side)*25.0-0.8,-1,z+3),vec3(float(side)*25.0+0.8,42,z+10),3.0,closest,normal,material);
        }
    }
    churchBox(ro,invRay,vec3(-33,57,6),vec3(33,59,72),4.0,closest,normal,material);
    churchBox(ro,invRay,vec3(-30,-6,78),vec3(30,53,81),1.0,closest,normal,material);
    if(material<0.0) return mist;
    vec3 hit=ro+rd*closest;
    vec2 uv=abs(normal.y)>0.5?hit.xz:abs(normal.x)>0.5?hit.zy:hit.xy;
    vec3 surface;
    if(material<0.5) surface=texture(Sampler0,fract(uv)).rgb;
    else if(material<1.5) surface=texture(Sampler1,fract(uv)).rgb;
    else if(material<2.5) {
        float x=abs(hit.x), repeatZ=abs(mod(hit.z+4.0,8.0)-4.0);
        // Red brick recesses every eight blocks, with coral surrounds and dark paving.
        if(x<1.5 && repeatZ<1.5) surface=texture(Sampler3,fract(uv)).rgb;
        else if(x<3.5 && repeatZ<3.5) surface=texture(Sampler4,fract(uv)).rgb;
        else surface=texture(Sampler2,fract(uv)).rgb;
    } else if(material<3.5) surface=texture(Sampler3,fract(uv)).rgb;
    else surface=texture(Sampler4,fract(uv)).rgb*1.15;
    surface*=normal.y>0.5?0.85:abs(normal.x)>0.5?0.69:0.80;
    float fog=1.0-exp(-closest*closest*0.00035);
    return mix(surface,mist,fog);
}
void main() {
    vec2 p = (texCoord - 0.5) * QuadHalfSize * 2.0;
    float height = max(0.01, HalfSize.y * Reveal);
    float band = floor(p.y / 0.19);
    float jiggle = (hash(band + floor(RiftTime * 1.2) * 0.017) - 0.5) * 0.16 * (1.0 - Opening);
    float steppedY = floor(abs(p.y) / 0.19 + 0.5) * 0.19;
    float width = mix(0.022, halfWidth(steppedY / max(Reveal, 0.001)), Opening);
    float edge = max(abs(p.x - jiggle) - width, abs(p.y) - height);
    float glow = exp(-max(edge, 0.0) * 8.5) * 0.50 * Reveal;
    if (edge > 0.65 || Reveal < 0.001) discard;
    float line = 1.0 - smoothstep(0.018, 0.05, abs(edge));
    float aperture = 1.0 - smoothstep(-0.025, 0.015, edge);
    vec3 color = vec3(0.07, 0.64, 0.50);
    if (edge < 0.025 && Opening > 0.01) {
        vec3 destination = churchEntrance(p);
        color = mix(vec3(0.02, 0.17, 0.14), destination, smoothstep(0.0, 0.5, Opening));
    }
    color += vec3(0.40, 1.0, 0.81) * line * 1.1;
    float alpha = max(aperture * mix(0.85, 1.0, Opening), glow) * min(1.0, Reveal * 8.0);
    fragColor = vec4(color, alpha) * vertexColor;
}
