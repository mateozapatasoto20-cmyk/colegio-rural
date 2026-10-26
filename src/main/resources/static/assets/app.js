/* JavaScript mínimo: solo comportamiento de interfaz. La lógica del sistema está en Java (Spring Boot) + MySQL. */
(function () {
  var raiz = document.documentElement;
  function guardar(k, v) { try { localStorage.setItem(k, v); } catch (e) {} }
  function leer(k) { try { return localStorage.getItem(k); } catch (e) { return null; } }

  /* Modo claro / oscuro */
  var tema = leer('tema'); if (tema) { raiz.setAttribute('data-theme', tema); }
  var bt = document.getElementById('btnTema');
  if (bt) bt.addEventListener('click', function () {
    var oscuro = raiz.getAttribute('data-theme') === 'dark' || (!raiz.getAttribute('data-theme') && matchMedia('(prefers-color-scheme: dark)').matches);
    var nuevo = oscuro ? 'light' : 'dark'; raiz.setAttribute('data-theme', nuevo); guardar('tema', nuevo);
  });

  /* Menú lateral en móvil */
  var side = document.querySelector('.side'), mb = document.getElementById('btnMenu');
  if (mb && side) mb.addEventListener('click', function () { side.classList.toggle('abierto'); });

  /* Diálogos (formularios emergentes) */
  function abrir(id, datos, titulo) {
    var d = document.getElementById(id); if (!d) return;
    var f = d.querySelector('form'); if (f && !datos) f.reset();
    if (f && datos) Object.keys(datos).forEach(function (k) { if (f.elements[k] && f.elements[k].type !== 'hidden' || (f.elements[k] && k === 'id')) f.elements[k].value = datos[k]; });
    var t = d.querySelector('[data-titulo]'); if (t && titulo) t.textContent = titulo;
    d.showModal(); var p = d.querySelector('input[type=text],input[type=number],select'); if (p) p.focus();
    d.dispatchEvent(new Event('abierto'));
  }
  document.addEventListener('click', function (e) {
    var o = e.target.closest('[data-open]');
    if (o) { var datos = o.getAttribute('data-fill'); abrir(o.getAttribute('data-open'), datos ? JSON.parse(datos) : null, o.getAttribute('data-title')); return; }
    if (e.target.closest('[data-cerrar]')) { var d = e.target.closest('dialog'); if (d) d.close(); return; }
    if (e.target.tagName === 'DIALOG') e.target.close();
  });

  /* Confirmación antes de eliminar (RF18) con diálogo propio */
  var conf = document.getElementById('dlgConfirm'), pendiente = null;
  document.addEventListener('submit', function (e) {
    var m = e.target.getAttribute && e.target.getAttribute('data-confirm');
    if (m && conf && !e.target.__ok) {
      e.preventDefault(); pendiente = e.target;
      conf.querySelector('[data-msg]').textContent = m; conf.showModal();
    }
  });
  if (conf) conf.querySelector('[data-si]').addEventListener('click', function () {
    conf.close(); if (pendiente) { pendiente.__ok = true; pendiente.submit(); }
  });

  /* Selectores que envían el formulario al cambiar */
  document.querySelectorAll('[data-auto]').forEach(function (s) { s.addEventListener('change', function () { s.form.submit(); }); });

  /* Avisos que se cierran solos */
  setTimeout(function () { document.querySelectorAll('.toast').forEach(function (t) { t.remove(); }); }, 5000);

  /* Aviso en vivo al escribir una nota: ¿generará alerta? */
  var nota = document.getElementById('campoNota'), asis = document.getElementById('campoAsis'), pista = document.getElementById('pistaAlerta');
  function evaluar() {
    if (!nota || !pista) return;
    var n = parseFloat(nota.value), a = parseFloat(asis.value), msgs = [];
    var nm = parseFloat(nota.dataset.min), am = parseFloat(asis.dataset.min);
    if (!isNaN(n) && n < nm) msgs.push('nota menor a ' + nm);
    if (!isNaN(a) && a < am) msgs.push('asistencia menor a ' + am + '%');
    pista.className = 'pista ' + (msgs.length ? 'mal' : 'bien');
    pista.textContent = msgs.length ? 'Se generará una alerta: ' + msgs.join(' y ') + '.' : (isNaN(n) && isNaN(a) ? '' : 'No se generará alerta.');
  }
  if (nota) { nota.addEventListener('input', evaluar); asis.addEventListener('input', evaluar); }

  /* Reabrir el diálogo con lo escrito si el servidor encontró un error */
  if (window.APP && window.APP.abrir) abrir(window.APP.abrir, window.APP.datos, window.APP.titulo);
})();

/* Pase de lista: marcar a todos y contar en vivo */
(function () {
  var f = document.getElementById('formLista'); if (!f) return;
  function contar() {
    var c = { presente: 0, tardanza: 0, falta: 0, justificada: 0 };
    f.querySelectorAll('input[type=radio]:checked').forEach(function (r) { c[r.value]++; });
    Object.keys(c).forEach(function (k) { var el = document.getElementById('n-' + k); if (el) el.textContent = c[k]; });
  }
  f.addEventListener('change', contar);
  var t = document.getElementById('btnTodos');
  if (t) t.addEventListener('click', function () { f.querySelectorAll('input[type=radio][value=presente]').forEach(function (r) { r.checked = true; }); contar(); });
  contar();
})();
