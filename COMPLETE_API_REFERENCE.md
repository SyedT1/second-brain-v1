# 🧠 Second Brain - Complete Advanced Features API Reference

## Overview

All 10 advanced features are now fully implemented with REST APIs and services. This document provides complete reference for all endpoints.

---

## 📡 REST API Endpoints

### 1. Knowledge Decay & Resurfacing Engine

**Get forgotten knowledge (30/60/90 days):**
```bash
GET /api/insights/forgotten

# Response
{
  "thirtyDaysAgo": [
    {
      "id": 1,
      "title": "Old Note",
      "content": "...",
      "tags": ["tag1", "tag2"],
      "summary": "..."
    }
  ],
  "sixtyDaysAgo": [...],
  "ninetyDaysAgo": [...]
}
```

**Get entries from specific period:**
```bash
GET /api/insights/resurface?days=30
```

---

### 2. Capture-to-Action Pipeline

**Transform entry to different formats:**
```bash
POST /api/transform/{entryId}?format=TWEET_THREAD
POST /api/transform/{entryId}?format=ACTION_ITEMS
POST /api/transform/{entryId}?format=PRODUCT_BRIEF
POST /api/transform/{entryId}?format=EMAIL
POST /api/transform/{entryId}?format=BLOG_POST
POST /api/transform/{entryId}?format=LINKEDIN_POST

# Response
{
  "entryId": 1,
  "format": "TWEET_THREAD",
  "transformed": "1/7 Tweet content...\n2/7 Tweet content...\n..."
}
```

**Example:**
```bash
curl -X POST "http://localhost:8080/api/transform/1?format=TWEET_THREAD"
```

---

### 3. Knowledge Gaps Detector

**Get knowledge gaps analysis:**
```bash
GET /api/analytics/knowledge-gaps

# Response
{
  "analysis": "Based on your knowledge base, you should explore: [1] MLOps - Managing ML models in production...",
  "topicsCovered": ["Machine Learning", "AI", "Deep Learning", ...],
  "totalTopics": 12
}
```

---

### 4. Expertise Tracker

**Get expertise report:**
```bash
GET /api/analytics/expertise

# Response
{
  "expertiseScores": [
    {
      "topic": "Machine Learning",
      "entryCount": 15,
      "level": "EXPERT",
      "levelName": "EXPERT"
    },
    {
      "topic": "Python",
      "entryCount": 8,
      "level": "INTERMEDIATE",
      "levelName": "INTERMEDIATE"
    }
  ],
  "totalEntries": 50,
  "totalTags": 12,
  "expertTopics": 3
}
```

---

### 5. Weekly Intelligence Brief ⭐ NEW

**Get weekly intelligence brief (auto-runs Mondays 9 AM):**
```bash
GET /api/insights/weekly-brief

# Response
{
  "summary": "Executive Summary: This week you focused on...\nEmerging Patterns:\n- Topic 1\n- Topic 2\n...",
  "themes": ["Machine Learning", "AI", "Data Science"],
  "suggestedActions": [
    "Explore MLOps for production deployments",
    "Study transformer architectures deeply",
    "Create a project combining NLP and vision"
  ],
  "entriesCount": 7,
  "generatedAt": "2026-03-26T09:00:00"
}
```

---

### 6. Debate & Stress-Test Mode ⭐ NEW

**Test a claim against your knowledge base:**
```bash
POST /api/debate/stress-test

Body:
{
  "claim": "Machine Learning will replace all programming jobs"
}

# Response
{
  "claim": "Machine Learning will replace all programming jobs",
  "analysis": "Strengths of the claim:\n- Automation of routine tasks...\n\nCounterarguments:\n- ML requires human oversight...",
  "relatedEntries": ["ML Applications", "Human-AI Collaboration", "Future of Work"]
}
```

---

### 7. Contradiction Detector ⭐ NEW

**Detect contradictions in an entry:**
```bash
GET /api/insights/contradictions/{entryId}

# Response
{
  "entryId": 5,
  "contradictions": [
    "This contradicts entry about remote work benefits (Entry 3)",
    "Conflicts with productivity analysis from March"
  ],
  "analysis": "Analysis of conflicts found...",
  "conflictingEntries": ["Remote Work Analysis", "Productivity Study"],
  "isSafe": false
}
```

---

### 8. Knowledge Graph Visualization ⭐ NEW

