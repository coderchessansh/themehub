(() => {
  const STYLE_ID = 'themehub-extension-style';
  const defaults = { bg: '#0f172a', text: '#ffffff', accent: '#5865f2', font: 'Inter, sans-serif' };

  function apply(theme = defaults) {
    let style = document.getElementById(STYLE_ID);
    if (!style) {
      style = document.createElement('style');
      style.id = STYLE_ID;
      (document.head || document.documentElement).appendChild(style);
    }

    style.textContent = `
      :root {
        --themehub-bg: ${theme.bg};
        --themehub-text: ${theme.text};
        --themehub-accent: ${theme.accent};
        --themehub-font: ${theme.font};
      }
      html.themehub-active, html.themehub-active body {
        background-color: var(--themehub-bg) !important;
        color: var(--themehub-text) !important;
        font-family: var(--themehub-font) !important;
      }
      html.themehub-active body * {
        font-family: var(--themehub-font) !important;
      }
      html.themehub-active a { color: var(--themehub-accent) !important; }
      html.themehub-active button,
      html.themehub-active [role="button"] {
        border-color: var(--themehub-accent) !important;
      }
    `;
    document.documentElement.classList.add('themehub-active');
  }

  chrome.storage.local.get({ themehubTheme: defaults, themehubEnabled: false }, data => {
    if (data.themehubEnabled) apply(data.themehubTheme);
  });

  chrome.storage.onChanged.addListener((changes, area) => {
    if (area !== 'local') return;
    if (changes.themehubEnabled && !changes.themehubEnabled.newValue) {
      document.documentElement.classList.remove('themehub-active');
    }
    if (changes.themehubTheme || changes.themehubEnabled) {
      chrome.storage.local.get({ themehubTheme: defaults, themehubEnabled: false }, data => {
        if (data.themehubEnabled) apply(data.themehubTheme);
      });
    }
  });
})();