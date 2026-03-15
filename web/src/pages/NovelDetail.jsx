import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import api from '../api/axios'
import './NovelDetail.css'

function NovelDetail() {
  const { id } = useParams()
  const [novel, setNovel] = useState(null)
  const [chapters, setChapters] = useState([])
  const [loading, setLoading] = useState(true)
  const [showToc, setShowToc] = useState(false)

  useEffect(() => {
    loadNovel()
  }, [id])

  const loadNovel = async () => {
    try {
      const [novelRes, chaptersRes] = await Promise.all([
        api.get(`/novels/${id}`),
        api.get(`/novels/${id}/chapters`)
      ])
      setNovel(novelRes.data)
      setChapters(chaptersRes.data)
    } catch (err) {
      console.error('Failed to load novel:', err)
    } finally {
      setLoading(false)
    }
  }

  if (loading) return <div className="loading">Loading...</div>
  if (!novel) return <div className="error">Novel not found</div>

  return (
    <div className="container">
      <div className="novel-detail-layout">
        {/* Table of Contents Sidebar */}
        <aside className={`toc-sidebar ${showToc ? 'show' : ''}`}>
          <div className="toc-header">
            <h3>📑 Table of Contents</h3>
            <button className="toc-close" onClick={() => setShowToc(false)}>✕</button>
          </div>
          <div className="toc-list">
            {chapters.map((chapter, index) => (
              <Link
                to={`/read/${novel.id}/${chapter.id}`}
                key={chapter.id}
                className="toc-item"
              >
                <span className="toc-num">{index + 1}</span>
                <span className="toc-title">{chapter.title}</span>
              </Link>
            ))}
          </div>
        </aside>

        {/* TOC Toggle Button (Mobile) */}
        <button className="toc-toggle" onClick={() => setShowToc(true)}>
          📑 Contents
        </button>

        {/* Main Content */}
        <div className="novel-detail">
          <div className="novel-header">
            <div className="novel-cover-big">📖</div>
            <div className="novel-header-info">
              <h1>{novel.title}</h1>
              <p className="author-big">by {novel.author}</p>
              <div className="novel-tags">
                <span className="genre-tag-big">{novel.genre}</span>
                <span className="chapter-count-big">{chapters.length} chapters</span>
              </div>
              <Link to={`/read/${novel.id}/${chapters[0]?.id}`} className="start-reading-btn">
                ▶ Start Reading
              </Link>
            </div>
          </div>

          {novel.summary && (
            <div className="novel-summary">
              <h2>📝 Summary</h2>
              <p>{novel.summary}</p>
            </div>
          )}

          {novel.description && (
            <div className="novel-description">
              <h2>📖 Description</h2>
              <p>{novel.description}</p>
            </div>
          )}

          {/* Quick TOC Preview */}
          <div className="toc-preview">
            <h2>📚 Table of Contents</h2>
            <div className="toc-grid">
              {chapters.map((chapter, index) => (
                <Link
                  to={`/read/${novel.id}/${chapter.id}`}
                  key={chapter.id}
                  className="toc-card"
                >
                  <div className="toc-card-num">{index + 1}</div>
                  <div className="toc-card-title">{chapter.title}</div>
                  <div className="toc-card-words">{chapter.wordCount || '—'} words</div>
                </Link>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default NovelDetail
