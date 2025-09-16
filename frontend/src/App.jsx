import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Chatbot from './components/Chatbot/Chatbot';
import AppLayout from './layouts/AppLayout';
import './App.scss';

// 임시 페이지 컴포넌트들
const HomePage = () => (
    <div className="container" style={{ padding: '2rem', textAlign: 'center', minHeight: '100vh' }}>
      <div className="card">
        <div className="card-body">
          <h1 className="text-primary">반띵 홈페이지</h1>
          <p>소분 모임을 찾아보세요!</p>
          <p>우측 하단의 챗봇을 클릭해보세요!</p>

          {/* 컬러 가이드 테스트 */}
          <div style={{ display: 'flex', gap: '16px', flexWrap: 'wrap', margin: '16px 0', justifyContent: 'center' }}>
            <div className="bg-primary" style={{ width: '100px', height: '60px', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white', fontSize: '12px' }}>Primary</div>
            <div className="bg-secondary" style={{ width: '100px', height: '60px', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white', fontSize: '12px' }}>Secondary</div>
            <div className="bg-accent" style={{ width: '100px', height: '60px', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white', fontSize: '12px' }}>Accent</div>
          </div>

          <button className="btn btn-primary">Primary 버튼</button>
          <button className="btn btn-secondary" style={{ marginLeft: '8px' }}>Secondary 버튼</button>
        </div>
      </div>
    </div>
);

const LoginPage = () => (
    <div className="container" style={{ padding: '2rem', textAlign: 'center' }}>
      <div className="card">
        <div className="card-body">
          <h1>로그인</h1>
          <p>카카오 로그인을 통해 서비스를 이용하세요</p>
          <button className="btn btn-accent">카카오 로그인</button>
        </div>
      </div>
    </div>
);

function App() {
  return (
      <Router>
        <div className="App">
          <Routes>
            <Route path="/" element={<AppLayout />}>
              <Route index element={<HomePage />} />
              <Route path="login" element={<LoginPage />} />
            </Route>
          </Routes>

          {/* 챗봇을 App 최상위에서 렌더링 */}
          <Chatbot />
        </div>
      </Router>
  );
}

export default App;