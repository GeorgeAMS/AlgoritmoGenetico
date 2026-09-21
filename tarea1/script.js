const W = 900, H = 520;
const NAMES = "ABCDEFGHIJKL".split("");
const map = document.getElementById("map");
const ctx = map.getContext("2d");
const chart = document.getElementById("chart");
const cctx = chart.getContext("2d");

let cities = [];
let pop = [];
let gen = 0;
let history = [];
let running = false;
let raf = 0;
let truckT = 0;

function $(id) { return document.getElementById(id); }
function bind(id, vis, fmt) {
  const el = $(id);
  const upd = () => { $(vis).textContent = fmt(el.value); };
  el.oninput = upd;
  upd();
}
bind("nCities", "vCities", (v) => v);
bind("nPop", "vPop", (v) => v);
bind("pc", "vPc", (v) => (Number(v) / 100).toFixed(2));
bind("pm", "vPm", (v) => (Number(v) / 100).toFixed(2));
bind("elite", "vElite", (v) => v);
bind("spd", "vSpd", (v) => v + "x");

function nCities() { return Number($("nCities").value); }
function nPop() { return Number($("nPop").value); }

function dist(a, b) { return Math.hypot(a.x - b.x, a.y - b.y); }
function tourLen(order) {
  let s = 0;
  for (let i = 0; i < order.length; i++) {
    s += dist(cities[order[i]], cities[order[(i + 1) % order.length]]);
  }
  return s;
}
function shuffle(arr) {
  const a = arr.slice();
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }
  return a;
}
function makeInd(order) {
  const km = tourLen(order);
  return { order, km, fit: 100000 / km };
}

function randomMap() {
  const c = nCities();
  const placed = [];
  for (let i = 0; i < c; i++) {
    let x, y, ok = false, tries = 0;
    while (!ok && tries < 50) {
      x = 90 + Math.random() * (W - 180);
      y = 80 + Math.random() * (H - 170);
      ok = placed.every((p) => Math.hypot(p.x - x, p.y - y) > 68);
      tries += 1;
    }
    placed.push({ name: NAMES[i], x, y });
  }
  cities = placed;
}

function evaluate() {
  pop = pop.map((p) => makeInd(p.order)).sort((a, b) => a.km - b.km);
}
function roulettePick() {
  const total = pop.reduce((s, p) => s + p.fit, 0);
  let r = Math.random() * total;
  for (const p of pop) { r -= p.fit; if (r <= 0) return p; }
  return pop[pop.length - 1];
}
function ox(a, b) {
  const C = a.length;
  const start = Math.floor(Math.random() * C);
  const end = start + 1 + Math.floor(Math.random() * (C - 1));
  const slice = a.slice(start, Math.min(end, C));
  const rest = b.filter((x) => !slice.includes(x));
  const child = [];
  let r = 0;
  for (let i = 0; i < C; i++) {
    if (i >= start && i < Math.min(end, C)) child.push(slice[i - start]);
    else { child.push(rest[r]); r += 1; }
  }
  return child;
}
function mutate(order, pm) {
  const o = order.slice();
  if (Math.random() < pm) {
    const i = Math.floor(Math.random() * o.length);
    let j = Math.floor(Math.random() * o.length);
    if (j === i) j = (j + 1) % o.length;
    [o[i], o[j]] = [o[j], o[i]];
  }
  return o;
}

function nextGen() {
  const n = nPop();
  const pc = Number($("pc").value) / 100;
  const pm = Number($("pm").value) / 100;
  const elite = Number($("elite").value);
  evaluate();
  const next = [];
  for (let i = 0; i < elite && i < pop.length; i++) next.push(makeInd(pop[i].order.slice()));
  while (next.length < n) {
    const p1 = roulettePick(), p2 = roulettePick();
    const c1 = Math.random() < pc ? ox(p1.order, p2.order) : p1.order.slice();
    next.push(makeInd(mutate(c1, pm)));
  }
  pop = next.slice(0, n);
  gen += 1;
  evaluate();
  history.push({ best: pop[0].km, avg: pop.reduce((s, p) => s + p.km, 0) / pop.length });
  truckT = 0;
}

