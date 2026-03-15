# Web Portal Guide 🌐

## Overview

The Novel Platform now includes a modern, user-friendly web portal built with React. This provides an intuitive interface for managing and reading novels without needing command-line tools.

## Quick Start

### Step 1: Start the Backend
```bash
./START_WITHOUT_DOCKER.sh
```
Wait for the backend to start on http://localhost:8080

### Step 2: Start the Web Portal
```bash
./START_WEB.sh
```

The web portal will automatically open at **http://localhost:3000**

### Step 3: Login
- Email: `admin@novelplatform.com`
- Password: `admin123`

## Features

### 🏠 Dashboard
- View statistics (total novels, genres)
- See recently added novels
- Quick access to all features

### 📚 Browse Novels
- View all novels in a beautiful grid layout
- Search by title or author
- Filter by genre
- Click any novel to see details

### 📖 Novel Details
- View complete novel information
- Read AI-generated summary
- See all chapters
- Start reading with one click

### 📤 Upload Novels (Admin Only)
- Drag-and-drop PDF upload
- Fill in novel details
- AI automatically:
  - Detects and splits chapters
  - Identifies genre
  - Generates summary
  - Moderates content

### 📱 Reading Experience
- Clean, distraction-free reader
- Easy chapter navigation
- Previous/Next buttons
- Responsive design for all devices

## Screenshots

### Login Page
```
┌─────────────────────────────────┐
│     📚 Novel Platform           │
│  Welcome back! Please login     │
│                                 │
│  Email: [________________]      │
│  Password: [____________]       │
│                                 │
│  [        Login        ]        │
└─────────────────────────────────┘
```

### Dashboard
```
┌─────────────────────────────────────────┐
│ 📚 Novel Platform    [Upload] [Logout]  │
├─────────────────────────────────────────┤
│                                         │
│  📖 Total Novels    🏷️ Genres   🤖 AI  │
│       12                8        Powered│
│                                         │
│  Recent Novels                          │
│  ┌──────┐ ┌──────┐ ┌──────┐           │
│  │ 📖   │ │ 📖   │ │ 📖   │           │
│  │Novel1│ │Novel2│ │Novel3│           │
│  └──────┘ └──────┘ └──────┘           │
└─────────────────────────────────────────┘
```

### Upload Page
```
┌─────────────────────────────────┐
│  📤 Upload Novel                │
│                                 │
│  ┌─────────────────────────┐   │
│  │     📄                  │   │
│  │  Click to select PDF    │   │
│  │  Max size: 50MB         │   │
│  └─────────────────────────┘   │
│                                 │
│  Title: [________________]      │
│  Author: [_______________]      │
│  Description: [__________]      │
│                                 │
│  🤖 AI Features (Automatic)     │
│  ✨ Chapter detection           │
│  🏷️ Genre identification        │
│  📝 Summary generation          │
│  🛡️ Content moderation          │
│                                 │
│  [   🚀 Upload Novel   ]        │
└─────────────────────────────────┘
```

## User Roles

### Admin
- Upload novels
- View all novels
- Read all content
- Access dashboard statistics

### Reader (Future)
- Browse novels
- Read content
- Bookmark chapters
- Track reading history

## Technical Details

### Technology Stack
- **Frontend**: React 18 + Vite
- **Routing**: React Router 6
- **HTTP Client**: Axios
- **Styling**: Modern CSS3
- **Build Tool**: Vite

### API Integration
All API calls go through the backend:
- `/api/auth/login` - Authentication
- `/api/novels` - Novel management
- `/api/chapters` - Chapter access
- `/api/genres` - Genre listing

### Authentication
- JWT token stored in localStorage
- Automatic token injection in API requests
- Redirect to login if unauthorized

## Customization

### Change Theme Colors
Edit `web/src/index.css`:
```css
/* Primary color */
background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

/* Accent color */
.btn-primary {
  background: #667eea;
}
```

### Add New Pages
1. Create component in `web/src/pages/`
2. Add route in `web/src/App.jsx`
3. Add navigation link in `web/src/components/Layout.jsx`

### Modify Layout
Edit `web/src/components/Layout.jsx` and `Layout.css`

## Development

### Install Dependencies
```bash
cd web
npm install
```

### Run Development Server
```bash
npm run dev
```

### Build for Production
```bash
npm run build
```

Output will be in `web/dist/`

### Preview Production Build
```bash
npm run preview
```

## Deployment

### Option 1: Static Hosting (Recommended)
1. Build the project: `npm run build`
2. Deploy `web/dist/` to:
   - Vercel
   - Netlify
   - AWS S3 + CloudFront
   - GitHub Pages

### Option 2: Nginx
```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/web/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
    }
}
```

### Option 3: Docker
```dockerfile
FROM node:18-alpine as build
WORKDIR /app
COPY web/package*.json ./
RUN npm install
COPY web/ ./
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
```

## Troubleshooting

### Web portal won't start
```bash
# Check Node.js version
node --version  # Should be 16+

# Reinstall dependencies
cd web
rm -rf node_modules package-lock.json
npm install
```

### Can't login
- Ensure backend is running on port 8080
- Check credentials: admin@novelplatform.com / admin123
- Clear browser cache and localStorage

### Upload fails
- Check file is PDF format
- Ensure file is under 50MB
- Verify backend is processing requests
- Check backend logs for errors

### Chapters not showing
- Wait for AI processing to complete (30-60 seconds)
- Refresh the page
- Check backend logs for AI errors

### Styling issues
- Clear browser cache
- Hard refresh (Ctrl+Shift+R)
- Check browser console for errors

## Browser Compatibility

✅ Chrome/Edge 90+
✅ Firefox 88+
✅ Safari 14+
✅ Mobile browsers (iOS Safari, Chrome Mobile)

## Performance

- Initial load: ~500KB (gzipped)
- Lazy loading for routes
- Optimized images and assets
- Fast navigation with React Router

## Security

- JWT authentication
- HTTPS recommended for production
- CORS configured in backend
- XSS protection
- CSRF protection

## Future Enhancements

- [ ] User registration
- [ ] Reading progress tracking
- [ ] Bookmarks and favorites
- [ ] Comments and ratings
- [ ] Dark mode
- [ ] Offline reading (PWA)
- [ ] Social sharing
- [ ] Reading statistics
- [ ] Multiple languages

## Support

For issues or questions:
1. Check backend logs
2. Check browser console
3. Verify API endpoints are working
4. Review this guide

## Comparison: Web vs Android vs CLI

| Feature | Web Portal | Android App | CLI |
|---------|-----------|-------------|-----|
| Upload Novels | ✅ Easy | ✅ Easy | ⚠️ Complex |
| Browse Novels | ✅ Beautiful | ✅ Native | ❌ No |
| Read Novels | ✅ Great | ✅ Best | ❌ No |
| Admin Features | ✅ Full | ⚠️ Limited | ✅ Full |
| Accessibility | ✅ Any device | ⚠️ Android only | ⚠️ Terminal |
| User-Friendly | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ |

## Conclusion

The web portal provides the most user-friendly way to interact with the Novel Platform. It's perfect for:
- Admins managing content
- Readers browsing and reading novels
- Anyone who prefers a visual interface

Start using it now:
```bash
./START_WEB.sh
```

Happy reading! 📚✨
