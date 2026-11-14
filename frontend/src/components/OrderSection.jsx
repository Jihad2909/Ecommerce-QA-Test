import { useState, useEffect } from 'react';
import api from '../services/api';

const STATUS_BADGES = {
  SUBMITTED: 'bg-blue-100 text-blue-800',
  CONFIRMED: 'bg-indigo-100 text-indigo-800',
  DELIVERED: 'bg-emerald-100 text-emerald-800',
  CANCELLED: 'bg-red-100 text-red-800',
};

export default function OrderSection({ products, orders, onOrderUpdated, onError }) {
  const [selectedProductId, setSelectedProductId] = useState('');
  const [quantity, setQuantity] = useState(1);

  useEffect(() => {
    if (products.length > 0 && (!selectedProductId || !products.some((p) => String(p.id) === String(selectedProductId)))) {
      setSelectedProductId(String(products[0].id));
    }
  }, [products, selectedProductId]);

  const handleCreateOrder = async (e) => {
    e.preventDefault();
    onError('');
    if (!selectedProductId) {
      onError('Veuillez sélectionner un produit valide.');
      return;
    }
    try {
      await api.post('/orders', {
        items: { [Number(selectedProductId)]: parseInt(quantity, 10) },
      });
      onOrderUpdated();
    } catch {
      onError('Erreur lors de la création de la commande (stock insuffisant ?)');
    }
  };

  const handleAction = async (id, action) => {
    onError('');
    try {
      await api.patch(`/orders/${id}/${action}`);
      onOrderUpdated();
    } catch {
      onError(`Erreur lors de l'action ${action}`);
    }
  };

  return (
    <section className="bg-white rounded-xl shadow-sm border border-slate-200 p-6">
      <h2 className="text-lg font-bold text-slate-800 mb-4">Gestion des Commandes</h2>

      <form onSubmit={handleCreateOrder} className="flex flex-wrap gap-3 mb-6">
        <select
          data-testid="order-product-select"
          value={selectedProductId}
          onChange={(e) => setSelectedProductId(e.target.value)}
          className="flex-1 min-w-60 px-3 py-2 border border-slate-300 rounded-lg text-sm bg-white focus:ring-2 focus:ring-blue-500 focus:outline-none"
        >
          {products.map((p) => (
            <option key={p.id} value={p.id}>
              {p.name} ({p.sku}) - Stock: {p.stockQuantity}
            </option>
          ))}
        </select>
        <input
          data-testid="order-qty"
          type="number"
          min="1"
          value={quantity}
          onChange={(e) => setQuantity(e.target.value)}
          className="w-24 px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
          required
        />
        <button
          data-testid="create-order-btn"
          type="submit"
          disabled={!selectedProductId}
          className="bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 text-white font-medium text-sm px-5 py-2 rounded-lg transition cursor-pointer"
        >
          Passer Commande
        </button>
      </form>

      <div className="overflow-x-auto">
        <table className="w-full text-sm text-left border-collapse">
          <thead>
            <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 uppercase text-xs">
              <th className="py-3 px-4">N° Commande</th>
              <th className="py-3 px-4">Total</th>
              <th className="py-3 px-4">Statut</th>
              <th className="py-3 px-4">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {orders.map((o) => (
              <tr key={o.id} data-testid={`order-row-${o.id}`} className="hover:bg-slate-50">
                <td className="py-3 px-4 font-mono text-slate-800">{o.orderNumber}</td>
                <td className="py-3 px-4 font-semibold text-slate-800">{o.totalAmount} DT</td>
                <td className="py-3 px-4" data-testid={`order-status-${o.id}`}>
                  <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${STATUS_BADGES[o.status] || 'bg-slate-100 text-slate-800'}`}>
                    {o.status}
                  </span>
                </td>
                <td className="py-3 px-4 space-x-2">
                  {o.status === 'SUBMITTED' && (
                    <>
                      <button
                        onClick={() => handleAction(o.id, 'confirm')}
                        className="bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-medium px-3 py-1.5 rounded transition cursor-pointer"
                      >
                        Confirmer
                      </button>
                      <button
                        onClick={() => handleAction(o.id, 'cancel')}
                        className="bg-red-500 hover:bg-red-600 text-white text-xs font-medium px-3 py-1.5 rounded transition cursor-pointer"
                      >
                        Annuler
                      </button>
                    </>
                  )}
                  {o.status === 'CONFIRMED' && (
                    <button
                      onClick={() => handleAction(o.id, 'deliver')}
                      className="bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-medium px-3 py-1.5 rounded transition cursor-pointer"
                    >
                      Livrer
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}