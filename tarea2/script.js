const W = 960, H = 540;
const GROUND = 455;
const STEPS = 12;
const HOLD = 8;
const GRAVITY = 0.14;
const THRUST = 0.40;
const MAX_VY = 5;
const MAX_VX = 4;
const MAX_ANG = 0.85;

const view = document.getElementById("view");
const ctx = view.getContext("2d");
const chart = document.getElementById("chart");
const cctx = chart.getContext("2d");
const pad = { x: 320, w: 320 };

let pop = [];
let gen = 0;
let frame = 0;
let running = false;
let raf = 0;
let history = [];
let landings = 0;
let firstLanding = null;
let hold = 0;
let flash = 0;
const rocks = Array.from({ length: 28 }, () => ({
  x: Math.random() * W,
  w: 10 + Math.random() * 28,
  h: 6 + Math.random() * 12
}));
const stars = Array.from({ length: 120 }, () => ({
  x: Math.random() * W,
  y: Math.random() * (GROUND - 50),
  s: Math.random() * 1.8 + 0.3,
  a: Math.random()
}));

function $(id) { return document.getElementById(id); }

function bindRange(id, vis, fmt) {
  const el = $(id);
  const update = () => { $(vis).textContent = fmt(el.value); };
  el.oninput = update;
  update();
}
bindRange("nPop", "vPop", (v) => v);
bindRange("pc", "vPc", (v) => (Number(v) / 100).toFixed(2));
bindRange("pm", "vPm", (v) => (Number(v) / 100).toFixed(2));
bindRange("elite", "vElite", (v) => v);
bindRange("spd", "vSpd", (v) => v + "x");

function randGene() {
  const rots = [-1, 0, 0, 1];
  return { rot: rots[Math.floor(Math.random() * rots.length)], thrust: Math.random() < 0.4 ? 1 : 0 };
}
function randomShip() {
  return { genes: Array.from({ length: STEPS }, randGene), sim: null };
}

function verdict(x, vx, vy, ang, hit) {
  const onPad = x >= pad.x && x <= pad.x + pad.w;
  const slow = Math.abs(vy) < MAX_VY && Math.abs(vx) < MAX_VX;
  const straight = Math.abs(ang) < MAX_ANG;
  const landed = hit && onPad && slow && straight;
  let why = "";
  if (!hit) why = "No alcanzó el suelo en este intento.";
  else if (landed) why = "Tres de tres: pista, velocidad suave y nave derecha.";
  else {
    const fails = [];
    if (!onPad) fails.push("cayó fuera de la pista");
    if (!slow) fails.push("iba demasiado rápido");
    if (!straight) fails.push("iba inclinada");
    why = "Choque porque " + fails.join(" y ") + ".";
  }
  return { onPad, slow, straight, landed, crashed: hit && !landed, why };
}

function simulate(genes) {
  let x = W / 2 + 70, y = 70, vx = 0, vy = 0, ang = 0;
  const path = [];
  let hit = false;
  outer: for (let i = 0; i < genes.length; i++) {
    const g = genes[i];
    for (let h = 0; h < HOLD; h++) {
      ang += g.rot * 0.08;
      ang = Math.max(-1, Math.min(1, ang));
      if (g.thrust) {
        vx += Math.sin(ang) * THRUST;
        vy -= Math.cos(ang) * THRUST;
      }
      vy += GRAVITY;
      vx *= 0.99;
      x += vx;
      y += vy;
      if (y < 10) { y = 10; if (vy < 0) vy = 0; }
      if (x < 16) { x = 16; vx = Math.abs(vx) * 0.2; }
      if (x > W - 16) { x = W - 16; vx = -Math.abs(vx) * 0.2; }
      path.push({ x, y, ang, thrust: g.thrust, vx, vy });
      if (y >= GROUND - 14) {
        y = GROUND - 14;
        hit = true;
        path[path.length - 1].y = y;
        path[path.length - 1].vx = vx;
        path[path.length - 1].vy = vy;
        break outer;
      }
    }
  }
  const v = verdict(x, vx, vy, ang, hit);
  const cx = pad.x + pad.w / 2;
  let fit = 4000 / (1 + Math.abs(x - cx) * 0.15);
  fit += 1500 / (1 + Math.abs(vy));
  fit += 600 / (1 + Math.abs(vx));
  fit += 400 / (1 + Math.abs(ang) * 2);
  if (hit) fit += 1000; else fit += y * 1.5;
  if (v.onPad && hit) fit += 4000;
  if (v.landed) fit += 10000;
  return { x, y, vx, vy, ang, path, hit, fit, ...v };
}

