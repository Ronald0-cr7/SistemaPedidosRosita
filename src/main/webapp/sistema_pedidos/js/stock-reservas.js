(() => {
  const ctx = document.currentScript.dataset.context;
  const reloj = document.getElementById('reservaReloj');
  let vence = Number(reloj?.dataset.vence || 0);
  let estabaReservado = vence > 0;
  async function refrescar() {
    try {
      const r = await fetch(ctx + '/StockDisponible', {cache:'no-store'});
      if (!r.ok) return;
      const d = await r.json();
      document.querySelectorAll('[data-stock]').forEach(el => el.textContent = d.stock[el.dataset.stock] ?? 0);
      document.querySelectorAll('[data-stock-button]').forEach(el => el.disabled = !(d.stock[el.dataset.stockButton] > 0));
      document.querySelectorAll('[data-reserva-count]').forEach(el => el.textContent = d.cantidad);
      if (reloj) { vence = d.vence; if (estabaReservado && !vence) location.reload(); }
    } catch(e) { /* El servidor siempre valida stock al confirmar. */ }
  }
  if (reloj) setInterval(() => {
    if (!vence) return;
    const s = Math.max(0, Math.ceil((vence-Date.now())/1000));
    reloj.textContent = `Reserva temporal: ${String(Math.floor(s/60)).padStart(2,'0')}:${String(s%60).padStart(2,'0')}`;
    if (!s) { reloj.textContent='Reserva vencida. Actualizando carrito…'; document.getElementById('placeOrder')?.setAttribute('disabled','disabled'); }
  }, 1000);
  refrescar();setInterval(refrescar,5000);
})();
