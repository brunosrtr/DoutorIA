'use client';

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { Sparkles, RefreshCw, HeartPulse, Loader2 } from 'lucide-react';
import { useHealthSummary, useRefreshHealthSummary } from '@/lib/api';
import type { HealthImprovementPoint } from '@/types/api';

const PRIORITY_STYLES: Record<HealthImprovementPoint['prioridade'], string> = {
  alta: 'border-red-500/60 bg-red-500/10 text-red-700 dark:text-red-300',
  media: 'border-amber-500/60 bg-amber-500/10 text-amber-700 dark:text-amber-300',
  baixa: 'border-emerald-500/60 bg-emerald-500/10 text-emerald-700 dark:text-emerald-300',
};

const CATEGORY_LABELS: Record<string, string> = {
  habito: 'Hábito',
  acompanhamento_medico: 'Acompanhamento médico',
  alimentacao: 'Alimentação',
  atividade_fisica: 'Atividade física',
  saude_mental: 'Saúde mental',
  medicacao: 'Medicação',
  sono: 'Sono',
  prevencao: 'Prevenção',
};

function formatDate(iso: string | null): string | null {
  if (!iso) return null;
  try {
    return new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
  } catch {
    return null;
  }
}

export default function HealthSummaryCard() {
  const { data, isLoading, isError, refetch } = useHealthSummary();
  const refresh = useRefreshHealthSummary();

  const isRefreshing = refresh.isPending;

  return (
    <Card>
      <CardHeader className="flex-row items-start justify-between space-y-0 gap-3">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <div className="p-1.5 rounded-md bg-primary/10 text-primary">
              <HeartPulse className="h-4 w-4" />
            </div>
            <CardTitle className="text-base">Pontos a melhorar</CardTitle>
          </div>
          <CardDescription className="flex items-center gap-1.5 text-xs">
            <Sparkles className="h-3 w-3" />
            Gerado pela IA com base no seu histórico
            {data?.geradoEm && !data.semDados && (
              <span className="text-muted-foreground/70">· {formatDate(data.geradoEm)}</span>
            )}
          </CardDescription>
        </div>
        <Button
          size="sm"
          variant="ghost"
          onClick={() => refresh.mutate()}
          disabled={isLoading || isRefreshing || data?.semDados}
          className="shrink-0"
        >
          {isRefreshing ? (
            <Loader2 className="h-3.5 w-3.5 animate-spin" />
          ) : (
            <RefreshCw className="h-3.5 w-3.5" />
          )}
          <span className="ml-1.5 hidden sm:inline">Atualizar</span>
        </Button>
      </CardHeader>

      <CardContent className="space-y-4">
        {isLoading ? (
          <div className="space-y-2">
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-5/6" />
            <Skeleton className="h-16 w-full" />
            <Skeleton className="h-16 w-full" />
          </div>
        ) : isError ? (
          <div className="py-6 flex flex-col items-center gap-2 text-center">
            <p className="text-sm text-muted-foreground">Não foi possível carregar o resumo.</p>
            <Button size="sm" variant="outline" onClick={() => refetch()}>Tentar novamente</Button>
          </div>
        ) : data?.semDados ? (
          <div className="py-6 flex flex-col items-center gap-2 text-center">
            <p className="text-sm text-muted-foreground">
              Realize sua primeira avaliação para receber sugestões personalizadas.
            </p>
          </div>
        ) : (
          <>
            {data?.resumo && (
              <p className="text-sm leading-relaxed text-muted-foreground">{data.resumo}</p>
            )}

            {data && data.pontosMelhoria.length > 0 && (
              <ul className="space-y-2.5">
                {data.pontosMelhoria.map((p, i) => (
                  <li
                    key={i}
                    className={`rounded-lg border p-3 ${PRIORITY_STYLES[p.prioridade]}`}
                  >
                    <div className="flex items-start justify-between gap-2">
                      <h4 className="font-medium text-sm leading-snug">{p.titulo}</h4>
                      <Badge variant="outline" className="text-[10px] shrink-0 capitalize border-current/40">
                        {p.prioridade}
                      </Badge>
                    </div>
                    <p className="text-xs mt-1 leading-relaxed text-foreground/80">{p.descricao}</p>
                    <p className="text-[10px] mt-2 uppercase tracking-wider opacity-70">
                      {CATEGORY_LABELS[p.categoria] ?? p.categoria}
                    </p>
                  </li>
                ))}
              </ul>
            )}

            {data && (
              <p className="text-[10px] text-muted-foreground/70 text-center pt-1">
                Baseado em {data.totalAvaliacoes} avaliaç{data.totalAvaliacoes === 1 ? 'ão' : 'ões'} · Esta análise não substitui consulta médica.
              </p>
            )}
          </>
        )}
      </CardContent>
    </Card>
  );
}
