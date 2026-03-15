import { useState } from 'react'
import api from '../api/axios'
import './Login.css'

function Login({ onLogin }) {
  const [email, setEmail] = useState('admin@novelplatform.com')
  const [password, setPassword] = useState('admin123')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const response = await api.post('/auth/login', { email, password })
      onLogin(response.data.token, { name: response.data.name, role: response.data.role })
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-page">
      <div className="login-card">
        <h1>📚 ReadSphere</h1>
        <p className="subtitle">Your AI-powered novel reading platform</p>
        
        {error && <div className="error">{error}</div>}
        
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Email Address</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Enter your email"
              required
            />
          </div>
          
          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              required
            />
          </div>
          
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? '⏳ Signing in...' : '🚀 Sign In'}
          </button>
        </form>
        
        <p className="hint">
          Demo: admin@novelplatform.com / admin123
        </p>
        
        <div className="login-features">
          <div className="feature">
            <div className="feature-icon">🤖</div>
            <div>AI Powered</div>
          </div>
          <div className="feature">
            <div className="feature-icon">📖</div>
            <div>Smart Chapters</div>
          </div>
          <div className="feature">
            <div className="feature-icon">✨</div>
            <div>Auto Detection</div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Login
