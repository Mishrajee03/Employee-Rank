/* leaderboard.js */
document.addEventListener('DOMContentLoaded', () => {
    Auth.requireAuth();
    initSidebar();

    let currentPage = 0;
    let searchQuery = '';

    // Tabs
    document.querySelectorAll('.lb-tab').forEach(tab => {
        tab.addEventListener('click', () => {
            document.querySelectorAll('.lb-tab').forEach(t => t.classList.remove('active'));
            document.querySelectorAll('.lb-panel').forEach(p => { p.classList.remove('active'); p.style.display = 'none'; });
            tab.classList.add('active');
            const panel = document.getElementById(`tab-${tab.dataset.tab}`);
            if (panel) { panel.classList.add('active'); panel.style.display = 'block'; }
            if (tab.dataset.tab === 'global') loadGlobal(0);
            if (tab.dataset.tab === 'company') loadCompanies('');
        });
    });

    // Load current user credits
    api.getMe().then(res => {
        document.getElementById('headerCredits').textContent = res.data?.totalCredits ?? 0;
    }).catch(() => {});

    // ---- GLOBAL LEADERBOARD ----
    async function loadGlobal(page = 0, query = '') {
        const tbody = document.getElementById('lbTableBody');
        tbody.innerHTML = '<tr><td colspan="7" class="empty-state">Loading…</td></tr>';
        try {
            let res;
            if (query) {
                res = await api.searchProfiles(query, page);
                renderSearchResults(res.data);
            } else {
                res = await api.globalLeaderboard(page);
                renderLeaderboard(res.data);
            }
        } catch (err) {
            tbody.innerHTML = `<tr><td colspan="7" class="empty-state">${err.message}</td></tr>`;
        }
    }

    function renderLeaderboard(data) {
        if (!data) return;
        const entries = data.content || [];
        const podium  = document.getElementById('podiumContainer');
        const tbody   = document.getElementById('lbTableBody');

        // Podium (top 3)
        podium.innerHTML = '';
        const top3order = entries.length >= 3 ? [entries[1], entries[0], entries[2]] : entries;
        const medalsOrder = entries.length >= 3 ? [2, 1, 3] : [1, 2, 3];
        const heights = entries.length >= 3 ? ['80px', '120px', '60px'] : ['100px'];

        top3order.forEach((u, i) => {
            if (!u) return;
            const initials = (u.fullName || u.username || '?')[0].toUpperCase();
            const rankEmoji = ['🥈', '🥇', '🥉'][i];
            podium.innerHTML += `
            <div class="podium-item">
                <div class="podium-avatar-wrap">
                    <div class="podium-avatar">${u.profilePicture ? `<img src="${u.profilePicture}" alt="">` : initials}</div>
                    <span class="podium-rank-badge">${rankEmoji}</span>
                </div>
                <span class="podium-name">${u.fullName || u.username}</span>
                <span class="podium-credits">✦ ${u.totalCredits} credits</span>
                <div class="podium-block" style="height:${heights[i]}">${medalsOrder[i]}</div>
            </div>`;
        });

        // Table (skip top 3)
        if (!entries.length) {
            tbody.innerHTML = '<tr><td colspan="7" class="empty-state">No entries yet.</td></tr>';
            return;
        }
        tbody.innerHTML = entries.slice(3).map((u, i) => {
            const rank = data.pageable ? data.pageable.pageNumber * data.pageable.pageSize + i + 4 : i + 4;
            const initials = (u.fullName || u.username || '?')[0].toUpperCase();
            const badgesHtml = (u.badges || []).slice(0, 3).map(b =>
                `<span title="${b.badgeName}">${badgeEmoji(b.badgeType)}</span>`).join('');
            return `
            <tr>
                <td><span class="rank-num">${rank}</span></td>
                <td>
                    <div class="user-cell">
                        <div class="user-cell-avatar">${u.profilePicture ? `<img src="${u.profilePicture}">` : initials}</div>
                        <div class="user-cell-info">
                            <span class="user-cell-name">${u.fullName || u.username}</span>
                            <span class="user-cell-sub">@${u.username}</span>
                        </div>
                    </div>
                </td>
                <td><span style="color:var(--text-2)">${u.companyName || '—'}</span></td>
                <td><span style="color:var(--text-2)">${u.jobTitle || '—'}</span></td>
                <td><span class="credits-num">✦ ${u.totalCredits}</span></td>
                <td style="font-size:1.1rem">${badgesHtml || '—'}</td>
                <td><a href="/public/${u.username}" class="btn-sm" style="text-decoration:none">View</a></td>
            </tr>`;
        }).join('');

        // Pagination
        renderPagination('lbPagination', data.totalPages || 1, data.pageable?.pageNumber || 0, (p) => loadGlobal(p, searchQuery));
    }

    function renderSearchResults(data) {
        const tbody = document.getElementById('lbTableBody');
        document.getElementById('podiumContainer').innerHTML = '';
        const entries = data?.content || [];
        if (!entries.length) { tbody.innerHTML = '<tr><td colspan="7" class="empty-state">No results found.</td></tr>'; return; }
        tbody.innerHTML = entries.map((u, i) => {
            const initials = (u.fullName || u.username || '?')[0].toUpperCase();
            return `
            <tr>
                <td><span class="rank-num">${i + 1}</span></td>
                <td><div class="user-cell">
                    <div class="user-cell-avatar">${u.profilePicture ? `<img src="${u.profilePicture}">` : initials}</div>
                    <div class="user-cell-info"><span class="user-cell-name">${u.fullName || u.username}</span><span class="user-cell-sub">@${u.username}</span></div>
                </div></td>
                <td>${u.companyName || '—'}</td>
                <td>${u.jobTitle || '—'}</td>
                <td><span class="credits-num">✦ ${u.totalCredits}</span></td>
                <td>${(u.badges || []).slice(0,3).map(b => badgeEmoji(b.badgeType)).join('')}</td>
                <td><a href="/public/${u.username}" class="btn-sm" style="text-decoration:none">View</a></td>
            </tr>`;
        }).join('');
    }

    function renderPagination(containerId, totalPages, currentP, onPage) {
        const el = document.getElementById(containerId);
        if (!el) return;
        if (totalPages <= 1) { el.innerHTML = ''; return; }
        let html = '';
        if (currentP > 0) html += `<button class="page-btn" data-page="${currentP - 1}">‹</button>`;
        for (let i = Math.max(0, currentP - 2); i <= Math.min(totalPages - 1, currentP + 2); i++) {
            html += `<button class="page-btn ${i === currentP ? 'active' : ''}" data-page="${i}">${i + 1}</button>`;
        }
        if (currentP < totalPages - 1) html += `<button class="page-btn" data-page="${currentP + 1}">›</button>`;
        el.innerHTML = html;
        el.querySelectorAll('.page-btn').forEach(btn => btn.addEventListener('click', () => onPage(parseInt(btn.dataset.page))));
    }

    // Search
    document.getElementById('lbSearchBtn')?.addEventListener('click', () => {
        searchQuery = document.getElementById('lbSearchInput').value.trim();
        loadGlobal(0, searchQuery);
    });
    document.getElementById('lbResetBtn')?.addEventListener('click', () => {
        searchQuery = '';
        document.getElementById('lbSearchInput').value = '';
        loadGlobal(0);
    });
    document.getElementById('lbSearchInput')?.addEventListener('keydown', e => {
        if (e.key === 'Enter') { searchQuery = e.target.value.trim(); loadGlobal(0, searchQuery); }
    });

    // ---- MONTHLY RESULTS ----
    populateMonthYear('monthlyMonth', 'monthlyYear');
    document.getElementById('loadMonthlyBtn')?.addEventListener('click', async () => {
        const cid   = document.getElementById('monthlyCompanyId').value;
        const month = parseInt(document.getElementById('monthlyMonth').value);
        const year  = parseInt(document.getElementById('monthlyYear').value);
        if (!cid) { showToast('Enter a company ID', 'error'); return; }
        try {
            const res = await api.companyLeaderboard(cid, month, year);
            renderMonthlyLeaderboard(res.data);
        } catch (err) { showToast(err.message, 'error'); }
    });

    function renderMonthlyLeaderboard(data) {
        const container = document.getElementById('monthlyLeaderboardContainer');
        const entries = data?.entries || [];
        if (!entries.length) { container.innerHTML = '<div class="empty-state">No results published for this period.</div>'; return; }
        container.innerHTML = `
        <h3 style="font-family:var(--font-display);margin-bottom:16px">${formatMonth(data.month, data.year)} Results</h3>
        <div style="overflow-x:auto">
        <table class="monthly-result-table">
            <thead><tr><th>Rank</th><th>Employee</th><th>Department</th><th>Score %</th><th>Grade</th><th>Credits</th><th>Performance</th></tr></thead>
            <tbody>${entries.map(e => `
            <tr>
                <td><strong class="${e.rank <= 3 ? `rank-${e.rank}` : ''}">#${e.rank}</strong></td>
                <td>
                    <div class="user-cell">
                        <div class="user-cell-avatar">${(e.fullName || '?')[0]}</div>
                        <div class="user-cell-info">
                            <span class="user-cell-name">${e.fullName}</span>
                            <span class="user-cell-sub">${e.jobTitle || ''}</span>
                        </div>
                    </div>
                </td>
                <td>${e.department || '—'}</td>
                <td><strong style="color:var(--accent)">${e.percentageScore?.toFixed(1)}%</strong></td>
                <td><span class="grade-pill ${gradeClass(e.grade)}">${e.grade}</span></td>
                <td><span style="color:var(--accent)">+${e.creditsEarned}</span></td>
                <td><span class="perf-badge ${perfBadgeClass(e.performanceCategory)}">${e.performanceCategory}</span></td>
            </tr>`).join('')}
            </tbody>
        </table></div>`;
    }

    // ---- COMPANIES ----
    async function loadCompanies(query) {
        const grid = document.getElementById('companyGrid');
        grid.innerHTML = '<div class="empty-state">Loading…</div>';
        try {
            const res = await api.searchCompanies(query);
            const companies = res.data?.content || [];
            if (!companies.length) { grid.innerHTML = '<div class="empty-state">No companies found.</div>'; return; }
            grid.innerHTML = companies.map(c => `
            <div class="company-card">
                <div class="company-name">${c.name}</div>
                <div class="company-industry">${c.industry || 'Industry not listed'}</div>
                <p style="font-size:0.8rem;color:var(--text-3);margin-top:8px">${c.description ? c.description.slice(0, 100) + (c.description.length > 100 ? '…' : '') : ''}</p>
                <div class="company-meta">
                    ${c.isVerified ? '<span class="company-tag verified">✓ Verified</span>' : ''}
                    ${c.isHiring ? '<span class="company-tag hiring">Hiring</span>' : ''}
                    ${c.location ? `<span class="company-tag">📍 ${c.location}</span>` : ''}
                    ${c.companySize ? `<span class="company-tag">👥 ${c.companySize}</span>` : ''}
                    <span class="company-tag">Min ${c.minCreditThreshold} credits</span>
                </div>
            </div>`).join('');
        } catch (err) {
            grid.innerHTML = `<div class="empty-state">${err.message}</div>`;
        }
    }
    document.getElementById('companySearchBtn')?.addEventListener('click', () => {
        loadCompanies(document.getElementById('companySearchInput').value.trim());
    });
    document.getElementById('companySearchInput')?.addEventListener('keydown', e => {
        if (e.key === 'Enter') loadCompanies(e.target.value.trim());
    });

    // Initial load
    loadGlobal(0);
});
