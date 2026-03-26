# Second Brain Frontend

Modern, clean web interface for the Second Brain API.

## Features

✨ **AI-Powered Enrichment**
- Automatic tag generation using Groq LLM (llama-3.3-70b-versatile)
- Intelligent summaries for all entries
- Real-time enrichment with loading states

📝 **Entry Management**
- Create Notes, Ideas, and Links
- View all entries in a beautiful card layout
- Delete entries with confirmation
- AI-generated tags and summaries highlighted

🔍 **Search & Filter**
- Search across titles, content, tags, and summaries
- Real-time filtering as you type
- Entry count updates dynamically

🎨 **Modern UI**
- Clean, responsive design
- Gradient headers and smooth animations
- Color-coded entry types (Notes, Ideas, Links)
- Mobile-friendly

## How to Use

### Option 1: Integrated with Spring Boot (Recommended)

1. Start the server:
   ```bash
   cd /home/thinker/IdeaProjects/second-brain-v1
   ./start.sh
   ```

2. Open your browser:
   ```
   http://localhost:8080
   ```

The frontend is automatically served by Spring Boot!

### Option 2: Standalone (File Protocol)

Simply open `index.html` in your browser:
```bash
cd frontend
open index.html  # macOS
xdg-open index.html  # Linux
```

**Note:** Make sure the API server is running on `http://localhost:8080`

## Creating Entries

1. **Select Type**: Choose Note 📝, Idea 💡, or Link 🔗
2. **Enter Title**: Give your entry a descriptive title
3. **Add Content**: Write your note, idea, or description
4. **For Links**: URL field appears automatically
5. **Create**: Click "Create Entry" and wait for AI enrichment (~1-2 seconds)

The AI will automatically:
- Generate relevant tags
- Create a concise summary
- Enrich the entry with metadata

## Search & Discovery

Use the search bar to find entries by:
- Title
- Content
- AI-generated tags
- AI-generated summaries

## Technology Stack

- **Frontend**: Vanilla HTML/CSS/JavaScript (no frameworks!)
- **Styling**: Modern CSS with CSS Variables
- **API**: REST API with synchronous enrichment
- **AI**: Groq LLM (llama-3.3-70b-versatile) for tags and summaries

## File Structure

```
frontend/
├── index.html    # Main HTML structure
├── styles.css    # Modern styling with gradients
├── app.js        # API interactions and UI logic
└── README.md     # This file
```

## Customization

### Change Colors

Edit `styles.css` CSS variables:
```css
:root {
    --primary-color: #6366f1;  /* Change primary color */
    --secondary-color: #10b981; /* Change secondary color */
    --background: #f8fafc;      /* Change background */
}
```

### Modify API URL

Edit `app.js`:
```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

## Browser Support

- Chrome/Edge (latest)
- Firefox (latest)
- Safari (latest)

## Notes

- Entries are enriched synchronously (you see tags/summary immediately)
- Delete confirmation prevents accidental deletions
- Search is client-side (fast, no API calls)
- Responsive design works on mobile and desktop
