/* auth.js */
document.addEventListener('DOMContentLoaded', () => {
    if (Auth.isLoggedIn()) { window.location.href = '/dashboard'; return; }

    const tabLogin    = document.getElementById('tabLogin');
    const tabRegister = document.getElementById('tabRegister');
    const loginForm   = document.getElementById('loginForm');
    const registerForm= document.getElementById('registerForm');

    function showLogin() {
        loginForm.style.display = 'flex'; loginForm.classList.add('active');
        registerForm.style.display = 'none'; registerForm.classList.remove('active');
        tabLogin.classList.add('active'); tabRegister.classList.remove('active');
    }
    function showRegister() {
        registerForm.style.display = 'flex'; registerForm.classList.add('active');
        loginForm.style.display = 'none'; loginForm.classList.remove('active');
        tabRegister.classList.add('active'); tabLogin.classList.remove('active');
    }

    tabLogin.addEventListener('click', showLogin);
    tabRegister.addEventListener('click', showRegister);
    document.getElementById('switchToRegister')?.addEventListener('click', (e) => { e.preventDefault(); showRegister(); });
    document.getElementById('switchToLogin')?.addEventListener('click', (e) => { e.preventDefault(); showLogin(); });

    // Password toggles
    document.querySelectorAll('.pwd-toggle').forEach(btn => {
        btn.addEventListener('click', () => {
            const input = document.getElementById(btn.dataset.target);
            input.type = input.type === 'password' ? 'text' : 'password';
            btn.textContent = input.type === 'password' ? '👁' : '🙈';
        });
    });

    // LOGIN
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const btn  = document.getElementById('loginBtn');
        const errEl = document.getElementById('loginError');
        errEl.style.display = 'none';
        btn.querySelector('.btn-text').textContent = 'Signing in…';
        btn.disabled = true;
        try {
            const res = await api.login({
                email:    document.getElementById('loginEmail').value.trim(),
                password: document.getElementById('loginPassword').value
            });
            Auth.setSession(res.data);
            window.location.href = '/dashboard';
        } catch (err) {
            errEl.textContent = err.message || 'Login failed. Check credentials.';
            errEl.style.display = 'block';
        } finally {
            btn.querySelector('.btn-text').textContent = 'Sign In';
            btn.disabled = false;
        }
    });

    // REGISTER
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const btn   = document.getElementById('registerBtn');
        const errEl  = document.getElementById('registerError');
        errEl.style.display = 'none';
        btn.querySelector('.btn-text').textContent = 'Creating account…';
        btn.disabled = true;
        const companyIdVal = document.getElementById('regCompanyId').value;
        try {
            const res = await api.register({
                fullName:   document.getElementById('regFullName').value.trim(),
                username:   document.getElementById('regUsername').value.trim(),
                email:      document.getElementById('regEmail').value.trim(),
                password:   document.getElementById('regPassword').value,
                role:       document.getElementById('regRole').value,
                jobTitle:   document.getElementById('regJobTitle').value.trim(),
                department: document.getElementById('regDepartment').value.trim(),
                companyId:  companyIdVal ? parseInt(companyIdVal) : null
            });
            Auth.setSession(res.data);
            window.location.href = '/dashboard';
        } catch (err) {
            errEl.textContent = err.message || 'Registration failed. Please try again.';
            errEl.style.display = 'block';
        } finally {
            btn.querySelector('.btn-text').textContent = 'Create Account';
            btn.disabled = false;
        }
    });
});
