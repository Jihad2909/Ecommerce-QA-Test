export default function Navbar({ onLogout }) {
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
        <button
          data-testid="logout-btn"
          onClick={onLogout}
          className="bg-red-600 hover:bg-red-700 text-white text-sm font-medium px-4 py-2 rounded-lg transition cursor-pointer"
        >
          Déconnexion
        </button>
      </div>
    </header>
  );
}