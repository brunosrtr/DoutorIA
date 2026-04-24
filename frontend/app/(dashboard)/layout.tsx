'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Home, Plus, ClipboardList, Stethoscope, UserCircle2 } from 'lucide-react';
import { ThemeToggle } from '@/components/ThemeToggle';
import { ProfileGuard } from '@/components/ProfileGuard';

const NAV_ITEMS = [
  { href: '/', label: 'Início', icon: Home },
  { href: '/new-assessment', label: 'Nova Avaliação', icon: Plus },
  { href: '/assessments', label: 'Histórico', icon: ClipboardList },
  { href: '/profile', label: 'Perfil', icon: UserCircle2 },
];


export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();

  return (
    <div className="flex min-h-screen">
      {/* Sidebar */}
      <aside className="hidden md:flex w-64 flex-col border-r bg-sidebar">
        <div className="px-6 py-5 flex items-center gap-2.5 border-b">
          <div className="p-1.5 rounded-md bg-gradient-to-br from-primary to-[oklch(0.4_0.12_215)] text-primary-foreground shadow-sm">
            <Stethoscope className="h-4 w-4" />
          </div>
          <div className="flex flex-col leading-tight">
            <span className="font-bold text-base tracking-tight">DoutorIA</span>
            <span className="text-[10px] text-muted-foreground uppercase tracking-wider">Assistente pessoal</span>
          </div>
        </div>
        <nav className="flex-1 p-3 space-y-0.5">
          {NAV_ITEMS.map(({ href, label, icon: Icon }) => {
            const active = pathname === href;
            return (
              <Link
                key={href}
                href={href}
                className={`group flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-all relative ${
                  active
                    ? 'bg-primary/10 text-primary font-medium'
                    : 'text-muted-foreground hover:text-foreground hover:bg-accent/60'
                }`}
              >
                {active && (
                  <span className="absolute left-0 top-1/2 -translate-y-1/2 h-5 w-0.5 rounded-r bg-primary" />
                )}
                <Icon className={`h-4 w-4 ${active ? 'text-primary' : ''}`} />
                {label}
              </Link>
            );
          })}
        </nav>
        <div className="p-4 border-t flex justify-between items-center">
          <span className="text-xs text-muted-foreground">v1.0</span>
          <ThemeToggle />
        </div>
      </aside>

      {/* Mobile header */}
      <div className="flex flex-col flex-1 min-w-0">
        <header className="md:hidden flex items-center justify-between px-4 py-3 border-b bg-card">
          <div className="flex items-center gap-2">
            <div className="p-1.5 rounded-md bg-gradient-to-br from-primary to-[oklch(0.4_0.12_215)] text-primary-foreground">
              <Stethoscope className="h-4 w-4" />
            </div>
            <span className="font-bold tracking-tight">DoutorIA</span>
          </div>
          <ThemeToggle />
        </header>

        {/* Mobile bottom nav */}
        <main className="flex-1 overflow-y-auto pb-16 md:pb-0">
          <ProfileGuard>{children}</ProfileGuard>
        </main>

        <nav className="md:hidden fixed bottom-0 left-0 right-0 border-t bg-card flex z-40">
          {NAV_ITEMS.map(({ href, label, icon: Icon }) => {
            const active = pathname === href;
            return (
              <Link
                key={href}
                href={href}
                className={`flex-1 flex flex-col items-center gap-1 py-2.5 text-xs transition-colors ${
                  active ? 'text-primary font-medium' : 'text-muted-foreground'
                }`}
              >
                <Icon className="h-4 w-4" />
                {label}
              </Link>
            );
          })}
        </nav>
      </div>
    </div>
  );
}
