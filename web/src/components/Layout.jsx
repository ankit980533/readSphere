import { Outlet, Link, useLocation } from 'react-router-dom'
import './Layout.css'

function Layout({ user, onLogout }) {
  const location = useLocation()

  return (
    <div className="layout">
      <nav className="navbar">
        <div className="nav-container">
          <Link to="/" className="logo">
            <span className="logo-icon">📚</span>
            ReadSphere
          </Link>
          
          <div className="nav-links">
            <Link to="/" className={location.pathname === '/' ? 'active' : ''}>
              Dashboard
            </Link>
            <Link to="/novels" className={location.pathname.startsWith('/novels') ? 'active' : ''}>
              Library
            </Link>
            {user?.role === 'ADMIN' && (
              <Link to="/upload" className={location.pathname === '/upload' ? 'active' : ''}>
                Upload
              </Link>
            )}
          </div>
          
          <div className="nav-user">
            <div className="user-info">
              <div className="user-avatar">
                {user?.name?.charAt(0)?.toUpperCase() || 'U'}
              </div>
              <div>
                <div className="user-name">{user?.name}</div>
                <div className="user-role">{user?.role}</div>
              </div>
            </div>
            <button onClick={onLogout} className="btn-logout">
              Logout
            </button>
          </div>
        </div>
      </nav>
      
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  )
}

export default Layout
