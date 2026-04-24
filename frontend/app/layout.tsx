'use client';

import { Inter, Plus_Jakarta_Sans } from 'next/font/google';
import './globals.css';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider } from 'next-themes';
import { useState } from 'react';

const inter = Inter({
  subsets: ['latin'],
  variable: '--font-sans',
  display: 'swap',
});

const jakarta = Plus_Jakarta_Sans({
  subsets: ['latin'],
  variable: '--font-heading',
  display: 'swap',
  weight: ['500', '600', '700'],
});

export default function RootLayout({ children }: { children: React.ReactNode }) {
  const [queryClient] = useState(() => new QueryClient({
    defaultOptions: {
      queries: { staleTime: 60_000, retry: 1 },
    },
  }));

  return (
    <html lang="pt-BR" className={`${inter.variable} ${jakarta.variable}`} suppressHydrationWarning>
      <head>
        <title>DoutorIA</title>
        <meta name="description" content="Análise de Sintomas e Diagnóstico Assistido por IA" />
      </head>
      <body className="min-h-screen bg-background font-sans antialiased">
        <QueryClientProvider client={queryClient}>
          <ThemeProvider attribute="class" defaultTheme="system" enableSystem>
            {children}
          </ThemeProvider>
        </QueryClientProvider>
      </body>
    </html>
  );
}
