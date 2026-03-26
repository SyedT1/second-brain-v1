// API Configuration
const API_BASE_URL = 'http://localhost:8080/api';

// DOM Elements
const createForm = document.getElementById('createForm');
const typeSelect = document.getElementById('type');
const urlGroup = document.getElementById('urlGroup');
const searchInput = document.getElementById('searchInput');
const entriesList = document.getElementById('entriesList');
const entryCount = document.getElementById('entryCount');
const submitBtn = document.getElementById('submitBtn');
const btnText = submitBtn.querySelector('.btn-text');
const loader = submitBtn.querySelector('.loader');

// State
let allEntries = [];

// Show/hide URL field based on type
typeSelect.addEventListener('change', (e) => {
    urlGroup.style.display = e.target.value === 'LINK' ? 'block' : 'none';
});

// Create Entry
createForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const formData = new FormData(e.target);
    const entry = {
        type: formData.get('type'),
        title: formData.get('title'),
        content: formData.get('content'),
        url: formData.get('url') || null
    };

    try {
        // Show loading state
        submitBtn.disabled = true;
        btnText.textContent = 'Creating & Enriching with AI...';
        loader.style.display = 'block';

        const response = await fetch(`${API_BASE_URL}/entries`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(entry)
        });

        if (!response.ok) {
            throw new Error('Failed to create entry');
        }

        const createdEntry = await response.json();

        // Show success
        btnText.textContent = '✓ Entry Created!';
        loader.style.display = 'none';

        // Reset form
        createForm.reset();

        // Reload entries
        await loadEntries();

        // Reset button after delay
        setTimeout(() => {
            btnText.textContent = 'Create Entry';
            submitBtn.disabled = false;
        }, 2000);

    } catch (error) {
        console.error('Error creating entry:', error);
        btnText.textContent = '✗ Error - Try Again';
        loader.style.display = 'none';
        submitBtn.disabled = false;

        setTimeout(() => {
            btnText.textContent = 'Create Entry';
        }, 3000);
    }
});

// Load Entries
async function loadEntries() {
    try {
        const response = await fetch(`${API_BASE_URL}/entries`);
        if (!response.ok) {
            throw new Error('Failed to load entries');
        }

        allEntries = await response.json();

        // Sort by creation date (newest first)
        allEntries.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

        renderEntries(allEntries);
        updateEntryCount(allEntries.length);
    } catch (error) {
        console.error('Error loading entries:', error);
        entriesList.innerHTML = '<div class="empty-state"><div class="empty-state-icon">⚠️</div><div class="empty-state-text">Error loading entries. Make sure the server is running.</div></div>';
    }
}

// Render Entries
function renderEntries(entries) {
    if (entries.length === 0) {
        entriesList.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">📝</div>
                <div class="empty-state-text">No entries yet</div>
                <p style="color: var(--text-secondary); font-size: 0.9rem;">Create your first note, idea, or link above!</p>
            </div>
        `;
        return;
    }

    entriesList.innerHTML = entries.map(entry => createEntryCard(entry)).join('');

    // Add delete listeners
    document.querySelectorAll('.btn-delete').forEach(btn => {
        btn.addEventListener('click', () => deleteEntry(btn.dataset.id));
    });
}

// Create Entry Card HTML
function createEntryCard(entry) {
    const date = new Date(entry.createdAt).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });

    const hasUrl = entry.url && entry.type === 'LINK';
    const titleHtml = hasUrl
        ? `<a href="${entry.url}" target="_blank">${entry.title}</a>`
        : entry.title;

    const tagsHtml = entry.tags && entry.tags.length > 0
        ? `<div class="entry-tags">
             ${entry.tags.map(tag => `<span class="tag">${tag}</span>`).join('')}
           </div>`
        : '';

    const summaryHtml = entry.summary && entry.summary !== entry.content
        ? `<div class="entry-summary">
             <div class="entry-summary-label">
                 <span class="ai-badge">AI</span> Summary
             </div>
             <div class="entry-summary-text">${entry.summary}</div>
           </div>`
        : '';

    const enrichmentBadge = entry.enrichedAt
        ? '<span class="enrichment-badge">✨ AI Enriched</span>'
        : '';

    return `
        <div class="entry-card ${entry.type}">
            <div class="entry-header">
                <div class="entry-title-section">
                    <span class="entry-type ${entry.type}">${getTypeIcon(entry.type)} ${entry.type}</span>
                    <h3 class="entry-title">${titleHtml}</h3>
                    <div class="entry-date">${date} ${enrichmentBadge}</div>
                </div>
                <div class="entry-actions">
                    <button class="btn-delete" data-id="${entry.id}" title="Delete">
                        🗑️
                    </button>
                </div>
            </div>

            ${summaryHtml}

            <div class="entry-content">${entry.content}</div>

            ${tagsHtml}
        </div>
    `;
}

// Get Type Icon
function getTypeIcon(type) {
    const icons = {
        'NOTE': '📝',
        'IDEA': '💡',
        'LINK': '🔗'
    };
    return icons[type] || '📄';
}

// Delete Entry
async function deleteEntry(id) {
    if (!confirm('Are you sure you want to delete this entry?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/entries/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error('Failed to delete entry');
        }

        await loadEntries();
    } catch (error) {
        console.error('Error deleting entry:', error);
        alert('Error deleting entry. Please try again.');
    }
}

// Search/Filter
searchInput.addEventListener('input', (e) => {
    const searchTerm = e.target.value.toLowerCase();

    if (!searchTerm) {
        renderEntries(allEntries);
        updateEntryCount(allEntries.length);
        return;
    }

    const filtered = allEntries.filter(entry => {
        const titleMatch = entry.title?.toLowerCase().includes(searchTerm);
        const contentMatch = entry.content?.toLowerCase().includes(searchTerm);
        const tagsMatch = entry.tags?.some(tag => tag.toLowerCase().includes(searchTerm));
        const summaryMatch = entry.summary?.toLowerCase().includes(searchTerm);

        return titleMatch || contentMatch || tagsMatch || summaryMatch;
    });

    renderEntries(filtered);
    updateEntryCount(filtered.length, allEntries.length);
});

// Update Entry Count
function updateEntryCount(count, total = null) {
    if (total !== null && count !== total) {
        entryCount.textContent = `${count} of ${total} entries`;
    } else {
        entryCount.textContent = `${count} ${count === 1 ? 'entry' : 'entries'}`;
    }
}

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    loadEntries();

    // Auto-refresh every 30 seconds (optional)
    // setInterval(loadEntries, 30000);
});
