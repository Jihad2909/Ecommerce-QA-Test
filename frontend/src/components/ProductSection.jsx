import { useState } from 'react';
import api from '../services/api';

export default function ProductSection({ products, onProductAdded, onError }) {
  const [form, setForm] = useState({
    name: '',
    sku: '',
    description: '',
    unitPrice: '',
    stockQuantity: '',
    minStockAlert: 5,
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    onError('');
    try {
      await api.post('/products', {
        ...form,
        unitPrice: parseFloat(form.unitPrice),
        stockQuantity: parseInt(form.stockQuantity, 10),
        minStockAlert: parseInt(form.minStockAlert, 10),
      });
      setForm({ name: '', sku: '', description: '', unitPrice: '', stockQuantity: '', minStockAlert: 5 });
      onProductAdded();
    } catch {
      onError('Erreur lors de la création du produit');
    }
  };

  return (
    <section className="bg-white rounded-xl shadow-sm border border-slate-200 p-6">
      <h2 className="text-lg font-bold text-slate-800 mb-4">Catalogue Produits</h2>

      <form onSubmit={handleSubmit} className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-6 gap-3 mb-6">
        <input
          data-testid="prod-name"
          placeholder="Nom"
          value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })}
          className="px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
          required
        />
        <input
          data-testid="prod-sku"
          placeholder="SKU"
          value={form.sku}
          onChange={(e) => setForm({ ...form, sku: e.target.value })}
          className="px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
          required
        />
        <input
          data-testid="prod-desc"
          placeholder="Description"
          value={form.description}
          onChange={(e) => setForm({ ...form, description: e.target.value })}
          className="px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
          required
        />
        <input
          data-testid="prod-price"
          type="number"
          step="0.01"
          placeholder="Prix (DT)"
          value={form.unitPrice}
          onChange={(e) => setForm({ ...form, unitPrice: e.target.value })}
          className="px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
          required
        />
        <input
          data-testid="prod-stock"
          type="number"
          placeholder="Stock"
          value={form.stockQuantity}
          onChange={(e) => setForm({ ...form, stockQuantity: e.target.value })}
          className="px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
          required
        />
        <button
          data-testid="add-prod-btn"
          type="submit"
          className="bg-blue-600 hover:bg-blue-700 text-white font-medium text-sm px-4 py-2 rounded-lg transition cursor-pointer"
        >
          + Ajouter
        </button>
      </form>

      <div className="overflow-x-auto">
        <table className="w-full text-sm text-left border-collapse">
          <thead>
            <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 uppercase text-xs">
              <th className="py-3 px-4">ID</th>
              <th className="py-3 px-4">SKU</th>
              <th className="py-3 px-4">Nom</th>
              <th className="py-3 px-4">Prix</th>
              <th className="py-3 px-4">Stock</th>
              <th className="py-3 px-4">État</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {products.map((p) => {
              const isLow = p.stockQuantity <= p.minStockAlert;
              return (
                <tr key={p.id} data-testid={`product-row-${p.sku}`} className="hover:bg-slate-50">
                  <td className="py-3 px-4 text-slate-500">{p.id}</td>
                  <td className="py-3 px-4 font-mono font-medium text-slate-800">{p.sku}</td>
                  <td className="py-3 px-4 text-slate-700">{p.name}</td>
                  <td className="py-3 px-4 font-semibold text-slate-800">{p.unitPrice} DT</td>
                  <td className="py-3 px-4 font-semibold">{p.stockQuantity}</td>
                  <td className="py-3 px-4">
                    {isLow ? (
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-800">
                        Stock Bas
                      </span>
                    ) : (
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-emerald-100 text-emerald-800">
                        Disponible
                      </span>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </section>
  );
}