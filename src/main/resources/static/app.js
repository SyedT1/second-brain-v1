// API Configuration
const API_BASE_URL = 'http://localhost:8080/api';

// DOM Elements - will be initialized after DOM loads
let navBtns, tabContents, createForm, typeSelect, urlGroup, searchInput, entriesList, entryCount, submitBtn, transformBtn, copyResultBtn;

// State
let allEntries = [];
let selectedFormat = null;
let selectedEntryId = null;

// Initialize DOM elements after page loads
document.addEventListener('DOMContentLoaded', function() {
    // Query DOM Elements
    navBtns = document.querySelectorAll('.nav-btn');
    tabContents = document.querySelectorAll('.tab-content');
    createForm = document.getElementById('createForm');
    typeSelect = document.getElementById('type');
    urlGroup = document.getElementById('urlGroup');
    searchInput = document.getElementById('searchInput');
    entriesList = document.getElementById('entriesList');
    entryCount = document.getElementById('entryCount');
    submitBtn = document.getElementById('submitBtn');
    transformBtn = document.getElementById('transformBtn');
    copyResultBtn = document.getElementById('copyResult');

    // Setup event listeners
    setupEventListeners();

    // Load initial data
    loadEntries();
});

function setupEventListeners() {
    // ==================== Tab Navigation ====================
    navBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const tabName = btn.dataset.tab;
            switchTab(tabName);
        });
    });

    // ==================== Entries Tab ====================
    typeSelect.addEventListener('change', (e) => {
        urlGroup.style.display = e.target.value === 'LINK' ? 'block' : 'none';
    });

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
            submitBtn.disabled = true;
            submitBtn.querySelector('.btn-text').textContent = 'Creating & Enriching...';
            submitBtn.querySelector('.loader').style.display = 'block';

            const response = await fetch(`${API_BASE_URL}/entries`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(entry)
            });

            if (!response.ok) throw new Error('Failed to create entry');

            createForm.reset();
            await loadEntries();

            submitBtn.querySelector('.btn-text').textContent = '✓ Entry Created!';
            setTimeout(() => {
                submitBtn.querySelector('.btn-text').textContent = 'Create Entry';
                submitBtn.disabled = false;
                submitBtn.querySelector('.loader').style.display = 'none';
            }, 2000);

        } catch (error) {
            console.error('Error creating entry:', error);
            submitBtn.querySelector('.btn-text').textContent = '✗ Error - Try Again';
            submitBtn.disabled = false;
            submitBtn.querySelector('.loader').style.display = 'none';
        }
    });

    // ==================== Search Entries ====================
    searchInput.addEventListener('input', (e) => {
        const searchTerm = e.target.value.toLowerCase();

        if (!searchTerm) {
            renderEntries(allEntries);
            updateEntryCount(allEntries.length);
            return;
        }

        const filtered = allEntries.filter(entry => {
            return (entry.title?.toLowerCase().includes(searchTerm) ||
                    entry.content?.toLowerCase().includes(searchTerm) ||
                    entry.tags?.some(tag => tag.toLowerCase().includes(searchTerm)) ||
                    entry.summary?.toLowerCase().includes(searchTerm));
        });

        renderEntries(filtered);
        updateEntryCount(filtered.length, allEntries.length);
    });

    // ==================== Format Selection ====================
    document.querySelectorAll('.format-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.format-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            selectedFormat = btn.dataset.format;
        });
    });

    // ==================== Transform Button ====================
    transformBtn.addEventListener('click', async () => {
        selectedEntryId = document.getElementById('entrySelect').value;

        if (!selectedEntryId || !selectedFormat) {
            alert('Please select an entry and format');
            return;
        }

        try {
            transformBtn.disabled = true;
            transformBtn.querySelector('.btn-text').textContent = 'Transforming...';
            transformBtn.querySelector('.loader').style.display = 'block';

            const response = await fetch(
                `${API_BASE_URL}/transform/${selectedEntryId}?format=${selectedFormat}`,
                { method: 'POST' }
            );

            if (!response.ok) throw new Error('Failed to transform entry');

            const data = await response.json();
            const resultDiv = document.getElementById('transformResult');

            resultDiv.innerHTML = `<pre>${data.transformed}</pre>`;
            copyResultBtn.style.display = 'block';

            transformBtn.querySelector('.btn-text').textContent = '✓ Transformed!';
            setTimeout(() => {
                transformBtn.querySelector('.btn-text').textContent = 'Transform';
                transformBtn.disabled = false;
                transformBtn.querySelector('.loader').style.display = 'none';
            }, 2000);

        } catch (error) {
            console.error('Error transforming entry:', error);
            document.getElementById('transformResult').innerHTML =
                '<p style="color: red;">Error: ' + error.message + '</p>';
            transformBtn.disabled = false;
            transformBtn.querySelector('.loader').style.display = 'none';
        }
    });

    // ==================== Copy Result Button ====================
    copyResultBtn.addEventListener('click', () => {
        const text = document.getElementById('transformResult').innerText;
        navigator.clipboard.writeText(text).then(() => {
            alert('Copied to clipboard!');
        });
    });
}

