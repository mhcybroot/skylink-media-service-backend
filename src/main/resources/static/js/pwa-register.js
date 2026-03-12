// PWA Service Worker Registration
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/service-worker.js')
      .then(registration => {
        console.log('SW registered: ', registration);
        
        // Check for updates
        registration.addEventListener('updatefound', () => {
          const newWorker = registration.installing;
          newWorker.addEventListener('statechange', () => {
            if (newWorker.state === 'installed' && navigator.serviceWorker.controller) {
              // New content available, refresh page
              if (confirm('New version available! Refresh to update?')) {
                window.location.reload();
              }
            }
          });
        });
      })
      .catch(registrationError => {
        console.log('SW registration failed: ', registrationError);
      });
  });
}

// Enhanced PWA Install Prompt
let deferredPrompt;
let installButton = null;
let installCard = null;

// Initialize install UI elements
function initializeInstallUI() {
  installButton = document.getElementById('pwa-install-btn');
  installCard = document.getElementById('pwa-install-card');
  
  console.log('PWA Install UI initialized:', { installButton, installCard });
  
  // Check if app is already installed
  if (window.matchMedia('(display-mode: standalone)').matches || window.navigator.standalone) {
    console.log('App is already installed, hiding install prompt');
    hideInstallPrompt();
    return;
  }
  
  // Show install card by default (for testing)
  showInstallPrompt();
}

// Show install prompt
function showInstallPrompt() {
  console.log('Showing install prompt');
  if (installCard) {
    installCard.style.display = 'block';
    installCard.classList.remove('pwa-install-hidden');
    installCard.classList.add('animate-fade-in');
  }
}

// Hide install prompt
function hideInstallPrompt() {
  console.log('Hiding install prompt');
  if (installCard) {
    installCard.classList.add('pwa-install-hidden');
  }
}

// Handle install button click
function handleInstallClick() {
  console.log('Install button clicked, deferredPrompt:', deferredPrompt);
  
  if (!deferredPrompt) {
    // If no deferred prompt, show manual install instructions
    showManualInstallInstructions();
    return;
  }
  
  // Show loading state
  if (installButton) {
    installButton.innerHTML = '<span class="animate-spin">⟳</span> Installing...';
    installButton.disabled = true;
  }
  
  deferredPrompt.prompt();
  
  deferredPrompt.userChoice.then((choiceResult) => {
    console.log('Install prompt result:', choiceResult.outcome);
    if (choiceResult.outcome === 'accepted') {
      console.log('User accepted the install prompt');
      showInstallSuccess();
    } else {
      // Reset button state
      if (installButton) {
        installButton.innerHTML = '📱 Install App on Your Phone';
        installButton.disabled = false;
      }
    }
    deferredPrompt = null;
  });
}

// Show manual install instructions
function showManualInstallInstructions() {
  if (installCard) {
    const isIOS = /iPad|iPhone|iPod/.test(navigator.userAgent);
    const instructions = isIOS 
      ? 'On iOS: Tap the Share button in Safari, then "Add to Home Screen"'
      : 'On Android: Tap the menu (⋮) in your browser, then "Add to Home screen" or "Install app"';
    
    installCard.innerHTML = `
      <div class="text-center p-6">
        <div class="text-white text-4xl mb-2">📱</div>
        <h3 class="text-lg font-semibold text-white mb-2">Install Instructions</h3>
        <p class="text-sm text-blue-100 mb-4">${instructions}</p>
        <button onclick="location.reload()" class="bg-white text-blue-600 px-4 py-2 rounded font-medium">
          Got it
        </button>
      </div>
    `;
  }
}

// Show install success message
function showInstallSuccess() {
  if (installCard) {
    installCard.innerHTML = `
      <div class="text-center p-6">
        <div class="text-green-600 text-4xl mb-2">✓</div>
        <h3 class="text-lg font-semibold text-green-800 mb-2">App Installed Successfully!</h3>
        <p class="text-sm text-green-600">You can now access Project Manager from your home screen.</p>
      </div>
    `;
    
    // Hide after 3 seconds
    setTimeout(() => {
      hideInstallPrompt();
    }, 3000);
  }
}

// Handle beforeinstallprompt event
window.addEventListener('beforeinstallprompt', (e) => {
  console.log('beforeinstallprompt event fired');
  e.preventDefault();
  deferredPrompt = e;
  
  // Initialize UI and show install prompt
  setTimeout(() => {
    initializeInstallUI();
    showInstallPrompt();
    
    // Add click handler to install button
    if (installButton) {
      installButton.addEventListener('click', handleInstallClick);
    }
  }, 1000); // Delay to ensure DOM is ready
});

// Handle app installed
window.addEventListener('appinstalled', (evt) => {
  console.log('App installed successfully');
  showInstallSuccess();
});

// Initialize on DOM ready
document.addEventListener('DOMContentLoaded', () => {
  console.log('DOM loaded, initializing PWA install UI');
  initializeInstallUI();
  
  // Add click handler to install button
  const btn = document.getElementById('pwa-install-btn');
  if (btn) {
    btn.addEventListener('click', handleInstallClick);
    console.log('Install button click handler added');
  }
});
