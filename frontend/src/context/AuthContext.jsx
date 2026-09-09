import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/axios';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('token') || null);
  const [loading, setLoading] = useState(false);

  const login = async (email, password) => {
    setLoading(true);
    try {
      const res = await api.post('/api/auth/login', { email, password });
      const { token, id, email: userEmail, fullName, role } = res.data;
      const userData = { id, email: userEmail, fullName, role };

      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(userData));

      setToken(token);
      setUser(userData);
      return { success: true };
    } catch (err) {
      return {
        success: false,
        message: err.response?.data?.message || 'Login failed. Please check your credentials.',
      };
    } finally {
      setLoading(false);
    }
  };

  const register = async (email, password, fullName, role = 'ROLE_CUSTOMER') => {
    setLoading(true);
    try {
      const res = await api.post('/api/auth/register', { email, password, fullName, role });
      const { token, id, email: userEmail, fullName: name, role: userRole } = res.data;
      const userData = { id, email: userEmail, fullName: name, role: userRole };

      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(userData));

      setToken(token);
      setUser(userData);
      return { success: true };
    } catch (err) {
      return {
        success: false,
        message: err.response?.data?.message || 'Registration failed.',
      };
    } finally {
      setLoading(false);
    }
  };

  const demoLogin = async (role) => {
    let email = 'customer@supportai.com';
    if (role === 'AGENT') email = 'agent@supportai.com';
    if (role === 'ADMIN') email = 'admin@supportai.com';
    return await login(email, 'password123');
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, login, register, demoLogin, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
