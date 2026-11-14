export default function Navbar({ onLogout, userRole }) {
  return (
    <header className="bg-slate-900 text-white shadow-md">
      <div className="max-w-6xl mx-auto px-6 py-4 flex justify-between items-center">
        <div className="flex items-center gap-3">
          <span className="bg-blue-600 text-xs font-bold uppercase px-2.5 py-1 rounded">
            QA-Test
          </span>
          <h1 className="text-xl font-semibold tracking-tight">
            Gestion Commerciale
          </h1>
        </div>
        <div className="flex items-center gap-4">
          <span
            data-testid="user-role-badge"
            className="bg-slate-800 border border-slate-700 text-slate-200 text-xs font-semibold px-3 py-1.5 rounded-full"
          >
            Rôle : {userRole}
          </span>
          <button
            data-testid="logout-btn"
            onClick={onLogout}
            className="bg-red-600 hover:bg-red-700 text-white text-sm font-medium px-4 py-2 rounded-lg transition cursor-pointer"
          >
            Déconnexion
          </button>
        </div>
      </div>
    </header>
  );
}