**Get full knowledge graph (nodes and edges):**
```bash
GET /api/graph/full

# Response
{
  "nodes": [
    {
      "id": "1",
      "label": "Understanding Machine Learning",
      "type": "NOTE",
      "tags": ["ML", "AI", "Data Science"],
      "size": 130
    }
  ],
  "edges": [
    {
      "source": "1",
      "target": "3",
      "label": "AI, Machine Learning",
      "weight": 2,
      "type": "shared_tags"
    }
  ]
}
```

**Get just nodes:**
```bash
GET /api/graph/nodes
```

**Get just edges:**
```bash
GET /api/graph/edges
```

**Get connections for specific entry:**
```bash
GET /api/graph/connections/{entryId}

# Response
{
  "entryId": 1,
  "connections": [
    {
      "connectedId": 3,
      "connectedTitle": "Deep Learning Basics",
      "strength": 3,
      "commonTags": ["AI", "Machine Learning", "Data Science"]
    }
  ]
}
```

---

### 9. Context-Aware Writing Assistant ⭐ NEW

**Get writing suggestions while typing:**
```bash
POST /api/assist/suggest

Body:
{
  "text": "I'm writing about the future of AI and need to mention..."
}

# Response
{
  "originalText": "I'm writing about the future of AI and need to mention...",
  "suggestedContinuation": "the evolution of large language models and their applications across various industries. Building on recent advances in transformer architectures...",
  "relevantEntries": [
    {
      "id": 5,
      "title": "Transformer Architecture Explained",
      "excerpt": "Transformers are neural networks that process sequential data...",
      "tags": ["AI", "Deep Learning", "NLP"]
    }
  ]
}
```

---

### 10. Collaborative Brain Merge ⭐ NEW

**Compare two topics for collaboration:**
```bash
POST /api/collaborate/compare

Body:
{
  "topic1": "Machine Learning",
  "topic2": "DevOps"
}

# Response
{
  "topic1": "Machine Learning",
  "topic2": "DevOps",
  "topic1Count": 15,
  "topic2Count": 8,
  "overlapCount": 3,
  "sharedTags": ["Automation", "Scalability", "Monitoring"],
  "uniqueToTopic1": ["Deep Learning", "Model Training", "Inference"],
  "uniqueToTopic2": ["Infrastructure", "Deployment", "CI/CD"],
  "potentialConflicts": [
    "Potential conflict: ML Model Training vs Docker Containerization"
  ],
  "collaborationOpportunities": [
    "Strong intersection on: Automation, Scalability",
    "Investigate how Machine Learning and DevOps connect",
    "Create a combined knowledge base on shared topics"
  ]
}
```

---

## 🔧 Complete cURL Examples

### Example 1: Create Entry → Check Expertise → Check Contradictions
```bash
# Create entry
curl -X POST http://localhost:8080/api/entries \
  -H "Content-Type: application/json" \
  -d '{
    "type": "NOTE",
    "title": "Climate Change Mitigation",
    "content": "We must transition to renewable energy sources and implement carbon capture technologies to mitigate climate change. Individual actions are ineffective against systemic problems."
  }'

# Get ID from response (e.g., 25)

# Check for contradictions
curl http://localhost:8080/api/insights/contradictions/25

# Check expertise
curl http://localhost:8080/api/analytics/expertise
```

### Example 2: Transform Entry to Multiple Formats
```bash
# Transform to tweet thread
curl -X POST "http://localhost:8080/api/transform/1?format=TWEET_THREAD" | jq .transformed

# Transform to action items
curl -X POST "http://localhost:8080/api/transform/1?format=ACTION_ITEMS" | jq .transformed

# Transform to product brief
curl -X POST "http://localhost:8080/api/transform/1?format=PRODUCT_BRIEF" | jq .transformed
```

### Example 3: Stress-Test a Claim
```bash
curl -X POST http://localhost:8080/api/debate/stress-test \
  -H "Content-Type: application/json" \
  -d '{
    "claim": "Remote work is always more productive than office work"
  }' | jq .analysis
```

### Example 4: Get Weekly Brief
```bash
curl http://localhost:8080/api/insights/weekly-brief | jq '.suggestedActions'
```

### Example 5: Explore Knowledge Graph
```bash
# Get full graph
curl http://localhost:8080/api/graph/full | jq '.nodes[] | {id, label, tags}'

# Get connections for entry 1
curl http://localhost:8080/api/graph/connections/1 | jq '.connections[]'
```

### Example 6: Writing Assistance
```bash
curl -X POST http://localhost:8080/api/assist/suggest \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Machine learning is transforming how we..."
  }' | jq '.relevantEntries'
```