function resetPop() {
  stop();
  const base = Array.from({ length: cities.length }, (_, i) => i);
  pop = Array.from({ length: nPop() }, () => makeInd(shuffle(base)));
  gen = 0;
  history = [];
  truckT = 0;
  evaluate();
  history.push({ best: pop[0].km, avg: pop.reduce((s, p) => s + p.km, 0) / pop.length });
  render();
}

function drawCity() {
  const sky = ctx.createLinearGradient(0, 0, 0, H);
  sky.addColorStop(0, "#7dd3fc");
  sky.addColorStop(0.55, "#bbf7d0");
  sky.addColorStop(1, "#86efac");
  ctx.fillStyle = sky;
  ctx.fillRect(0, 0, W, H);

  ctx.fillStyle = "#38bdf8";
  ctx.beginPath();
  ctx.ellipse(W - 80, H - 40, 160, 36, 0, 0, Math.PI * 2);
  ctx.fill();
  ctx.fillStyle = "#4ade80";
  ctx.fillRect(0, H - 70, 220, 70);
  ctx.fillStyle = "#22c55e";
  ctx.beginPath();
  ctx.arc(70, H - 70, 40, 0, Math.PI * 2);
  ctx.arc(130, H - 62, 28, 0, Math.PI * 2);
  ctx.fill();

  ctx.strokeStyle = "rgba(255,255,255,.55)";
  ctx.lineWidth = 18;
  for (let i = 0; i < 4; i++) {
    ctx.beginPath();
    ctx.moveTo(0, 90 + i * 90);
    ctx.lineTo(W, 110 + i * 80);
    ctx.stroke();
  }
  ctx.strokeStyle = "rgba(253,186,116,.35)";
  ctx.lineWidth = 10;
  for (let i = 0; i < 5; i++) {
    ctx.beginPath();
    ctx.moveTo(80 + i * 170, 40);
    ctx.lineTo(40 + i * 170, H - 20);
    ctx.stroke();
  }

  ctx.fillStyle = "rgba(255,255,255,.55)";
  ctx.font = "700 13px Segoe UI";
}

function drawRoutes() {
  const faded = pop.slice(1, Math.min(8, pop.length));
  faded.forEach((ind) => {
    ctx.beginPath();
    ctx.strokeStyle = "rgba(120,113,108,.22)";
    ctx.lineWidth = 2;
    ind.order.forEach((idx, i) => {
      const p = cities[idx];
      if (i === 0) ctx.moveTo(p.x, p.y); else ctx.lineTo(p.x, p.y);
    });
    ctx.closePath();
    ctx.stroke();
  });

  const best = pop[0].order;
  ctx.beginPath();
  ctx.strokeStyle = "#ea580c";
  ctx.lineWidth = 5;
  ctx.lineJoin = "round";
  best.forEach((idx, i) => {
    const p = cities[idx];
    if (i === 0) ctx.moveTo(p.x, p.y); else ctx.lineTo(p.x, p.y);
  });
  ctx.closePath();
  ctx.stroke();

  for (let i = 0; i < best.length; i++) {
    const a = cities[best[i]];
    const b = cities[best[(i + 1) % best.length]];
    const mx = (a.x + b.x) / 2, my = (a.y + b.y) / 2;
    const ang = Math.atan2(b.y - a.y, b.x - a.x);
    ctx.save();
    ctx.translate(mx, my);
    ctx.rotate(ang);
    ctx.fillStyle = "#9a3412";
    ctx.beginPath();
    ctx.moveTo(8, 0); ctx.lineTo(-5, 5); ctx.lineTo(-5, -5);
    ctx.fill();
    ctx.restore();
  }
}