function evalAll() {
  pop.forEach((s) => { s.sim = simulate(s.genes); });
  pop.sort((a, b) => b.sim.fit - a.sim.fit);
}

function tournament() {
  let best = pop[Math.floor(Math.random() * Math.min(12, pop.length))];
  for (let i = 0; i < 3; i++) {
    const c = pop[Math.floor(Math.random() * Math.min(18, pop.length))];
    if (c.sim.fit > best.sim.fit) best = c;
  }
  return best;
}
function crossover(a, b, pc) {
  if (Math.random() > pc) return a.genes.map((g) => ({ ...g }));
  const cut = 1 + Math.floor(Math.random() * (STEPS - 1));
  return a.genes.slice(0, cut).concat(b.genes.slice(cut)).map((g) => ({ ...g }));
}
function mutate(genes, pm) {
  return genes.map((g) => (Math.random() < pm ? randGene() : { ...g }));
}

function breed() {
  const n = Number($("nPop").value);
  const pc = Number($("pc").value) / 100;
  const pm = Number($("pm").value) / 100;
  const elite = Number($("elite").value);
  evalAll();
  const next = [];
  for (let i = 0; i < elite; i++) next.push({ genes: pop[i].genes.map((g) => ({ ...g })), sim: null });
  while (next.length < n) {
    next.push({ genes: mutate(crossover(tournament(), tournament(), pc), pm), sim: null });
  }
  pop = next;
  gen += 1;
  evalAll();
  landings = pop.filter((s) => s.sim.landed).length;
  if (landings > 0 && firstLanding === null) firstLanding = gen;
  history.push({ best: pop[0].sim.fit, avg: pop.reduce((s, p) => s + p.sim.fit, 0) / pop.length });
  if (history.length > 120) history.shift();
  frame = 0;
  hold = pop[0].sim.landed ? 55 : pop[0].sim.hit ? 28 : 8;
  flash = pop[0].sim.landed ? 18 : 0;
}

function reset() {
  pop = Array.from({ length: Number($("nPop").value) }, randomShip);
  gen = 0;
  frame = 0;
  hold = 0;
  flash = 0;
  history = [];
  evalAll();
  firstLanding = null;
  landings = pop.filter((s) => s.sim.landed).length;
  if (landings > 0) firstLanding = 0;
  history.push({ best: pop[0].sim.fit, avg: pop.reduce((s, p) => s + p.sim.fit, 0) / pop.length });
}

function roundRect(x, y, w, h, r) {
  ctx.beginPath();
  ctx.moveTo(x + r, y);
  ctx.arcTo(x + w, y, x + w, y + h, r);
  ctx.arcTo(x + w, y + h, x, y + h, r);
  ctx.arcTo(x, y + h, x, y, r);
  ctx.arcTo(x, y, x + w, y, r);
  ctx.closePath();
}

