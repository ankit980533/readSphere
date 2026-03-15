import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/axios'
import './UploadNovel.css'

function UploadNovel() {
  const navigate = useNavigate()
  const [formData, setFormData] = useState({
    title: '',
    author: '',
    description: ''
  })
  const [file, setFile] = useState(null)
  const [uploading, setUploading] = useState(false)
  const [progress, setProgress] = useState('')
  const [error, setError] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!file) {
      setError('Please select a PDF file')
      return
    }

    setUploading(true)
    setError('')
    setProgress('Uploading PDF...')

    const data = new FormData()
    data.append('file', file)
    if (formData.title) data.append('title', formData.title)
    if (formData.author) data.append('author', formData.author)
    if (formData.description) data.append('description', formData.description)

    try {
      setProgress('Processing PDF with AI (detecting title, author, chapters, genre)...')
      const response = await api.post('/novels/upload', data, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      
      setProgress(`Success! Detected: "${response.data.title}" - Redirecting...`)
      setTimeout(() => navigate(`/novels/${response.data.id}`), 2000)
    } catch (err) {
      setError(err.response?.data?.message || 'Upload failed')
      setProgress('')
    } finally {
      setUploading(false)
    }
  }

  return (
    <div className="container">
      <div className="upload-container">
        <h1>📤 Upload Novel</h1>
        <p className="page-subtitle">Just upload a PDF - AI will detect everything automatically!</p>

        {error && <div className="error">{error}</div>}
        {progress && <div className="progress-message">{progress}</div>}

        <form onSubmit={handleSubmit} className="upload-form">
          <div className="file-upload-area">
            <input
              type="file"
              accept=".pdf"
              onChange={(e) => setFile(e.target.files[0])}
              id="file-input"
              className="file-input"
            />
            <label htmlFor="file-input" className="file-label">
              <div className="file-icon">📄</div>
              <div className="file-text">
                {file ? file.name : 'Click to select PDF file'}
              </div>
              <div className="file-hint">Max size: 50MB</div>
            </label>
          </div>

          <div className="form-group">
            <label>Novel Title <span className="optional">(optional - AI will detect)</span></label>
            <input
              type="text"
              value={formData.title}
              onChange={(e) => setFormData({...formData, title: e.target.value})}
              placeholder="Leave empty for AI detection"
            />
          </div>

          <div className="form-group">
            <label>Author Name <span className="optional">(optional - AI will detect)</span></label>
            <input
              type="text"
              value={formData.author}
              onChange={(e) => setFormData({...formData, author: e.target.value})}
              placeholder="Leave empty for AI detection"
            />
          </div>

          <div className="form-group">
            <label>Description <span className="optional">(optional - AI will generate)</span></label>
            <textarea
              value={formData.description}
              onChange={(e) => setFormData({...formData, description: e.target.value})}
              placeholder="Leave empty for AI-generated summary"
              rows="4"
            />
          </div>

          <div className="ai-features">
            <h3>🤖 AI Auto-Detection</h3>
            <ul>
              <li>📖 Book title & author name</li>
              <li>✨ Chapter detection and splitting</li>
              <li>🏷️ Genre identification</li>
              <li>📝 Summary generation</li>
              <li>🛡️ Content moderation</li>
            </ul>
          </div>

          <button type="submit" className="btn-submit" disabled={uploading}>
            {uploading ? '⏳ AI Processing...' : '🚀 Upload & Auto-Detect'}
          </button>
        </form>
      </div>
    </div>
  )
}

export default UploadNovel
