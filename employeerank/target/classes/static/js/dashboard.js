/* dashboard.js */
document.addEventListener('DOMContentLoaded', () => {
    Auth.requireAuth();
    initSidebar();

    const user = Auth.getUser();
    let trendChart = null;

    // Populate month/year selectors
    populateMonthYear('dashMonthSel', 'dashYearSel');
    populateMonthYear('scoreMonth', 'scoreYear');

    async function loadDashboard() {
        try {
            const me = await api.getMe();
            const u  = me.data;
            // Update header credits
            document.getElementById('headerCredits').textContent = u.totalCredits ?? 0;
            document.getElementById('statCredits').textContent   = u.totalCredits ?? 0;

            // Load current month summary
            const now = new Date();
            try {
                const sumRes = await api.getMySummary(now.getMonth() + 1, now.getFullYear());
                const sum = sumRes.data;
                document.getElementById('statMonthScore').textContent = `${sum.percentage?.toFixed(1) ?? 0}%`;
                document.getElementById('statMonthGrade').textContent = `Grade: ${sum.grade ?? '—'}`;
                renderCategoryList(sum.categoryBreakdown || []);
            } catch { /* no scores yet */ }

            // Badges count + render
            if (u.badges) {
                document.getElementById('statBadges').textContent = u.badges.length;
                renderBadges(u.badges);
            }

            // Load all results for trend
            const resultsRes = await api.getMyResults();
            const results = resultsRes.data || [];
            renderTrendChart(results);

            // rank
            if (results.length > 0) {
                const latest = results[0];
                document.getElementById('statRank').textContent = latest.rankInCompany ? `#${latest.rankInCompany}` : '—';
            }
        } catch (err) {
            showToast('Failed to load dashboard: ' + err.message, 'error');
        }
    }

    function renderCategoryList(categories) {
        const container = document.getElementById('scoreCategoryList');
        if (!categories.length) {
            container.innerHTML = '<div class="empty-state">No scores for this month.</div>';
            return;
        }
        container.innerHTML = categories.map(c => {
            const pct = Math.round(c.percentage || 0);
            let barClass = 'low';
            if (pct >= 90) barClass = 'top';
            else if (pct >= 75) barClass = 'great';
            else if (pct >= 60) barClass = 'good';
            else if (pct >= 45) barClass = 'mid';
            return `
            <div class="category-item">
                <span class="cat-name">${c.category.replace(/_/g, ' ')}</span>
                <div class="cat-bar-wrap">
                    <div class="cat-bar ${barClass}" style="width:${pct}%"></div>
                </div>
                <span class="cat-pct">${pct}%</span>
                <span class="cat-count">${c.scoreCount} scores</span>
            </div>`;
        }).join('');
    }

    function renderBadges(badges) {
        const container = document.getElementById('badgeContainer');
        if (!badges.length) {
            container.innerHTML = '<div class="empty-state">No badges yet. Keep performing!</div>';
            return;
        }
        container.innerHTML = badges.slice(0, 12).map(b => `
            <div class="badge-card">
                <div class="badge-icon badge-${b.badgeType}">${badgeEmoji(b.badgeType)}</div>
                <div class="badge-card-info">
                    <span class="badge-card-name">${b.badgeName}</span>
                    <span class="badge-card-date">${b.awardedMonth ? formatMonth(b.awardedMonth, b.awardedYear) : ''}</span>
                </div>
            </div>`).join('');
    }

    function renderTrendChart(results) {
        const canvas = document.getElementById('trendChart');
        if (!canvas) return;
        const sorted  = [...results].sort((a, b) => a.resultYear !== b.resultYear ? a.resultYear - b.resultYear : a.resultMonth - b.resultMonth);
        const labels  = sorted.map(r => `${MONTHS[r.resultMonth - 1].slice(0,3)} ${r.resultYear}`);
        const data    = sorted.map(r => r.percentageScore);

        if (trendChart) trendChart.destroy();
        trendChart = new Chart(canvas, {
            type: 'line',
            data: {
                labels,
                datasets: [{
                    label: 'Performance %',
                    data,
                    borderColor: '#e8ff47',
                    backgroundColor: 'rgba(232,255,71,0.08)',
                    borderWidth: 2,
                    pointBackgroundColor: '#e8ff47',
                    pointRadius: 4,
                    tension: 0.4,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                plugins: { legend: { display: false }, tooltip: { callbacks: { label: ctx => `${ctx.raw?.toFixed(1)}%` } } },
                scales: {
                    x: { grid: { color: 'rgba(255,255,255,0.04)' }, ticks: { color: '#5a5a70', font: { size: 11 } } },
                    y: { min: 0, max: 100, grid: { color: 'rgba(255,255,255,0.04)' }, ticks: { color: '#5a5a70', font: { size: 11 }, callback: v => v + '%' } }
                }
            }
        });
    }

    // Load scores on demand
    document.getElementById('dashLoadScores')?.addEventListener('click', async () => {
        const month = parseInt(document.getElementById('dashMonthSel').value);
        const year  = parseInt(document.getElementById('dashYearSel').value);
        try {
            const res = await api.getMySummary(month, year);
            renderCategoryList(res.data?.categoryBreakdown || []);
        } catch (err) {
            showToast('No scores found for this period.', 'info');
            document.getElementById('scoreCategoryList').innerHTML = '<div class="empty-state">No scores found.</div>';
        }
    });

    // SCORE MODAL (for managers)
    const scoreModal   = document.getElementById('scoreModal');
    const managerLink  = document.getElementById('managerSection');
    if (managerLink && user?.role !== 'ROLE_EMPLOYEE') managerLink.style.display = 'flex';

    document.getElementById('closeScoreModal')?.addEventListener('click', () => scoreModal.style.display = 'none');
    document.getElementById('cancelScoreModal')?.addEventListener('click', () => scoreModal.style.display = 'none');
    managerLink?.addEventListener('click', (e) => { e.preventDefault(); scoreModal.style.display = 'flex'; });

    document.getElementById('scoreForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const month = parseInt(document.getElementById('scoreMonth').value);
        const year  = parseInt(document.getElementById('scoreYear').value);
        try {
            await api.addScore({
                employeeId:  parseInt(document.getElementById('scoreEmployeeId').value),
                category:    document.getElementById('scoreCategory').value,
                points:      parseInt(document.getElementById('scorePoints').value),
                maxPoints:   parseInt(document.getElementById('scoreMaxPoints').value),
                comments:    document.getElementById('scoreComments').value,
                scoreMonth:  month,
                scoreYear:   year,
                isPeerReview: document.getElementById('scorePeerReview').checked
            });
            showToast('Score submitted successfully!', 'success');
            scoreModal.style.display = 'none';
            e.target.reset();
        } catch (err) {
            showToast(err.message, 'error');
        }
    });

    loadDashboard();
});