function drawBackground(t) {
  const sky = ctx.createLinearGradient(0, 0, 0, H);
  sky.addColorStop(0, "#090014");
  sky.addColorStop(0.55, "#1e0b3b");
  sky.addColorStop(1, "#3b0764");
  ctx.fillStyle = sky;
  ctx.fillRect(0, 0, W, H);

  ctx.fillStyle = "#c4b5fd";
  ctx.beginPath();
  ctx.arc(820, 70, 38, 0, Math.PI * 2);
  ctx.fill();
  ctx.fillStyle = "rgba(167,139,250,.18)";
  ctx.beginPath();
  ctx.arc(820, 70, 62, 0, Math.PI * 2);
  ctx.fill();

  stars.forEach((st) => {
    ctx.globalAlpha = 0.4 + 0.6 * Math.abs(Math.sin(t * 0.04 + st.a * 8));
    ctx.fillStyle = "#fff";
    ctx.fillRect(st.x, st.y, st.s, st.s);
  });
  ctx.globalAlpha = 1;

  const dirt = ctx.createLinearGradient(0, GROUND - 20, 0, H);
  dirt.addColorStop(0, "#6b3f1f");
  dirt.addColorStop(0.35, "#4a2c14");
  dirt.addColorStop(1, "#1c0f08");
  ctx.fillStyle = dirt;
  ctx.fillRect(0, GROUND, W, H - GROUND);
  ctx.fillStyle = "#7c4a28";
  rocks.forEach((r) => {
    if (r.x > pad.x - 8 && r.x < pad.x + pad.w + 8) return;
    ctx.fillRect(r.x, GROUND - r.h + 8, r.w, r.h);
  });

  const glow = ctx.createRadialGradient(pad.x + pad.w / 2, GROUND, 10, pad.x + pad.w / 2, GROUND, 180);
  glow.addColorStop(0, "rgba(250, 204, 21, .35)");
  glow.addColorStop(1, "rgba(250, 204, 21, 0)");
  ctx.fillStyle = glow;
  ctx.fillRect(pad.x - 40, GROUND - 90, pad.w + 80, 90);

  ctx.fillStyle = "#111";
  roundRect(pad.x - 6, GROUND - 8, pad.w + 12, 16, 6);
  ctx.fill();
  ctx.fillStyle = "#facc15";
  roundRect(pad.x, GROUND - 10, pad.w, 12, 5);
  ctx.fill();
  ctx.fillStyle = "#86efac";
  ctx.fillRect(pad.x + 14, GROUND - 6, pad.w - 28, 5);
  ctx.strokeStyle = "rgba(255,255,255,.45)";
  ctx.setLineDash([16, 12]);
  ctx.beginPath();
  ctx.moveTo(pad.x + 16, GROUND - 4);
  ctx.lineTo(pad.x + pad.w - 16, GROUND - 4);
  ctx.stroke();
  ctx.setLineDash([]);

  const blink = Math.sin(t * 0.2) > 0;
  ctx.fillStyle = blink ? "#4ade80" : "#14532d";
  ctx.beginPath(); ctx.arc(pad.x + 10, GROUND - 18, 5, 0, Math.PI * 2); ctx.fill();
  ctx.beginPath(); ctx.arc(pad.x + pad.w - 10, GROUND - 18, 5, 0, Math.PI * 2); ctx.fill();

  ctx.fillStyle = "#fde68a";
  ctx.font = "800 14px Segoe UI";
  ctx.textAlign = "center";
  ctx.fillText("PISTA", pad.x + pad.w / 2, GROUND + 28);
  ctx.textAlign = "left";
}

function drawShip(p, best, crashed, landed, finished) {
  ctx.save();
  ctx.translate(p.x, p.y);
  ctx.rotate(p.ang);
  const s = best ? 1.35 : 0.72;
  if (best) {
    ctx.shadowColor = landed && finished ? "#4ade80" : "#67e8f9";
    ctx.shadowBlur = 18;
  }
  if (p.thrust && !finished) {
    ctx.fillStyle = Math.random() > 0.4 ? "#fdba74" : "#fb7185";
    ctx.beginPath();
    ctx.moveTo(-5 * s, 12 * s);
    ctx.lineTo(0, (22 + Math.random() * 8) * s);
    ctx.lineTo(5 * s, 12 * s);
    ctx.fill();
  }
  ctx.fillStyle = best ? "#e0f2fe" : "rgba(203,213,225,.55)";
  ctx.beginPath();
  ctx.moveTo(0, -16 * s);
  ctx.lineTo(10 * s, 8 * s);
  ctx.lineTo(0, 4 * s);
  ctx.lineTo(-10 * s, 8 * s);
  ctx.closePath();
  ctx.fill();
  ctx.fillStyle = best ? "#0ea5e9" : "rgba(100,116,139,.7)";
  ctx.beginPath();
  ctx.ellipse(0, -4 * s, 5 * s, 6 * s, 0, 0, Math.PI * 2);
  ctx.fill();
  ctx.fillStyle = best ? "#38bdf8" : "rgba(148,163,184,.5)";
  ctx.fillRect(-12 * s, 6 * s, 7 * s, 3 * s);
  ctx.fillRect(5 * s, 6 * s, 7 * s, 3 * s);
  if (p.y > GROUND - 80) {
    ctx.strokeStyle = best ? "#cbd5e1" : "rgba(148,163,184,.6)";
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(-6 * s, 8 * s); ctx.lineTo(-11 * s, 16 * s);
    ctx.moveTo(6 * s, 8 * s); ctx.lineTo(11 * s, 16 * s);
    ctx.stroke();
  }
  if (finished && crashed && best) {
    ctx.shadowBlur = 0;
    ctx.strokeStyle = "#fb7185";
    ctx.lineWidth = 3;
    ctx.beginPath();
    ctx.moveTo(-12, -12); ctx.lineTo(12, 12);
    ctx.moveTo(12, -12); ctx.lineTo(-12, 12);
    ctx.stroke();
  }
  ctx.restore();
}

