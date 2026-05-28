/* jobs.js */
document.addEventListener('DOMContentLoaded', () => {
    Auth.requireAuth();
    initSidebar();

    const user = Auth.getUser();
    let myCredits = 0;
    let selectedJobId = null;

    // Load credits
    api.getMe().then(res => {
        myCredits = res.data?.totalCredits || 0;
        document.getElementById('headerCredits').textContent = myCredits;
        document.getElementById('noticeCredits').textContent = myCredits;

        const role = user?.role || '';
        if (role === 'ROLE_COMPANY' || role === 'ROLE_ADMIN') {
            document.getElementById('postJobBtn').style.display = 'block';
            document.getElementById('manageJobsTab').style.display = 'block';
        }
    }).catch(() => {});

    // Tab switching
    document.querySelectorAll('.job-tab').forEach(tab => {
        tab.addEventListener('click', () => {
            document.querySelectorAll('.job-tab').forEach(t => t.classList.remove('active'));
            document.querySelectorAll('.jobs-panel').forEach(p => { p.classList.remove('active'); p.style.display = 'none'; });
            tab.classList.add('active');
            const panel = document.getElementById(`tab-${tab.dataset.tab}`);
            if (panel) { panel.classList.add('active'); panel.style.display = 'block'; }
            if (tab.dataset.tab === 'eligible') loadEligibleJobs();
            if (tab.dataset.tab === 'all') loadAllJobs();
            if (tab.dataset.tab === 'applied') loadMyApplications();
        });
    });

    // ---- ELIGIBLE JOBS ----
    async function loadEligibleJobs(query = '', page = 0) {
        const grid = document.getElementById('eligibleJobsGrid');
        grid.innerHTML = '<div class="empty-state">Loading…</div>';
        try {
            const res = await api.eligibleJobs(query, page);
            renderJobGrid(grid, res.data?.content || [], true);
            renderPagination('eligiblePagination', res.data?.totalPages || 1, page, p => loadEligibleJobs(query, p));
        } catch (err) {
            if (err.message.includes('403') || err.message.includes('denied')) {
                // Fallback for non-employees
                const res = await api.searchAllJobs(query, page);
                renderJobGrid(grid, res.data?.content || [], false);
            } else {
                grid.innerHTML = `<div class="empty-state">${err.message}</div>`;
            }
        }
    }

    async function loadAllJobs(query = '', page = 0) {
        const grid = document.getElementById('allJobsGrid');
        grid.innerHTML = '<div class="empty-state">Loading…</div>';
        try {
            const res = await api.searchAllJobs(query, page);
            renderJobGrid(grid, res.data?.content || [], false);
            renderPagination('allJobsPagination', res.data?.totalPages || 1, page, p => loadAllJobs(query, p));
        } catch (err) {
            grid.innerHTML = `<div class="empty-state">${err.message}</div>`;
        }
    }

    function renderJobGrid(container, jobs, showEligibility) {
        if (!jobs.length) { container.innerHTML = '<div class="empty-state">No jobs found.</div>'; return; }
        container.innerHTML = jobs.map(job => {
            const eligible = !showEligibility || myCredits >= job.minCreditsRequired;
            return `
            <div class="job-card" data-jobid="${job.id}">
                <div class="job-card-header">
                    <div class="job-company-logo">
                        ${job.companyLogoUrl ? `<img src="${job.companyLogoUrl}" alt="">` : '🏢'}
                    </div>
                    <div class="job-title-wrap">
                        <div class="job-title">${job.title}</div>
                        <div class="job-company-name">${job.companyName}${job.companyIsVerified ? ' <span class="job-verified">✓</span>' : ''}</div>
                    </div>
                </div>
                <div class="job-tags">
                    ${job.jobType ? `<span class="job-tag type">${job.jobType}</span>` : ''}
                    ${job.location ? `<span class="job-tag location">📍 ${job.location}</span>` : ''}
                    ${job.minCreditsRequired > 0 ? `<span class="job-tag credits">✦ ${job.minCreditsRequired} credits needed</span>` : '<span class="job-tag credits">✦ Open to all</span>'}
                </div>
                ${job.salaryRange ? `<div class="job-salary">💰 ${job.salaryRange}</div>` : ''}
                <p style="font-size:0.8rem;color:var(--text-2);line-height:1.5">${(job.description || '').slice(0, 120)}…</p>
                <div class="job-footer">
                    <span class="job-date">${timeAgo(job.createdAt)}</span>
                    ${job.alreadyApplied
                        ? `<span class="job-applied-badge">✓ Applied</span>`
                        : eligible
                            ? `<button class="btn-primary apply-btn" data-jobid="${job.id}" data-jobtitle="${job.title}" data-company="${job.companyName}">Apply ↗</button>`
                            : `<span style="font-size:0.75rem;color:var(--text-3)">Need ${job.minCreditsRequired} credits</span>`
                    }
                </div>
            </div>`;
        }).join('');

        container.querySelectorAll('.apply-btn').forEach(btn => {
            btn.addEventListener('click', () => openApplyModal(btn.dataset.jobid, btn.dataset.jobtitle, btn.dataset.company));
        });
    }

    function renderPagination(containerId, totalPages, currentP, onPage) {
        const el = document.getElementById(containerId);
        if (!el || totalPages <= 1) { if (el) el.innerHTML = ''; return; }
        let html = '';
        if (currentP > 0) html += `<button class="page-btn" data-page="${currentP - 1}">‹</button>`;
        for (let i = Math.max(0, currentP - 2); i <= Math.min(totalPages - 1, currentP + 2); i++) {
            html += `<button class="page-btn ${i === currentP ? 'active' : ''}" data-page="${i}">${i + 1}</button>`;
        }
        if (currentP < totalPages - 1) html += `<button class="page-btn" data-page="${currentP + 1}">›</button>`;
        el.innerHTML = html;
        el.querySelectorAll('.page-btn').forEach(btn => btn.addEventListener('click', () => onPage(parseInt(btn.dataset.page))));
    }

    // ---- APPLICATIONS ----
    async function loadMyApplications() {
        const list = document.getElementById('applicationsList');
        list.innerHTML = '<div class="empty-state">Loading…</div>';
        try {
            const res = await api.myApplications();
            const apps = res.data || [];
            if (!apps.length) { list.innerHTML = '<div class="empty-state">You haven\'t applied to any jobs yet.</div>'; return; }
            list.innerHTML = apps.map(app => `
            <div class="application-card">
                <div style="flex:1">
                    <div style="font-family:var(--font-display);font-weight:700;font-size:0.95rem">${app.applicantName}</div>
                    <div style="font-size:0.78rem;color:var(--text-3);margin-top:2px">Applied ${timeAgo(app.appliedAt)}</div>
                    ${app.companyNotes ? `<div style="font-size:0.8rem;color:var(--text-2);margin-top:8px;padding:8px;background:var(--bg-3);border-radius:var(--radius)">${app.companyNotes}</div>` : ''}
                </div>
                <span class="app-status ${app.status}">${app.status}</span>
            </div>`).join('');
        } catch (err) {
            list.innerHTML = `<div class="empty-state">${err.message}</div>`;
        }
    }

    // ---- APPLY MODAL ----
    function openApplyModal(jobId, title, company) {
        selectedJobId = jobId;
        document.getElementById('applyJobId').value = jobId;
        document.getElementById('applyModalTitle').textContent = `Apply – ${title}`;
        document.getElementById('applyJobBanner').innerHTML = `
            <div>
                <div style="font-family:var(--font-display);font-weight:700">${title}</div>
                <div style="font-size:0.8rem;color:var(--text-2)">${company}</div>
            </div>`;
        document.getElementById('applyModal').style.display = 'flex';
    }

    document.getElementById('closeApplyModal')?.addEventListener('click', () => document.getElementById('applyModal').style.display = 'none');
    document.getElementById('cancelApplyModal')?.addEventListener('click', () => document.getElementById('applyModal').style.display = 'none');

    document.getElementById('applyForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const jobId       = document.getElementById('applyJobId').value;
        const coverLetter = document.getElementById('applyCoverLetter').value;
        try {
            await api.applyJob(jobId, { coverLetter });
            showToast('Application submitted successfully!', 'success');
            document.getElementById('applyModal').style.display = 'none';
            document.getElementById('applyCoverLetter').value = '';
            loadEligibleJobs();
        } catch (err) {
            showToast(err.message, 'error');
        }
    });

    // ---- POST JOB ----
    document.getElementById('postJobBtn')?.addEventListener('click', () => {
        document.getElementById('postJobModal').style.display = 'flex';
    });
    document.getElementById('closePostJobModal')?.addEventListener('click', () => document.getElementById('postJobModal').style.display = 'none');
    document.getElementById('cancelPostJob')?.addEventListener('click', () => document.getElementById('postJobModal').style.display = 'none');

    document.getElementById('postJobForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const companyId = document.getElementById('postJobCompanyId').value || 1;
        try {
            await api.postJob(companyId, {
                title:              document.getElementById('jobTitle').value,
                description:        document.getElementById('jobDesc').value,
                requirements:       document.getElementById('jobRequirements').value,
                salaryRange:        document.getElementById('jobSalary').value,
                location:           document.getElementById('jobLocation').value,
                jobType:            document.getElementById('jobType').value,
                minCreditsRequired: parseInt(document.getElementById('jobMinCredits').value || '0')
            });
            showToast('Job posted successfully!', 'success');
            document.getElementById('postJobModal').style.display = 'none';
            e.target.reset();
        } catch (err) {
            showToast(err.message, 'error');
        }
    });

    // Search
    document.getElementById('jobSearchBtn')?.addEventListener('click', () => {
        loadEligibleJobs(document.getElementById('jobSearchInput').value.trim());
    });
    document.getElementById('jobResetBtn')?.addEventListener('click', () => {
        document.getElementById('jobSearchInput').value = '';
        loadEligibleJobs();
    });
    document.getElementById('allJobSearchBtn')?.addEventListener('click', () => {
        loadAllJobs(document.getElementById('allJobSearch').value.trim());
    });
    document.getElementById('jobSearchInput')?.addEventListener('keydown', e => {
        if (e.key === 'Enter') loadEligibleJobs(e.target.value.trim());
    });

    loadEligibleJobs();
});
