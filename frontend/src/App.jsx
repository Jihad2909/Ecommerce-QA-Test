import { useState, useEffect, useCallback } from 'react';
import api from './services/api';
import Navbar from './components/Navbar';
import LoginForm from './components/LoginForm';
import ProductSection from './components/ProductSection';
import OrderSection from './components/OrderSection';

export default function App() {
  const [token, setToken] = useState(() => localStorage.getItem('token') || '');
  const [products, setProducts] = useState([]);
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState('');

  const fetchData = useCallback(async () => {
    try {
      const prodRes = await api.get('/products');
      const prodData = prodRes.data;
      setProducts(Array.isArray(prodData) ? prodData : (prodData.content || []));
    } catch (err) {
      console.error('Erreur GET /products:', err);
    }

    try {
      const ordRes = await api.get('/orders/orders');
      const ordData = ordRes.data;
      setOrders(Array.isArray(ordData) ? ordData : (ordData.content || []));
    } catch (err) {
      console.error('Erreur GET /orders/orders:', err);
    }
  }, []);

  useEffect(() => {
    if (token) {
      fetchData();
    }
  }, [token, fetchData]);

  const handleLoginSuccess = (newToken) => {
    localStorage.setItem('token', newToken);
    setToken(newToken);
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    setToken('');
    setProducts([]);
    setOrders([]);
  };

  if (!token) {
    return <LoginForm onLoginSuccess={handleLoginSuccess} />;
  }

  return (
    <div className="min-h-screen bg-slate-100">
      <Navbar onLogout={handleLogout} />
      <main className="max-w-6xl mx-auto px-6 py-8 space-y-6">
        {error && (
          <div data-testid="error-msg" className="p-4 rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm font-medium">
            {error}
          </div>
        )}
        <ProductSection products={products} onProductAdded={fetchData} onError={setError} />
        <OrderSection products={products} orders={orders} onOrderUpdated={fetchData} onError={setError} />
      </main>
    </div>
  );
}