function nowPoint() {
  const path = pop[0].sim.path;
  const t = Math.max(0, Math.min(frame, path.length - 1));
  const finished = frame >= path.length - 1;
  return { p: path[t], t, finished, path };
}

function drawWorld() {
  drawBackground(frame + gen * 10);
  const { p, t, finished, path } = nowPoint();

  if (pop[0] && path.length > 2) {
    ctx.beginPath();
    ctx.strokeStyle = "rgba(103,232,249,.55)";
    ctx.lineWidth = 2;
    const start = Math.max(0, t - 28);
    ctx.moveTo(path[start].x, path[start].y);
    for (let i = start; i <= t; i++) ctx.lineTo(path[i].x, path[i].y);
    ctx.stroke();
  }

  pop.forEach((s, i) => {
    if (i === 0) return;
    if (i > 10) return;
    const pt = s.sim.path[Math.min(t, s.sim.path.length - 1)];
    if (pt) drawShip(pt, false, s.sim.crashed, s.sim.landed, finished && t >= s.sim.path.length - 1);
  });
  if (p) drawShip(p, true, pop[0].sim.crashed, pop[0].sim.landed, finished);

  if (finished && pop[0].sim.landed) {
    ctx.fillStyle = "rgba(74, 222, 128, .18)";
    ctx.beginPath();
    ctx.arc(p.x, GROUND - 8, 46 + flash, 0, Math.PI * 2);
    ctx.fill();
  }
  if (finished && pop[0].sim.crashed && p) {
    ctx.fillStyle = "rgba(251, 113, 133, .25)";
    ctx.beginPath();
    ctx.ellipse(p.x, GROUND - 4, 28, 10, 0, 0, Math.PI * 2);
    ctx.fill();
  }

  if (flash > 0) {
    ctx.fillStyle = `rgba(74,222,128,${flash / 40})`;
    ctx.fillRect(0, 0, W, H);
    flash -= 1;
  }

  ctx.fillStyle = "rgba(7,4,15,.72)";
  roundRect(14, 12, 160, 44, 12);
  ctx.fill();
  ctx.fillStyle = "#f5f3ff";
  ctx.font = "800 16px Segoe UI";
  ctx.fillText("Gen " + gen, 28, 42);
}

function hud() {
  const best = pop[0].sim;
  const { finished } = nowPoint();
  let state = "Vuelo";
  if (finished && best.landed) state = "Aterrizaje";
  else if (finished && best.crashed) state = "Choque";
  $("sState").textContent = state;
  $("sState").style.color = state === "Aterrizaje" ? "#4ade80" : state === "Choque" ? "#fb7185" : "";
  $("sGen").textContent = String(gen);
  $("sBest").textContent = best.fit.toFixed(0);
  $("sLand").textContent = String(landings);
}

function drawChart() {
  cctx.fillStyle = "#0b0714";
  cctx.fillRect(0, 0, chart.width, chart.height);
  if (history.length < 2) return;
  const max = Math.max(...history.map((h) => h.best), 1);
  function line(key, color) {
    cctx.beginPath();
    cctx.strokeStyle = color;
    cctx.lineWidth = 2;
    history.forEach((h, i) => {
      const x = (i / (history.length - 1)) * (chart.width - 16) + 8;
      const y = 24 + (chart.height - 32) - (h[key] / max) * (chart.height - 36);
      if (i === 0) cctx.moveTo(x, y); else cctx.lineTo(x, y);
    });
    cctx.stroke();
  }
  line("avg", "#64748b");
  line("best", "#67e8f9");
}

function loop() {
  if (!running) return;
  const longest = Math.max(...pop.map((s) => s.sim.path.length));
  if (frame < longest - 1) frame += Number($("spd").value);
  else if (hold > 0) hold -= 1;
  else breed();
  drawWorld();
  drawChart();
  hud();
  raf = requestAnimationFrame(loop);
}

$("btnRun").onclick = () => {
  running = !running;
  $("btnRun").textContent = running ? "Pausar" : "Iniciar";
  if (running) loop();
  else cancelAnimationFrame(raf);
};
$("btnReset").onclick = () => {
  running = false;
  $("btnRun").textContent = "Iniciar";
  cancelAnimationFrame(raf);
  reset();
  drawWorld();
  drawChart();
  hud();
};

reset();
drawWorld();
drawChart();
hud();