function drawPins() {
  cities.forEach((p, i) => {
    ctx.beginPath();
    ctx.fillStyle = "rgba(234,88,12,.2)";
    ctx.arc(p.x, p.y, 22, 0, Math.PI * 2);
    ctx.fill();
    ctx.beginPath();
    ctx.fillStyle = "#ea580c";
    ctx.arc(p.x, p.y, 16, 0, Math.PI * 2);
    ctx.fill();
    ctx.fillStyle = "#fff";
    ctx.font = "800 14px Segoe UI";
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillText(p.name, p.x, p.y);
  });
  ctx.textAlign = "left";
}

function truckPos() {
  const order = pop[0].order;
  const segs = order.length;
  const total = segs;
  const t = truckT % total;
  const i = Math.floor(t);
  const f = t - i;
  const a = cities[order[i]];
  const b = cities[order[(i + 1) % segs]];
  return {
    x: a.x + (b.x - a.x) * f,
    y: a.y + (b.y - a.y) * f,
    ang: Math.atan2(b.y - a.y, b.x - a.x)
  };
}

function drawTruck() {
  const p = truckPos();
  ctx.save();
  ctx.translate(p.x, p.y);
  ctx.rotate(p.ang);
  ctx.fillStyle = "#1d4ed8";
  ctx.fillRect(-16, -9, 26, 18);
  ctx.fillStyle = "#facc15";
  ctx.fillRect(8, -8, 12, 16);
  ctx.fillStyle = "#0f172a";
  ctx.beginPath(); ctx.arc(-8, 10, 4, 0, Math.PI * 2); ctx.arc(10, 10, 4, 0, Math.PI * 2); ctx.fill();
  ctx.restore();
}

function drawChart() {
  cctx.fillStyle = "#fff";
  cctx.fillRect(0, 0, chart.width, chart.height);
  if (history.length < 2) return;
  const maxY = Math.max(...history.map((h) => h.avg));
  const minY = Math.min(...history.map((h) => h.best)) * 0.92;
  function line(key, color) {
    cctx.beginPath();
    cctx.strokeStyle = color;
    cctx.lineWidth = 2;
    history.forEach((pt, i) => {
      const x = (i / (history.length - 1)) * (chart.width - 16) + 8;
      const y = 22 + (chart.height - 30) - ((pt[key] - minY) / (maxY - minY || 1)) * (chart.height - 34);
      if (i === 0) cctx.moveTo(x, y); else cctx.lineTo(x, y);
    });
    cctx.stroke();
  }
  line("avg", "#a8a29e");
  line("best", "#ea580c");
}

function render() {
  drawCity();
  drawRoutes();
  drawPins();
  drawTruck();
  $("gGen").textContent = String(gen);
  $("gCit").textContent = String(cities.length);
  $("gPop").textContent = String(pop.length);
  $("gDist").textContent = pop[0].km.toFixed(0) + " u";
  $("routeText").textContent = pop[0].order.map((i) => cities[i].name).join(" → ") +
    " → " + cities[pop[0].order[0]].name;
  drawChart();
}

function stop() {
  running = false;
  cancelAnimationFrame(raf);
  $("btnRun").textContent = "Iniciar";
}

function loop() {
  if (!running) return;
  truckT += 0.012 * Number($("spd").value);
  if (truckT >= pop[0].order.length) {
    nextGen();
  }
  render();
  raf = requestAnimationFrame(loop);
}

$("btnRun").onclick = () => {
  running = !running;
  $("btnRun").textContent = running ? "Pausar" : "Iniciar";
  if (running) loop();
  else cancelAnimationFrame(raf);
};
$("btnStep").onclick = () => { stop(); nextGen(); render(); };
$("btnReset").onclick = () => resetPop();
$("btnMap").onclick = () => { randomMap(); resetPop(); };
$("nCities").onchange = () => { randomMap(); resetPop(); };
$("nPop").onchange = () => resetPop();

randomMap();
resetPop();
