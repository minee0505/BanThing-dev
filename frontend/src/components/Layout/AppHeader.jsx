import React from 'react';
import Logo from '../Others/Logo.jsx';
import {Link} from 'react-router-dom';

const AppHeader = () => {
  return (
    <>
      <Logo />
      <Link to='/login'>로그인</Link>
    </>
  );
};

export default AppHeader;