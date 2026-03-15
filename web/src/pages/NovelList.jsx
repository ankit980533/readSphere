import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/axios'
import './NovelList.css'

function NovelList() {
  const [novels, setNovels] = useState([])
  const [genres, setGenres] = useState([])
  const [selectedGenre, setSelectedGenre] = useState('all')
  const [searchTerm, setSearchTerm] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      const [novelsRes, genresRes] = await Promise.all([
        api.get('/novels'),
        api.get('/genres')
      ])
      setNovels(novelsRes.data)
      setGenres(genresRes.data)
    } catch (err) {
      console.error('Failed to load novels:', err)
    } finally {
      setLoading(false)
    }
  }

  const filteredNovels = novels.filter(novel => {
    const matchesGenre = selectedGenre === 'all' || novel.genre === selectedGenre
    const matchesSearch = novel.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         novel.author.toLowerCase().includes(searchTerm.toLowerCase())
    return matchesGenre && matchesSearch
  })

  return (
    <div className="container">
      <div className="novels-header">
        <h1>📚 Novel Library</h1>
        <Link to="/upload" className="btn-upload-top">
          <span>+</span> Upload Novel
        </Link>
      </div>

      <div className="filters">
        <input
          type="text"
          placeholder="🔍 Search by title or author..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="search-input"
        />
        
        <div className="genre-filters">
          <button
            className={selectedGenre === 'all' ? 'genre-btn active' : 'genre-btn'}
            onClick={() => setSelectedGenre('all')}
          >
            All Genres
          </button>
          {genres.map(genre => (
            <button
              key={genre.id}
              className={selectedGenre === genre.name ? 'genre-btn active' : 'genre-btn'}
              onClick={() => setSelectedGenre(genre.name)}
            >
              {genre.name}
            </button>
          ))}
        </div>
      </div>

      {loading ? (
        <div className="loading">Loading novels...</div>
      ) : filteredNovels.length === 0 ? (
        <div className="empty-state">
          <p>📭</p>
          <p className="hint">
            {searchTerm ? 'No novels match your search' : 'No novels found'}
          </p>
        </div>
      ) : (
        <div className="novels-grid-large">
          {filteredNovels.map(novel => (
            <Link to={`/novels/${novel.id}`} key={novel.id} className="novel-card-large">
              <div className="novel-cover-large">📖</div>
              <div className="novel-info">
                <h3>{novel.title}</h3>
                <p className="author">by {novel.author}</p>
                <p className="description">
                  {novel.summary || novel.description || 'No description available'}
                </p>
                <div className="novel-meta">
                  <span className="genre-tag">{novel.genre}</span>
                  <span className="chapter-count">📑 {novel.chapterCount || 0} chapters</span>
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}

export default NovelList
