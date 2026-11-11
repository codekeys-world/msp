// ===== SIDEBAR COLLAPSE =====
function initSidebar() {
  const sidebar = document.getElementById('sidebar');
  const mainContent = document.querySelector('.main-content');
  const toggleBtn = document.getElementById('sidebar-toggle');
  const saved = localStorage.getItem('sidebarCollapsed');

  if (saved === 'true') {
    sidebar.classList.add('collapsed');
    if (mainContent) mainContent.classList.add('sidebar-collapsed');
  }

  if (toggleBtn) {
    toggleBtn.addEventListener('click', function() {
      sidebar.classList.toggle('collapsed');
      if (mainContent) mainContent.classList.toggle('sidebar-collapsed');
      localStorage.setItem('sidebarCollapsed', sidebar.classList.contains('collapsed'));
    });
  }

  // Mobile toggle
  const mobileToggle = document.getElementById('mobile-toggle');
  if (mobileToggle) {
    mobileToggle.addEventListener('click', function() {
      sidebar.classList.toggle('mobile-show');
    });
  }
}

// ===== USER DROPDOWN =====
function initUserDropdown() {
  const btn = document.getElementById('user-dropdown-btn');
  const menu = document.getElementById('user-dropdown-menu');

  if (btn && menu) {
    btn.addEventListener('click', function(e) {
      e.stopPropagation();
      menu.classList.toggle('show');
    });

    document.addEventListener('click', function(e) {
      if (!btn.contains(e.target) && !menu.contains(e.target)) {
        menu.classList.remove('show');
      }
    });
  }
}

// ===== SEARCH FILTER =====
function initSearchFilter() {
  const toggleBtns = document.querySelectorAll('.search-filter-header');
  toggleBtns.forEach(function(btn) {
    btn.addEventListener('click', function() {
      const body = this.nextElementSibling;
      body.classList.toggle('show');
      const icon = this.querySelector('.toggle-icon');
      if (icon) {
        icon.style.transform = body.classList.contains('show') ? 'rotate(180deg)' : '';
      }
    });
  });

  // Clear filters
  const clearBtns = document.querySelectorAll('.clear-filters-btn');
  clearBtns.forEach(function(btn) {
    btn.addEventListener('click', function() {
      const form = this.closest('.search-filter-body') || this.closest('.search-filter');
      form.querySelectorAll('input, select').forEach(function(el) {
        el.value = '';
      });
    });
  });
}

// ===== ALERTS AUTO-DISMISS =====
function initAlerts() {
  document.querySelectorAll('.alert').forEach(function(alert) {
    setTimeout(function() {
      alert.style.transition = 'opacity 0.3s';
      alert.style.opacity = '0';
      setTimeout(function() { alert.remove(); }, 300);
    }, 4000);
  });
}

// ===== FORM VALIDATION =====
function initForms() {
  document.querySelectorAll('form[data-validate]').forEach(function(form) {
    form.addEventListener('submit', function(e) {
      let valid = true;
      form.querySelectorAll('[required]').forEach(function(input) {
        if (!input.value.trim()) {
          input.classList.add('is-invalid');
          valid = false;
        } else {
          input.classList.remove('is-invalid');
        }
      });
      if (!valid) e.preventDefault();
    });
  });
}

// ===== TOOLTIPS =====
function initTooltips() {
  document.querySelectorAll('[title]').forEach(function(el) {
    el.addEventListener('mouseenter', function() {
      if (this.closest('.sidebar.collapsed')) {
        this.setAttribute('data-bs-toggle', 'tooltip');
      }
    });
  });
}

// ===== DOWNLOAD ZIP =====
function downloadZip() {
  const link = document.createElement('a');
  link.href = '#';
  link.download = 'gestion-app.zip';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  alert('Le fichier ZIP sera telecharge.');
}

// ===== INIT =====
document.addEventListener('DOMContentLoaded', function() {
  initSidebar();
  initUserDropdown();
  initSearchFilter();
  initAlerts();
  initForms();
  initTooltips();
});
