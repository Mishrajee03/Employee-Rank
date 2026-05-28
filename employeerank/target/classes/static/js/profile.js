/* profile.js */
document.addEventListener('DOMContentLoaded', () => {
    Auth.requireAuth();
    initSidebar();

    const user = Auth.getUser();

    async function loadProfile() {
        try {
            const res = await api.getMe();
            const u = res.data;

            document.getElementById('headerCredits').textContent = u.totalCredits ?? 0;

            // Profile card
            const initials = (u.fullName || u.username || '?')[0].toUpperCase();
            document.getElementById('profileAvatar').textContent = initials;
            document.getElementById('profileFullName').textContent = u.fullName || u.username;
            document.getElementById('profileJobTitle').textContent = u.jobTitle || '—';
            document.getElementById('profileDept').textContent = u.department ? `@ ${u.department}` : '';
            document.getElementById('profileCredits').textContent = u.totalCredits ?? 0;

            const toggle = document.getElementById('publicProfileToggle');
            toggle.checked = u.isPublicProfile;

            toggle.addEventListener('change', async () => {
                try {
                    await api.updateMe({ isPublicProfile: toggle.checked });
                    showToast(`Profile is now ${toggle.checked ? 'public' : 'private'}`, 'success');
                } catch (err) { showToast(err.message, 'error'); toggle.checked = !toggle.checked; }
            });

            document.getElementById('publicProfileLink').href = `/public/${u.username}`;

            // Latest grade from results
            try {
                const resultsRes = await api.getMyResults();
                const results = resultsRes.data || [];
                const published = results.filter(r => r.isPublished);
                if (published.length) {
                    const latest = published[0];
                    const gradeBadge = document.getElementById('profileGradeBadge');
                    gradeBadge.textContent = latest.grade;
                    gradeBadge.className = `profile-grade-badge ${gradeClass(latest.grade)}`;
                }
                renderPerfHistory(published);
            } catch {}

            // Links
            const linksEl = document.getElementById('profileLinks');
            linksEl.innerHTML = '';
            if (u.linkedinUrl) linksEl.innerHTML += `<a href="${u.linkedinUrl}" target="_blank" class="profile-link-btn">in LinkedIn</a>`;
            if (u.githubUrl)   linksEl.innerHTML += `<a href="${u.githubUrl}"   target="_blank" class="profile-link-btn">⌥ GitHub</a>`;
            if (u.portfolioUrl) linksEl.innerHTML += `<a href="${u.portfolioUrl}" target="_blank" class="profile-link-btn">↗ Portfolio</a>`;

            // Badges
            const badgeRow = document.getElementById('profileBadgeRow');
            badgeRow.innerHTML = (u.badges || []).slice(0, 8).map(b =>
                `<span class="mini-badge badge-${b.badgeType}" title="${b.badgeName}">${badgeEmoji(b.badgeType)}</span>`
            ).join('');

            // Pre-fill edit form
            document.getElementById('editFullName').value    = u.fullName    || '';
            document.getElementById('editJobTitle').value    = u.jobTitle    || '';
            document.getElementById('editDepartment').value  = u.department  || '';
            document.getElementById('editPhone').value       = u.phone       || '';
            document.getElementById('editBio').value         = u.bio         || '';
            document.getElementById('editSkills').value      = u.skills      || '';
            document.getElementById('editYOE').value         = u.yearsOfExperience || '';
            document.getElementById('editLinkedin').value    = u.linkedinUrl || '';
            document.getElementById('editGithub').value      = u.githubUrl   || '';
            document.getElementById('editPortfolio').value   = u.portfolioUrl || '';

        } catch (err) {
            showToast('Failed to load profile: ' + err.message, 'error');
        }
    }

    function renderPerfHistory(results) {
        const container = document.getElementById('perfHistoryList');
        if (!results.length) {
            container.innerHTML = '<div class="empty-state">No published results yet.</div>';
            return;
        }
        container.innerHTML = results.slice(0, 12).map(r => `
        <div class="perf-history-item">
            <span class="perf-hist-month">${formatMonth(r.resultMonth, r.resultYear)}</span>
            <span class="grade-pill ${gradeClass(r.grade)}" style="width:36px;height:36px">${r.grade}</span>
            <div class="perf-hist-bar">
                <div class="perf-hist-bar-fill" style="width:${r.percentageScore}%"></div>
            </div>
            <span class="perf-hist-pct">${r.percentageScore?.toFixed(1)}%</span>
            <span class="perf-hist-credits" title="Credits earned">+${r.creditsEarned} ✦</span>
        </div>`).join('');
    }

    // Save profile form
    document.getElementById('profileForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const btn = e.target.querySelector('button[type="submit"]');
        btn.textContent = 'Saving…';
        btn.disabled = true;
        try {
            await api.updateMe({
                fullName:         document.getElementById('editFullName').value.trim(),
                jobTitle:         document.getElementById('editJobTitle').value.trim(),
                department:       document.getElementById('editDepartment').value.trim(),
                phone:            document.getElementById('editPhone').value.trim(),
                bio:              document.getElementById('editBio').value.trim(),
                skills:           document.getElementById('editSkills').value.trim(),
                yearsOfExperience: parseInt(document.getElementById('editYOE').value) || null,
                linkedinUrl:      document.getElementById('editLinkedin').value.trim(),
                githubUrl:        document.getElementById('editGithub').value.trim(),
                portfolioUrl:     document.getElementById('editPortfolio').value.trim()
            });
            showToast('Profile updated successfully!', 'success');
            await loadProfile();
        } catch (err) {
            showToast(err.message, 'error');
        } finally {
            btn.textContent = 'Save Changes';
            btn.disabled = false;
        }
    });

    loadProfile();
});
