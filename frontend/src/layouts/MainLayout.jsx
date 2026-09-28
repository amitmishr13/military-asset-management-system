import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Navbar } from '../components/Navbar';
import { Sidebar } from '../components/Sidebar';

export const MainLayout = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const toggleSidebar = () => {
    setSidebarOpen(prev => !prev);
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', backgroundColor: 'var(--bg-main)' }}>
      <Navbar onToggleSidebar={toggleSidebar} />

      <div style={{ display: 'flex', flex: 1, position: 'relative' }}>
        <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

        <main style={{ flex: 1, padding: '1.5rem', overflowY: 'auto', minWidth: 0 }}>
          <Outlet />
        </main>
      </div>

      <style>{`
        @media (max-width: 767px) {
          .sidebar {
            position: fixed;
            top: 60px;
            bottom: 0;
            left: -230px;
            z-index: 30;
            transition: left 0.25s ease;
          }
          .sidebar.open {
            left: 0;
            box-shadow: 4px 0 15px rgba(0,0,0,0.5);
          }
        }
      `}</style>
    </div>
  );
};

export default MainLayout;
