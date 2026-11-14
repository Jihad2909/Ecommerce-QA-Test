import { useState, useEffect, useCallback, useMemo } from 'react';
import api from './services/api';
import Navbar from './components/Navbar';
import LoginForm from './components/LoginForm';
import ProductSection from './components/ProductSection';
import OrderSection from './components/OrderSection';
import AuditLogSection from './components/AuditLogSection';

function extractRoleFromToken(token) {
  if (!token) return 'CLIENT';
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    const rawRole = payload.role || (Array.isArray(payload.roles) ? payload.roles[0] : '') || '';
    return String(rawRole).replace('ROLE_', '').toUpperCase() || 'ADMIN';
  } catch {
    return 'CLIENT';
  }
}

export default function App() {
  const [token, setToken] = useState(() => localStorage.getItem('token') || '');
  const [products, setProducts] = useState([]);
  const [orders, setOrders] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
  const [error, setError] = useState('');

  const userRole = useMemo(() => extractRoleFromToken(token), [token]);

  const fetchAuditLogs = useCallback(async () => {
    if (userRole !== 'ADMIN') return;
    try {
      const res = await api.get('dashboard/audit-logs');
      const data = res.data;
      setAuditLogs(Array.isArray(data) ? data : (data.content || []));
    } catch (err) {
      console.error('Erreur GET /audit-logs:', err);
    }
  }, [userRole]);

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

    if (userRole === 'ADMIN') {
      fetchAuditLogs();
    }
  }, [userRole, fetchAuditLogs]);

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
    setAuditLogs([]);
  };

  const kpis = useMemo(() => {
    const totalRevenue = orders
      .filter((o) => o.status !== 'CANCELLED')
      .reduce((sum, o) => sum + (Number(o.totalAmount) || 0), 0);
    const lowStockCount = products.filter((p) => p.stockQuantity <= p.minStockAlert).length;
    return {
      totalProducts: products.length,
      lowStockCount,
      totalOrders: orders.length,
      totalRevenue: totalRevenue.toFixed(2),
    };
  }, [products, orders]);

  if (!token) {
    return <LoginForm onLoginSuccess={handleLoginSuccess} />;
  }

  return (
    <div className="min-h-screen bg-slate-100">
      <Navbar onLogout={handleLogout} userRole={userRole} />
      <main className="max-w-6xl mx-auto px-6 py-8 space-y-6">
        {error && (
          <div data-testid="error-msg" className="p-4 rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm font-medium">
            {error}
          </div>
        )}

        <section className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4" data-testid="dashboard-kpis">
          <div className="bg-white p-5 rounded-xl shadow-sm border border-slate-200">
            <p className="text-xs font-medium text-slate-500 uppercase">Chiffre d'affaires</p>
            <p className="text-2xl font-bold text-slate-800 mt-1">{kpis.totalRevenue} DT</p>
          </div>
          <div className="bg-white p-5 rounded-xl shadow-sm border border-slate-200">
            <p className="text-xs font-medium text-slate-500 uppercase">Commandes totales</p>
            <p className="text-2xl font-bold text-blue-600 mt-1">{kpis.totalOrders}</p>
          </div>
          <div className="bg-white p-5 rounded-xl shadow-sm border border-slate-200">
            <p className="text-xs font-medium text-slate-500 uppercase">Produits au catalogue</p>
            <p className="text-2xl font-bold text-slate-800 mt-1">{kpis.totalProducts}</p>
          </div>
          <div className="bg-white p-5 rounded-xl shadow-sm border border-slate-200">
            <p className="text-xs font-medium text-slate-500 uppercase">Alertes stock bas</p>
            <p className={`text-2xl font-bold mt-1 ${kpis.lowStockCount > 0 ? 'text-amber-600' : 'text-emerald-600'}`}>
              {kpis.lowStockCount}
            </p>
          </div>
        </section>

        <ProductSection
          products={products}
          userRole={userRole}
          onProductAdded={fetchData}
          onError={setError}
        />
        <OrderSection
          products={products}
          orders={orders}
          userRole={userRole}
          onOrderUpdated={fetchData}
          onError={setError}
        />

        {/* Section Traçabilité réservée exclusivement à l'ADMIN */}
        {userRole === 'ADMIN' && (
          <AuditLogSection auditLogs={auditLogs} onRefresh={fetchAuditLogs} />
        )}
      </main>
    </div>
  );
}