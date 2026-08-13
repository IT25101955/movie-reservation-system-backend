document.addEventListener('DOMContentLoaded', () => {
  // Context navigation on task/detail screens.
  const p = window.location.pathname;
  const needsBack = /^\/(movies\/[^/]+|booking|payment|tickets|reviews|account\/booking)/.test(p);
  if (needsBack && !document.querySelector('.cine-page-nav')) {
    const bar = document.createElement('div');
    bar.className = 'cine-page-nav';
    bar.innerHTML = '<button type="button" class="cine-back-btn" aria-label="Go back">← Back</button><a class="cine-home-link" href="/">⌂ Home</a>';
    const header = document.querySelector('.cinemax-site-header');
    if (header) header.insertAdjacentElement('afterend', bar); else document.body.prepend(bar);
    bar.querySelector('button').addEventListener('click', () => {
      if (window.history.length > 1) window.history.back(); else window.location.href = '/movies';
    });
  }

  // Seat hover information.
  const seats = document.querySelectorAll('.seat[data-seat-code]');
  if (seats.length) {
    const tip = document.createElement('div');
    tip.className = 'cine-seat-tooltip'; tip.hidden = true; document.body.appendChild(tip);
    seats.forEach(seat => {
      seat.addEventListener('mouseenter', e => {
        const category=(seat.dataset.category||'Standard').toUpperCase();
        const code=seat.dataset.seatCode || seat.textContent.trim();
        let detail = category === 'VIP' ? `VIP · LKR ${seat.dataset.vipPrice || ''}` : `Standard · Adult LKR ${seat.dataset.adultPrice || ''} · Child LKR ${seat.dataset.childPrice || ''}`;
        tip.textContent = `${code} • ${detail.replace(/ · LKR $/,'')}`; tip.hidden=false;
      });
      seat.addEventListener('mousemove', e => {tip.style.left=e.clientX+'px'; tip.style.top=e.clientY+'px';});
      seat.addEventListener('mouseleave', () => tip.hidden=true);
    });
  }

  // Prevent accidental double submits; gives clear feedback.
  document.querySelectorAll('form').forEach(form => form.addEventListener('submit', () => {
    const btn=form.querySelector('button[type="submit"]'); if(!btn || btn.dataset.keepLabel==='true') return;
    const original=btn.innerHTML; btn.dataset.originalLabel=original; setTimeout(()=>{if(!btn.disabled){btn.disabled=true;btn.innerHTML='Please wait…';}},0);
  }));
});
