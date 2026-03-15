import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import api from '../api/axios'
import './Reader.css'

function Reader() {
  const { novelId, chapterId } = useParams()
  const navigate = useNavigate()
  const [chapter, setChapter] = useState(null)
  const [chapters, setChapters] = useState([])
  const [novel, setNovel] = useState(null)
  const [loading, setLoading] = useState(true)
  const [progress, setProgress] = useState(0)

  useEffect(() => {
    loadChapter()
    window.scrollTo(0, 0)
  }, [chapterId])

  useEffect(() => {
    const handleScroll = () => {
      const scrollTop = window.scrollY
      const docHeight = document.documentElement.scrollHeight - window.innerHeight
      const scrollPercent = (scrollTop / docHeight) * 100
      setProgress(Math.min(scrollPercent, 100))
    }
    
    window.addEventListener('scroll', handleScroll)
    return () => window.removeEventListener('scroll', handleScroll)
  }, [])

  const loadChapter = async () => {
    try {
      const [chapterRes, chaptersRes, novelRes] = await Promise.all([
        api.get(`/chapters/${chapterId}`),
        api.get(`/novels/${novelId}/chapters`),
        api.get(`/novels/${novelId}`)
      ])
      setChapter(chapterRes.data)
      setChapters(chaptersRes.data)
      setNovel(novelRes.data)
    } catch (err) {
      console.error('Failed to load chapter:', err)
    } finally {
      setLoading(false)
    }
  }

  const currentIndex = chapters.findIndex(c => c.id === parseInt(chapterId))
  const prevChapter = currentIndex > 0 ? chapters[currentIndex - 1] : null
  const nextChapter = currentIndex < chapters.length - 1 ? chapters[currentIndex + 1] : null

  if (loading) return <div className="loading">Loading chapter...</div>

  return (
    <div className="reader">
      {/* Reading Progress Bar */}
      <div className="reading-progress">
        <div className="reading-progress-bar" style={{ width: `${progress}%` }} />
      </div>

      <div className="reader-header">
        <button onClick={() => navigate(`/novels/${novelId}`)} className="btn-back">
          ← Back
        </button>
        <h2>{chapter?.title}</h2>
      </div>

      {/* Chapter Info */}
      <div className="chapter-info-bar">
        <span className="chapter-number">
          Chapter {currentIndex + 1} of {chapters.length}
        </span>
        <span className="word-count">
          📖 {chapter?.wordCount?.toLocaleString() || '—'} words
        </span>
      </div>

      <div className="reader-content">
        <div className="chapter-text">
          {chapter?.content.split('\n').filter(p => p.trim()).map((para, i) => (
            <p key={i}>{para}</p>
          ))}
        </div>
      </div>

      <div className="reader-navigation">
        {prevChapter ? (
          <button
            onClick={() => navigate(`/read/${novelId}/${prevChapter.id}`)}
            className="btn-nav"
          >
            ← Previous
          </button>
        ) : <div />}
        
        {nextChapter ? (
          <button
            onClick={() => navigate(`/read/${novelId}/${nextChapter.id}`)}
            className="btn-nav btn-next"
          >
            Next Chapter →
          </button>
        ) : (
          <button
            onClick={() => navigate(`/novels/${novelId}`)}
            className="btn-nav btn-next"
          >
            ✓ Finished
          </button>
        )}
      </div>
    </div>
  )
}

export default Reader
