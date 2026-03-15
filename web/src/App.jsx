import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useState, useEffect } from 'react'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import NovelList from './pages/NovelList'
import NovelDetail from './pages/NovelDetail'
import UploadNovel from './pages/UploadNovel'
import Reader from './pages/Reader'
import Layout from './components/Layout'

function App() {
  const [token, setToken] = useState(localStorage.getItem('token'))
  const [user, setUser] = useState(JSON.parse(localStorage.getItem('user') || 'null'))

  const login = (token, userData) => {
    localStorage.setItem('token', token)
    localStorage.setItem('user', JSON.stringify(userData))
    setToken(token)
    setUser(userData)
  }

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setToken(null)
    setUser(null)
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={!token ? <Login onLogin={login} /> : <Navigate to="/" />} />
        <Route path="/" element={token ? <Layout user={user} onLogout={logout} /> : <Navigate to="/login" />}>
          <Route index element={<Dashboard />} />
          <Route path="novels" element={<NovelList />} />
          <Route path="novels/:id" element={<NovelDetail />} />
          <Route path="upload" element={<UploadNovel />} />
          <Route path="read/:novelId/:chapterId" element={<Reader />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
