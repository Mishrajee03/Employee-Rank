/* ============================================================
   api.js — centralized API client + auth helpers
   ============================================================ */
const API_BASE = '/api';

const Auth = {
    getToken:        () => localStorage.getItem('accessToken'),
    getRefreshToken: () => localStorage.getItem('refreshToken'),
    getUser:         () => { try { return JSON.parse(localStorage.getItem('user') || 'null'); } catch { return null; } },
    setSession(data) {
        localStorage.setItem('accessToken',  data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);
        localStorage.setItem('user', JSON.stringify({
            id: data.userId, username: data.username,
            email: data.email, fullName: data.fullName,
            role: data.role, profilePicture: data.profilePicture
        }));
    },
    clearSession() {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
    },
    isLoggedIn: () => !!localStorage.getItem('accessToken'),
    redirectToLogin() { window.location.href = '/login'; },
    requireAuth() { if (!Auth.isLoggedIn()) Auth.redirectToLogin(); }
};

async function apiFetch(path, options = {}) {
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    const token = Auth.getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;

    let res = await fetch(API_BASE + path, { ...options, headers });

    // Try refresh if 401
    if (res.status === 401 && Auth.getRefreshToken()) {
        const refreshRes = await fetch(API_BASE + '/auth/refresh', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken: Auth.getRefreshToken() })
        });
        if (refreshRes.ok) {
            const refreshData = await refreshRes.json();
            Auth.setSession(refreshData.data);
            headers['Authorization'] = `Bearer ${Auth.getToken()}`;
            res = await fetch(API_BASE + path, { ...options, headers });
        } else {
            Auth.clearSession();
            Auth.redirectToLogin();
            return;
        }
    }

    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.error || data.message || `HTTP ${res.status}`);
    return data;
}

const api = {
    // Auth
    login: (body)    => apiFetch('/auth/login',    { method: 'POST', body: JSON.stringify(body) }),
    register: (body) => apiFetch('/auth/register', { method: 'POST', body: JSON.stringify(body) }),
    logout: ()       => apiFetch('/auth/logout',   { method: 'POST' }),

    // Users
    getMe:          ()       => apiFetch('/users/me'),
    updateMe:       (body)   => apiFetch('/users/me', { method: 'PUT', body: JSON.stringify(body) }),
    getPublicProfile: (username) => apiFetch(`/public/profile/${username}`),
    searchProfiles: (q, page = 0) => apiFetch(`/public/search?query=${encodeURIComponent(q)}&page=${page}&size=12`),

    // Scores
    addScore:      (body)  => apiFetch('/scores', { method: 'POST', body: JSON.stringify(body) }),
    getMyScores:   (m, y)  => apiFetch(`/scores/my-scores${m ? `?month=${m}&year=${y}` : ''}`),
    getMySummary:  (m, y)  => apiFetch(`/scores/my-summary?month=${m}&year=${y}`),
    getEmpScores:  (id, m, y) => apiFetch(`/scores/employee/${id}${m ? `?month=${m}&year=${y}` : ''}`),
    getEmpSummary: (id, m, y) => apiFetch(`/scores/employee/${id}/summary?month=${m}&year=${y}`),

    // Monthly Results
    getMyResults:   ()          => apiFetch('/results/my-results'),
    getEmpResults:  (id)        => apiFetch(`/results/employee/${id}`),
    generateResult: (id, m, y)  => apiFetch(`/results/generate/${id}?month=${m}&year=${y}`, { method: 'POST' }),
    publishResult:  (id, body)  => apiFetch(`/results/${id}/publish`, { method: 'POST', body: JSON.stringify(body) }),
    companyLeaderboard: (cid, m, y) => apiFetch(`/results/company/${cid}/leaderboard?month=${m}&year=${y}`),
    processResults: (m, y)      => apiFetch(`/results/process?month=${m}&year=${y}`, { method: 'POST' }),

    // Leaderboard
    globalLeaderboard: (page = 0) => apiFetch(`/leaderboard/global?page=${page}&size=20`),
    companyLeaderboardUsers: (cid) => apiFetch(`/leaderboard/company/${cid}`),

    // Companies
    createCompany:  (body)   => apiFetch('/companies', { method: 'POST', body: JSON.stringify(body) }),
    getCompany:     (id)     => apiFetch(`/companies/${id}`),
    updateCompany:  (id, b)  => apiFetch(`/companies/${id}`, { method: 'PUT', body: JSON.stringify(b) }),
    searchCompanies: (q = '', page = 0) => apiFetch(`/companies/search?query=${encodeURIComponent(q)}&page=${page}&size=12`),
    hiringCompanies: () => apiFetch('/companies/hiring'),

    // Jobs
    searchAllJobs:  (q = '', page = 0) => apiFetch(`/jobs/search?query=${encodeURIComponent(q)}&page=${page}&size=10`),
    eligibleJobs:   (q = '', page = 0) => apiFetch(`/jobs/eligible?query=${encodeURIComponent(q)}&page=${page}&size=10`),
    applyJob:       (id, body)   => apiFetch(`/jobs/${id}/apply`, { method: 'POST', body: JSON.stringify(body) }),
    myApplications: ()           => apiFetch('/jobs/my-applications'),
    jobApplications:(id)         => apiFetch(`/jobs/${id}/applications`),
    postJob:        (cid, body)  => apiFetch(`/jobs/company/${cid}`, { method: 'POST', body: JSON.stringify(body) }),
    updateAppStatus:(id, status, notes) => apiFetch(`/jobs/applications/${id}/status`, {
        method: 'PATCH', body: JSON.stringify({ status, notes })
    }),
};