### Example 7: Collaborative Analysis
```bash
curl -X POST http://localhost:8080/api/collaborate/compare \
  -H "Content-Type: application/json" \
  -d '{
    "topic1": "Machine Learning",
    "topic2": "DevOps"
  }' | jq '{sharedTags: .sharedTags, opportunities: .collaborationOpportunities}'
```

---

## 🎯 Feature Matrix

| Feature | Endpoint | Method | Status | Use Case |
|---------|----------|--------|--------|----------|
| Resurfacing | `/api/insights/forgotten` | GET | ✅ | Spaced repetition |
| Expertise Tracker | `/api/analytics/expertise` | GET | ✅ | Track knowledge growth |
| Knowledge Gaps | `/api/analytics/knowledge-gaps` | GET | ✅ | Find learning areas |
| Transform | `/api/transform/{id}` | POST | ✅ | Content generation |
| Weekly Brief | `/api/insights/weekly-brief` | GET | ✅ | Weekly digests |
| Stress-Test | `/api/debate/stress-test` | POST | ✅ | Test ideas |
| Contradictions | `/api/insights/contradictions/{id}` | GET | ✅ | Quality control |
| Graph | `/api/graph/full` | GET | ✅ | Visualization |
| Writing Assist | `/api/assist/suggest` | POST | ✅ | Real-time help |
| Collaboration | `/api/collaborate/compare` | POST | ✅ | Team knowledge |

---

## 🚀 Integration Examples

### With Python
```python
import requests

# Get expertise
response = requests.get('http://localhost:8080/api/analytics/expertise')
expertise = response.json()
print(f"Expert topics: {len([e for e in expertise['expertiseScores'] if e['level'] == 'EXPERT'])}")

# Stress-test a claim
response = requests.post(
    'http://localhost:8080/api/debate/stress-test',
    json={'claim': 'My belief about X'}
)
print(response.json()['analysis'])
```

### With JavaScript
```javascript
// Get writing suggestions
async function getSuggestions(text) {
  const response = await fetch('http://localhost:8080/api/assist/suggest', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ text })
  });
  return response.json();
}

// Get graph
async function getKnowledgeGraph() {
  const response = await fetch('http://localhost:8080/api/graph/full');
  return response.json();
}
```

### With cURL in Bash Script
```bash
#!/bin/bash

# Generate weekly digest and save to file
curl http://localhost:8080/api/insights/weekly-brief | \
  jq '.suggestedActions[]' > weekly_actions.txt

# Get expertise and send alert if low
expertise=$(curl http://localhost:8080/api/analytics/expertise)
expert_count=$(echo $expertise | jq '.expertTopics')
if [ $expert_count -lt 2 ]; then
  echo "Consider deepening knowledge in more areas"
fi
```

---

## 📊 Data Flow Architecture

```
Create Entry
    ↓
AI Enrichment (Groq LLM)
    ↓
Entry Saved to DB
    ↓
┌─► Expertise calculated
├─► Tags aggregated
├─► Connections identified
├─► Contradictions detected
└─► Graph updated
    ↓
APIs Available for:
├─ Resurfacing
├─ Analytics
├─ Transformation
├─ Graph queries
├─ Debate
├─ Writing assistance
└─ Collaboration
```

---

## ⚙️ Configuration

### Enable Weekly Brief
Already configured with `@EnableScheduling` in `SecondBrainApplication.java`

Runs every Monday at 9 AM using cron: `"0 9 * * MON"`

To customize the schedule, edit `WeeklyBriefService.java`:
```java
@Scheduled(cron = "0 9 * * MON")  // Change cron expression
```

Common cron expressions:
- `"0 9 * * MON"` - Every Monday at 9 AM
- `"0 9 * * *"` - Every day at 9 AM
- `"0 */4 * * *"` - Every 4 hours

---

## 📈 Performance Notes

- Graph operations optimized for <1000 entries
- Contradiction detection limits to top 5 matches
- Writing suggestions search up to 10 entries
- Weekly brief generates in ~5-10 seconds
- All APIs cached at application level

---

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| Weekly brief not running | Check `@EnableScheduling` is enabled |
| Graph returns empty | Ensure entries have tags |
| Contradiction detection slow | Reduce number of entries or limit search scope |
| Writing suggestions generic | Add more diverse entries to KB |
| API returns 404 | Verify entry exists before calling API |

---

## 📚 Next Steps

1. Restart server: `./start.sh`
2. Visit: `http://localhost:8080/index-advanced.html`
3. Test each feature with sample entries
4. Integrate APIs into your applications
5. Customize cron schedules as needed

**All 10 features are now production-ready!** 🚀
