(function () {
  const s = document.getElementById("algoritmo"),
    p = document.getElementById("visualizer-play"),
    b = document.getElementById("visualizer-bars"),
    t = document.getElementById("visualizer-status"),
    a = document.getElementById("visualizer-algorithm");
  if (!s || !p || !b) return;
  const base = [38, 78, 24, 63, 46, 91, 17, 56, 31, 70, 43, 84];
  let v = [...base],
    run = false,
    stop = false,
    steps = 0,
    checks = 0;
  function draw(active = [], sorted = []) {
    b.innerHTML = "";
    v.forEach((n, i) => {
      const c = document.createElement("div");
      c.className = "visualizer-column";
      const l = document.createElement("small");
      l.textContent = n;
      const e = document.createElement("span");
      e.className =
        "visualizer-bar" +
        (active.includes(i) ? " active" : "") +
        (sorted.includes(i) ? " sorted" : "");
      e.style.height = n + "%";
      e.setAttribute("aria-label", "Valor " + n);
      c.append(l, e);
      b.append(c);
    });
  }
  function text(x) {
    a.textContent = s.value;
    t.innerHTML =
      "<strong>" +
      s.value +
      "</strong> · " +
      x +
      " · Passos: " +
      steps +
      " · Comparações: " +
      checks;
  }
  const wait = (n) => new Promise((r) => setTimeout(r, n));
  async function cmp(i, j) {
    if (stop) return false;
    checks++;
    draw([i, j]);
    text("comparando");
    await wait(80);
    return !stop;
  }
  async function swap(i, j) {
    if (!(await cmp(i, j))) return false;
    [v[i], v[j]] = [v[j], v[i]];
    steps++;
    draw([i, j]);
    await wait(70);
    return true;
  }
  async function simple(type) {
    if (type === "selection") {
      for (let start = 0; start < v.length - 1 && !stop; start++) {
        let m = start;
        for (let i = start + 1; i < v.length; i++) {
          if (!(await cmp(m, i))) return;
          if (v[i] < v[m]) m = i;
        }
        if (m !== start && !(await swap(start, m))) return;
      }
    } else
      for (let end = v.length - 1; end > 0 && !stop; end--)
        for (let i = 0; i < end; i++)
          if (v[i] > v[i + 1] && !(await swap(i, i + 1))) return;
  }
  async function insertion() {
    for (let i = 1; i < v.length && !stop; i++) {
      let j = i;
      while (j && v[j] < v[j - 1]) {
        if (!(await swap(j, j - 1))) return;
        j--;
      }
    }
  }
  async function mergeSort(x, o = 0) {
    if (x.length < 2) return x;
    const m = x.length >> 1,
      l = await mergeSort(x.slice(0, m), o),
      r = await mergeSort(x.slice(m), o + m),
      out = [];
    while (l.length && r.length && !stop) {
      out.push(l[0] <= r[0] ? l.shift() : r.shift());
      steps++;
      const preview = out.concat(l, r);
      v.splice(o, preview.length, ...preview);
      draw(
        [o + out.length - 1],
        Array.from({ length: out.length }, (_, i) => o + i),
      );
      text("intercalando");
      await wait(110);
    }
    out.push(...l, ...r);
    v.splice(o, out.length, ...out);
    draw(
      [],
      Array.from({ length: out.length }, (_, i) => o + i),
    );
    await wait(110);
    return out;
  }
  async function quick(lo, hi) {
    if (lo >= hi || stop) return;
    const pivot = v[hi];
    let k = lo;
    for (let i = lo; i < hi; i++) {
      if (!(await cmp(i, hi))) return;
      if (v[i] <= pivot) {
        if (k !== i && !(await swap(k, i))) return;
        k++;
      }
    }
    if (k !== hi && !(await swap(k, hi))) return;
    await quick(lo, k - 1);
    await quick(k + 1, hi);
  }
  async function start() {
    if (run) {
      stop = true;
      return;
    }
    run = true;
    stop = false;
    steps = 0;
    checks = 0;
    v = [...base];
    p.textContent = "Parar animação";
    p.classList.add("visualizer-stop");
    s.disabled = true;
    draw();
    text("iniciando");
    if (s.value === "BUBBLE") await simple("bubble");
    else if (s.value === "SELECTION") await simple("selection");
    else if (s.value === "INSERTION") await insertion();
    else if (s.value === "MERGE") await mergeSort([...v]);
    else await quick(0, v.length - 1);
    const ok = !stop;
    draw([], ok ? v.map((_, i) => i) : []);
    text(ok ? "concluído" : "interrompido");
    p.textContent = "Reproduzir animação";
    p.classList.remove("visualizer-stop");
    s.disabled = false;
    run = false;
  }
  s.addEventListener("change", () => {
    if (run) return;
    v = [...base];
    steps = checks = 0;
    draw();
    text("pronto");
  });
  p.addEventListener("click", start);
  draw();
  text("pronto");
})();
