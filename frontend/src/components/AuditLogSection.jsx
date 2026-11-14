import { useState } from 'react';

export default function AuditLogSection({ auditLogs, onRefresh }) {
  const [filterAction, setFilterAction] = useState('');

  const filteredLogs = auditLogs.filter((log) => {
    const actionText = String(log.action || log.eventType || '').toLowerCase();
    const userText = String(log.performedBy || log.userEmail || log.username || '').toLowerCase();
    const detailsText = String(log.details || log.description || log.entityName || '').toLowerCase();
    const q = filterAction.toLowerCase();
    return actionText.includes(q) || userText.includes(q) || detailsText.includes(q);
  });

  return (
    <section
      data-testid="audit-log-section"
      className="bg-white rounded-xl shadow-sm border border-slate-200 p-6"
    >
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-5">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-lg font-bold text-slate-800">Journal d'Audit & Traçabilité</h2>
            <span className="bg-purple-100 text-purple-800 text-xs font-semibold px-2.5 py-0.5 rounded-full">
              ADMIN ONLY
            </span>
          </div>
          <p className="text-xs text-slate-500 mt-0.5">
            Historique immuable des actions sensibles (qui a fait quoi et quand)
          </p>
        </div>

        <div className="flex items-center gap-2">
          <input
            data-testid="audit-search-input"
            type="text"
            placeholder="Filtrer par action ou utilisateur..."
            value={filterAction}
            onChange={(e) => setFilterAction(e.target.value)}
            className="px-3 py-1.5 border border-slate-300 rounded-lg text-sm w-60 focus:ring-2 focus:ring-purple-500 focus:outline-none"
          />
          <button
            data-testid="refresh-audit-btn"
            onClick={onRefresh}
            className="bg-slate-800 hover:bg-slate-900 text-white text-xs font-medium px-3.5 py-2 rounded-lg transition cursor-pointer"
          >
            Actualiser
          </button>
        </div>
      </div>

      <div className="overflow-x-auto max-h-80 overflow-y-auto">
        <table className="w-full text-sm text-left border-collapse">
          <thead className="sticky top-0 bg-slate-50 border-b border-slate-200 text-slate-600 uppercase text-xs">
            <tr>
              <th className="py-3 px-4">ID</th>
              <th className="py-3 px-4">Date / Heure</th>
              <th className="py-3 px-4">Utilisateur</th>
              <th className="py-3 px-4">Action</th>
              <th className="py-3 px-4">Détails / Entité</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {filteredLogs.length === 0 ? (
              <tr>
                <td colSpan="5" className="py-6 text-center text-slate-400">
                  Aucune trace d'audit enregistrée.
                </td>
              </tr>
            ) : (
              filteredLogs.map((log) => {
                const rawDate = log.timestamp || log.createdAt || log.actionDate;
                const formattedDate = rawDate
                  ? new Date(rawDate).toLocaleString('fr-FR')
                  : '-';
                return (
                  <tr key={log.id} data-testid={`audit-row-${log.id}`} className="hover:bg-slate-50">
                    <td className="py-2.5 px-4 text-slate-400 font-mono text-xs">#{log.id}</td>
                    <td className="py-2.5 px-4 text-slate-600 text-xs whitespace-nowrap">{formattedDate}</td>
                    <td className="py-2.5 px-4 font-medium text-slate-800">
                      {log.performedBy || log.userEmail || log.username || 'Système'}
                    </td>
                    <td className="py-2.5 px-4">
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded text-xs font-mono font-semibold bg-slate-100 text-slate-800">
                        {log.action || log.eventType}
                      </span>
                    </td>
                    <td className="py-2.5 px-4 text-slate-600 text-xs">
                      {log.details || log.description || `${log.entityName || ''} ${log.entityId ? `#${log.entityId}` : ''}`}
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}