/* ---- TOAST ---- */
function showToast(message, type = 'info', duration = 3500) {
    const container = document.getElementById('toastContainer');
    if (!container) return;
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    container.appendChild(toast);
    setTimeout(() => { toast.style.opacity = '0'; toast.style.transform = 'translateY(10px)'; setTimeout(() => toast.remove(), 300); }, duration);
}

/* ---- SIDEBAR INIT ---- */
function initSidebar() {
    const user = Auth.getUser();
    if (!user) return;
    const nameEl   = document.getElementById('sidebarName');
    const roleEl   = document.getElementById('sidebarRole');
    const avatarEl = document.getElementById('sidebarAvatar');
    if (nameEl)   nameEl.textContent = user.fullName || user.username;
    if (roleEl)   roleEl.textContent = (user.role || '').replace('ROLE_', '');
    if (avatarEl) avatarEl.textContent = (user.fullName || user.username || '?')[0].toUpperCase();

    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', async () => {
            try { await api.logout(); } catch {}
            Auth.clearSession();
            Auth.redirectToLogin();
        });
    }

    const toggle = document.getElementById('sidebarToggle');
    const sidebar = document.getElementById('sidebar');
    if (toggle && sidebar) {
        toggle.addEventListener('click', () => sidebar.classList.toggle('open'));
        document.addEventListener('click', e => {
            if (!sidebar.contains(e.target) && !toggle.contains(e.target)) {
                sidebar.classList.remove('open');
            }
        });
    }
}

/* ---- MONTH/YEAR SELECTORS ---- */
const MONTHS = ['January','February','March','April','May','June','July','August','September','October','November','December'];
function populateMonthYear(monthSelId, yearSelId, defaultToCurrentMonth = true) {
    const now = new Date();
    const monthSel = document.getElementById(monthSelId);
    const yearSel  = document.getElementById(yearSelId);
    if (!monthSel || !yearSel) return;
    monthSel.innerHTML = MONTHS.map((m, i) =>
        `<option value="${i+1}" ${defaultToCurrentMonth && i+1 === now.getMonth()+1 ? 'selected' : ''}>${m}</option>`
    ).join('');
    for (let y = now.getFullYear(); y >= 2020; y--) {
        yearSel.innerHTML += `<option value="${y}" ${y === now.getFullYear() ? 'selected' : ''}>${y}</option>`;
    }
}

/* ---- GRADE HELPERS ---- */
function gradeClass(grade) {
    if (!grade) return '';
    if (grade.startsWith('A')) return 'grade-A';
    if (grade.startsWith('B')) return 'grade-B';
    if (grade.startsWith('C')) return 'grade-C';
    if (grade === 'D') return 'grade-D';
    return 'grade-F';
}

function badgeEmoji(type) {
    return { BRONZE: '🥉', SILVER: '🥈', GOLD: '🥇', PLATINUM: '💎', DIAMOND: '💠', LEGEND: '⬡' }[type] || '🏅';
}

function perfBadgeClass(cat) {
    const map = { 'Outstanding': 'perf-outstanding', 'Exceeds Expectations': 'perf-exceeds', 'Meets Expectations': 'perf-meets', 'Needs Improvement': 'perf-needs', 'Unsatisfactory': 'perf-unsatisfactory' };
    return map[cat] || '';
}

function formatMonth(m, y) { return `${MONTHS[m-1]} ${y}`; }
function timeAgo(dateStr) {
    const diff = Date.now() - new Date(dateStr).getTime();
    const d = Math.floor(diff / 86400000);
    if (d === 0) return 'Today'; if (d === 1) return 'Yesterday';
    if (d < 30) return `${d} days ago`; if (d < 365) return `${Math.floor(d/30)}mo ago`;
    return `${Math.floor(d/365)}y ago`;
}