function switchTab(tabName) {
    // Hide all tabs
    tabContents.forEach(tab => tab.classList.remove('active'));

    // Show selected tab
    const selectedTab = document.getElementById(tabName + '-tab');
    if (selectedTab) {
        selectedTab.classList.add('active');
    }

    // Update active button
    navBtns.forEach(btn => btn.classList.remove('active'));
    document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');

    // Load tab-specific data
    if (tabName === 'insights') loadInsights();
    if (tabName === 'analytics') loadAnalytics();
    if (tabName === 'transform') loadTransformData();
}

async function loadEntries() {
    try {
        const response = await fetch(`${API_BASE_URL}/entries`);
        if (!response.ok) throw new Error('Failed to load entries');

        allEntries = await response.json();
        allEntries.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

        renderEntries(allEntries);
        updateEntryCount(allEntries.length);
    } catch (error) {
        console.error('Error loading entries:', error);
    }
}

function renderEntries(entries) {
    if (entries.length === 0) {
        entriesList.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">📝</div>
                <div class="empty-state-text">No entries yet</div>
            </div>
        `;
        return;
    }

    entriesList.innerHTML = entries.map(entry => createEntryCard(entry)).join('');

    document.querySelectorAll('.btn-delete').forEach(btn => {
        btn.addEventListener('click', () => deleteEntry(btn.dataset.id));
    });
}

function createEntryCard(entry) {
    const date = new Date(entry.createdAt).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });

    const hasUrl = entry.url && entry.type === 'LINK';
    const titleHtml = hasUrl ? `<a href="${entry.url}" target="_blank">${entry.title}</a>` : entry.title;

    const tagsHtml = entry.tags && entry.tags.length > 0
        ? `<div class="entry-tags">${entry.tags.map(tag => `<span class="tag">${tag}</span>`).join('')}</div>`
        : '';

    const summaryHtml = entry.summary && entry.summary !== entry.content
        ? `<div class="entry-summary"><strong>Summary:</strong> ${entry.summary}</div>`
        : '';

    return `
        <div class="entry-card ${entry.type}">
            <div class="entry-header">
                <div>
                    <span class="entry-type ${entry.type}">${entry.type}</span>
                    <h3 class="entry-title">${titleHtml}</h3>
                    <div class="entry-date">${date}</div>
                </div>
                <button class="btn-delete" data-id="${entry.id}">🗑️</button>
            </div>
            ${summaryHtml}
            <div class="entry-content">${entry.content}</div>
            ${tagsHtml}
        </div>
    `;
}

async function deleteEntry(id) {
    if (!confirm('Delete this entry?')) return;

    try {
        await fetch(`${API_BASE_URL}/entries/${id}`, { method: 'DELETE' });
        await loadEntries();
    } catch (error) {
        console.error('Error deleting entry:', error);
    }
}

function updateEntryCount(count, total = null) {
    entryCount.textContent = total ? `${count} of ${total} entries` : `${count} entries`;
}

// ==================== Insights Tab ====================
async function loadInsights() {
    try {
        const response = await fetch(`${API_BASE_URL}/insights/forgotten`);
        if (!response.ok) throw new Error('Failed to load insights');

        const data = await response.json();

        renderInsightList(data.thirtyDaysAgo, 'insights-30');
        renderInsightList(data.sixtyDaysAgo, 'insights-60');
        renderInsightList(data.ninetyDaysAgo, 'insights-90');

    } catch (error) {
        console.error('Error loading insights:', error);
        document.getElementById('insights-30').innerHTML = '<p>Error loading insights</p>';
    }
}

function renderInsightList(entries, containerId) {
    const container = document.getElementById(containerId);

    if (!entries || entries.length === 0) {
        container.innerHTML = '<p style="color: #999;">No entries in this period</p>';
        return;
    }

    container.innerHTML = entries.map(entry => `
        <div class="insight-item">
            <strong>${entry.title}</strong>
            <p>${entry.summary || entry.content.substring(0, 100)}...</p>
            <div class="insight-tags">
                ${entry.tags?.map(tag => `<span class="mini-tag">${tag}</span>`).join('') || ''}
            </div>
        </div>
    `).join('');
}

// ==================== Analytics Tab ====================
async function loadAnalytics() {
    try {
        // Load Expertise
        const expertiseResponse = await fetch(`${API_BASE_URL}/analytics/expertise`);
        if (expertiseResponse.ok) {
            const expertise = await expertiseResponse.json();
            renderExpertiseChart(expertise);
        }

        // Load Knowledge Gaps
        const gapsResponse = await fetch(`${API_BASE_URL}/analytics/knowledge-gaps`);
        if (gapsResponse.ok) {
            const gaps = await gapsResponse.json();
            renderKnowledgeGaps(gaps);
        }

    } catch (error) {
        console.error('Error loading analytics:', error);
    }
}

function renderExpertiseChart(expertise) {
    const container = document.getElementById('expertiseChart');

    if (!expertise.expertiseScores || expertise.expertiseScores.length === 0) {
        container.innerHTML = '<p>No expertise data yet</p>';
        return;
    }

    const html = `
        <div class="expertise-stats">
            <div class="stat">
                <span class="stat-label">Total Entries</span>
                <span class="stat-value">${expertise.totalEntries}</span>
            </div>
            <div class="stat">
                <span class="stat-label">Topics Covered</span>
                <span class="stat-value">${expertise.totalTags}</span>
            </div>
            <div class="stat">
                <span class="stat-label">Expertise Areas</span>
                <span class="stat-value">${expertise.expertTopics}</span>
            </div>
        </div>
        <div class="expertise-list">
            ${expertise.expertiseScores.map(score => `
                <div class="expertise-item">
                    <div class="expertise-info">
                        <strong>${score.topic}</strong>
                        <span class="expertise-level level-${score.level.toLowerCase()}">${score.level}</span>
                    </div>
                    <div class="expertise-bar">
                        <div class="expertise-bar-fill" style="width: ${Math.min(100, (score.entryCount / 10) * 100)}%"></div>
                    </div>
                    <span class="expertise-count">${score.entryCount} entries</span>
                </div>
            `).join('')}
        </div>
    `;

    container.innerHTML = html;
}

function renderKnowledgeGaps(gaps) {
    const container = document.getElementById('knowledgeGapsReport');

    const html = `
        <div class="knowledge-gaps-content">
            <div class="analysis-text">${gaps.analysis}</div>
            <div class="topics-info">
                <p><strong>Topics Currently Covered:</strong> ${gaps.totalTopics}</p>
                <p style="font-size: 0.9rem; color: #999; margin-top: 0.5rem;">
                    ${gaps.topicsCovered.slice(0, 10).join(', ')}${gaps.topicsCovered.length > 10 ? '...' : ''}
                </p>
            </div>
        </div>
    `;

    container.innerHTML = html;
}

// ==================== Transform Tab ====================
async function loadTransformData() {
    try {
        const response = await fetch(`${API_BASE_URL}/entries`);
        if (!response.ok) throw new Error('Failed to load entries');

        const entries = await response.json();
        const select = document.getElementById('entrySelect');

        select.innerHTML = '<option value="">Choose an entry...</option>' +
            entries.map(e => `<option value="${e.id}">${e.title}</option>`).join('');

    } catch (error) {
        console.error('Error loading transform data:', error);
    }
}
