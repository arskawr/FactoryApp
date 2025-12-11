import React from 'react';
import { Link } from 'react-router-dom';

const HomePage = () => {
  return (
    <div style={{ textAlign: 'center', padding: '20px' }}>
      <h1>Добро пожаловать в автоматизированную систему кондитерской фабрики</h1>
      <p>Здесь вы можете войти как клиент для заказа или админ для управления.</p>
      <Link to="/login"><button>Войти</button></Link>
      <Link to="/register"><button>Зарегистрироваться</button></Link>
      <div>
        <h2>Ассортимент</h2>
        <ul>
          <li><Link to="/category/cakes">Торты</Link></li>
          <li><Link to="/category/pastries">Пирожные</Link></li>
          <li><Link to="/category/marshmallow">Зефир</Link></li>
        </ul>
      </div>
    </div>
  );
};

export default HomePage;