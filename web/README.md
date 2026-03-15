# Novel Platform - Web Portal 🌐

A modern, user-friendly web interface for the Novel Platform.

## Features

- 🔐 **Login/Authentication** - Secure JWT-based authentication
- 📚 **Browse Novels** - View all novels with search and filter
- 📤 **Upload Novels** - Easy PDF upload with drag-and-drop
- 📖 **Read Online** - Beautiful reading experience with chapter navigation
- 🤖 **AI-Powered** - Automatic chapter detection, genre identification, and summaries
- 🎨 **Modern UI** - Clean, responsive design with smooth animations

## Quick Start

### 1. Install Dependencies
```bash
cd web
npm install
```

### 2. Start Development Server
```bash
npm run dev
```

The web portal will open at: **http://localhost:3000**

### 3. Login
Use the default admin credentials:
- Email: `admin@novelplatform.com`
- Password: `admin123`

## Requirements

- Node.js 16+ and npm
- Backend server running on http://localhost:8080

## Project Structure

```
web/
├── src/
│   ├── api/
│   │   └── axios.js          # API client configuration
│   ├── components/
│   │   ├── Layout.jsx        # Main layout with navigation
│   │   └── Layout.css
│   ├── pages/
│   │   ├── Login.jsx         # Login page
│   │   ├── Dashboard.jsx     # Dashboard with stats
│   │   ├── NovelList.jsx     # Browse all novels
│   │   ├── NovelDetail.jsx   # Novel details and chapters
│   │   ├── UploadNovel.jsx   # Upload PDF novels
│   │   └── Reader.jsx        # Chapter reading view
│   ├── main.jsx              # App entry point
│   └── index.css             # Global styles
├── index.html
├── vite.config.js            # Vite configuration
└── package.json
```

## Available Scripts

- `npm run dev` - Start development server (http://localhost:3000)
- `npm run build` - Build for production
- `npm run preview` - Preview production build

## Usage

### For Readers
1. Browse novels on the home page
2. Click on a novel to see details
3. Click on a chapter to start reading
4. Navigate between chapters with Previous/Next buttons

### For Admins
1. Click "Upload" in the navigation
2. Select a PDF file
3. Fill in novel details (title, author, description)
4. Click "Upload Novel"
5. AI will automatically process the novel

## Technology Stack

- **React 18** - UI framework
- **React Router 6** - Navigation
- **Vite** - Build tool and dev server
- **Axios** - HTTP client
- **CSS3** - Styling with modern features

## API Integration

The web portal connects to the Spring Boot backend via proxy:
- Development: Vite proxy forwards `/api/*` to `http://localhost:8080`
- Production: Configure nginx or similar to proxy API requests

## Customization

### Change Colors
Edit `web/src/index.css` and component CSS files to customize the color scheme.

### Add Features
- User registration
- Bookmarks and reading history
- Comments and ratings
- Dark mode
- Reading progress tracking

## Troubleshooting

### Port 3000 already in use
```bash
# Kill the process using port 3000
lsof -ti:3000 | xargs kill -9

# Or use a different port
npm run dev -- --port 3001
```

### Backend connection failed
- Ensure backend is running: `./START_WITHOUT_DOCKER.sh`
- Check backend is on port 8080
- Verify CORS is enabled in backend

### Build fails
```bash
# Clear cache and reinstall
rm -rf node_modules package-lock.json
npm install
```

## Production Deployment

### Build
```bash
npm run build
```

### Deploy
The `dist/` folder contains static files that can be deployed to:
- Nginx
- Apache
- Vercel
- Netlify
- AWS S3 + CloudFront

### Environment Variables
For production, configure:
- API base URL
- Authentication settings
- Analytics (optional)

## Browser Support

- Chrome/Edge (latest)
- Firefox (latest)
- Safari (latest)
- Mobile browsers

## License

Part of the Novel Platform project.
