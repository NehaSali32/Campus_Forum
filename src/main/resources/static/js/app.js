// =========================================================================
// Campus Forum - Academic Discussion & College Communication Platform
// =========================================================================

const App = {
    currentUser: null,
    currentTheme: 'light',
    notificationPollingInterval: null,

    // SVG Collegiate Icon Library (Feather/Lucide style, crisp stroke)
    icons: {
        bell: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path></svg>',
        sun: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line><line x1="1" y1="12" x2="3" y2="12"></line><line x1="21" y1="12" x2="23" y2="12"></line><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line></svg>',
        moon: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path></svg>',
        search: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>',
        plus: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>',
        calendar: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>',
        clock: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>',
        mapPin: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>',
        user: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>',
        users: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>',
        book: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path></svg>',
        graduationCap: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><path d="M22 10v6M2 10l10-5 10 5-10 5z"></path><path d="M6 12v5c0 2 2 3 6 3s6-1 6-3v-5"></path></svg>',
        briefcase: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path></svg>',
        shield: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>',
        fileText: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>',
        download: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>',
        externalLink: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path><polyline points="15 3 21 3 21 9"></polyline><line x1="10" y1="14" x2="21" y2="3"></line></svg>',
        thumbsUp: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"></path></svg>',
        messageCircle: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"></path></svg>',
        bookmark: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"></path></svg>',
        bookmarkFilled: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="currentColor" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"></path></svg>',
        pin: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><line x1="12" y1="17" x2="12" y2="22"></line><path d="M5 17h14v-1.76a2 2 0 0 0-1.11-1.79l-1.78-.89V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v7.56l-1.78.89A2 2 0 0 0 5 15.24Z"></path></svg>',
        check: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><polyline points="20 6 9 17 4 12"></polyline></svg>',
        checkCircle: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>',
        alertCircle: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>',
        alertTriangle: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>',
        x: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>',
        filter: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"></polygon></svg>',
        tag: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"></path><line x1="7" y1="7" x2="7.01" y2="7"></line></svg>',
        eye: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>',
        info: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>',
        lock: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>',
        folder: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path></svg>',
        paperclip: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"></path></svg>',
        trendingUp: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"></polyline><polyline points="17 6 23 6 23 12"></polyline></svg>',
        institution: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><line x1="3" y1="21" x2="21" y2="21"></line><line x1="3" y1="10" x2="21" y2="10"></line><polyline points="12 3 2 10 22 10 12 3"></polyline><line x1="6" y1="10" x2="6" y2="21"></line><line x1="10" y1="10" x2="10" y2="21"></line><line x1="14" y1="10" x2="14" y2="21"></line><line x1="18" y1="10" x2="18" y2="21"></line></svg>',
        megaphone: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><path d="M3 11l18-5v12L3 13v-2z"></path><path d="M11.6 16.8a3 3 0 1 1-5.8-1.6"></path></svg>',
        chevronUp: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><polyline points="18 15 12 9 6 15"></polyline></svg>',
        chevronRight: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><polyline points="9 18 15 12 9 6"></polyline></svg>',
        edit: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path></svg>',
        trash: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>',
        logOut: '<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="{c}"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>'
    },

    // Render an SVG icon by name
    icon(name, size = 18, className = '') {
        const template = this.icons[name] || this.icons.info;
        return template.replace(/\{s\}/g, size).replace(/\{c\}/g, className);
    },

    // Return standard author display properties (name, initials, role badges, metadata)
    getAuthorBadges(user) {
        if (!user) return { name: 'Anonymous Member', initials: '?', badges: '', meta: '' };
        
        const name = user.fullName || 'Member';
        const initials = name.split(' ').filter(Boolean).map(n => n[0]).slice(0, 2).join('').toUpperCase() || 'U';
        
        let badges = [];
        let metaParts = [];

        if (user.role === 'ADMIN') {
            badges.push('<span class="badge badge-role admin">Administrator</span>');
        } else if (user.role === 'DEAN') {
            badges.push('<span class="badge badge-dean">Dean</span>');
            badges.push(`<span class="badge-verified">${this.icon('checkCircle', 12)} Academic Official</span>`);
        } else if (user.role === 'HOD') {
            badges.push('<span class="badge badge-hod">Head of Department (HOD)</span>');
            badges.push(`<span class="badge-verified">${this.icon('checkCircle', 12)} Academic Official</span>`);
        } else if (user.role === 'FACULTY') {
            badges.push('<span class="badge badge-role faculty">Faculty</span>');
            badges.push(`<span class="badge-verified">${this.icon('checkCircle', 12)} Verified Faculty</span>`);
        } else if (user.role === 'STUDENT') {
            if (user.designation === 'CLUB_LEADER') {
                badges.push('<span class="badge badge-club-leader">Club Leader</span>');
            }
            badges.push('<span class="badge badge-role student">Student</span>');
        }

        if (user.department && user.department.name) {
            metaParts.push(this.escapeHtml(user.department.name));
        } else if (user.department && user.department.code) {
            metaParts.push(this.escapeHtml(user.department.code));
        }
        if (user.designation && user.designation !== 'STUDENT' && user.designation !== 'CLUB_LEADER') {
            metaParts.push(this.escapeHtml(user.designation));
        }
        if (user.year) {
            metaParts.push(`Year: ${this.escapeHtml(user.year)}`);
        }
        if (user.division) {
            metaParts.push(`Div: ${this.escapeHtml(user.division)}`);
        }

        return {
            name,
            initials,
            badges: badges.join(' '),
            meta: metaParts.join(' • ')
        };
    },

    async init() {
        this.initTheme();
        const user = await this.checkAuth();

        const currentPath = window.location.pathname;
        const isLoginPage = currentPath.includes('login') || currentPath === '/login';

        // Strict Auth Guard: If unauthenticated and not on login page, redirect to login
        if (!user && !isLoginPage) {
            window.location.replace('/login');
            return;
        }

        // If authenticated and on login page, redirect to home
        if (user && isLoginPage) {
            window.location.replace('/');
            return;
        }

        this.renderNavbar();
        this.initGlobalModalHandlers();

        if (user) {
            this.loadNotifications();
            // Poll for notifications every 60 seconds
            if (!this.notificationPollingInterval) {
                this.notificationPollingInterval = setInterval(() => this.loadNotifications(), 60000);
            }
        }
    },

    initTheme() {
        const savedTheme = localStorage.getItem('campus_connect_theme') || 'light';
        this.setTheme(savedTheme);
    },

    setTheme(theme) {
        this.currentTheme = theme;
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem('campus_connect_theme', theme);
        this.updateThemeButton();
    },

    toggleTheme() {
        const newTheme = this.currentTheme === 'light' ? 'dark' : 'light';
        this.setTheme(newTheme);
    },

    updateThemeButton() {
        const btn = document.getElementById('theme-toggle-btn');
        if (btn) {
            if (this.currentTheme === 'dark') {
                btn.innerHTML = `${this.icon('sun', 15)} <span>Light</span>`;
                btn.title = 'Switch to Light Theme';
            } else {
                btn.innerHTML = `${this.icon('moon', 15)} <span>Dark</span>`;
                btn.title = 'Switch to Dark Theme';
            }
        }
    },

    async checkAuth() {
        try {
            const res = await fetch('/api/auth/me');
            if (res.ok) {
                const json = await res.json();
                this.currentUser = json.data;
            } else {
                this.currentUser = null;
            }
        } catch (e) {
            this.currentUser = null;
        }
        return this.currentUser;
    },

    // Unified Modal Open & Close System
    openModal(id) {
        const modal = document.getElementById(id);
        if (!modal) {
            console.warn(`Modal element with ID "${id}" was not found.`);
            return;
        }
        modal.classList.add('active', 'show', 'open');
        document.body.style.overflow = 'hidden';

        // Focus first input field if available
        const firstInput = modal.querySelector('input:not([type="hidden"]), select, textarea');
        if (firstInput) {
            setTimeout(() => firstInput.focus(), 100);
        }
    },

    closeModal(id) {
        const modal = document.getElementById(id);
        if (!modal) return;
        modal.classList.remove('active', 'show', 'open');

        // Check if any other modal is still active
        const hasOpenModals = document.querySelector('.modal-backdrop.active, .modal-backdrop.show, .modal-backdrop.open');
        if (!hasOpenModals) {
            document.body.style.overflow = '';
        }
    },

    initGlobalModalHandlers() {
        // Close modal when clicking directly on the backdrop (outside modal-content)
        document.addEventListener('click', (e) => {
            if (e.target && e.target.classList && e.target.classList.contains('modal-backdrop')) {
                e.target.classList.remove('active', 'show', 'open');
                if (!document.querySelector('.modal-backdrop.active, .modal-backdrop.show, .modal-backdrop.open')) {
                    document.body.style.overflow = '';
                }
            }
            // Also listen for any button with data-dismiss="modal" or .modal-close
            if (e.target.matches('[data-dismiss="modal"]') || e.target.closest('.modal-close') || e.target.matches('.modal-close')) {
                const backdrop = e.target.closest('.modal-backdrop');
                if (backdrop) {
                    backdrop.classList.remove('active', 'show', 'open');
                    if (!document.querySelector('.modal-backdrop.active, .modal-backdrop.show, .modal-backdrop.open')) {
                        document.body.style.overflow = '';
                    }
                }
            }
        });

        // Close on Escape key
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' || e.keyCode === 27) {
                const openModals = document.querySelectorAll('.modal-backdrop.active, .modal-backdrop.show, .modal-backdrop.open');
                if (openModals.length > 0) {
                    const topModal = openModals[openModals.length - 1];
                    topModal.classList.remove('active', 'show', 'open');
                    if (openModals.length === 1) {
                        document.body.style.overflow = '';
                    }
                }
            }
        });
    },

    renderNavbar() {
        const currentPath = window.location.pathname;
        const navBrand = document.querySelector('.nav-brand');
        if (navBrand) {
            navBrand.innerHTML = `
                <div class="brand-icon">
                    CF
                </div>
                <div>
                    <div class="brand-title">Campus Forum</div>
                    <div class="brand-subtitle">Academic Discussion &amp; College Communication Platform</div>
                </div>
            `;
        }

        // Update nav links with role-specific portal
        const navLinks = document.querySelector('.nav-links');
        if (navLinks && this.currentUser) {
            let portalLink = '';
            if (this.currentUser.role === 'STUDENT' || this.currentUser.role === 'CLUB_LEADER' || this.currentUser.role === 'EVENT_LEADER') {
                portalLink = `<li><a href="/student" class="${currentPath === '/student' ? 'active' : ''}">Student Portal</a></li>`;
            } else if (this.currentUser.role === 'FACULTY') {
                portalLink = `<li><a href="/faculty" class="${currentPath === '/faculty' ? 'active' : ''}">Faculty Portal</a></li>`;
            } else if (this.currentUser.role === 'HOD') {
                portalLink = `<li><a href="/faculty?view=hod" class="${currentPath === '/faculty' ? 'active' : ''}">Faculty &amp; HOD Portal</a></li>`;
            } else if (this.currentUser.role === 'DEAN') {
                portalLink = `<li><a href="/faculty?view=dean" class="${currentPath === '/faculty' ? 'active' : ''}">Faculty &amp; Dean Portal</a></li>`;
            } else if (this.currentUser.role === 'ADMIN') {
                portalLink = `<li><a href="/admin" class="${currentPath === '/admin' ? 'active' : ''}">Admin Console</a></li>
                              <li><a href="/faculty" class="${currentPath === '/faculty' ? 'active' : ''}">Faculty Portal</a></li>`;
            }

            navLinks.innerHTML = `
                <li><a href="/" class="${currentPath === '/' ? 'active' : ''}">Home</a></li>
                <li><a href="/notices" class="${currentPath === '/notices' ? 'active' : ''}">Notices</a></li>
                <li><a href="/forum" class="${currentPath === '/forum' || currentPath.includes('topic-detail') ? 'active' : ''}">Discussions</a></li>
                <li><a href="/events" class="${currentPath === '/events' ? 'active' : ''}">Events</a></li>
                <li><a href="/resources" class="${currentPath === '/resources' ? 'active' : ''}">Resources</a></li>
                ${portalLink}
                <li><a href="/about" class="${currentPath === '/about' ? 'active' : ''}">About</a></li>
            `;
        }

        const authContainer = document.getElementById('nav-auth-container');
        if (!authContainer) return;

        const themeBtnHtml = `
            <button id="theme-toggle-btn" class="theme-toggle-btn" onclick="App.toggleTheme()" title="Toggle Light/Dark Theme">
                ${this.currentTheme === 'dark' ? `${this.icon('sun', 15)} <span>Light</span>` : `${this.icon('moon', 15)} <span>Dark</span>`}
            </button>
        `;

        if (this.currentUser) {
            const roleBadgeClass = this.currentUser.role.toLowerCase();
            const identifier = this.currentUser.role === 'STUDENT' 
                ? (this.currentUser.prnRollNo ? `PRN: ${this.currentUser.prnRollNo}` : 'Student')
                : (this.currentUser.employeeId ? `Emp ID: ${this.currentUser.employeeId}` : this.currentUser.role);

            const initials = (this.currentUser.fullName || 'CC')
                .split(' ')
                .map(n => n[0])
                .slice(0, 2)
                .join('')
                .toUpperCase();

            authContainer.innerHTML = `
                <div style="display:flex; align-items:center; gap:12px;">
                    ${themeBtnHtml}
                    
                    <!-- Notification Bell Component -->
                    <div class="notification-wrapper">
                        <button class="notification-btn" onclick="App.toggleNotificationDropdown()" title="Notifications">
                            ${this.icon('bell', 18)}
                            <span class="notification-badge-count" id="nav-notif-count" style="display:none;">0</span>
                        </button>
                        <div class="notification-dropdown" id="nav-notif-dropdown">
                            <div class="notification-header">
                                <span style="font-weight:700;">Notifications</span>
                                <button class="btn btn-sm btn-outline" style="padding:2px 8px; font-size:0.75rem;" onclick="App.markAllNotificationsRead()">Mark all read</button>
                            </div>
                            <div class="notification-list" id="nav-notif-list">
                                <div style="padding:16px; text-align:center; color:var(--muted); font-size:0.85rem;">Loading notifications...</div>
                            </div>
                        </div>
                    </div>

                    <a href="/profile" style="text-decoration:none; display:flex; align-items:center; gap:10px;" title="View Profile">
                        <div style="text-align:right;">
                            <div style="font-weight:700; font-size:0.88rem; color:var(--dark); line-height:1.2;">${this.escapeHtml(this.currentUser.fullName)}</div>
                            <div style="font-size:0.72rem; color:var(--muted); display:flex; align-items:center; gap:4px; justify-content:flex-end; margin-top:2px;">
                                <span class="badge-role ${roleBadgeClass}">${this.currentUser.role}</span>
                                <span>${identifier}</span>
                            </div>
                        </div>
                        <div style="width:34px; height:34px; border-radius:50%; background:var(--navy); color:#ffffff; font-weight:700; font-size:0.8rem; display:flex; align-items:center; justify-content:center; border:2px solid var(--border);">
                            ${initials}
                        </div>
                    </a>

                    <button onclick="App.logout()" class="btn btn-sm btn-outline" style="color:var(--danger); border-color:var(--danger-border); display:flex; align-items:center; gap:4px;" title="Sign out of Campus Forum">
                        ${this.icon('logOut', 14)}
                        <span>Logout</span>
                    </button>
                </div>
            `;
        } else {
            authContainer.innerHTML = `
                <div style="display:flex; align-items:center; gap:10px;">
                    ${themeBtnHtml}
                    <a href="/login" class="btn btn-primary btn-sm">Sign In</a>
                </div>
            `;
        }
    },

    toggleNotificationDropdown() {
        const dropdown = document.getElementById('nav-notif-dropdown');
        if (dropdown) {
            dropdown.classList.toggle('show');
            if (dropdown.classList.contains('show')) {
                this.loadNotifications();
            }
        }
    },

    async loadNotifications() {
        try {
            const res = await fetch('/api/notifications');
            if (!res.ok) return;
            const json = await res.json();
            const data = json.data || {};
            const list = data.notifications || [];
            const unread = data.unreadCount || 0;

            const badge = document.getElementById('nav-notif-count');
            if (badge) {
                if (unread > 0) {
                    badge.innerText = unread;
                    badge.style.display = 'block';
                } else {
                    badge.style.display = 'none';
                }
            }

            const container = document.getElementById('nav-notif-list');
            if (!container) return;

            if (list.length === 0) {
                container.innerHTML = '<div style="padding:20px; text-align:center; color:var(--muted); font-size:0.85rem;">No notifications yet.</div>';
                return;
            }

            container.innerHTML = list.map(n => `
                <div class="notification-item ${n.read ? '' : 'unread'}" onclick="App.markNotificationRead(${n.id})">
                    <div class="notification-item-title">${this.escapeHtml(n.title)}</div>
                    <div style="color:var(--dark-muted); font-size:0.82rem; margin-top:2px;">${this.escapeHtml(n.message)}</div>
                    <div class="notification-item-time" style="margin-top:4px;">${this.formatDate(n.createdAt)}</div>
                </div>
            `).join('');
        } catch (e) {
            console.error('Failed to load notifications', e);
        }
    },

    async markNotificationRead(id) {
        try {
            await fetch(`/api/notifications/${id}/read`, { method: 'PUT' });
            this.loadNotifications();
        } catch (e) {
            console.error(e);
        }
    },

    async markAllNotificationsRead() {
        try {
            await fetch('/api/notifications/read-all', { method: 'PUT' });
            this.loadNotifications();
            this.showToast('All notifications marked as read', 'info');
        } catch (e) {
            console.error(e);
        }
    },

    async logout() {
        try {
            await fetch('/api/auth/logout', { method: 'POST' });
            window.location.replace('/login');
        } catch (e) {
            window.location.replace('/login');
        }
    },

    showToast(message, type = 'info') {
        let container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            container.className = 'toast-container';
            document.body.appendChild(container);
        }

        const iconSvgs = {
            success: this.icon('checkCircle', 18),
            error: this.icon('alertCircle', 18),
            warning: this.icon('alertTriangle', 18),
            info: this.icon('info', 18)
        };

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.innerHTML = `
            <span style="display:flex; align-items:center;">${iconSvgs[type] || iconSvgs.info}</span>
            <div style="flex:1; font-weight:500;">${this.escapeHtml(message)}</div>
            <button onclick="this.parentElement.remove()" style="background:none;border:none;cursor:pointer;color:var(--muted);font-size:1.1rem;display:flex;align-items:center;">&times;</button>
        `;

        container.appendChild(toast);

        setTimeout(() => {
            if (toast.parentElement) {
                toast.style.opacity = '0';
                toast.style.transform = 'translateX(100%)';
                toast.style.transition = 'all 0.3s ease';
                setTimeout(() => toast.remove(), 300);
            }
        }, 3500);
    },

    formatDate(dateStr) {
        if (!dateStr) return '';
        try {
            const d = new Date(dateStr);
            return d.toLocaleDateString('en-US', {
                month: 'short',
                day: 'numeric',
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        } catch (e) {
            return dateStr;
        }
    },

    openModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.add('open', 'show', 'active');
            modal.style.display = 'flex';
            document.body.style.overflow = 'hidden';
            const firstInput = modal.querySelector('input:not([type=hidden]), textarea, select');
            if (firstInput) setTimeout(() => firstInput.focus(), 60);
        } else {
            console.warn(`Modal #${modalId} not found in DOM.`);
        }
    },

    closeModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.remove('open', 'show', 'active');
            modal.style.display = 'none';
            document.body.style.overflow = '';
        }
    },

    escapeHtml(str) {
        if (!str) return '';
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }
};

// Global shortcuts for modal control
window.openModal = (id) => App.openModal(id);
window.closeModal = (id) => App.closeModal(id);

// Close modal when clicking on backdrop
document.addEventListener('click', (e) => {
    if (e.target && (e.target.classList.contains('modal-backdrop') || e.target.classList.contains('modal-overlay'))) {
        App.closeModal(e.target.id);
    }
});

// Close modal with Escape key
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        const openModals = document.querySelectorAll('.modal-backdrop.open, .modal-backdrop.show, .modal-backdrop.active');
        openModals.forEach(m => App.closeModal(m.id));
    }
});

// Close notification dropdown when clicking outside
document.addEventListener('click', (e) => {
    const wrapper = document.querySelector('.notification-wrapper');
    const dropdown = document.getElementById('nav-notif-dropdown');
    if (wrapper && dropdown && !wrapper.contains(e.target)) {
        dropdown.classList.remove('show');
    }
});

// Auto initialize on DOM ready
document.addEventListener('DOMContentLoaded', () => App.init());
