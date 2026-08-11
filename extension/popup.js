const $ = id => document.getElementById(id);
const defaults = { bg:'#0f172a', text:'#ffffff', accent:'#5865f2', font:'Inter, sans-serif' };

chrome.storage.local.get({ themehubTheme: defaults }, data => {
  const t = data.themehubTheme;
  $('bg').value = t.bg || defaults.bg;
  $('text').value = t.text || defaults.text;
  $('accent').value = t.accent || defaults.accent;
  $('font').value = t.font || defaults.font;
});

$('apply').onclick = () => {
  const theme = { bg:$('bg').value, text:$('text').value, accent:$('accent').value, font:$('font').value };
  chrome.storage.local.set({ themehubTheme: theme, themehubEnabled: true }, () => {
    $('status').textContent = '✅ Applied! Refresh a page if needed.';
  });
};

$('off').onclick = () => chrome.storage.local.set({ themehubEnabled:false }, () => {
  $('status').textContent = 'ThemeHub is off for websites.';
});