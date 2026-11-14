import { useState } from 'react';
import api from '../services/api';

export default function LoginForm({ onLoginSuccess }) {
  const [email, setEmail] = useState('jihad@admin');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const res = await api.post('/auth/login', { email, password });
      localStorage.setItem('token', res.data.token);
      onLoginSuccess(res.data.token);
    } catch {
      setError('Identifiants invalides');
    }
  };

  return (
    <div className="min-h-screen bg-slate-100 flex items-center justify-center p-4">
      <div className="w-full max-w-md bg-white rounded-2xl shadow-lg p-8 border border-slate-200">
        <h2 className="text-2xl font-bold text-slate-800 text-center mb-2">
          Gestion Commerciale
        </h2>
        <p className="text-sm text-slate-500 text-center mb-6">
          Connectez-vous pour accéder au tableau de bord
        </p>

        {error && (
          <div
            data-testid="error-msg"
            className="mb-4 p-3 rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm"
          >
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">
              Email
            </label>
            <input
              data-testid="email-input"
              type="email"
              placeholder="jihad@admin"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-3.5 py-2.5 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">
              Mot de passe
            </label>
            <input
              data-testid="password-input"
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-3.5 py-2.5 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>

          <button
            data-testid="login-btn"
            type="submit"
            className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2.5 rounded-lg transition cursor-pointer"
          >
            Se connecter
          </button>
        </form>
      </div>
    </div>
  );
}