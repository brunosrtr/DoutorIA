'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Home, Plus, ClipboardList, Stethoscope, UserCircle2 } from 'lucide-react';
import { ThemeToggle } from '@/components/ThemeToggle';
import { ProfileGuard } from '@/components/ProfileGuard';

const NAV_ITEMS = [
  { href: '/', label: 'Dashboard', icon: Home },
  { href: '/new-assessment', label: 'Nova Avaliação', icon: Plus },
  { href: '/assessments', label: 'Histórico', icon: ClipboardList },
  { href: '/profile', label: 'Perfil', icon: UserCircle2 },
];


export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();

  return (
    <div className="flex min-h-screen">
      {/* Sidebar */}
      <aside className="flex w-16 md:w-64 flex-col border-r bg-sidebar">
        <div className="px-3 md:px-6 py-5 flex items-center gap-2.5 border-b justify-center md:justify-start">
          <div className="p-1.5 rounded-md bg-gradient-to-br from-primary to-[oklch(0.4_0.12_215)] text-primary-foreground shadow-sm shrink-0">
            <Stethoscope className="h-4 w-4" />
          </div>
          <div className="hidden md:flex flex-col leading-tight">
            <span className="font-bold text-base tracking-tight">DoutorIA</span>
            <span className="text-[10px] text-muted-foreground uppercase tracking-wider">Assistente pessoal</span>
          </div>
        </div>
        <nav className="flex-1 p-2 md:p-3 space-y-0.5">
          {NAV_ITEMS.map(({ href, label, icon: Icon }) => {
            const active = pathname === href;
            return (
              <Link
                key={href}
                href={href}
                title={label}
                className={`group flex items-center justify-center md:justify-start gap-3 px-2 md:px-3 py-2 rounded-lg text-sm transition-all relative ${
                  active
                    ? 'bg-primary/10 text-primary font-medium'
                    : 'text-muted-foreground hover:text-foreground hover:bg-accent/60'
                }`}
              >
                {active && (
                  <span className="absolute left-0 top-1/2 -translate-y-1/2 h-5 w-0.5 rounded-r bg-primary" />
                )}
                <Icon className={`h-4 w-4 shrink-0 ${active ? 'text-primary' : ''}`} />
                <span className="hidden md:inline">{label}</span>
              </Link>
            );
          })}
        </nav>
        <div className="p-3 md:p-4 border-t flex justify-center md:justify-between items-center">
          <span className="hidden md:inline text-xs text-muted-foreground">v1.0</span>
          <ThemeToggle />
        </div>
      </aside>

      <div className="flex flex-col flex-1 min-w-0">
        <main className="flex-1 overflow-y-auto">
          <ProfileGuard>{children}</ProfileGuard>
        </main>
      </div>
    </div>
  );
}
