'use client';

import { useState, useEffect, useRef } from 'react';
import { useSymptomSearch } from '@/lib/api';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import type { SymptomCatalogItem } from '@/types/api';

interface SymptomSearchProps {
  onSelect: (symptom: SymptomCatalogItem | { id: null; nome: string; custom: true }) => void;
  placeholder?: string;
}

function useDebounce(value: string, delay: number) {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const t = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(t);
  }, [value, delay]);
  return debounced;
}

export default function SymptomSearch({ onSelect, placeholder = 'Buscar sintoma...' }: SymptomSearchProps) {
  const [query, setQuery] = useState('');
  const [open, setOpen] = useState(false);
  const debounced = useDebounce(query, 300);
  const { data: results = [], isLoading } = useSymptomSearch(debounced);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  function highlight(text: string, q: string) {
    if (!q) return text;
    const idx = text.toLowerCase().indexOf(q.toLowerCase());
    if (idx === -1) return text;
    return (
      <>
        {text.slice(0, idx)}
        <mark className="bg-yellow-200 dark:bg-yellow-800 rounded">{text.slice(idx, idx + q.length)}</mark>
        {text.slice(idx + q.length)}
      </>
    );
  }

  return (
    <div ref={ref} className="relative">
      <Input
        value={query}
        onChange={(e) => { setQuery(e.target.value); setOpen(true); }}
        placeholder={placeholder}
        onFocus={() => setOpen(true)}
      />
      {open && query.length >= 2 && (
        <div className="absolute z-50 w-full mt-1 bg-popover border border-border rounded-md shadow-md max-h-64 overflow-y-auto">
          {isLoading && (
            <div className="p-2 text-sm text-muted-foreground">Buscando...</div>
          )}
          {!isLoading && results.length === 0 && (
            <button
              className="w-full text-left p-2 text-sm hover:bg-accent"
              onClick={() => {
                onSelect({ id: null, nome: query, custom: true });
                setQuery('');
                setOpen(false);
              }}
            >
              Usar &quot;<strong>{query}</strong>&quot; como sintoma personalizado
            </button>
          )}
          {results.map((s) => (
            <button
              key={s.id}
              className="w-full text-left p-2 text-sm hover:bg-accent flex items-center justify-between gap-2"
              onClick={() => {
                onSelect(s);
                setQuery('');
                setOpen(false);
              }}
            >
              <span>{highlight(s.nome, debounced)}</span>
              <Badge variant="outline" className="text-xs shrink-0">{s.categoria}</Badge>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
