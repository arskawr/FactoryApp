import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import HomePage from './components/HomePage';
import LoginPage from './components/LoginPage';  // AuthWindow
import CustomerGUI from './components/CustomerGUI';  // ClientGUI
// Добавь другие роуты по ТЗ

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<HomePage />} />  // Приветственное окно
        <Route path="/login" element={<LoginPage />} />  // Авторизация
        <Route path="/customer" element={<CustomerGUI />} />  // Клиентский интерфейс
      </Routes>
    </Router>
  );
}

export default App;