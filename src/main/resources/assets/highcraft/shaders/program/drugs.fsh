#version 150

// HighCraft - full-screen effect for drug states.
// All intensities are 0..1 and set by the client every frame (DrugPostEffect).

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;
uniform float HcTime;
uniform float Stoned;
uniform float Trip;
uniform float Rush;
uniform float Sedated;
uniform float Withdrawal;
uniform float Overdose;
uniform float Acid;
uniform float Meth;
uniform float Ecstasy;

out vec4 fragColor;

vec3 rgb2hsv(vec3 c) {
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec3 blurSample(vec2 uv, float radius) {
    vec2 o = oneTexel * radius;
    vec3 s = texture(DiffuseSampler, uv).rgb * 0.2;
    s += texture(DiffuseSampler, uv + vec2( o.x, 0.0)).rgb * 0.1;
    s += texture(DiffuseSampler, uv + vec2(-o.x, 0.0)).rgb * 0.1;
    s += texture(DiffuseSampler, uv + vec2(0.0,  o.y)).rgb * 0.1;
    s += texture(DiffuseSampler, uv + vec2(0.0, -o.y)).rgb * 0.1;
    s += texture(DiffuseSampler, uv + vec2( o.x,  o.y)).rgb * 0.1;
    s += texture(DiffuseSampler, uv + vec2(-o.x,  o.y)).rgb * 0.1;
    s += texture(DiffuseSampler, uv + vec2( o.x, -o.y)).rgb * 0.1;
    s += texture(DiffuseSampler, uv + vec2(-o.x, -o.y)).rgb * 0.1;
    return s;
}

void main() {
    float t = HcTime;
    vec2 uv = texCoord;

    // ---------- Distortion ----------
    // Trip: flowing waves
    uv += Trip * 0.010 * vec2(sin(uv.y * 17.0 + t * 1.7), cos(uv.x * 13.0 + t * 1.3));
    // Stoned: slow "breathing"
    uv += Stoned * 0.0035 * vec2(sin(t * 0.9 + uv.y * 6.0), cos(t * 0.7 + uv.x * 5.0));
    // Withdrawal: shaking
    uv += Withdrawal * 0.0018 * vec2(sin(t * 37.0), cos(t * 41.0));
    // LSD: kaleidoscope at the screen edges + "breathing" world
    if (Acid > 0.01) {
        float aspect = InSize.x / max(1.0, InSize.y);
        vec2 p = uv - 0.5;
        p.x *= aspect;
        float r = length(p);
        float seg = 6.2831853 / 6.0;
        float a = mod(atan(p.y, p.x) + t * 0.15, seg);
        a = abs(a - seg * 0.5);
        vec2 kp = vec2(cos(a), sin(a)) * r * (1.0 + 0.08 * sin(t * 0.8 + r * 10.0));
        kp.x /= aspect;
        vec2 kuv = clamp(kp + 0.5, 0.0, 1.0);
        uv = mix(uv, kuv, Acid * smoothstep(0.15, 0.6, r) * 0.45);
        uv += Acid * 0.006 * vec2(sin(uv.y * 25.0 + t * 2.3), sin(uv.x * 21.0 - t * 1.9));
        uv = 0.5 + (uv - 0.5) * (1.0 - 0.035 * Acid * sin(t * 1.3));
    }
    // Meth: tunnel focus + nervous micro-jitter
    if (Meth > 0.01) {
        float twitch = pow(max(0.0, sin(t * 2.3) * sin(t * 3.7)), 6.0);
        uv += Meth * 0.007 * twitch * vec2(sin(t * 91.0), cos(t * 77.0));
        uv = 0.5 + (uv - 0.5) * (1.0 - 0.05 * Meth - 0.01 * Meth * sin(t * 9.0));
    }
    // Ecstasy: soft swaying, gently "breathing" to the beat
    float danceBeat = pow(max(0.0, sin(t * 6.7)), 6.0);
    uv += Ecstasy * 0.003 * vec2(sin(t * 1.1 + uv.y * 4.0), cos(t * 0.9 + uv.x * 4.0));
    uv = 0.5 + (uv - 0.5) * (1.0 - 0.012 * Ecstasy * danceBeat);
    // Overdose: pulsing zoom
    vec2 c = uv - 0.5;
    float pulse = sin(t * 4.0) * 0.5 + 0.5;
    uv = 0.5 + c * (1.0 - Overdose * 0.025 * pulse);
    uv = clamp(uv, oneTexel, 1.0 - oneTexel);

    // ---------- Chromatic aberration ----------
    float ca = Rush * 0.007 * (0.6 + 0.4 * sin(t * 7.5)) + Trip * 0.006 + Overdose * 0.006 + Acid * 0.009
             + Meth * 0.005 * (0.5 + 0.5 * sin(t * 13.0)) + Ecstasy * 0.004 * danceBeat;
    vec3 col;
    col.r = texture(DiffuseSampler, clamp(uv + c * ca, 0.0, 1.0)).r;
    col.g = texture(DiffuseSampler, uv).g;
    col.b = texture(DiffuseSampler, clamp(uv - c * ca, 0.0, 1.0)).b;

    // ---------- Blur / double vision ----------
    float blur = Sedated * 1.0 + Stoned * 0.35 + Overdose * 0.6;
    if (blur > 0.01) {
        col = mix(col, blurSample(uv, 1.5 + 2.0 * blur), clamp(blur, 0.0, 1.0) * 0.7);
    }
    if (Overdose > 0.01) {
        vec3 ghost = texture(DiffuseSampler, clamp(uv + vec2(0.012 * sin(t * 1.5), 0.004 * cos(t * 1.1)) * Overdose, 0.0, 1.0)).rgb;
        col = mix(col, ghost, 0.35 * Overdose);
    }

    // ---------- Colour grading ----------
    float lum = dot(col, vec3(0.299, 0.587, 0.114));
    // Stoned: richer, warm green colours
    col = mix(vec3(lum), col, 1.0 + 0.35 * Stoned);
    col *= mix(vec3(1.0), vec3(1.04, 1.07, 0.93), Stoned);

    // Trip: hue rotation depending on position and time + rainbow edges
    if (Trip > 0.01) {
        vec3 hsv = rgb2hsv(clamp(col, 0.0, 1.0));
        hsv.x = fract(hsv.x + Trip * (0.18 * sin(t * 0.5 + uv.x * 3.0 + uv.y * 2.0) + t * 0.04));
        hsv.y = min(1.0, hsv.y * (1.0 + 0.7 * Trip) + 0.1 * Trip);
        vec3 tripped = hsv2rgb(hsv);
        float bands = sin(lum * 28.0 - t * 3.0) * 0.5 + 0.5;
        tripped = mix(tripped, hsv2rgb(vec3(fract(lum * 2.0 + t * 0.1), 0.85, max(hsv.z, 0.35))), 0.18 * bands);
        col = mix(col, tripped, clamp(Trip, 0.0, 1.0));
    }

    // LSD: neon colour cycles and glowing edges
    if (Acid > 0.01) {
        vec2 ex = vec2(oneTexel.x * 1.5, 0.0), ey = vec2(0.0, oneTexel.y * 1.5);
        vec3 gx = texture(DiffuseSampler, clamp(uv + ex, 0.0, 1.0)).rgb - texture(DiffuseSampler, clamp(uv - ex, 0.0, 1.0)).rgb;
        vec3 gy = texture(DiffuseSampler, clamp(uv + ey, 0.0, 1.0)).rgb - texture(DiffuseSampler, clamp(uv - ey, 0.0, 1.0)).rgb;
        float edge = clamp((length(gx) + length(gy)) * 1.5, 0.0, 1.0);
        vec3 hsv = rgb2hsv(clamp(col, 0.0, 1.0));
        hsv.x = fract(hsv.x + t * 0.12 + length(c) * 1.5 + 0.2 * sin(t * 0.7));
        hsv.y = min(1.0, hsv.y * 1.8 + 0.25);
        hsv.z = min(1.0, hsv.z * 1.1 + 0.05);
        vec3 acidCol = hsv2rgb(hsv);
        acidCol += edge * hsv2rgb(vec3(fract(t * 0.3 + uv.y + uv.x * 0.5), 1.0, 1.0)) * 0.85;
        col = mix(col, acidCol, clamp(Acid, 0.0, 1.0));
    }

    // Cocaine rush: contrast, brightness, sharpness, cool white
    if (Rush > 0.01) {
        vec3 sharp = col * 2.0 - blurSample(uv, 1.0);
        col = mix(col, sharp, 0.6 * Rush);
        col = mix(col, (col - 0.5) * 1.35 + 0.56, Rush * 0.7);
        lum = dot(col, vec3(0.299, 0.587, 0.114));
        col = mix(vec3(lum), col, 1.0 + 0.25 * Rush);
        col *= mix(vec3(1.0), vec3(0.97, 1.0, 1.06), Rush);
    }

    // Meth: cold, over-sharpened, harsh contrast with glowing white edges
    if (Meth > 0.01) {
        vec3 blurM = blurSample(uv, 1.2);
        vec3 sharp = col + (col - blurM) * 2.2;
        col = mix(col, sharp, 0.9 * Meth);
        col = mix(col, (col - 0.5) * 1.6 + 0.53, Meth * 0.7);
        float l2 = dot(col, vec3(0.299, 0.587, 0.114));
        col = mix(col, vec3(l2) * vec3(0.78, 1.0, 1.22), 0.5 * Meth);
        vec3 edgeM = abs(col - blurM);
        col += Meth * 1.1 * vec3(dot(edgeM, vec3(0.33))) * vec3(0.75, 1.0, 1.15);
    }

    // Ecstasy: warm pink bloom, saturated colours, brightness pulsing to the beat
    if (Ecstasy > 0.01) {
        vec3 bloom = blurSample(uv, 3.0);
        col += Ecstasy * 0.9 * max(bloom - 0.35, 0.0) * vec3(1.25, 0.75, 1.15);
        float l3 = dot(col, vec3(0.299, 0.587, 0.114));
        col = mix(vec3(l3), col, 1.0 + 0.8 * Ecstasy);
        col = mix(col, col * vec3(1.15, 0.86, 1.12) + vec3(0.06, 0.0, 0.05), 0.6 * Ecstasy);
        col *= 1.0 + Ecstasy * 0.18 * danceBeat;
        float sparkle = pow(max(0.0, sin(uv.x * 40.0 + t * 2.0) * sin(uv.y * 30.0 - t * 1.5)), 24.0);
        col += Ecstasy * sparkle * 0.35 * vec3(1.0, 0.6, 0.9);
    }

    // Sedated / withdrawal: desaturated
    lum = dot(col, vec3(0.299, 0.587, 0.114));
    col = mix(col, vec3(lum), clamp(Sedated * 0.55 + Withdrawal * 0.6, 0.0, 1.0));
    col *= mix(vec3(1.0), vec3(0.86, 0.92, 1.06), Withdrawal);
    col *= mix(vec3(1.0), vec3(1.02, 0.92, 1.0), Sedated);

    // ---------- Vignette ----------
    float dist = length(c);
    float vig = smoothstep(0.35, 0.85, dist);
    col *= 1.0 - vig * (Sedated * 0.6 + Withdrawal * 0.45 + Stoned * 0.25);
    col = mix(col, vec3(0.55, 0.0, 0.0), vig * Overdose * (0.4 + 0.4 * pulse));
    float beat = pow(max(0.0, sin(t * 8.0)), 8.0);
    col += vec3(1.0) * vig * Rush * 0.12 * beat;
    col = mix(col, vec3(1.0, 0.45, 0.8), vig * Ecstasy * 0.25 * (0.5 + 0.5 * danceBeat));
    col *= 1.0 - vig * Meth * (0.45 + 0.15 * sin(t * 11.0));

    fragColor = vec4(clamp(col, 0.0, 1.0), 1.0);
}
