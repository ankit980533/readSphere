import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/axios'
import './Dashboard.css'

function Dashboard() {
  const [stats, setStats] = useState({ novels: 0, genres: 0, chapters: 0 })
  const [recentNovels, setRecentNovels] = useState([])

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      const [novelsRes, genresRes] = await Promise.all([
        api.get('/novels'),
        api.get('/genres')
      ])
      const totalChapters = novelsRes.data.reduce((sum, n) => sum + (n.chapterCount || 0), 0)
      setStats({ 
        novels: novelsRes.data.length, 
        genres: genresRes.data.length,
        chapters: totalChapters
      })
      setRecentNovels(novelsRes.data.slice(0, 6))
    } catch (err) {
      console.error('Failed to load data:', err)
    }
  }

  return (
    <div className="container">
      <div className="dashboard-header">
        <h1>📚 Welcome to ReadSphere</h1>
        <p>Your AI-powered novel reading platform</p>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon">📖</div>
          <div className="stat-value">{stats.novels}</div>
          <div className="stat-label">Novels</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon">📑</div>
          <div className="stat-value">{stats.chapters}</div>
          <div className="stat-label">Chapters</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon">🏷️</div>
          <div className="stat-value">{stats.genres}</div>
          <div className="stat-label">Genres</div>
        </div>
      </div>

      <div className="section">
        <div className="section-header">
          <h2>📚 Recent Novels</h2>
          <Link to="/novels" className="view-all">View All →</Link>
        </div>
        
        {recentNovels.length === 0 ? (
          <div className="empty-state">
            <p>📭</p>
            <div className="hint">No novels uploaded yet</div>
            <Link to="/upload" className="btn-upload">Upload Your First Novel</Link>
          </div>
        ) : (
          <div className="novels-grid">
            {recentNovels.map(novel => (
              <Link to={`/novels/${novel.id}`} key={novel.id} className="novel-card">
                <div className="novel-cover">📖</div>
                <h3>{novel.title}</h3>
                <p className="novel-author">by {novel.author}</p>
                <span className="novel-genre">{novel.genre}</span>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default Dashboard
